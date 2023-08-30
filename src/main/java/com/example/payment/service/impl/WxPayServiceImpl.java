package com.example.payment.service.impl;

import com.example.payment.config.WxPayConfig;
import com.example.payment.entity.OrderInfo;
import com.example.payment.entity.RefundInfo;
import com.example.payment.enums.OrderStatus;
import com.example.payment.enums.PayType;
import com.example.payment.enums.wxpay.WxApiType;
import com.example.payment.enums.wxpay.WxNotifyType;
import com.example.payment.enums.wxpay.WxTradeState;
import com.example.payment.service.OrderInfoService;
import com.example.payment.service.PaymentInfoService;
import com.example.payment.service.RefundInfoService;
import com.example.payment.service.WxPayService;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wxz
 * @date 11:41 2023/8/25
 */
@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService
{
    private final ReentrantLock lock = new ReentrantLock();

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private CloseableHttpClient wxPayClient;

    @Resource
    private CloseableHttpClient wxPayNoSignClient;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RefundInfoService refundInfoService;

    /**
     * 创建订单调用native接口
     *
     * @param productId 商品ID
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author wxz
     * @date 11:58 2023/8/25
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws IOException
    {
        log.info("生成订单");

        // 生成订单
        OrderInfo info = orderInfoService.createOrderByProductId(productId, PayType.WXPAY.getType());
        if (info != null && StringUtils.hasText(info.getCodeUrl()))
        {
            log.info("订单已存在，直接返回二维码");

            // 返回二维码
            HashMap<String, Object> map = new HashMap<>(10);
            map.put("codeUrl", info.getCodeUrl());
            map.put("orderNo", info.getOrderNo());
            return map;
        }

        log.info("调用统一下单API");

        // 调用API
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        // 请求body参数
        assert info != null;
        String jsonParams = getString(info);

        log.info("请求参数：{}", jsonParams);

        StringEntity entity = new StringEntity(jsonParams, StandardCharsets.UTF_8);
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost))
        {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                log.info("success,return body = " + bodyAsString);
            }
            else if (statusCode == 204)
            {
                log.info("success");
            }
            else
            {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
                throw new IOException("request failed");
            }
            // 响应结果
            Gson gson = new Gson();
            HashMap<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            // 二维码
            String codeUrl = resultMap.get("code_url");
            // 保存二维码
            orderInfoService.saveCodeUrl(info.getOrderNo(), codeUrl);
            // 返回二维码
            HashMap<String, Object> map = new HashMap<>(10);
            map.put("codeUrl", codeUrl);
            map.put("orderNo", info.getOrderNo());

            return map;
        }
    }

    /**
     * 证书和回调解密
     *
     * @param bodyMap 回掉参数
     * @author wxz
     * @date 09:19 2023/8/29
     */
    @Override
    public void processOrder(Map<String, Object> bodyMap) throws Exception
    {
        log.info("处理订单");

        // 解密报文
        String plainText = decryptFromResource(bodyMap);

        // 将明文转换成map
        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String) plainTextMap.get("out_trade_no");

        // 采用数据锁进行并发控制，以免函数重入造成的数据混乱
        if (lock.tryLock())
        {
            try
            {
                // 处理重复的通知
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus))
                {
                    log.info("订单已支付，直接返回");

                    return;
                }
                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);

                // 记录支付日志
                paymentInfoService.createPaymentInfo(plainText);
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 10:39 2023/8/29
     */
    @Override
    public void cancelOrder(String orderNo)
    {
        // 调用微信支付的关单接口
        this.closeOrder(orderNo);

        // 更新订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);
    }

    /**
     * 查询订单
     *
     * @param orderNo 订单编号
     * @return java.lang.String
     * @author wxz
     * @date 11:18 2023/8/29
     */
    @Override
    public String queryOrder(String orderNo)
    {
        log.info("查询订单编号：{}", orderNo);

        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpGet))
        {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                log.info("success,return body = " + bodyAsString);
            }
            else if (statusCode == 204)
            {
                log.info("success");
            }
            else
            {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
                throw new IOException("request failed");
            }

            return bodyAsString;
        }
        catch (IOException e)
        {
            log.error("查询订单失败", e);

            return null;
        }
    }

    /**
     * 核实订单状态
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 11:55 2023/8/29
     */
    @Override
    public void checkOrderStatus(String orderNo)
    {
        log.info("核实订单状态");

        // 查询订单
        String result = queryOrder(orderNo);
        if (StringUtils.hasText(result))
        {
            Gson gson = new Gson();
            Map<String, Object> orderMap = gson.fromJson(result, HashMap.class);
            // 获取微信支付端的订单状态
            String tradeState = (String) orderMap.get("trade_state");
            // 判断订单状态
            if (WxTradeState.SUCCESS.getType().equals(tradeState))
            {
                log.warn("订单已支付:{}", orderNo);

                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);

                // 记录支付日志
                paymentInfoService.createPaymentInfo(result);
            }
            else if (WxTradeState.NOTPAY.getType().equals(tradeState))
            {
                log.warn("订单未支付");

                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
            }
            else
            {
                log.info("订单状态未知");
            }
        }
    }

    /**
     * 退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @author wxz
     * @date 14:24 2023/8/29
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reFund(String orderNo, String reason)
    {
        log.info("创建退款单记录");

        // 根据订单编号创建退款单
        RefundInfo refundInfo = refundInfoService.createRefundInfoByOrderNo(orderNo, reason);

        log.info("调用微信支付的退款接口");

        // 调用微信支付的退款接口
        String url = wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(url);

        // 请求body参数
        String jsonParams = getRefund(orderNo, reason, refundInfo);

        log.info("请求参数：{}", jsonParams);

        StringEntity entity = new StringEntity(jsonParams, StandardCharsets.UTF_8);
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost))
        {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                log.info("success,return body = " + bodyAsString);
            }
            else if (statusCode == 204)
            {
                log.info("success");
            }
            else
            {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
                throw new IOException("request failed");
            }

            // 更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);

            // 更新退款单
            refundInfoService.updateRefund(bodyAsString);
        }
        catch (IOException e)
        {
            log.error("退款失败", e);
        }
    }

    /**
     * 退款参数
     *
     * @param orderNo    订单编号
     * @param reason     退款原因
     * @param refundInfo 退款信息
     * @return java.lang.String
     * @author wxz
     * @date 15:22 2023/8/29
     */
    private String getRefund(String orderNo, String reason, RefundInfo refundInfo)
    {
        Gson gson = new Gson();
        Map<String, Object> paramsMap = new HashMap<>(10);
        // 订单编号
        paramsMap.put("out_trade_no", orderNo);
        // 退款单编号
        paramsMap.put("out_refund_no", refundInfo.getRefundNo());
        // 退款原因
        paramsMap.put("reason", reason);
        // 退款通知地址
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));

        HashMap<Object, Object> amountMap = new HashMap<>(10);
        // 退款金额
        amountMap.put("refund", refundInfo.getRefund());
        // 原订单金额
        amountMap.put("total", refundInfo.getTotalFee());
        // 币种
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);

        // 将参数转换成JSON字符串
        return gson.toJson(paramsMap);
    }

    /**
     * 查询退款
     *
     * @param refundNo 退款编号
     * @return java.lang.String
     * @author wxz
     * @date 15:16 2023/8/29
     */
    @Override
    public String queryRefund(String refundNo)
    {
        log.info("查询退款");

        String url = String.format(WxApiType.DOMESTIC_REFUNDS_QUERY.getType(), refundNo);
        url = wxPayConfig.getDomain().concat(url);

        // 创建远程请求GET对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpGet))
        {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                log.info("success,return body = " + bodyAsString);
            }
            else if (statusCode == 204)
            {
                log.info("success");
            }
            else
            {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
                throw new IOException("request failed");
            }

            // 更新退款单
            return bodyAsString;
        }
        catch (IOException e)
        {
            log.error("查询退款失败", e);

            return null;
        }
    }

    /**
     * 处理退款单
     *
     * @param bodyMap 退款信息参数
     * @author wxz
     * @date 15:49 2023/8/29
     */
    @Override
    public void processRefund(Map<String, Object> bodyMap) throws Exception
    {
        log.info("处理退款单");

        // 解密报文
        String plainText = decryptFromResource(bodyMap);

        // 将明文转换成map
        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String) plainTextMap.get("out_refund_no");

        if (lock.tryLock())
        {
            try
            {
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus))
                {
                    log.info("订单已退款，直接返回");

                    return;
                }

                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);

                // 更新退款单
                refundInfoService.updateRefund(plainText);
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    /**
     * 获取账单URL
     *
     * @param billDate billDate
     * @param type     type
     * @return java.lang.String
     * @author wxz
     * @date 16:00 2023/8/29
     */
    @Override
    public String queryBill(String billDate, String type)
    {
        log.warn("申请账单");

        String url = "";

        if ("tradebill".equals(type))
        {
            url = WxApiType.TRADE_BILLS.getType();
        }
        else if ("fundflowbill".equals(type))
        {
            url = WxApiType.FUND_FLOW_BILLS.getType();
        }
        else
        {
            throw new RuntimeException("账单类型错误");
        }

        url = wxPayConfig.getDomain().concat(url).concat("?bill_date=").concat(billDate);

        // 创建远程请求GET对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpGet))
        {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                log.info("success,return body = " + bodyAsString);
            }
            else if (statusCode == 204)
            {
                log.info("success");
            }
            else
            {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
                throw new IOException("request failed");
            }

            // 获取账单下载地址
            Gson gson = new Gson();
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            return resultMap.get("download_url");
        }
        catch (IOException e)
        {
            log.error("申请账单失败", e);

            return null;
        }
    }

    /**
     * 下载账单
     *
     * @param billDate billDate
     * @param type     type
     * @return java.lang.String
     * @author wxz
     * @date 16:10 2023/8/29
     */
    @Override
    public String downloadBill(String billDate, String type)
    {
        log.warn("下载账单");

        // 获取账单URL地址
        String url = this.queryBill(billDate, type);
        // 创建远程请求GET对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 使用wxPayClient完成签名并执行请求
        try (CloseableHttpResponse response = wxPayNoSignClient.execute(httpGet))
        {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                log.info("success,return body = " + bodyAsString);
            }
            else if (statusCode == 204)
            {
                log.info("success");
            }
            else
            {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
                throw new IOException("request failed");
            }

            return bodyAsString;
        }
        catch (IOException e)
        {
            log.error("下载账单失败", e);

            return null;
        }
    }

    /**
     * 关单接口的调用
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 10:41 2023/8/29
     */
    private void closeOrder(String orderNo)
    {
        log.info("订单编号：{}", orderNo);

        // 创建远程请求对象
        String url = String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpPost httpPost = new HttpPost(url);

        // 组装JSON请求体
        Map<String, String> paramsMap = new HashMap<>(10);
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String jsonParams = new Gson().toJson(paramsMap);

        log.info("请求参数：{}", jsonParams);

        // 将请求参数设置到请求对象中
        StringEntity entity = new StringEntity(jsonParams, StandardCharsets.UTF_8);
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost))
        {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                log.info("success 200");
            }
            else if (statusCode == 204)
            {
                log.info("success 204");
            }
            else
            {
                log.info("failed,resp code = " + statusCode);
                throw new IOException("request failed");
            }
        }
        catch (IOException e)
        {
            log.error("关单失败", e);
        }
    }

    /**
     * 对称解密
     *
     * @param bodyMap 回调参数
     * @return java.lang.String
     * @author wxz
     * @date 09:22 2023/8/29
     */
    private String decryptFromResource(Map<String, Object> bodyMap) throws Exception
    {
        log.info("对称解密");

        // 通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        // 数据密文
        String ciphertext = resourceMap.get("ciphertext");
        // 随即串
        String nonce = resourceMap.get("nonce");
        // 附加数据
        String associatedData = resourceMap.get("associated_data");

        log.info("数据密文：{}", ciphertext);

        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        String plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);

        log.info("解密后的数据：{}", plainText);

        return plainText;
    }

    /**
     * 生成请求参数
     *
     * @param info 订单信息
     * @return java.lang.String
     * @author wxz
     * @date 10:51 2023/8/27
     */
    private String getString(OrderInfo info)
    {
        Gson gson = new Gson();
        Map<Object, Object> paramsMap = new HashMap<>(10);
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", info.getTitle());
        paramsMap.put("out_trade_no", info.getOrderNo());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        Map<Object, Object> amountMap = new HashMap<>(10);
        amountMap.put("total", info.getTotalFee());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);
        // 将参数转换成JSON字符串
        return gson.toJson(paramsMap);
    }
}
