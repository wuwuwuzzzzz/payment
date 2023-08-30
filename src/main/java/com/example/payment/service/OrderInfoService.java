package com.example.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.payment.entity.OrderInfo;
import com.example.payment.enums.OrderStatus;

import java.util.List;

/**
 * @author wxz
 * @date 12:04 2023/8/24
 */
public interface OrderInfoService extends IService<OrderInfo>
{
    /**
     * 生成订单
     *
     * @param productId   商品ID
     * @param paymentType 支付类型
     * @return com.example.payment.entity.OrderInfo
     * @author wxz
     * @date 10:58 2023/8/28
     */
    OrderInfo createOrderByProductId(Long productId, String paymentType);

    /**
     * 保存二维码
     *
     * @param orderNo 商品编号
     * @param codeUrl 二维码
     * @author wxz
     * @date 11:21 2023/8/28
     */
    void saveCodeUrl(String orderNo, String codeUrl);

    /**
     * 获取订单列表
     *
     * @return java.util.List<com.example.payment.entity.OrderInfo>
     * @author wxz
     * @date 17:13 2023/8/28
     */
    List<OrderInfo> listOrderByCreateTimeDesc();

    /**
     * 更新订单状态
     *
     * @param orderNo     订单编号
     * @param orderStatus 订单状态
     * @author wxz
     * @date 09:40 2023/8/29
     */
    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);

    /**
     * 查询订单状态
     *
     * @param orderNo 订单编号
     * @return java.lang.String
     * @author wxz
     * @date 10:11 2023/8/29
     */
    String getOrderStatus(String orderNo);

    /**
     * 查询创建超过minutes分钟的未支付订单
     *
     * @param minutes     分钟
     * @param paymentType 支付类型
     * @return java.util.List<com.example.payment.entity.OrderInfo>
     * @author wxz
     * @date 11:36 2023/8/29
     */
    List<OrderInfo> getNoPayOrderByDuration(int minutes, String paymentType);

    /**
     * 根据订单编号获取订单
     *
     * @param orderNo 订单编号
     * @return com.example.payment.entity.OrderInfo
     * @author wxz
     * @date 14:34 2023/8/29
     */
    OrderInfo getOrderByOrderNo(String orderNo);
}
