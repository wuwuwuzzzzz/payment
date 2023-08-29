package com.example.payment.util;


import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

import static com.wechat.pay.contrib.apache.httpclient.constant.WechatPayHttpHeaders.*;

/**
 * @author wxz
 * @date 20:34 2023/8/28
 */
@AllArgsConstructor
public class WechatPay2ValidatorForRequest
{
    protected static final Logger log = LoggerFactory.getLogger(WechatPay2ValidatorForRequest.class);

    /**
     * 应答超时时间（单位为分钟）
     */
    protected static final long RESPONSE_EXPIRED_MINUTES = 5;

    /**
     * verifier
     */
    protected final Verifier verifier;

    /**
     * body
     */
    protected final String body;

    /**
     * requestId
     */
    protected final String requestId;

    /**
     * @return java.lang.IllegalArgumentException
     * @author wxz
     * @date 20:42 2023/8/28
     */
    protected static IllegalArgumentException parameterError(String message, Object... args)
    {
        message = String.format(message, args);
        return new IllegalArgumentException("parameter error: " + message);
    }

    /**
     * @return java.lang.IllegalArgumentException
     * @author wxz
     * @date 20:45 2023/8/28
     */
    protected static IllegalArgumentException verifyFail(String message, Object... args)
    {
        message = String.format(message, args);
        return new IllegalArgumentException("signature verify fail: " + message);
    }

    /**
     * @return boolean
     * @author wxz
     * @date 20:42 2023/8/28
     */
    public final boolean validate(HttpServletRequest request)
    {
        try
        {
            // 处理请求参数
            validateParameters(request);

            String message = buildMessage(request);
            String serial = request.getHeader(WECHAT_PAY_SERIAL);
            String signature = request.getHeader(WECHAT_PAY_SIGNATURE);

            if (!verifier.verify(serial, message.getBytes(StandardCharsets.UTF_8), signature))
            {
                throw verifyFail("serial=[%s] message=[%s] sign=[%s], request-id=[%s]",
                        serial, message, signature, request.getHeader(REQUEST_ID));
            }
        }
        catch (IllegalArgumentException e)
        {
            log.warn(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 处理请求参数
     *
     * @author wxz
     * @date 20:35 2023/8/28
     */
    protected final void validateParameters(HttpServletRequest request)
    {

        // NOTE: ensure HEADER_WECHAT_PAY_TIMESTAMP at last
        String[] headers = {WECHAT_PAY_SERIAL, WECHAT_PAY_SIGNATURE, WECHAT_PAY_NONCE, WECHAT_PAY_TIMESTAMP};

        String header = null;
        for (String headerName : headers)
        {
            header = request.getHeader(headerName);
            if (header == null)
            {
                throw parameterError("empty [%s], request-id=[%s]", headerName, requestId);
            }
        }

        String timestampStr = header;
        try
        {
            Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestampStr));
            // 拒绝过期应答
            if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= RESPONSE_EXPIRED_MINUTES)
            {
                throw parameterError("timestamp=[%s] expires, request-id=[%s]", timestampStr, requestId);
            }
        }
        catch (DateTimeException | NumberFormatException e)
        {
            throw parameterError("invalid timestamp=[%s], request-id=[%s]", timestampStr, requestId);
        }
    }

    /**
     * @return java.lang.String
     * @author wxz
     * @date 20:45 2023/8/28
     */
    protected final String buildMessage(HttpServletRequest request)
    {
        String timestamp = request.getHeader(WECHAT_PAY_TIMESTAMP);
        String nonce = request.getHeader(WECHAT_PAY_NONCE);
        return timestamp + "\n"
                + nonce + "\n"
                + body + "\n";
    }
}
