package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.util.Date;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.api.utils.JsonUtil;
import com.jinfang.golf.constants.DeviceType;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.sms.home.SmsHome;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.home.VerifyCodeHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.user.model.UserCentify;
import com.jinfang.golf.user.model.VerifyCode;
import com.jinfang.golf.utils.FormatCheckUtil;
import com.jinfang.golf.utils.Md5;

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
	 * 
	 * @param identity
	 * @param pwd
	 * @throws Exception
	 */
	@Post("sendVerifyCode")
	public String sendVerifyCode(@Param("phone") String phone) throws Exception {

		if (StringUtils.isBlank(phone)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "请输入手机号码！"));
		}

		if (FormatCheckUtil.isMobileNO(phone)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "手机号码格式不正确！"));
		}

		String code = verifyCodeHome.getCode(phone);

		// 发送验证码
		smsHome.sendVerifyCodeSms(phone, code);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "发送成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}

	/**
	 * 校验验证码
	 * 
	 * @param phone
	 * @param code
	 * @return
	 * @throws Exception
	 */
	@Post("checkVerifyCode")
	public String checkVerifyCode(@Param("phone") String phone,
			@Param("code") String code) throws Exception {

		if (StringUtils.isBlank(phone)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "请输入手机号码！"));
		}

		if (StringUtils.isBlank(phone)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "请输入验证码！"));
		}

		VerifyCode verifyCode = verifyCodeHome.get(phone);

		if (verifyCode != null && verifyCode.getCode().equals(code)
				&& verifyCode.getExpriedTime().after(new Date())) {
			BaseResponseItem<String> result = new BaseResponseItem<String>(
					ResponseStatus.OK, "验证成功！");
			Type type = new TypeToken<BaseResponseItem<String>>() {
			}.getType();
			return "@" + BeanJsonUtils.convertToJson(result, type);
		} else {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "验证码失效！"));
		}

	}

	/**
	 * 注册
	 * 
	 * @param phone
	 * @param userName
	 * @param email
	 * @param passWord
	 * @return
	 * @throws Exception
	 */
	@Post("register")
	public String register(@Param("phone") String phone,
			@Param("userName") String userName, @Param("device") String device,
			@Param("pwd") String passWord, @Param("realName") String realName,
			@Param("sfzId") String sfzId) throws Exception {

		if (StringUtils.isBlank(userName)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "请输入用户名！"));
		}

		User user = null;

		// 手机号注册
		if (StringUtils.isNotBlank(phone)) {
			user = userHome.getByPhone(phone);
			if (user != null) {
				return "@"
						+ BeanJsonUtils
								.convertToJsonWithException(new GolfException(
										ResponseStatus.SERVER_ERROR,
										"该手机号已经被注册！"));
			} else {
				// user = userHome.getByDevice(device);
				// if (user != null&&user.getStatus()==0) {
				// user.setPhone(phone);
				// user.setPassWord(passWord);
				// user.setUserName(userName);
				// user.setStatus(1);
				// userHome.updateForReg(user);
				// } else {
				user = new User();
				user.setPhone(phone);
				user.setPassWord(passWord);
				user.setUserName(userName);
				if (StringUtils.isNotBlank(realName)
						&& StringUtils.isNotBlank(sfzId)) {
					user.setStatus(1);
				} else {
					user.setStatus(0);
				}
				user.setCity("北京");
				user.setHeadUrl(GolfConstant.DEFAULT_HEAD_URL);
				Integer userId = userHome.save(user);

				// 保存实名认证信息
				if (StringUtils.isNotBlank(realName)
						&& StringUtils.isNotBlank(sfzId)) {

					UserCentify centify = new UserCentify();
					centify.setUserId(userId);
					centify.setRealName(realName);
					centify.setSfzId(sfzId);
					userHome.saveCentifyInfo(centify);
				}

				// 保存设备号
				userHome.saveUserDevice(userId, device);
				// }
				String token = passport.createAppToken(userId,
						Integer.MAX_VALUE);
				user.setToken(token);
				user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + user.getHeadUrl());

				// 更新登录token
				String appKey = inv.getRequest().getHeader("appKey");
				String source = DeviceType.ANDROID.getType();
				if (StringUtils.equals(appKey, GolfConstant.APPKEY_IOS_VALUE)) {
					source = DeviceType.IOS.getType();
				}

				userHome.updateTokenAndSource(userId, token, source);
				BaseResponseItem<User> result = new BaseResponseItem<User>(
						ResponseStatus.OK, "注册成功！");
				Type type = new TypeToken<BaseResponseItem<User>>() {
				}.getType();
				result.setData(user);
				return "@"
						+ BeanJsonUtils.convertToJsonWithGsonBuilder(result,
								type);

			}
		}

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "注册成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}
	
	
	/**
	 * 匿名注册
	 * 
	 * @param phone
	 * @param userName
	 * @param email
	 * @param passWord
	 * @return
	 * @throws Exception
	 */
	@Post("anoRegister")
	public String anonymous(@Param("phone") String phone,
			@Param("userName") String userName) throws Exception {

		if (StringUtils.isBlank(userName)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "请输入用户名！"));
		}

		User user = null;

		// 手机号注册
		if (StringUtils.isNotBlank(phone)) {
			user = userHome.getByPhone(phone);
			if (user != null) {
				return "@"
						+ BeanJsonUtils
								.convertToJsonWithException(new GolfException(
										ResponseStatus.SERVER_ERROR,
										"该手机号已经被注册！"));
			} else {
				// user = userHome.getByDevice(device);
				// if (user != null&&user.getStatus()==0) {
				// user.setPhone(phone);
				// user.setPassWord(passWord);
				// user.setUserName(userName);
				// user.setStatus(1);
				// userHome.updateForReg(user);
				// } else {
				user = new User();
				user.setPhone(phone);
				user.setPassWord(Md5.md5s("123456"));
				user.setUserName(userName);
			    user.setStatus(0);
				user.setCity("北京");
				user.setHeadUrl(GolfConstant.DEFAULT_HEAD_URL);
				Integer userId = userHome.save(user);

				// }
				String token = passport.createAppToken(userId,
						Integer.MAX_VALUE);
				user.setToken(token);
				user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + user.getHeadUrl());

				// 更新登录token
				String appKey = inv.getRequest().getHeader("appKey");
				String source = DeviceType.ANDROID.getType();
				if (StringUtils.equals(appKey, GolfConstant.APPKEY_IOS_VALUE)) {
					source = DeviceType.IOS.getType();
				}

				userHome.updateTokenAndSource(userId, token, source);
				BaseResponseItem<User> result = new BaseResponseItem<User>(
						ResponseStatus.OK, "注册成功！");
				Type type = new TypeToken<BaseResponseItem<User>>() {
				}.getType();
				result.setData(user);
				return "@"
						+ BeanJsonUtils.convertToJsonWithGsonBuilder(result,
								type);

			}
		}

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "注册成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}

}
