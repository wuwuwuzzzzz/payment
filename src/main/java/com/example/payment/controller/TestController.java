package com.example.payment.controller;

import com.example.payment.config.WxPayConfig;
import com.example.payment.vo.R;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wxz
 * @date 10:22 2023/8/25
 */
@Api(tags = "测试")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Resource
    private WxPayConfig wxPayConfig;

    @GetMapping
    public R getWxPayConfig() {
        String mchId = wxPayConfig.getMchId();
        return R.ok().data("wxPayConfig", mchId);
    }
}
