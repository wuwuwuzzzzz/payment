package com.example.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.payment.entity.RefundInfo;

/**
 * @author wxz
 * @date 12:05 2023/8/24
 */
public interface RefundInfoService extends IService<RefundInfo>
{
    /**
     * 退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @return com.example.payment.entity.RefundInfo
     * @author wxz
     * @date 14:33 2023/8/29
     */
    RefundInfo createRefundInfoByOrderNo(String orderNo, String reason);

    /**
     * 更新退款订单
     *
     * @param content 退款结果通知参数
     * @author wxz
     * @date 14:42 2023/8/29
     */
    void updateRefund(String content);
}
