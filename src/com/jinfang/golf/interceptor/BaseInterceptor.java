/**
 * 
 */
package com.jinfang.golf.interceptor;

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

        stopWatchs.set(new Log4JStopWatch());
        String token = inv.getParameter("_jftk");
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

}
