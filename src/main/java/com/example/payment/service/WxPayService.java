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

    /**
     * 证书和回调解密
     *
     * @param bodyMap 回调参数
     * @author wxz
     * @date 09:18 2023/8/29
     */
    void processOrder(Map<String, Object> bodyMap) throws Exception;

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 10:39 2023/8/29
     */
    void cancelOrder(String orderNo);

    /**
     * 查询订单
     *
     * @param orderNo 订单编号
     * @return java.lang.Object
     * @author wxz
     * @date 11:18 2023/8/29
     */
    Object queryOrder(String orderNo);

    void checkOrderStatus(String orderNo);
}
