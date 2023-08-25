package com.example.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.payment.entity.PaymentInfo;
import com.example.payment.mapper.PaymentInfoMapper;
import com.example.payment.service.PaymentInfoService;
import org.springframework.stereotype.Service;

/**
 * @author wxz
 * @date 12:06 2023/8/24
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService
{

}
