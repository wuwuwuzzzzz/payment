package com.example.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.payment.entity.OrderInfo;

/**
 * @author wxz
 * @date 12:04 2023/8/24
 */
public interface OrderInfoService extends IService<OrderInfo>
{
    /**
     * 生成订单
     *
     * @param productId 商品ID
     * @return com.example.payment.entity.OrderInfo
     * @author wxz
     * @date 10:58 2023/8/28
     */
    OrderInfo createOrderByProductId(Long productId);

    /**
     * 保存二维码
     *
     * @param orderNo 商品编号
     * @param codeUrl 二维码
     * @author wxz
     * @date 11:21 2023/8/28
     */
    void saveCodeUrl(String orderNo, String codeUrl);
}
