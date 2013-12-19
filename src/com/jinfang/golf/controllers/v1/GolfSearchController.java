package com.jinfang.golf.controllers.v1;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.relation.home.UserRelationHome;
import com.jinfang.golf.search.home.GolfSearchHome;
import com.jinfang.golf.team.home.UserTeamHome;
import com.jinfang.golf.team.model.GolfTeam;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.MathUtil;
import com.jinfang.golf.utils.UploadUtil;
import com.jinfang.golf.utils.UserHolder;

@Path("search")
public class GolfSearchController {

	@Autowired
	private Invocation inv;

	@Autowired
	private UserHome userHome;
	
	@Autowired
	private GolfSearchHome golfSearchHome;

	@Autowired
	private UserTeamHome userTeamHome;
	
	@Autowired
	private UserRelationHome userRelationHome;

	@Autowired
	private UserHolder userHolder;

	/**
	 * 球手搜索
	 * @param keyWord
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("user")
	@LoginRequired
	public String searchUser(@Param("keyWord") String keyWord,
			@Param("offset") Integer offset, @Param("limit") Integer limit) throws Exception {

		User user = userHolder.getUserInfo();

		List<User> userList = golfSearchHome.searchUserList(keyWord, offset, limit);
		
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
				ResponseStatus.OK, "返回结果！");
		Type type = new TypeToken<BaseResponseItem<List<User>>>() {
		}.getType();
		result.setData(userList);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}
	
	/**
	 * 球队搜索
	 * @param keyWord
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@LoginRequired
	@Post("team")
	public String searchTeam(@Param("keyWord") String keyWord,
			@Param("offset") Integer offset, @Param("limit") Integer limit) throws Exception {

		List<GolfTeam> teamList = golfSearchHome.searchTeamList(keyWord, offset, limit);
		
		if (teamList != null) {
			for (GolfTeam team : teamList) {
				team.setLogo(GolfConstant.IMAGE_DOMAIN + team.getLogo());
			}
		}
		
		BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
				ResponseStatus.OK, "返回结果！");
		Type type = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
		}.getType();
		result.setData(teamList);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}
	
	
	@Post("teamFullIndex")
	public String teamIndex() throws Exception {

		golfSearchHome.fullImportTeam();
		
		return "@success";

	}
	
	@Post("userFullIndex")
	public String userIndex() throws Exception {

		golfSearchHome.fullImportUser();
		
		return "@success";

	}



}
