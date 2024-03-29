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
     * @return java.lang.String
     * @author wxz
     * @date 11:18 2023/8/29
     */
    String queryOrder(String orderNo);

    /**
     * 核实订单状态
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 11:55 2023/8/29
     */
    void checkOrderStatus(String orderNo);

    /**
     * 退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @author wxz
     * @date 14:24 2023/8/29
     */
    void reFund(String orderNo, String reason);

    /**
     * 查询退款
     *
     * @param refundNo 退款编号
     * @return java.lang.String
     * @author wxz
     * @date 15:16 2023/8/29
     */
    String queryRefund(String refundNo);

    /**
     * 处理退款单
     *
     * @param bodyMap 退款信息参数
     * @author wxz
     * @date 15:49 2023/8/29
     */
    void processRefund(Map<String, Object> bodyMap) throws Exception;

    /**
     * 获取账单URL
     *
     * @param billDate billDate
     * @param type     type
     * @return java.lang.String
     * @author wxz
     * @date 15:59 2023/8/29
     */
    String queryBill(String billDate, String type);

    /**
     * 下载账单
     *
     * @param billDate billDate
     * @param type     type
     * @return java.lang.String
     * @author wxz
     * @date 16:10 2023/8/29
     */
    String downloadBill(String billDate, String type);
}
