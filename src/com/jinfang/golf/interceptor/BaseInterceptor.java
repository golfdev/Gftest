/**
 * 
 */
package com.jinfang.golf.interceptor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Interceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.api.utils.JsonUtil;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.passport.model.PassportTicket;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.UserHolder;

@Component
@Interceptor
public class BaseInterceptor extends ControllerInterceptorAdapter {
    
    private Log logger = LogFactory.getLog(LoginRequiredInterceptor.class);

    private ThreadLocal<StopWatch> stopWatchs = new ThreadLocal<StopWatch>();

    @Autowired
    private UserHolder userHolder;

    @Autowired
    private Passport passport;

    @Autowired
    private UserHome userHome;

    @Override
    protected Object before(Invocation inv) throws Exception {

        String appKey = inv.getRequest().getHeader("appKey");
        logger.info("appKey:" + inv.getRequest().getHeader("appKey"));
        if (!StringUtils.equals(appKey, GolfConstant.APPKEY_VALUE)) {
            JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "appKey error", null);
            return false;
        }
        
        if(!validateSign(inv)){
        	JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "参数非法！", null);
            return false;
        }

        stopWatchs.set(new Log4JStopWatch());
        String token = inv.getParameter("token");
        logger.info("token:" + token);
        PassportTicket passportTicket = passport.readInToken(token);
        if (passportTicket != null) {
            logger.info("init the user holder");
            initUserHolder(inv, passportTicket);
        }
        return true;

    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {

        if (stopWatchs.get() != null) {
            stopWatchs.get().stop(inv.getRequest().getRequestURI());
        }

        stopWatchs.remove();
        userHolder.clean();
    }

    private void initUserHolder(Invocation inv, PassportTicket passportTicket) {
        userHolder.setPassportTicket(passportTicket);
        User user = userHome.getById(passportTicket.getUserId());
        userHolder.setUserInfo(user);
    }
    
    
    /**
     * 做param的sign验证
     * 将param按字典排序后再md5
     */
    public  boolean validateSign(Invocation inv) {
        String sign = inv.getRequest().getParameter("sign");
        //@Auto temp
        if (sign == null || "".equals(sign)) {
            return false;
        }
        StringBuffer stringBuffer = new StringBuffer();
        List<String> params = new ArrayList<String>();
        Map<String, String[]> map = inv.getRequest().getParameterMap();

        for (Map.Entry<String, String[]> m : map.entrySet()) {
            String value = "";
            for (String s : m.getValue()) {
                value += s;
            }
            if(!"sign".equals(m.getKey())) {
                params.add(m.getKey() + "=" + value);
            }
        }

        Collections.sort(params);
        for (String param : params) {
            stringBuffer.append(param);
        }
        String secret="golf_jf_security";
        return sign.equals(getSignature(stringBuffer.toString(), secret));
    }
    
    /**
     * 生成sign验证串
     * 因iphone的base64复杂，暂时只是MD5
     */
    private static String getSignature(String parameters, String secret) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }

        // 因iphone手机客户端暂时无法base64,现阶段只返回MD5
        StringBuffer result = new StringBuffer();
        for (byte b : md.digest((parameters + secret).getBytes())) {
            result.append(Integer.toHexString((b & 0xf0) >>> 4));
            result.append(Integer.toHexString(b & 0x0f));
        }
        return result.toString();
    }
    
    public static void main(String[] args){
    	System.out.println(getSignature("id=4","golf_jf_security"));
    }

}
