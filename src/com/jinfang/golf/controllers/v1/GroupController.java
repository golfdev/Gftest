package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.group.home.GroupHome;
import com.jinfang.golf.group.home.GroupUserHome;
import com.jinfang.golf.group.home.UserGroupHome;
import com.jinfang.golf.group.manager.GroupManager;
import com.jinfang.golf.group.model.Group;
import com.jinfang.golf.group.model.UserGroup;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.model.GroupUserModel;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.UserHolder;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

@LoginRequired
@Path("group")
public class GroupController {
	@Autowired
	private UserHolder userHolder;
	@Autowired
	private GroupManager groupManager;
	@Autowired
	private UserGroupHome userGroupHome;
	@Autowired
	private GroupHome groupHome;
	@Autowired
	private UserHome userHome;
	@Autowired
	private GroupUserHome groupUserHome;
	
	@Post("createGroup")
	public String createGroup(@Param("name") String name, @Param("userIds") String ids) throws Exception {
		int userId = userHolder.getUserInfo().getId();
		Set<Integer> set = new HashSet<Integer>();
		if (StringUtils.isNotBlank(ids)) {
			for (String id : ids.split(",")) {
				set.add(Integer.parseInt(id));
			}
		}
		Group group = groupManager.createGroup(userId, name, set, Group.TYPE_TEAM);
		BaseResponseItem<Group> result = new BaseResponseItem<Group>(ResponseStatus.OK, "微群创建成功!");
		Type type = new TypeToken<BaseResponseItem<Group>>() {}.getType();
		result.setData(group);
	    
	    return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
	@Post("addUser")
	public String addUser(@Param("groupId") int groupId, @Param("userIds") String ids) throws Exception {
		if (groupId <= 0 || StringUtils.isBlank(ids)) {
			return "@" + BeanJsonUtils.convertToJsonWithException(new GolfException(ResponseStatus.SERVER_ERROR,"缺少groupId和userIds"));
		}
		Set<Integer> set = new HashSet<Integer>();
		for (String id : ids.split(",")) {
			set.add(Integer.parseInt(id));
		}
		groupManager.addUser(groupId, set);
		BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "添加用户成功!");
		Type type = new TypeToken<BaseResponseItem<String>>() {}.getType();
	    
	    return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
	@Post("delUser")
	public String delUser(@Param("groupId") int groupId, @Param("userId") String id) throws Exception {
		int userId = Integer.parseInt(id);
		if (groupId <= 0 || userId <= 0) {
			return "@" + BeanJsonUtils.convertToJsonWithException(new GolfException(ResponseStatus.SERVER_ERROR,"缺少groupId和userId"));
		}
		groupManager.delUser(groupId, userId);
		BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "删除用户成功!");
		Type type = new TypeToken<BaseResponseItem<String>>() {}.getType();
	    
	    return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
	@Post("getList")
	public String getList(@Param("offset") int offset, @Param("limit") int limit) throws Exception {
		offset = offset < 0 ? 0 : offset;
		limit = limit <= 0 ? 100 : limit;
		int userId = userHolder.getUserInfo().getId();
		List<Group> models = new ArrayList<Group>();
		List<UserGroup> list = userGroupHome.gets(userId, offset, limit);
		if (list != null && list.size() > 0) {
			List<Integer> groupIds = new ArrayList<Integer>(list.size());
			for (UserGroup ug : list) {
				groupIds.add(ug.getGroupId());
			}
			Map<Integer, Group> map = groupHome.getByIds(groupIds);
			if (map != null && map.size() > 0) {
				for (Entry<Integer, Group> e : map.entrySet()) {
					Group group = e.getValue();
					group.setLogoUrl(Group.DEFAULT_LOGO);
					int lastUserId = group.getLastUserId();
					if (lastUserId > 0) {
						User user = userHome.getById(lastUserId);
						group.setLastUserName(user.getUserName());
						if (group.getType() == Group.TYPE_NORMAL) {
							group.setLogoUrl(user.getHeadUrl());
						}
					}
					models.add(group);
				}
				sortGroupList(models);
			}
			
		}
		BaseResponseItem<List<Group>> result = new BaseResponseItem<List<Group>>(ResponseStatus.OK, "获取微群成功!");
		Type type = new TypeToken<BaseResponseItem<List<Group>>>() {}.getType();
		result.setData(models);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
	private void sortGroupList(List<Group> list) {
		Comparator<Group> comparator = new Comparator<Group>() {
			@Override
			public int compare(Group group1, Group group2) {
				if (group1.getLastTime() != null && group2.getLastText() != null) {
					return (int)(group1.getLastTime().getTime() - group2.getLastTime().getTime());
				} else {
					return (int)(group1.getTime().getTime() - group2.getTime().getTime());
				}
			}
		};
		Collections.sort(list, comparator);
	}
	// userIds预留着，对于那些已经退群当时客户端仍然保存了他们的聊天记录的用户，需要显示
	@Post("getUsers")
	public String getUsers(@Param("groupId") int groupId, @Param("offset") int offset, @Param("limit") int limit) throws Exception {
		if (groupId <= 0) {
			return "@" + BeanJsonUtils.convertToJsonWithException(new GolfException(ResponseStatus.SERVER_ERROR,"缺少groupId"));
		}
		offset = offset < 0 ? 0 : offset;
		limit = limit <= 0 ? 100 : limit;
		List<GroupUserModel> models = new ArrayList<GroupUserModel>();
		List<UserGroup> list = groupUserHome.gets(groupId, offset, limit);
		if (list != null && list.size() > 0) {
			List<Integer> userIds = new ArrayList<Integer>();
			for (UserGroup ug : list) {
				userIds.add(ug.getUserId());
			}
			Map<Integer, User> map = userHome.getUserMapByIds(userIds);
			if (map != null && map.size() > 0) {
				for (UserGroup ug : list) {
					int uid = ug.getUserId();
					User user = map.get(uid);
					if (user != null) {
						GroupUserModel gum = new GroupUserModel();
						gum.setUserId(uid);
						gum.setUserName(user.getUserName());
						gum.setHeadurl(user.getHeadUrl());
						models.add(gum);
					}
				}
			}
		}
		BaseResponseItem<List<GroupUserModel>> result = new BaseResponseItem<List<GroupUserModel>>(ResponseStatus.OK, "获取微群用户成功!");
		Type type = new TypeToken<BaseResponseItem<List<GroupUserModel>>>() {}.getType();
		result.setData(models);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
	
	// userIds预留着，对于那些已经退群当时客户端仍然保存了他们的聊天记录的用户，需要显示
	@Post("getSpecifyUsers")
	public String getSpecifyUsers(@Param("groupId") int groupId, @Param("userIds") String ids) throws Exception {
		if (groupId <= 0 || StringUtils.isBlank(ids)) {
			return "@" + BeanJsonUtils.convertToJsonWithException(new GolfException(ResponseStatus.SERVER_ERROR,"缺少groupId和userIds"));
		}
		List<GroupUserModel> list = new ArrayList<GroupUserModel>();
		for (String id : ids.split(",")) {
			int userId = Integer.parseInt(id);
			User user = userHome.getById(userId);
			if (user != null) {
				GroupUserModel gum = new GroupUserModel();
				gum.setUserId(userId);
				gum.setUserName(user.getUserName());
				gum.setHeadurl(gum.getHeadurl());
				UserGroup ug = userGroupHome.get(userId, groupId);
				if (ug != null) {
					gum.setStatus(0);
				} else {
					gum.setStatus(1);
				}
				list.add(gum);
			}
		}
		
		BaseResponseItem<List<GroupUserModel>> result = new BaseResponseItem<List<GroupUserModel>>(ResponseStatus.OK, "获取微群指定成功!");
		Type type = new TypeToken<BaseResponseItem<List<GroupUserModel>>>() {}.getType();
		result.setData(list);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
	@Post("delete")
	public String delete(@Param("groupId") int groupId) throws Exception {
		if (groupId <= 0) {
			return "@" + BeanJsonUtils.convertToJsonWithException(new GolfException(ResponseStatus.SERVER_ERROR,"缺少groupId"));
		}
		int userId = userHolder.getUserInfo().getId();
		groupManager.updateUserGroupStatus(userId, groupId, UserGroup.STATUS_HIDE);
		BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "删除微群成功!");
		Type type = new TypeToken<BaseResponseItem<String>>() {}.getType();
	    
	    return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
}
