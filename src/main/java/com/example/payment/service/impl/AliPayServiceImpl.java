package com.example.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.example.payment.entity.OrderInfo;
import com.example.payment.entity.RefundInfo;
import com.example.payment.enums.OrderStatus;
import com.example.payment.enums.PayType;
import com.example.payment.enums.alipay.AliTradeStatus;
import com.example.payment.service.AliPayService;
import com.example.payment.service.OrderInfoService;
import com.example.payment.service.PaymentInfoService;
import com.example.payment.service.RefundInfoService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wxz
 * @date 19:25 2023/8/29
 */
@Slf4j
@Service
public class AliPayServiceImpl implements AliPayService
{
    private final ReentrantLock lock = new ReentrantLock();

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private Environment config;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RefundInfoService refundInfoService;

    /**
     * 业务参数
     *
     * @param info 订单信息
     * @return com.alibaba.fastjson.JSONObject
     * @author wxz
     * @date 09:51 2023/8/30
     */
    private JSONObject getBizContent(OrderInfo info)
    {
        // 组装参数
        JSONObject bizContent = new JSONObject();
        // 商户订单号
        bizContent.put("out_trade_no", info.getOrderNo());
        // 支付金额
        BigDecimal total = new BigDecimal(info.getTotalFee()
                                              .toString()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        bizContent.put("total_amount", total);
        // 订单标题
        bizContent.put("subject", info.getTitle());
        // 电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        return bizContent;
    }

    /**
     * 调用支付宝接口
     *
     * @param info 订单信息
     * @return com.alipay.api.request.AlipayTradePagePayRequest
     * @author wxz
     * @date 09:22 2023/8/30
     */
    private AlipayTradePagePayRequest getAlipayTradePagePayRequest(OrderInfo info)
    {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 异步通知地址
        request.setNotifyUrl(config.getProperty("alipay.notify-url"));
        // 支付成功返回地址
        request.setReturnUrl(config.getProperty("alipay.return-url"));
        // 业务参数
        JSONObject bizContent = getBizContent(info);
        // 执行请求 调用支付宝接口
        request.setBizContent(bizContent.toString());

        return request;
    }

    /**
     * 响应为表单格式，可嵌入页面，具体以返回的结果为准
     *
     * @param productId 商品ID
     * @return java.lang.String
     * @author wxz
     * @date 19:28 2023/8/29
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String tradeCreate(Long productId)
    {
        log.info("生成订单");

        try
        {
            // 生成订单
            OrderInfo info = orderInfoService.createOrderByProductId(productId, PayType.ALIPAY.getType());

            // 调用支付宝接口
            AlipayTradePagePayRequest request = getAlipayTradePagePayRequest(info);
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (response.isSuccess())
            {
                log.info("调用成功");

                return response.getBody();
            }
            else
            {
                log.error("调用失败");

                throw new RuntimeException("调用支付宝接口失败");
            }
        }
        catch (AlipayApiException e)
        {
            throw new RuntimeException("调用支付宝接口失败");
        }

    }

    /**
     * 处理业务
     *
     * @param params 回调参数
     * @author wxz
     * @date 10:24 2023/8/30
     */
    @Override
    public void processOrder(Map<String, String> params)
    {
        log.info("处理订单");

        try
        {
            if (lock.tryLock())
            {
                // 处理重复通知
                String orderStatus = orderInfoService.getOrderStatus(params.get("out_trade_no"));
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus))
                {
                    return;
                }

                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(params.get("out_trade_no"), OrderStatus.SUCCESS);

                // 记录支付日志
                paymentInfoService.createPaymentInfoForAliPay(params);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 11:31 2023/8/30
     */
    @Override
    public void cancelOrder(String orderNo)
    {
        // 调用支付宝提供的统一收单交易关闭接口
        this.closeOrder(orderNo);

        // 更新用户订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);
    }

    /**
     * 查询订单
     *
     * @param orderNo 订单编号
     * @return java.lang.String
     * @author wxz
     * @date 14:16 2023/8/30
     */
    @Override
    public String queryOrder(String orderNo)
    {
        try
        {
            log.info("查单接口调用");

            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderNo);
            request.setBizContent(bizContent.toString());
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess())
            {
                log.info("调用成功");

                return response.getBody();
            }
            else
            {
                log.error("调用失败");

                return null;
            }
        }
        catch (AlipayApiException e)
        {
            throw new RuntimeException("查单接口调用失败");
        }
    }

