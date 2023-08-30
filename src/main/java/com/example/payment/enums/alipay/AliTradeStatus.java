package com.example.payment.enums.alipay;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wxz
 * @date 14:41 2023/8/30
 */
@AllArgsConstructor
@Getter
public enum AliTradeStatus
{
    /**
     * 支付成功
     */
    SUCCESS("TRADE_SUCCESS"),

    /**
     * 未支付
     */
    NOTPAY("WAIT_BUYER_PAY"),

    /**
     * 已关闭
     */
    CLOSED("RADE_CLOSED"),

    /**
     * 转入退款
     */
    REFUND("REFUND");

    /**
     * 类型
     */
    private final String type;
}
