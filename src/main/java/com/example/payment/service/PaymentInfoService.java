package com.example.payment.service;

import java.util.Map;

/**
 * @author wxz
 * @date 12:05 2023/8/24
 */
public interface PaymentInfoService
{
    /**
     * 记录支付日志
     *
     * @param plainText 解密报文
     * @author wxz
     * @date 09:43 2023/8/29
     */
    void createPaymentInfo(String plainText);

    /**
     * 记录支付日志（支付宝）
     *
     * @param params 回调参数
     * @author wxz
     * @date 10:27 2023/8/30
     */
    void createPaymentInfoForAliPay(Map<String, String> params);
}
