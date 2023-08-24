package com.example.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wxz
 * @date 16:11 2023/8/23
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @GetMapping("/test")
    public String test() {
        return "product";
    }
}
