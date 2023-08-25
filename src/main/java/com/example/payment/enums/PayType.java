package com.example.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @author wxz
 * @date 11:29 2023/8/25
 */
@AllArgsConstructor
@Getter
public enum PayType
{
    /**
     * 微信
     */
    WXPAY("微信"),


    /**
     * 支付宝
     */
    ALIPAY("支付宝");

    /**
     * 类型
     */
    private final String type;
}
