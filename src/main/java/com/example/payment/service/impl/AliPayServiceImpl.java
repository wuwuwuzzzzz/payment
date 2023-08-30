package com.example.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.example.payment.entity.OrderInfo;
import com.example.payment.enums.OrderStatus;
import com.example.payment.service.AliPayService;
import com.example.payment.service.OrderInfoService;
import com.example.payment.service.PaymentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author wxz
 * @date 19:25 2023/8/29
 */
@Slf4j
@Service
public class AliPayServiceImpl implements AliPayService
{
    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private Environment config;

    @Resource
    private PaymentInfoService paymentInfoService;

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
            OrderInfo info = orderInfoService.createOrderByProductId(productId);

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

        // 更新订单状态
        orderInfoService.updateStatusByOrderNo(params.get("out_trade_no"), OrderStatus.SUCCESS);

        // 记录支付日志
        paymentInfoService.createPaymentInfoForAliPay(params);
    }
}
