package com.example.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.payment.entity.Product;
import com.example.payment.mapper.ProductMapper;
import com.example.payment.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * @author wxz
 * @date 12:06 2023/8/24
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService
{

}
