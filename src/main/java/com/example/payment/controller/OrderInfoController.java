package com.example.payment.controller;

import com.example.payment.enums.OrderStatus;
import com.example.payment.service.OrderInfoService;
import com.example.payment.vo.R;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查询订单状态
     *
     * @param orderNo 订单编号
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 10:25 2023/8/29
     */
    @GetMapping("/query-order-status/{orderNo}")
    public R queryOrderStatus(@PathVariable String orderNo)
    {
        String orderStatus = orderInfoService.getOrderStatus(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus))
        {
            return R.ok().setMsg("支付成功");
        }
        return R.ok().setCode(101).setMsg("支付中...");
    }
}
