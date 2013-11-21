package com.jinfang.golf.controllers.v1;

import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jinfang.golf.api.utils.JsonUtil;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.FormatCheckUtil;

@Path("user")
public class UserLoginController {

	@Autowired
	private Invocation inv;
	
	@Autowired
	private UserHome userHome;
	
	@Autowired
	private Passport passport;
	
	/**
	 * 登录，返回授权token
	 * @param name
	 * @param pwd
	 * @throws Exception
	 */
	@Post("slogin")
	public void login(@Param("identity") String identity, @Param("pwd") String pwd) throws Exception {
	    
	    
	    if(StringUtils.isBlank(identity)){
	        JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "用户标识不能为空！", null);
	        return;
	    }
	    
	    User user = null;
	    
	    if(FormatCheckUtil.isEmail(identity)){
	        user = userHome.getByEmail(identity);
	    }else if(FormatCheckUtil.isMobileNO(identity)){
	        user = userHome.getByPhone(identity);
	    }else{
	        JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "用户标识格式非法！", null);
            return;
	    }
	    
	    if(user==null){
	        JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "用户不存在！", null);
            return;
	    }
		
		String token = passport.createAppToken(user.getId(), Integer.MAX_VALUE);
		
		Map<String, Object> resultMap = new HashMap<String,Object>();
		resultMap.put("_jftk", token);
		resultMap.put("user",user );
		JsonUtil.printResult(inv, ResponseStatus.OK, "登录成功！", resultMap);
		return;
		
	}
}
