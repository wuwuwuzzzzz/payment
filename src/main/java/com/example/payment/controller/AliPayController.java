package com.example.payment.controller;

import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.payment.entity.OrderInfo;
import com.example.payment.service.AliPayService;
import com.example.payment.service.OrderInfoService;
import com.example.payment.vo.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

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

    @Resource
    private Environment config;

    @Resource
    private OrderInfoService orderInfoService;

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

    /**
     * 支付通知
     *
     * @return java.lang.String
     * @author wxz
     * @date 09:55 2023/8/30
     */
    @PostMapping("/trade/notify")
    public String tradeNotify(@RequestParam Map<String, String> params) throws Exception
    {
        log.info("支付通知");

        log.info("支付宝支付通知参数: {}", params);

        // 调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                config.getProperty("alipay.alipay-public-key"),
                AlipayConstants.CHARSET_UTF8,
                AlipayConstants.SIGN_TYPE_RSA2);

        if (!signVerified)
        {
            log.error("签名验证失败");

            return "fail";
        }

        // 商家需要验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号
        String outTradeNo = params.get("out_trade_no");
        OrderInfo info = orderInfoService.getOrderByOrderNo(outTradeNo);
        if (info == null)
        {
            log.error("订单不存在");

            return "fail";
        }

        // 判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）
        String totalAmount = params.get("total_amount");
        // 订单金额（分）
        int totalAmountInt = new BigDecimal(totalAmount).multiply(new BigDecimal("100")).intValue();
        // 订单金额（分）
        int totalFeeInt = info.getTotalFee();
        if (totalAmountInt != totalFeeInt)
        {
            log.error("订单金额不一致");

            return "fail";
        }

        // 校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方（有的时候，一个商家可能有多个 seller_id/seller_email）
        String sellerId = params.get("seller_id");
        if (!sellerId.equals(config.getProperty("alipay.seller-id")))
        {
            log.error("seller_id 不一致");

            return "fail";
        }

        // 验证 app_id 是否为该商家本身
        String appId = params.get("app_id");
        if (!appId.equals(config.getProperty("alipay.app-id")))
        {
            log.error("app_id 不一致");

            return "fail";
        }

        // 交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus))
        {
            log.error("交易状态不正确");

            return "fail";
        }

        // 处理业务
        aliPayService.processOrder(params);

        return "success";
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 11:29 2023/8/30
     */
    @PostMapping("/trade/close/{orderNo}")
    public R cancel(@PathVariable String orderNo)
    {
        log.info("取消订单");

        aliPayService.cancelOrder(orderNo);

        return R.ok().setMsg("取消订单成功");
    }

    /**
     * 查询订单
     *
     * @param orderNo 订单编号
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 14:15 2023/8/30
     */
    @GetMapping("/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo)
    {
        log.info("查询订单");

        return R.ok().setMsg("查询订单成功").data("result", aliPayService.queryOrder(orderNo));
    }

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 14:52 2023/8/30
     */
    @PostMapping("/trade/refund/{orderNo}/{reason}")
    public R refunds(@PathVariable String orderNo, @PathVariable String reason)
    {
        log.info("申请退款");

        aliPayService.refund(orderNo, reason);

        return R.ok();
    }

    /**
     * 查询退款
     *
     * @param orderNo 订单编号
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 15:28 2023/8/30
     */
    @GetMapping("/trade/fastpay/refund/{orderNo}")
    public R queryRefund(@PathVariable String orderNo)
    {
        log.info("查询退款");

        return R.ok().setMsg("查询退款成功").data("result", aliPayService.queryRefund(orderNo));
    }

    @GetMapping("/bill/downloadurl/query/{billDate}/{type}")
    public R queryTradeBill(@PathVariable String billDate, @PathVariable String type)
    {
        log.info("查询对账单下载地址");

        return R.ok().setMsg("查询对账单下载地址成功").data("downloadUrl", aliPayService.queryBill(billDate, type));
    }
}
