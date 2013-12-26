package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
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
import com.jinfang.golf.appointment.home.GolfAppointmentHome;
import com.jinfang.golf.appointment.model.GolfAppointment;
import com.jinfang.golf.appointment.model.GolfAppointmentMember;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.UserHolder;

@Path("appoint")
@LoginRequired
public class GolfAppointController {

	@Autowired
	private Invocation inv;

	@Autowired
	private UserHome userHome;

	@Autowired
	private GolfAppointmentHome golfAppointmentHome;

	@Autowired
	private UserHolder userHolder;

	/**
	 * 返回约球的基本信息
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
									ResponseStatus.SERVER_ERROR, "球场id为空！"));
		}
		GolfAppointment appoint = golfAppointmentHome.getGolfAppointment(id);

		BaseResponseItem<GolfAppointment> result = new BaseResponseItem<GolfAppointment>(
				ResponseStatus.OK, "返回信息！");
		Type type = new TypeToken<BaseResponseItem<GolfAppointment>>() {
		}.getType();
		result.setData(appoint);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}

	/**
	 * 约球列表
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

		if (offset == null) {
			offset = 0;
		}

		if (limit == null) {
			limit = 10;
		}

		if (StringUtils.isEmpty(city)) {
			city = "北京";
		}

		offset = offset * limit;

		User host = userHolder.getUserInfo();

		List<GolfAppointment> appointList = golfAppointmentHome
				.getAppointmentList(host.getId(), city, offset, limit);

		BaseResponseItem<List<GolfAppointment>> result = new BaseResponseItem<List<GolfAppointment>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfAppointment>>>() {
		}.getType();
		result.setData(appointList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}

	/**
	 * 约球申请入住
	 * 
	 * @param appointId
	 * @return
	 * @throws Exception
	 */
	@Post("apply")
	public String apply(@Param("appointId") Integer appointId) throws Exception {

		if (appointId == null || appointId == 0) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "请求非法！"));
		}

		User user = userHolder.getUserInfo();

		GolfAppointmentMember member = new GolfAppointmentMember();
		member.setAppointId(appointId);
		member.setUserId(user.getId());
		golfAppointmentHome.addParter(member);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "申请成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}

	/**
	 * 约球申请入住
	 * 
	 * @param appointId
	 * @return
	 * @throws Exception
	 */
	@Post("create")
	public String create(@Param("clubId") Integer clubId,
			@Param("appointTime") String appointTime,
			@Param("avgPrice") Integer avgPrice,
			@Param("gender") Integer gender,
			@Param("minHandicap") Integer minHandicap,
			@Param("maxHandicap") Integer maxHandicap,
			@Param("privateSetting") Integer privateSetting,
			@Param("description") String description, @Param("city") String city)
			throws Exception {

		if (clubId == null || clubId == 0 || StringUtils.isBlank(appointTime)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}

		if (avgPrice == null) {
			avgPrice = 0;
		}

		if (gender == null) {
			gender = 0;
		}
		if (minHandicap == null) {
			minHandicap = 0;
		}

		if (privateSetting == null) {
			privateSetting = 0;
		}

		User user = userHolder.getUserInfo();

		GolfAppointment appoint = new GolfAppointment();
		appoint.setClubId(clubId);
		appoint.setCreatorId(user.getId());
		appoint.setGender(gender);
		appoint.setAvgPrice(avgPrice);
		appoint.setAppointTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
				.parse(appointTime));
		appoint.setCity(city);
		appoint.setDescription(description);
		appoint.setMinHandicap(minHandicap);
		appoint.setMaxHandicap(maxHandicap);
		appoint.setPrivateSetting(privateSetting);

		golfAppointmentHome.createApponint(appoint);

		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "发起成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}

}
