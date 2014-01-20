package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.relation.home.UserRelationHome;
import com.jinfang.golf.relation.model.UserFollowRelation;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.UserHolder;

@LoginRequired
@Path("relation")
public class UserRelationController {

	@Autowired
	private Invocation inv;

	@Autowired
	private UserRelationHome userRelationHome;
	
	@Autowired
	private UserHome userHome;

	@Autowired
	private UserHolder userHolder;

	/**
	 * 关注
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@Post("follow")
	public String follow(@Param("userId") Integer userId) throws Exception {

		if (userId == null || userId == 0) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "用户id为空！"));
		}

		UserFollowRelation relation = new UserFollowRelation();
		relation.setHost(userHolder.getUserInfo().getId());
		relation.setGuest(userId);

		userRelationHome.addRelation(relation);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "关注成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}

	/**
	 * 取消关注
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@Post("unFollow")
	public String unFollow(@Param("userId") Integer userId) throws Exception {

		if (userId == null || userId == 0) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "用户id为空！"));
		}

		userRelationHome.removeRelation(userHolder.getUserInfo().getId(),
				userId);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "取消关注成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}
	
	@Post("followList")
	public String followList(@Param("offset") Integer offset, @Param("limit") Integer limit) throws Exception {

		offset = offset*limit;
		List<Integer> userIdList = userRelationHome.getFollowList(userHolder.getUserInfo().getId(),offset,limit);

		List<User> userList = userHome.getUserListByIds(userIdList);
		
		if (userList != null) {
			for (User temp : userList) {
				if(StringUtils.isNotBlank(temp.getHeadUrl())){
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN + temp.getHeadUrl());
				}else{
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
				}
				
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
	
	
	@Post("fansList")
	public String fansList(@Param("offset") Integer offset, @Param("limit") Integer limit) throws Exception {

		offset = offset*limit;
		List<Integer> userIdList = userRelationHome.getFansList(userHolder.getUserInfo().getId(),offset,limit);

		List<User> userList = userHome.getUserListByIds(userIdList);
		if (userList != null) {
			for (User temp : userList) {
				if(StringUtils.isNotBlank(temp.getHeadUrl())){
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN + temp.getHeadUrl());
				}else{
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
				}
				
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
	
	@Post("friendList")
	public String friendList(@Param("offset") Integer offset, @Param("limit") Integer limit) throws Exception {

		offset = offset*limit;
		List<Integer> userIdList = userRelationHome.getFriendList(userHolder.getUserInfo().getId(),offset,limit);

		List<User> userList = userHome.getUserListByIds(userIdList);
		
		if (userList != null) {
			for (User temp : userList) {
				if(StringUtils.isNotBlank(temp.getHeadUrl())){
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN + temp.getHeadUrl());
				}else{
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
				}
				
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
	
	
	@Post("chatUserList")
	public String chatUserList(@Param("offset") Integer offset, @Param("limit") Integer limit) throws Exception {

		offset = offset*limit;
		List<User> userList = userHome.getAllUserListByCity(offset, limit, "北京");

//		List<User> userList = userHome.getUserListByIds(userIdList);
		User host = userHolder.getUserInfo();
		List<User> resultList = new ArrayList<User>();
		if (userList != null) {
			for (User temp : userList) {
				if(StringUtils.isNotBlank(temp.getHeadUrl())){
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN + temp.getHeadUrl());
				}else{
					temp.setHeadUrl(GolfConstant.IMAGE_DOMAIN +GolfConstant.DEFAULT_HEAD_URL);
				}
				
				if(!temp.getId().equals(host.getId())){
					resultList.add(temp);
				}
				
			}
		}
		BaseResponseItem<List<User>> result = new BaseResponseItem<List<User>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<User>>>() {
		}.getType();
		result.setData(resultList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	 
	 

}
