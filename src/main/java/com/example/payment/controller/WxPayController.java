package com.example.payment.controller;

import com.example.payment.service.WxPayService;
import com.example.payment.util.HttpUtils;
import com.example.payment.util.WechatPay2ValidatorForRequest;
import com.example.payment.vo.R;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    @Resource
    private Verifier verifier;

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

    /**
     * 支付通知
     *
     * @return java.lang.String
     * @author wxz
     * @date 20:06 2023/8/28
     */
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response)
    {
        Gson gson = new Gson();
        // 应答对象
        HashMap<Object, Object> map = new HashMap<>(10);
        try
        {
            // 处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);

            log.info("微信支付通知参数：{}", bodyMap);

            // 验证签名
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest = new WechatPay2ValidatorForRequest(verifier, body, bodyMap.get("id")
                                                                                                                                   .toString());
            if (!wechatPay2ValidatorForRequest.validate(request))
            {
                log.error("验签失败");

                // 失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("msg", "验签失败");

                return gson.toJson(map);
            }

            log.info("验签成功");

            //  处理订单
            wxPayService.processOrder(bodyMap);

            // 成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("msg", "成功");

            return gson.toJson(map);
        }
        catch (Exception e)
        {
            // 失败应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("msg", "失败");

            return gson.toJson(map);
        }
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 10:58 2023/8/29
     */
    @PostMapping("/cancel/{orderNo}")
    public R cancel(@PathVariable("orderNo") String orderNo)
    {
        log.info("取消订单");

        wxPayService.cancelOrder(orderNo);
        return R.ok().setMsg("取消成功");
    }

    /**
     * 查询订单
     *
     * @param orderNo 订单编号
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 11:16 2023/8/29
     */
    @GetMapping("/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo)
    {
        log.info("查询订单");

        return R.ok().setMsg("查询成功").data("result", wxPayService.queryOrder(orderNo));
    }

    /**
     * 退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 14:23 2023/8/29
     */
    @ApiOperation("退款")
    @PostMapping("/refunds/{orderNo}/{reason}")
    public R reFunds(@PathVariable String orderNo, @PathVariable String reason)
    {
        log.info("退款");

        wxPayService.reFund(orderNo, reason);
        return R.ok();
    }

    /**
     * 查询退款
     *
     * @param refundNo 退款编号
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 15:20 2023/8/29
     */
    @GetMapping("/query-refund/{refundNo}")
    public R queryRefund(@PathVariable String refundNo)
    {
        log.info("查询退款");

        return R.ok().setMsg("查询成功").data("result", wxPayService.queryRefund(refundNo));
    }

    /**
     * 退款结果通知
     *
     * @return java.lang.String
     * @author wxz
     * @date 15:45 2023/8/29
     */
    @PostMapping("/refunds/notify")
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        log.info("退款通知");

        Gson gson = new Gson();
        // 应答对象
        HashMap<Object, Object> map = new HashMap<>(10);

        try
        {
            // 处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String requestId = (String) bodyMap.get("id");

            log.info("微信退款通知参数：{}", bodyMap);

            // 验证签名
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest
                    = new WechatPay2ValidatorForRequest(verifier, body, requestId);

            if (!wechatPay2ValidatorForRequest.validate(request))
            {
                log.error("验签失败");

                // 失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("msg", "验签失败");

                return gson.toJson(map);
            }

            log.info("验签成功");

            // 处理退款
            wxPayService.processRefund(bodyMap);

            // 成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("msg", "成功");

            return gson.toJson(map);
        }
        catch (Exception e)
        {
            // 失败应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("msg", "失败");

            return gson.toJson(map);
        }
    }

    /**
     * 获取账单URL
     *
     * @param billDate billDate
     * @param type     type
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 15:58 2023/8/29
     */
    @GetMapping("/querybill/{billDate}/{type}")
    public R queryTradeBill(@PathVariable String billDate, @PathVariable String type)
    {
        log.info("查询对账单");

        return R.ok().setMsg("查询成功").data("downloadUrl", wxPayService.queryBill(billDate, type));
    }

    /**
     * 下载账单
     *
     * @param billDate billDate
     * @param type     type
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 16:09 2023/8/29
     */
    @GetMapping("/downloadbill/{billDate}/{type}")
    public R downloadBill(@PathVariable String billDate, @PathVariable String type)
    {
        log.info("下载账单");

        return R.ok().setMsg("下载成功").data("result", wxPayService.downloadBill(billDate, type));
    }
}
