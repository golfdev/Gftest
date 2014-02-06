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
import com.jinfang.golf.team.home.UserTeamHome;
import com.jinfang.golf.team.model.GolfTeam;
import com.jinfang.golf.team.model.UserTeamApply;
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
    @Post("create")
    public String createTeam(@Param("name") String name, @Param("city") String city,
            @Param("logo") String logo, @Param("contacts") String contacts,
            @Param("phone") String phone, @Param("purpose") String purpose,
            @Param("description") String description, @Param("createdDate") String createdDate)
            throws Exception {

        if (StringUtils.isEmpty(createdDate)) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "球队创建日期为空！"));
        }

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
        team.setLogo(GolfConstant.IMAGE_DOMAIN + team.getLogo());
        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "创建球队成功！");
        Type type = new TypeToken<BaseResponseItem<String>>() {
        }.getType();
        return "@" + BeanJsonUtils.convertToJson(result, type);

    }

    /**
     * 返回球队基本信息
     * 
     * @param id
     * @return
     * @throws Exception
     */
    @LoginRequired
    @Post("show")
    public String show(@Param("id") Integer id) throws Exception {

        if (id == null || id == 0) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "球队id为空！"));
        }
        GolfTeam team = userTeamHome.getGolfTeamById(id);

        team.setLogo(GolfConstant.IMAGE_DOMAIN + team.getLogo());
        BaseResponseItem<GolfTeam> result = new BaseResponseItem<GolfTeam>(ResponseStatus.OK,
                "返回球队信息！");
        Type type = new TypeToken<BaseResponseItem<GolfTeam>>() {
        }.getType();
        result.setData(team);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }

    /**
     * 球队入队申请
     * 
     * @param teamId
     * @return
     * @throws Exception
     */
    @LoginRequired
    @Post("apply")
    public String apply(@Param("teamId") Integer teamId) throws Exception {

        if (teamId == null || teamId == 0) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "球队id为空！"));
        }

        User host = userHolder.getUserInfo();
        UserTeamApply apply = new UserTeamApply();
        apply.setUserId(host.getId());
        apply.setTeamId(teamId);
        apply.setStatus(0);
        userTeamHome.addApply(apply);

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "发送成功！");
        Type type = new TypeToken<BaseResponseItem<String>>() {
        }.getType();
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }

    /**
     * 球队申请列表
     * 
     * @param teamId
     * @param offset
     * @param limit
     * @return
     * @throws Exception
     */
    @Post("applyList")
    @LoginRequired
    public String applyList(@Param("teamId") Integer teamId, @Param("offset") Integer offset,
            @Param("limit") Integer limit) throws Exception {

        if (teamId == null || teamId == 0) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "球队id为空！"));
        }

        List<UserTeamApply> applyList = userTeamHome.getTeamApplyList(teamId, offset, limit);

        BaseResponseItem<List<UserTeamApply>> result = new BaseResponseItem<List<UserTeamApply>>(
                ResponseStatus.OK, "发送成功！");
        Type type = new TypeToken<BaseResponseItem<List<UserTeamApply>>>() {
        }.getType();
        result.setData(applyList);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }

    /**
     * 处理球队申请
     * 
     * @param teamId
     * @param userId
     * @param status
     * @return
     * @throws Exception
     */
    @LoginRequired
    @Post("apply/handle")
    public String applyHandle(@Param("teamId") Integer teamId, @Param("userId") Integer userId,
            @Param("status") Integer status) throws Exception {

        if (teamId == null || userId == null || status == null) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "参数非法！"));
        }

        userTeamHome.updateApplyStatus(teamId, userId, status);

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "处理成功！");
        Type type = new TypeToken<BaseResponseItem<String>>() {
        }.getType();
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }

    /**
     * 处理球队申请
     * 
     * @param teamId
     * @param userId
     * @param status
     * @return
     * @throws Exception
     */
    @LoginRequired
    @Post("apply/agreeAll")
    public String agreeAll(@Param("teamId") Integer teamId, @Param("userIds") List<Integer> userIds)
            throws Exception {

        if (teamId == null || userIds == null) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "参数非法！"));
        }
        for (Integer userId : userIds) {
            userTeamHome.updateApplyStatus(teamId, userId, 1);
        }

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "处理成功！");
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
    @Post("uploadPhoto")
    public String uploadPhoto(@Param("logo") MultipartFile logo) throws Exception {

        String path = processTeamLogo(logo);

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK,
                "返回logo信息！");
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
    @Post("edit")
    public String edit(@Param("id") Integer id, @Param("name") String name,
            @Param("city") String city, @Param("logo") String logo,
            @Param("contacts") String contacts, @Param("phone") String phone,
            @Param("purpose") String purpose, @Param("description") String description)
            throws Exception {

        GolfTeam team = userTeamHome.getGolfTeamById(id);
        
        if(team==null){
        	 return "@"
                     + BeanJsonUtils.convertToJsonWithException(new GolfException(
                             ResponseStatus.SERVER_ERROR, "球队不存在！"));
        }
        
        team.setCity(city);
        team.setName(name);
        team.setLogo(logo);
        team.setContacts(contacts);
        team.setPhone(phone);
        team.setPurpose(purpose);
        team.setDescription(description);
        userTeamHome.updateGolfTeam(team);

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "更新成功！");
        Type type = new TypeToken<BaseResponseItem<String>>() {
        }.getType();
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }

    /**
     * 编辑球队公告
     * 
     * @param id
     * @param notice
     * @return
     * @throws Exception
     */
    @LoginRequired
    @Post("editNotice")
    public String editNotice(@Param("id") Integer id, @Param("notice") String notice)
            throws Exception {

        GolfTeam team = userTeamHome.getGolfTeamById(id);
        team.setNotice(notice);

        userTeamHome.updateGolfTeamNotice(team);

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "更新成功！");
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
    public String list(@Param("city") String city, @Param("offset") Integer offset,
            @Param("limit") Integer limit) throws Exception {

        offset = offset * limit;

        User host = userHolder.getUserInfo();
        Integer userId = 0;
        if (host != null) {
            userId = host.getId();
        }

        List<GolfTeam> teamList = userTeamHome.getGolfTeamList(userId, city, offset, limit);

        BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
                ResponseStatus.OK, "成功！");
        Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
        }.getType();
        result.setData(teamList);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

    }

    /**
     * 加入的球队列表
     * 
     * @param offset
     * @param limit
     * @return
     * @throws Exception
     */
    @Post("joinList")
    @LoginRequired
    public String joinList(@Param("offset") Integer offset, @Param("limit") Integer limit)
            throws Exception {

        offset = offset * limit;

        User host = userHolder.getUserInfo();
        Integer userId = 0;
        if (host != null) {
            userId = host.getId();
        }

        List<GolfTeam> teamList = userTeamHome.getJoinedGolfTeamList(userId, offset, limit);
        if (teamList != null) {
            for (GolfTeam team : teamList) {
                team.setLogo(GolfConstant.IMAGE_DOMAIN + team.getLogo());
            }
        }

        BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
                ResponseStatus.OK, "成功！");
        Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
        }.getType();
        result.setData(teamList);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

    }
    
    
    /**
     * 用户的球队列表
     * 
     * @param offset
     * @param limit
     * @return
     * @throws Exception
     */
    @Post("userTeamList")
    @LoginRequired
    public String userTeamList(@Param("userId") Integer userId,@Param("offset") Integer offset, @Param("limit") Integer limit)
            throws Exception {
    	
    	if(offset==null){
    		offset=0;
    	}
    	
    	if(limit==null){
    		limit=0;
    	}

        offset = offset * limit;
        
        if(userId==null){
        	 User host = userHolder.getUserInfo();
             if (host != null) {
                 userId = host.getId();
             }
        }


        List<GolfTeam> teamList = userTeamHome.getTeamListByUid(userId,offset,limit);
        if (teamList != null) {
            for (GolfTeam team : teamList) {
                team.setLogo(GolfConstant.IMAGE_DOMAIN + team.getLogo());
            }
        }

        BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
                ResponseStatus.OK, "成功！");
        Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
        }.getType();
        result.setData(teamList);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

    }

    /**
     * 创建球队列表
     * 
     * @param offset
     * @param limit
     * @return
     * @throws Exception
     */
    @Post("createdList")
    @LoginRequired
    public String createdList(@Param("offset") Integer offset, @Param("limit") Integer limit)
            throws Exception {

        offset = offset * limit;

        User host = userHolder.getUserInfo();
        Integer userId = 0;
        if (host != null) {
            userId = host.getId();
        }

        List<GolfTeam> teamList = userTeamHome.getMyCreatedGolfTeamList(userId, offset, limit);

        if (teamList != null) {
            for (GolfTeam team : teamList) {
                team.setLogo(GolfConstant.IMAGE_DOMAIN + team.getLogo());
            }
        }

        BaseResponseItem<List<GolfTeam>> result = new BaseResponseItem<List<GolfTeam>>(
                ResponseStatus.OK, "成功！");
        Type listType = new TypeToken<BaseResponseItem<List<GolfTeam>>>() {
        }.getType();
        result.setData(teamList);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

    }

    /**
     * 成员列表
     * 
     * @param offset
     * @param limit
     * @return
     * @throws Exception
     */
    @Post("memberList")
    @LoginRequired
    public String memberList(@Param("teamId") Integer teamId, @Param("offset") Integer offset,
            @Param("limit") Integer limit) throws Exception {

        offset = offset * limit;

        List<User> userList = userTeamHome.getMemberListByTeamId(teamId, offset, limit);

        if (userList != null) {
            for (User user : userList) {
                if (StringUtils.isNotBlank(user.getHeadUrl())) {
                    user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + user.getHeadUrl());
                } else {
                    user.setHeadUrl(GolfConstant.IMAGE_DOMAIN + GolfConstant.DEFAULT_HEAD_URL);
                }
            }
        }

        BaseResponseItem<List<User>> result = new BaseResponseItem<List<User>>(ResponseStatus.OK,
                "成功！");
        Type listType = new TypeToken<BaseResponseItem<List<User>>>() {
        }.getType();
        result.setData(userList);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

    }

    /**
     * 邀请某人加入球队
     * 
     * @param userId
     * @param teamId
     * @return
     * @throws Exception
     */
    @Post("invite")
    @LoginRequired
    public String invite(@Param("userId") Integer userId, @Param("teamId") Integer teamId)
            throws Exception {

        GolfTeam team = userTeamHome.getGolfTeamById(teamId);

        if (team == null) {
            BaseResponseItem<String> result = new BaseResponseItem<String>(
                    ResponseStatus.SERVER_ERROR, "球队不存在！");
            Type type = new TypeToken<BaseResponseItem<String>>() {
            }.getType();
            return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
        }
        User host = userHolder.getUserInfo();
        if (team.getCreatorId().equals(host.getId())) {
            userTeamHome.addMember(teamId, userId);
        } else {
            BaseResponseItem<String> result = new BaseResponseItem<String>(
                    ResponseStatus.SERVER_ERROR, "无权限！");
            Type type = new TypeToken<BaseResponseItem<String>>() {
            }.getType();
            return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

        }

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "邀请成功！");
        Type type = new TypeToken<BaseResponseItem<String>>() {
        }.getType();
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }

    /**
     * 移除成员
     * 
     * @param userId
     * @return
     * @throws Exception
     */
    @Post("removeMember")
    @LoginRequired
    public String removeUser(@Param("userId") Integer userId, @Param("teamId") Integer teamId)
            throws Exception {

        GolfTeam team = userTeamHome.getGolfTeamById(teamId);
        User host = userHolder.getUserInfo();
        if (team.getCreatorId().equals(host.getId())) {
            userTeamHome.removeFromTeam(userId, teamId);
        } else {
            BaseResponseItem<String> result = new BaseResponseItem<String>(
                    ResponseStatus.SERVER_ERROR, "无权限！");
            Type type = new TypeToken<BaseResponseItem<String>>() {
            }.getType();
            return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

        }

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "移除成功！");
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
        String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        String saveFilePath = GolfConstant.IMAGE_PATH + "/logo/" + MathUtil.getImgFileDirectory()
                + "/";
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
