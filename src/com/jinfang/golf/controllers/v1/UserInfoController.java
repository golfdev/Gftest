package com.jinfang.golf.controllers.v1;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.relation.home.UserRelationHome;
import com.jinfang.golf.sms.home.SmsHome;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.home.VerifyCodeHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.user.model.UserCentify;
import com.jinfang.golf.utils.MathUtil;
import com.jinfang.golf.utils.UploadUtil;
import com.jinfang.golf.utils.UserHolder;

@LoginRequired
@Path("user")
public class UserInfoController {

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

	@Autowired
	private UserHolder userHolder;

	@Autowired
	private UserRelationHome userRelationHome;

	/**
	 * 返回用户基本信息
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Post("show")
	public String show(@Param("id") Integer id) throws Exception {

		if (id == null || id == 0) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "用户id为空！"));
		}
		User host = userHolder.getUserInfo();
		User user = userHome.getById(id);
		user.setFollowCount(userRelationHome.getFollowCount(id));
		user.setFansCount(userRelationHome.getFansCount(id));
		user.setFriendCount(userRelationHome.getFriendCount(id));
		
		if(StringUtils.isNotBlank(user.getHeadUrl())){
			user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + user.getHeadUrl());
		}else{
			user.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
		}
		if (!host.getId().equals(user.getId())) {
			user.setToken(null);
			user.setPassWord(null);
			user.setPhone(null);
		} else {
			UserCentify centify = userHome.getUserCentify(id);
			if(centify!=null){
				user.setRealName(centify.getRealName());
				user.setSfzId(centify.getSfzId());
			}
		}
		
	    user.setIsFollowed(userRelationHome.isFollow(host.getId(), id));

		BaseResponseItem<User> result = new BaseResponseItem<User>(
				ResponseStatus.OK, "返回用户信息！");
		Type type = new TypeToken<BaseResponseItem<User>>() {
		}.getType();
		result.setData(user);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}

	/**
	 * 上传头像
	 * 
	 * @param userHead
	 * @return
	 * @throws Exception
	 */
	@Post("uploadPhoto")
	public String uploadPhoto(@Param("userHead") MultipartFile userHead)
			throws Exception {

		String path = processUserHead(userHead);
		User user = userHolder.getUserInfo();
		user.setHeadUrl(path);
		userHome.updateHeadUrl(user);

		user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + user.getHeadUrl());

		BaseResponseItem<User> result = new BaseResponseItem<User>(
				ResponseStatus.OK, "返回用户信息！");
		Type type = new TypeToken<BaseResponseItem<User>>() {
		}.getType();
		result.setData(user);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}

	/**
	 * 编辑用户信息
	 * 
	 * @param userHead
	 * @return
	 * @throws Exception
	 */
	@Post("edit")
	public String edit(@Param("userName") String userName,
			@Param("gender") Integer gender, @Param("city") String city,
			@Param("description") String description,
			@Param("realName") String realName, @Param("sfzId") String sfzId)
			throws Exception {

		User user = userHolder.getUserInfo();

		if (StringUtils.isNotBlank(userName)) {
			user.setUserName(userName);
		}

		if (gender != null) {
			user.setGender(gender);
		}

		if (StringUtils.isNotBlank(city)) {
			user.setCity(city);
		}

		if (StringUtils.isNotBlank(description)) {
			user.setDescription(description);
		}

		userHome.updateUser(user);
		
		if(StringUtils.isNotBlank(user.getHeadUrl())){
			user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + user.getHeadUrl());
		}else{
			user.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
		}

		if (StringUtils.isNotBlank(realName) && StringUtils.isNotBlank(sfzId)) {

			UserCentify centify = new UserCentify();
			centify.setUserId(user.getId());
			centify.setRealName(realName);
			centify.setSfzId(sfzId);
			userHome.saveCentifyInfo(centify);
		}

		BaseResponseItem<User> result = new BaseResponseItem<User>(
				ResponseStatus.OK, "返回用户信息！");
		Type type = new TypeToken<BaseResponseItem<User>>() {
		}.getType();
		result.setData(user);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}

	/**
	 * 球手列表
	 * 
	 * @param type
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("list")
	public String list(@Param("type") Integer type,@Param("city") String city,
			@Param("offset") Integer offset, @Param("limit") Integer limit)
			throws Exception {

		User user = userHolder.getUserInfo();
		List<User> userList = null;
		
		if(offset==null){
			offset=0;
		}

		offset = offset * limit;
		
		if(type==null){
			type=1;
		}

		if (type == 0) {
			userList = userHome.getAllUserList(offset, limit);
		} else if (type == 1) {
			if(StringUtils.isEmpty(city)){
				city = user.getCity();
			}
			
			if(StringUtils.isEmpty(city)){
				city = "北京";
			}
//			if(user.getStatus()!=null&&user.getStatus()==1){
//				userList = userHome.getAllUserListByCity(offset, limit, city);
//			}else{
//				userList = userHome.getAllUserListByCityAndStatus(offset, limit, city,0);
//			}
			
			if(CollectionUtils.isEmpty(userList)){
				userList = userHome.getAllUserListByCity(offset, limit, city);
			}
		} else if (type == 2) {

		} else if (type == 3) {
			userList = userHome.getAllUserListByStatus(offset, limit, 1);
		} else if (type == 4) {
			userList = userHome.getAllUserListByStatus(offset, limit, 0);
		}

		if (userList != null) {
			List<Integer> uidList = new ArrayList<Integer>();
			for (User temp : userList) {
				if(StringUtils.isNotBlank(temp.getHeadUrl())){
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN + temp.getHeadUrl());
				}else{
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
				}
				uidList.add(temp.getId());
			}
			Map<Integer, Integer> isFollowMap = userRelationHome.isFollowBatch(user.getId(), uidList);
			
			for (User temp : userList) {
				temp.setIsFollowed(isFollowMap.get(temp.getId()));
			}
			
		}

		BaseResponseItem<List<User>> result = new BaseResponseItem<List<User>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<User>>>() {
		}.getType();
		result.setData(userList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}

	@Post("uploadDeviceToken")
	public String uploadDeviceToken(@Param("deviceToken") String deviceToken)
			throws Exception {

		if (StringUtils.isBlank(deviceToken)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR,
									"设备touken不能为空！"));
		}

		User user = userHolder.getUserInfo();

		userHome.uploadDeviceToken(user.getId(), deviceToken);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "上传设备token成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}

	/**
	 * 处理用户头像
	 * 
	 * @param imgFile
	 * @return
	 */
	private String processUserHead(MultipartFile imgFile) {
		if (imgFile == null) {
			return null;
		}
		String fileName = imgFile.getOriginalFilename();
		String name = String.valueOf(System.currentTimeMillis());
		String suffix = fileName.substring(fileName.lastIndexOf("."),
				fileName.length());
		String saveFilePath = GolfConstant.IMAGE_PATH + "/head/"
				+ MathUtil.getImgFileDirectory() + "/";
		String saveDBPath = "/head/" + MathUtil.getImgFileDirectory() + "/";
		try {
			UploadUtil.saveFile(imgFile, saveFilePath, name + suffix);
			// String fileSrcPath = saveFilePath + name + suffix;
			// String tinyPath = saveFilePath + name + "50x50" + suffix;
			// String mainPath = saveFilePath + name + "200x200" + suffix;
			// EasyImage.resize(fileSrcPath, tinyPath, 50, 50);
			// EasyImage.resize(fileSrcPath, mainPath, 200, 200);
			return saveDBPath + name + suffix;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
