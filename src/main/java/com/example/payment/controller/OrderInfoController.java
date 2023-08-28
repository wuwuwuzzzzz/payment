package com.example.payment.controller;

import com.example.payment.service.OrderInfoService;
import com.example.payment.vo.R;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wxz
 * @date 17:10 2023/8/28
 */
@CrossOrigin
@RestController
@RequestMapping("/api/order-info")
@Api(tags = "订单管理")
public class OrderInfoController
{
    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 获取订单列表
     *
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 17:13 2023/8/28
     */
    @GetMapping("/list")
    public R list()
    {
        return R.ok().data("list", orderInfoService.listOrderByCreateTimeDesc());
    }

}
