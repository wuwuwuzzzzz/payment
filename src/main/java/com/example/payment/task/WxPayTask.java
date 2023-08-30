package com.example.payment.task;

import com.example.payment.entity.OrderInfo;
import com.example.payment.service.OrderInfoService;
import com.example.payment.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务
 *
 * @author wxz
 * @date 11:29 2023/8/29
 */
@Slf4j
@Component
public class WxPayTask
{
    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private WxPayService wxPayService;

    /**
     * 从第0秒开始每隔30秒执行一次
     *
     * @author wxz
     * @date 11:33 2023/8/29
     */
//    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm()
    {
        List<OrderInfo> infoList = orderInfoService.getNoPayOrderByDuration(5);

        for (OrderInfo info : infoList)
        {
            log.warn("超时订单: {}", info.getOrderNo());

            // 核实订单状态: 调用微信支付查单接口
            wxPayService.checkOrderStatus(info.getOrderNo());
        }
    }
}
