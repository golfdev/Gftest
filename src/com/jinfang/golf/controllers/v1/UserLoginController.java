package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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
import com.jinfang.golf.constants.DeviceType;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.relation.home.UserRelationHome;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.user.model.UserCentify;
import com.jinfang.golf.utils.FormatCheckUtil;

@Path("user")
public class UserLoginController {

	@Autowired
	private Invocation inv;

	@Autowired
	private UserHome userHome;

	@Autowired
	private Passport passport;

	@Autowired
	private UserRelationHome userRelationHome;

	/**
	 * 登录，返回授权token
	 * 
	 * @param name
	 * @param pwd
	 * @throws Exception
	 */
	@Post("slogin")
	public String login(@Param("identity") String identity,
			@Param("pwd") String pwd) throws Exception {

		if (StringUtils.isBlank(identity)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "用户名或密码不正确！"));
		}

		User user = null;

		if (FormatCheckUtil.isEmail(identity)) {
			user = userHome.getByEmail(identity);
		} else if (FormatCheckUtil.isMobileNO(identity)) {
			user = userHome.getByPhone(identity);
		} else {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "用户名或密码不正确！"));
		}
		if (user == null) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "用户名或密码不正确！"));
		}

		if (StringUtils.isNotBlank(pwd) && pwd.equals(user.getPassWord())) {
			String token = passport.createAppToken(user.getId(),
					Integer.MAX_VALUE);
			user.setToken(token);

			// 更新登录token
			String appKey = inv.getRequest().getHeader("appKey");
			String source = DeviceType.ANDROID.getType();
			if (StringUtils.equals(appKey, GolfConstant.APPKEY_IOS_VALUE)) {
				source = DeviceType.IOS.getType();
			}

			user.setFollowCount(userRelationHome.getFollowCount(user.getId()));
			user.setFansCount(userRelationHome.getFansCount(user.getId()));
			user.setFriendCount(userRelationHome.getFriendCount(user.getId()));
			if(StringUtils.isNotBlank(user.getHeadUrl())){
				user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + user.getHeadUrl());
			}else{
				user.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
			}
			userHome.updateTokenAndSource(user.getId(), token, source);
			
			UserCentify centify = userHome.getUserCentify(user.getId());
			if(centify!=null){
				user.setRealName(centify.getRealName());
				user.setSfzId(centify.getSfzId());
			}
			BaseResponseItem<User> result = new BaseResponseItem<User>(
					ResponseStatus.OK, "登录成功！");
			Type type = new TypeToken<BaseResponseItem<User>>() {
			}.getType();
			result.setData(user);
			return "@" + BeanJsonUtils.convertToJson(result, type);
		} else {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "用户名或密码不正确！"));
		}

	}
	
	
	@Post("checkToken")
	public String checkToken(@Param("token") String token) throws Exception {

		if (StringUtils.isBlank(token)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "token不能为空！"));
		}

		return "@"
		+ BeanJsonUtils
				.convertToJsonWithException(new GolfException(
						ResponseStatus.OK, "success！"));

	}

	public static void main(String[] args) {
		BaseResponseItem<User> result = new BaseResponseItem<User>(
				ResponseStatus.OK, "登录成功！");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", "test");
		User user = new User();
		user.setId(1);
		user.setUserName("abc");
		user.setToken("test");
		map.put("user", user);
		// result.setData(user);
		result.setData(user);
		Type type = new TypeToken<BaseResponseItem<User>>() {
		}.getType();
		System.out.println(BeanJsonUtils.convertToJson(result, type));

	}
}
