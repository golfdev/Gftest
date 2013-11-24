package com.jinfang.golf.controllers.v1;

import java.util.Date;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jinfang.golf.api.utils.JsonUtil;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.sms.home.SmsHome;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.home.VerifyCodeHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.user.model.VerifyCode;
import com.jinfang.golf.utils.FormatCheckUtil;

@Path("user")
public class UserRegController {

	@Autowired
	private Invocation inv;
	
	@Autowired
	private UserHome userHome;
	
	@Autowired
	private VerifyCodeHome verifyCodeHome;
	
	@Autowired
	private SmsHome smsHome;
	
	@Autowired
	private Passport passport;
	
	/**
	 * 发送验证码
	 * @param identity
	 * @param pwd
	 * @throws Exception
	 */
	@Post("sendVerifyCode")
	public void sendVerifyCode(@Param("phone") String phone) throws Exception {
	    
	    
	    if(StringUtils.isBlank(phone)){
	        JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "请输入手机号码！", null);
	        return;
	    }
	    
	    if(FormatCheckUtil.isMobileNO(phone)){
            JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "手机号码格式不正确！", null);
            return;
        }
	    
	    String code = verifyCodeHome.getCode(phone);
	    
	    //发送验证码
	    smsHome.sendVerifyCodeSms(phone, code);

		JsonUtil.printResult(inv, ResponseStatus.OK, "发送验证码成功！", null);
		return;
		
	}
	
	
	/**
     * 发送验证码
     * @param identity
     * @param pwd
     * @throws Exception
     */
    @Post("checkVerifyCode")
    public void checkVerifyCode(@Param("phone") String phone,@Param("code") String code) throws Exception {
        
        
        if(StringUtils.isBlank(phone)){
            JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "请输入手机号码！", null);
            return;
        }
        
        if(StringUtils.isBlank(phone)){
            JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "请输入验证码！", null);
            return;
        }
        
        VerifyCode verifyCode = verifyCodeHome.get(phone);
        
        if(verifyCode!=null&&verifyCode.getCode().equals(code)&&verifyCode.getExpriedTime().after(new Date())){
            JsonUtil.printResult(inv, ResponseStatus.OK, "验证成功！", null);
        }else{
            JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "验证码失效！", null);
        }
      
        return;
        
    }
    
    /**
     * 发送验证码
     * @param identity
     * @param pwd
     * @throws Exception
     */
    @Post("register")
    public void register(@Param("phone") String phone,@Param("userName") String userName,@Param("email") String email,@Param("pwd") String passWord) throws Exception {
        
        
        if(StringUtils.isBlank(userName)){
            JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "请输入用户名！", null);
            return;
        }
        
        if(StringUtils.isBlank(phone)&&StringUtils.isBlank(email)){
            JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "请输入手机或邮箱！", null);
            return;
        }
        
        User user = null;
        
        //手机号注册
        if(StringUtils.isNotBlank(phone)){
            user = userHome.getByPhone(phone);
            if(user!=null){
                JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "该手机号已经被注册！", null);
                return;
            }else{
                user = new User();
                user.setPhone(phone);
                user.setPassWord(passWord);
                user.setEmail(email);
                user.setUserName(userName);
                userHome.save(user);
                JsonUtil.printResult(inv, ResponseStatus.OK, "注册成功！", null);
                return;
            }
        }
        
        //邮箱注册
        if(StringUtils.isNotBlank(email)){
            user = userHome.getByEmail(email);
            if(user!=null){
                JsonUtil.printResult(inv, ResponseStatus.SERVER_ERROR, "该邮箱已经被注册！", null);
                return;
            }else{
                user = new User();
                user.setPhone(phone);
                user.setPassWord(passWord);
                user.setEmail(email);
                user.setUserName(userName);
                userHome.save(user);
                JsonUtil.printResult(inv, ResponseStatus.OK, "注册成功！", null);
                return;
            }
        }
      
        return;
        
    }
}
