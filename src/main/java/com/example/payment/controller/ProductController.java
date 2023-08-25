package com.example.payment.controller;

import com.example.payment.entity.Product;
import com.example.payment.service.ProductService;
import com.example.payment.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author wxz
 * @date 16:11 2023/8/23
 */
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController
{
    @Autowired
    private ProductService productService;

    /**
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 11:56 2023/8/25
     */
    @ApiOperation("测试接口")
    @GetMapping("/test")
    public R test()
    {
        return R.ok().data("now", new Date());
    }

    /**
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 12:11 2023/8/24
     */
    @GetMapping("/list")
    public R list()
    {
        List<Product> list = productService.list();
        return R.ok().data("productList", list);
    }
}
