package com.example.payment.task;

import com.example.payment.entity.OrderInfo;
import com.example.payment.enums.PayType;
import com.example.payment.service.AliPayService;
import com.example.payment.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wxz
 * @date 14:26 2023/8/30
 */
@Slf4j
@Component
public class AliPayTask
{
    @Resource
    AliPayService aliPayService;
    @Resource
    private OrderInfoService orderInfoService;

    /**
     * @author wxz
     * @date 14:28 2023/8/30
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm()
    {
        log.info("支付宝定时任务");

        List<OrderInfo> infoList = orderInfoService.getNoPayOrderByDuration(5, PayType.ALIPAY.getType());

        for (OrderInfo info : infoList)
        {
            log.warn("超时订单: {}", info.getOrderNo());

            // 核实订单状态: 调用微信支付查单接口
            aliPayService.checkOrderStatus(info.getOrderNo());
        }
    }
}
