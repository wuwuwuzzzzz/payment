package com.example.payment.service;

import java.io.IOException;
import java.util.Map;

/**
 * @author wxz
 * @date 11:46 2023/8/25
 */
public interface WxPayService
{
    /**
     * 生成订单
     *
     * @param productId 商品ID
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @throws IOException
     * @author wxz
     * @date 11:47 2023/8/25
     */
    Map<String, Object> nativePay(Long productId) throws IOException;
}
