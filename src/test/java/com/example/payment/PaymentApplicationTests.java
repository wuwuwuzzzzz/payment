package com.example.payment;

import com.example.payment.config.WxPayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class PaymentApplicationTests
{
    @Resource
    private WxPayConfig wxPayConfig;

    @Test
    void test()
    {
        System.out.println(wxPayConfig.getPrivateKey(wxPayConfig.getPrivateKeyPath()));
    }
}
