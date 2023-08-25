package com.example.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.payment.entity.OrderInfo;
import com.example.payment.mapper.OrderInfoMapper;
import com.example.payment.service.OrderInfoService;
import org.springframework.stereotype.Service;

/**
 * @author wxz
 * @date 12:05 2023/8/24
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService
{

}
