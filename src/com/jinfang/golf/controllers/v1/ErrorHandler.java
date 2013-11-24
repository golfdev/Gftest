package com.jinfang.golf.controllers.v1;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.Invocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.WebUtils;

import com.jinfang.golf.api.utils.JsonUtil;
import com.jinfang.golf.constants.ResponseStatus;

public class ErrorHandler implements ControllerErrorHandler{

	@Override
	public Object onError(Invocation inv, Throwable ex) throws Throwable {
		Log logger = LogFactory.getLog(inv.getControllerClass());
		logger.error("", ex);
		inv.getRequest().removeAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
		
	    JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "服务器内部错误！", null);
		return "";
	}

}
