package com.example.payment.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wxz
 * @date 12:09 2023/8/24
 */
@Configuration
@MapperScan("com.example.payment.mapper")
@EnableTransactionManagement
public class MybatisPlusConfig {
}
