package com.example.payment.controller;

import com.example.payment.service.WxPayService;
import com.example.payment.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author wxz
 * @date 11:39 2023/8/25
 */
@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
@Api(tags = "微信支付")
@Slf4j
public class WxPayController
{
    @Resource
    private WxPayService wxPayService;

    /**
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 11:56 2023/8/25
     */
    @ApiOperation(("调用统一下单API，生成支付二维码"))
    @PostMapping("/native/{productId}")
    public R nativePay(@PathVariable("productId") Long productId) throws IOException
    {
        log.info("发起支付请求");
        return R.ok().setData(wxPayService.nativePay(productId));
    }
}