    /**
     * 核实订单
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 14:30 2023/8/30
     */
    @Override
    public void checkOrderStatus(String orderNo)
    {
        log.info("核实支付宝订单状态");

        // 查询订单
        String result = queryOrder(orderNo);
        if (StringUtils.hasText(result))
        {
            Gson gson = new Gson();
            Map<String, LinkedHashMap> resultMap = gson.fromJson(result, HashMap.class);
            // 获取支付宝的订单状态
            LinkedHashMap alipayTradeQueryResponse = resultMap.get("alipay_trade_query_response");
            String tradeStatus = (String) alipayTradeQueryResponse.get("trade_status");
            // 判断订单状态
            if (AliTradeStatus.SUCCESS.getType().equals(tradeStatus))
            {
                log.warn("订单已支付:{}", orderNo);

                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);

                // 记录支付日志
                paymentInfoService.createPaymentInfoForAliPay(alipayTradeQueryResponse);
            }
            else if (AliTradeStatus.NOTPAY.getType().equals(tradeStatus))
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
        else
        {
            log.warn("订单未创建");

            // 更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }
    }

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @author wxz
     * @date 14:55 2023/8/30
     */
    @Override
    public void refund(String orderNo, String reason)
    {
        log.info("调用退款API");

        // 创建退款单
        RefundInfo refundInfo = refundInfoService.createRefundInfoByOrderNoForAliPay(orderNo, reason);

        // 调用统一收单交易退款接口
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        // 组装当前业务方法的请求参数
        JSONObject bizContent = new JSONObject();
        // 订单编号
        bizContent.put("out_trade_no", orderNo);
        // 退款金额
        BigDecimal refund = new BigDecimal(refundInfo.getRefund()
                                                     .toString()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        bizContent.put("refund_amount", refund);
        // 退款原因
        bizContent.put("refund_reason", reason);
        request.setBizContent(bizContent.toString());

        // 执行请求 调用支付宝接口
        try
        {
            AlipayTradeRefundResponse response = alipayClient.execute(request);

            if (response.isSuccess())
            {
                log.info("调用成功");

                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);

                // 更新退款单
                refundInfoService.updateRefundForAliPay(refundInfo.getRefundNo(), response.getBody(), AliTradeStatus.REFUND_SUCCESS.getType());
            }
            else
            {
                log.error("调用失败");

                throw new RuntimeException("调用退款API失败");
            }
        }
        catch (AlipayApiException e)
        {
            throw new RuntimeException("调用退款API失败");
        }
    }

    /**
     * 查询退款
     *
     * @param orderNo 订单编号
     * @return java.lang.String
     * @author wxz
     * @date 15:29 2023/8/30
     */
    @Override
    public String queryRefund(String orderNo)
    {
        log.info("查询退款");

        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        bizContent.put("out_request_no", orderNo);
        request.setBizContent(bizContent.toString());

        try
        {
            AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess())
            {
                log.info("调用成功");

                return response.getBody();
            }
            else
            {
                log.error("调用失败");

                return null;
            }
        }
        catch (AlipayApiException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭订单
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 11:33 2023/8/30
     */
    private void closeOrder(String orderNo)
    {
        try
        {
            log.info("关闭订单: {}", orderNo);

            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderNo);
            request.setBizContent(bizContent.toString());
            AlipayTradeCloseResponse response = alipayClient.execute(request);

            if (response.isSuccess())
            {
                log.info("调用成功");
            }
            else
            {
                log.error("调用失败");
            }
        }
        catch (AlipayApiException e)
        {
            throw new RuntimeException(e);
        }
    }
}
