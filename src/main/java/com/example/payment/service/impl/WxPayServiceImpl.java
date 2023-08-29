package com.example.payment.service.impl;

import com.example.payment.config.WxPayConfig;
import com.example.payment.entity.OrderInfo;
import com.example.payment.enums.OrderStatus;
import com.example.payment.enums.wxpay.WxApiType;
import com.example.payment.enums.wxpay.WxNotifyType;
import com.example.payment.service.OrderInfoService;
import com.example.payment.service.PaymentInfoService;
import com.example.payment.service.WxPayService;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
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
    private CloseableHttpClient httpClient;
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private PaymentInfoService paymentInfoService;

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
        OrderInfo info = orderInfoService.createOrderByProductId(productId);
        if (info != null && StringUtils.hasText(info.getCodeUrl()))
        {
            log.info("订单已存在，直接返回二维码");

            // 返回二维码
            HashMap<String, Object> map = new HashMap<>(10);
            map.put("codeUrl", info.getCodeUrl());
            map.put("orderNo", info.getOrderNo());
            return map;
        }

        // TODO 存入数据库

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
        try (CloseableHttpResponse response = httpClient.execute(httpPost))
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
