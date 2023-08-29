package com.example.payment.service;

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
}
