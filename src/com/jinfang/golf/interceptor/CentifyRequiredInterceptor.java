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
public class CentifyRequiredInterceptor extends ControllerInterceptorAdapter {

	private Log logger = LogFactory.getLog(CentifyRequiredInterceptor.class);

	@Autowired
	private UserHolder userHolder;

	@Override
	public Class<? extends Annotation> getRequiredAnnotationClass() {
		return CentifyRequired.class;
	}
	
	public int getPriority() {
        return 100;
    }

	@Override
	protected Object before(Invocation inv) throws Exception {

		
		if (userHolder.getUserInfo().getStatus() != 1) {
			JsonUtil.printResult(inv, ResponseStatus.NO_AUTH,
					"未认证用户不能操作!", null);

			return false;
		}

		return true;
	}

}
