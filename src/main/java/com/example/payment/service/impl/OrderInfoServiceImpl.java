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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author wxz
 * @date 12:05 2023/8/24
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService
{
    @Resource
    private ProductMapper productMapper;

    /**
     * 生成订单
     *
     * @param productId   商品ID
     * @param paymentType 支付类型
     * @return com.example.payment.entity.OrderInfo
     * @author wxz
     * @date 11:24 2023/8/30
     */
    @Override
    public OrderInfo createOrderByProductId(Long productId, String paymentType)
    {
        // 查找已存在但未支付的订单
        OrderInfo info = this.getNoPayOrderByProductId(productId, paymentType);
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
        info.setPaymentType(paymentType);
        // 存入数据库
        baseMapper.insert(info);
        return info;
    }

    /**
     * 保存二维码
     *
     * @param orderNo 订单编号
     * @param codeUrl 二维码
     * @author wxz
     * @date 11:24 2023/8/28
     */
    @Override
    public void saveCodeUrl(String orderNo, String codeUrl)
    {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        OrderInfo info = new OrderInfo();
        info.setCodeUrl(codeUrl);
        baseMapper.update(info, wrapper);
    }

    /**
     * 获取订单列表
     *
     * @return java.util.List<com.example.payment.entity.OrderInfo>
     * @author wxz
     * @date 17:14 2023/8/28
     */
    @Override
    public List<OrderInfo> listOrderByCreateTimeDesc()
    {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>().orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }

    /**
     * 更新订单状态
     *
     * @param orderNo     订单编号
     * @param orderStatus 订单状态
     * @author wxz
     * @date 09:40 2023/8/29
     */
    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus)
    {
        log.info("更新订单状态");

        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);

        OrderInfo info = new OrderInfo();
        info.setOrderStatus(orderStatus.getType());

        baseMapper.update(info, wrapper);
    }

    /**
     * 查询订单状态
     *
     * @param orderNo 订单编号
     * @return java.lang.String
     * @author wxz
     * @date 10:11 2023/8/29
     */
    @Override
    public String getOrderStatus(String orderNo)
    {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        OrderInfo info = baseMapper.selectOne(wrapper);
        if (info == null)
        {
            return null;
        }
        return info.getOrderStatus();
    }

    /**
     * 查询创建超过minutes分钟的未支付订单
     *
     * @param minutes     分钟
     * @param paymentType 支付类型
     * @return java.util.List<com.example.payment.entity.OrderInfo>
     * @author wxz
     * @date 11:38 2023/8/29
     */
    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes, String paymentType)
    {
        Instant instant = Instant.now().minus(Duration.ofMillis(minutes));
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        wrapper.le("create_time", instant);
        wrapper.eq("payment_type", paymentType);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据订单编号获取订单
     *
     * @param orderNo 订单编号
     * @return com.example.payment.entity.OrderInfo
     * @author wxz
     * @date 14:34 2023/8/29
     */
    @Override
    public OrderInfo getOrderByOrderNo(String orderNo)
    {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 查找已存在但未支付的订单
     *
     * @param productId   商品ID
     * @param paymentType 支付类型
     * @return com.example.payment.entity.OrderInfo
     * @author wxz
     * @date 11:13 2023/8/28
     */
    private OrderInfo getNoPayOrderByProductId(Long productId, String paymentType)
    {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        wrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        wrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(wrapper);
    }
}
