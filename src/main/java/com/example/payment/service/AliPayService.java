package com.example.payment.service;

import com.alipay.api.AlipayApiException;

/**
 * @author wxz
 * @date 19:23 2023/8/29
 */
public interface AliPayService
{
    /**
     * 响应为表单格式，可嵌入页面，具体以返回的结果为准
     *
     * @param productId 商品ID
     * @return java.lang.String
     * @author wxz
     * @date 19:26 2023/8/29
     */
    String tradeCreate(Long productId);
}
