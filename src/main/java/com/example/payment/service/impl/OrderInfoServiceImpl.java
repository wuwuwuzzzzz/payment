package com.example.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.payment.entity.OrderInfo;
import com.example.payment.entity.Product;
import com.example.payment.enums.OrderStatus;
import com.example.payment.mapper.OrderInfoMapper;
import com.example.payment.mapper.ProductMapper;
import com.example.payment.service.OrderInfoService;
import com.example.payment.util.OrderNoUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wxz
 * @date 12:05 2023/8/24
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService
{
    @Resource
    private ProductMapper productMapper;

    @Override
    public OrderInfo createOrderByProductId(Long productId)
    {
        // 查找已存在但未支付的订单
        OrderInfo info = this.getNoPayOrderByProductId(productId);
        if (info != null)
        {
            return info;
        }
        // 获取商品信息
        Product product = productMapper.selectById(productId);
        // 生成订单
        info = new OrderInfo();
        info.setTitle(product.getTitle());
        info.setOrderNo(OrderNoUtils.getOrderNo());
        info.setProductId(productId);
        info.setTotalFee(product.getPrice());
        info.setOrderStatus(OrderStatus.NOTPAY.getType());
        // 存入数据库
        baseMapper.insert(info);
        return info;
    }

    /**
     * 查找已存在但未支付的订单
     *
     * @param productId 商品ID
     * @return com.example.payment.entity.OrderInfo
     * @author wxz
     * @date 11:13 2023/8/28
     */
    private OrderInfo getNoPayOrderByProductId(Long productId)
    {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        wrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        return baseMapper.selectOne(wrapper);
    }
}
