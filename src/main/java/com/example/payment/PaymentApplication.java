package com.example.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wxz
 * @date 16:12 2023/8/23
 */
@SpringBootApplication
@EnableScheduling
public class PaymentApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
