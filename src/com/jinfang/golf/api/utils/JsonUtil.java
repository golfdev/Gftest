package com.jinfang.golf.api.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

/**
 * @author weihua.zhang 
			weihua.zhang@opi-corp.com
 *
 */
public class JsonUtil {

    private static Log logger = LogFactory.getLog(JsonUtil.class);
    
    
    public static final void printResult(Invocation inv, int status, String msg, Map<String, Object> data){
        if(data == null){
            data = new HashMap<String, Object>();
        }
        Map<String, Object> resultMap = new HashMap<String,Object>();
        resultMap.put("status", status);
        resultMap.put("msg", msg);
        resultMap.put("data", data);

        String respStr = new Gson().toJson(resultMap);
        
        HttpServletResponse response = inv.getResponse();
        response.setContentType("application/json; charset=UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.print(respStr);
            out.flush();
            out.close();
        } catch (IOException e) {
            logger.error("输入ajax结果时异常出现",e);
        }
    }
    
}
