package com.jinfang.golf.controllers.v1;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.interceptor.CentifyRequired;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.team.home.UserTeamHome;
import com.jinfang.golf.team.model.GolfTeam;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.MathUtil;
import com.jinfang.golf.utils.UploadUtil;
import com.jinfang.golf.utils.UserHolder;


@Path("team")
public class GolfTeamController {

	@Autowired
	private Invocation inv;

	@Autowired
	private UserHome userHome;

	@Autowired
	private UserTeamHome userTeamHome;
	

	@Autowired
	private UserHolder userHolder;

	/**
	 * 创建球队
	 * 
	 * @param name
	 * @param city
	 * @param logo
	 * @param contacts
	 * @param phone
	 * @param clubName
	 * @return
	 * @throws Exception
	 */
	@LoginRequired
	@CentifyRequired
	@Post("create")
	public String createTeam(@Param("name") String name,
			@Param("city") String city, @Param("logo") String logo,
			@Param("contacts") String contacts, @Param("phone") String phone,
			@Param("purpose") String purpose, @Param("description") String description,
			@Param("createdDate") String createdDate
			) throws Exception {

		GolfTeam team = new GolfTeam();
		team.setName(name);
		team.setCity(city);
		team.setContacts(contacts);
		team.setPhone(phone);
		team.setLogo(logo);
		team.setCreatorId(userHolder.getUserInfo().getId());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		team.setCreatedDate(df.parse(createdDate));
		team.setPurpose(purpose);
		team.setDescription(description);
		userTeamHome.createGolfTeam(team);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "创建球队成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}
	
	
	/**
     * 返回球队基本信息
     * @param id
     * @return
     * @throws Exception
     */
	@LoginRequired
    @Post("show")
    public String show(@Param("id") Integer id) throws Exception {

        if (id==null||id==0) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "球队id为空！"));
        }
        GolfTeam team = userTeamHome.getGolfTeamById(id);

       
		BaseResponseItem<GolfTeam> result = new BaseResponseItem<GolfTeam>(ResponseStatus.OK,"返回球队信息！");
	    Type type = new TypeToken<BaseResponseItem<GolfTeam>>() {}.getType();
	    result.setData(team);
	    return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }
    
    
    /**
     * 返回球队基本信息
     * @param id
     * @return
     * @throws Exception
     */
    @Post("apply")
    public String apply(@Param("teamId") Integer teamId) throws Exception {

        if (teamId==null||teamId==0) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "球队id为空！"));
        }
        
//        User host =  userHolder.getUserInfo();
//        UserTeamApply apply = new UserTeamApply();
//        apply.setUserId(host.getId());
//        apply.setTeamId(teamId);
//        apply.setStatus(0);
//        userTeamHome.addApply(apply);
       
        BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "发送成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);


    }

	/**
	 * 上传球队logo
	 * 
	 * @param userHead
	 * @return
	 * @throws Exception
	 */
	@LoginRequired
	@CentifyRequired
	@Post("uploadPhoto")
	public String uploadPhoto(@Param("logo") MultipartFile userHead)
			throws Exception {

		String path = processTeamLogo(userHead);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "返回用户信息！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		result.setData(path);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}

	/**
	 * 编辑球队信息
	 * 
	 * @param id
	 * @param name
	 * @param city
	 * @param logo
	 * @param contacts
	 * @param phone
	 * @param clubName
	 * @return
	 * @throws Exception
	 */
	@LoginRequired
	@CentifyRequired
	@Post("edit")
	public String edit(@Param("id") Integer id, @Param("name") String name,
			@Param("city") String city, @Param("logo") String logo,
			@Param("contacts") String contacts, @Param("phone") String phone,
			@Param("purpose") String purpose, @Param("description") String description) throws Exception {

		GolfTeam team = userTeamHome.getGolfTeamById(id);
		team.setCity(city);
		team.setName(name);
		team.setLogo(logo);
		team.setContacts(contacts);
		team.setPhone(phone);
		team.setPurpose(purpose);
		team.setDescription(description);
		userTeamHome.updateGolfTeam(team);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "更新成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}

	/**
	 * 球队列表
	 * 
	 * @param city
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("list")
	public String list(@Param("city") String city,
			@Param("offset") Integer offset, @Param("limit") Integer limit)
			throws Exception {

		offset = offset * limit;
		
		User host = userHolder.getUserInfo();
		Integer userId = 0;
		if(host!=null){
			userId = host.getId();
		}

		List<GolfTeam> teamList = userTeamHome.getGolfTeamList(userId,city, offset,
				limit);

		BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
		}.getType();
		result.setData(teamList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	
	/**
	 * 加入的球队列表
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("joinList")
	public String joinList(
			@Param("offset") Integer offset, @Param("limit") Integer limit)
			throws Exception {

		offset = offset * limit;
		
		User host = userHolder.getUserInfo();
		Integer userId = 0;
		if(host!=null){
			userId = host.getId();
		}

		List<GolfTeam> teamList = userTeamHome.getJoinedGolfTeamList(userId, offset, limit);

		BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
		}.getType();
		result.setData(teamList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	/**
	 * 创建球队列表
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("createdList")
	public String createdList(
			@Param("offset") Integer offset, @Param("limit") Integer limit)
			throws Exception {

		offset = offset * limit;
		
		User host = userHolder.getUserInfo();
		Integer userId = 0;
		if(host!=null){
			userId = host.getId();
		}

		List<GolfTeam> teamList = userTeamHome.getMyCreatedGolfTeamList(userId, offset, limit);

		BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
		}.getType();
		result.setData(teamList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	/**
	 * 成员列表
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("memberList")
	public String memberList(@Param("teamId") Integer teamId,
			@Param("offset") Integer offset, @Param("limit") Integer limit)
			throws Exception {

		offset = offset * limit;
		
		List<User> userList = userTeamHome.getMemberListByTeamId(teamId, offset, limit);

		BaseResponseItem<List<User>> result = new BaseResponseItem<List<User>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
		}.getType();
		result.setData(userList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	
	/**
	 * 成员列表
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("invite")
	public String invite(@Param("userId") Integer userId)
			throws Exception {

		
		  BaseResponseItem<String> result = new BaseResponseItem<String>(
					ResponseStatus.OK, "邀请成功！");
			Type type = new TypeToken<BaseResponseItem<String>>() {
			}.getType();
			return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}




	/**
	 * 处理logo
	 * 
	 * @param imgFile
	 * @return
	 */
	private String processTeamLogo(MultipartFile imgFile) {
		if (imgFile == null) {
			return null;
		}
		String fileName = imgFile.getOriginalFilename();
		String name = String.valueOf(System.currentTimeMillis());
		String suffix = fileName.substring(fileName.lastIndexOf("."),
				fileName.length());
		String saveFilePath = GolfConstant.HEAD_PATH + "/logo/"
				+ MathUtil.getImgFileDirectory() + "/";
		String saveDBPath = "/logo/" + MathUtil.getImgFileDirectory() + "/";
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
