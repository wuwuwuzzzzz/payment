package com.example.payment.controller;

import com.example.payment.service.AliPayService;
import com.example.payment.vo.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author wxz
 * @date 19:20 2023/8/29
 */
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/ali-pay")
@Api(tags = "支付宝支付")
public class AliPayController
{
    @Resource
    private AliPayService aliPayService;

    /**
     * 统一收单下单并支付页面接口
     *
     * @param productId 商品ID
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 19:22 2023/8/29
     */
    @PostMapping("/trade/page/pay/{productId}")
    public R tradePagePay(@PathVariable Long productId)
    {
        log.info("统一收单下单并支付页面接口调用");

        String formStr = aliPayService.tradeCreate(productId);

        return R.ok().data("formStr", formStr);
    }
}
