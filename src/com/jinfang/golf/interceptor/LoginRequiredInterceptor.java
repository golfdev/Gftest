package com.jinfang.golf.interceptor;

import java.lang.annotation.Annotation;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinfang.golf.api.utils.JsonUtil;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.utils.UserHolder;

@Component
@Interceptor(oncePerRequest = true)
public class LoginRequiredInterceptor extends ControllerInterceptorAdapter {

	private Log logger = LogFactory.getLog(LoginRequiredInterceptor.class);

	@Autowired
	private UserHolder userHolder;

	@Override
	public Class<? extends Annotation> getRequiredAnnotationClass() {
		return LoginRequired.class;
	}

	@Override
	protected Object before(Invocation inv) throws Exception {

		if (userHolder == null) {
			logger.info("userHolder is null");
		}

		if (userHolder.getPassportTicket() == null) {
			logger.info("userHolder.getPassportTicket is null");
		}

		if (userHolder == null || userHolder.getPassportTicket() == null|| userHolder.getUserInfo() == null) {
			JsonUtil.printResult(inv, ResponseStatus.NOT_LOGIN,
					"需要登录！", null);

			return false;
		}

		return true;
	}
	
	public int getPriority() {
        return 200;
    }

}
