package com.example.payment.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author wxz
 * @date 11:33 2023/8/25
 */
public class HttpUtils
{
    /**
     * 将通知参数转化为字符串
     *
     * @return java.lang.String
     * @author wxz
     * @date 11:34 2023/8/25
     */
    public static String readData(HttpServletRequest request)
    {
        BufferedReader br = null;
        try
        {
            StringBuilder result = new StringBuilder();
            br = request.getReader();
            for (String line; (line = br.readLine()) != null; )
            {
                if (result.length() > 0)
                {
                    result.append("\n");
                }
                result.append(line);
            }
            return result.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}