package com.example.payment.service.impl;

import com.example.payment.entity.OrderInfo;
import com.example.payment.enums.OrderStatus;
import com.example.payment.service.WxPayService;
import com.example.payment.util.OrderNoUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author wxz
 * @date 11:41 2023/8/25
 */
@Service
public class WxPayServiceImpl implements WxPayService
{
    /**
     * @param productId 商品ID
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author wxz
     * @date 11:58 2023/8/25
     */
    @Override
    public Map<String, Object> nativePay(Long productId)
    {
        // 生成订单
        OrderInfo info = new OrderInfo();
        info.setTitle("测试商品");
        info.setOrderNo(OrderNoUtils.getOrderNo());
        info.setProductId(productId);
        info.setTotalFee(1);
        info.setOrderStatus(OrderStatus.NOTPAY.getType());
        //
        return null;
    }
}
