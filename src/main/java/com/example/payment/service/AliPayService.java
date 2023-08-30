package com.example.payment.service;

import java.util.Map;

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

    /**
     * 处理业务
     *
     * @param params 回调参数
     * @author wxz
     * @date 10:24 2023/8/30
     */
    void processOrder(Map<String, String> params);

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 11:30 2023/8/30
     */
    void cancelOrder(String orderNo);

    /**
     * 查询订单
     *
     * @param orderNo 订单编号
     * @return java.lang.String
     * @author wxz
     * @date 14:16 2023/8/30
     */
    String queryOrder(String orderNo);

    /**
     * 核实订单
     *
     * @param orderNo 订单编号
     * @author wxz
     * @date 14:30 2023/8/30
     */
    void checkOrderStatus(String orderNo);

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @author wxz
     * @date 14:54 2023/8/30
     */
    void refund(String orderNo, String reason);

    /**
     * 查询退款
     *
     * @param orderNo 订单编号
     * @return java.lang.String
     * @author wxz
     * @date 15:29 2023/8/30
     */
    String queryRefund(String orderNo);

    /**
     * 获取账单地址
     *
     * @param billDate 日期
     * @param type     类型
     * @return java.lang.String
     * @author wxz
     * @date 15:34 2023/8/30
     */
    String queryBill(String billDate, String type);
}
