package com.example.payment.util;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 订单号工具类
 *
 * @author wxz
 * @date 11:31 2023/8/25
 */
public class OrderNoUtils
{
    /**
     * 获取订单编号
     *
     * @return java.lang.String
     * @author wxz
     * @date 11:32 2023/8/25
     */
    public static String getOrderNo()
    {
        return "ORDER_" + getNo();
    }

    /**
     * 获取退款单编号
     *
     * @return java.lang.String
     * @author wxz
     * @date 11:32 2023/8/25
     */
    public static String getRefundNo()
    {
        return "REFUND_" + getNo();
    }

    /**
     * 获取编号
     *
     * @return java.lang.String
     * @author wxz
     * @date 11:32 2023/8/25
     */
    public static String getNo()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String newDate = sdf.format(new Date());
        StringBuilder result = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 3; i++)
        {
            result.append(random.nextInt(10));
        }
        return newDate + result;
    }

}
