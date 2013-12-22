package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.club.home.GolfClubHome;
import com.jinfang.golf.club.model.GolfClub;
import com.jinfang.golf.club.model.GolfClubOrder;
import com.jinfang.golf.club.model.GolfClubWayItem;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.UserHolder;

@Path("club")
@LoginRequired
public class GolfClubController {

	@Autowired
	private Invocation inv;

	@Autowired
	private UserHome userHome;
	
	@Autowired
	private GolfClubHome golfClubHome;


	@Autowired
	private UserHolder userHolder;


	/**
	 * 返回球场基本信息
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
		GolfClub club = golfClubHome.getGolfClubById(id);
		
		club.setLogo(GolfConstant.IMAGE_DOMAIN + club.getLogo());
		BaseResponseItem<GolfClub> result = new BaseResponseItem<GolfClub>(
				ResponseStatus.OK, "返回球场信息！");
		Type type = new TypeToken<BaseResponseItem<GolfClub>>() {
		}.getType();
		result.setData(club);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

	}



	/**
	 * 球场可用日期
	 * @param clubId
	 * @return
	 * @throws Exception
	 */
	@Post("availableDate")
	public String availableDate(@Param("clubId") Integer clubId)
			throws Exception {
		
		if(clubId==null){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "球场id为空！"));
		}

		List<String> dateList = golfClubHome.getAvailableDateList(clubId);


		BaseResponseItem<List<String>> result = new BaseResponseItem<List<String>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<String>>>() {
		}.getType();
		result.setData(dateList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	
	/**
	 * 可用球道及时间
	 * @param clubId
	 * @param selectedDate
	 * @return
	 * @throws Exception
	 */
	@Post("availableTime")
	public String availableTime(@Param("clubId") Integer clubId,@Param("selectedDate") String selectedDate)
			throws Exception {
		
		if(clubId==null||StringUtils.isBlank(selectedDate)){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数不完整！"));
		}

		List<Date> itemList = golfClubHome.getAvailableTimeList(clubId, selectedDate);

		BaseResponseItem<List<Date>> result = new BaseResponseItem<List<Date>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<Date>>>() {
		}.getType();
		result.setData(itemList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	
	/**
	 * 可用球道及时间
	 * @param clubId
	 * @param selectedDate
	 * @return
	 * @throws Exception
	 */
	@Post("availableItem")
	public String availableItems(@Param("clubId") Integer clubId,@Param("selectedTime") String selectedTime)
			throws Exception {
		
		if(clubId==null||StringUtils.isBlank(selectedTime)){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数不完整！"));
		}

		List<GolfClubWayItem> itemList = golfClubHome.getAvailableItemList(clubId, selectedTime);

		BaseResponseItem<List<GolfClubWayItem>> result = new BaseResponseItem<List<GolfClubWayItem>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfClubWayItem>>>() {
		}.getType();
		result.setData(itemList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	/**
	 * 预订球场
	 * @param clubId
	 * @param teeTime
	 * @param fairWays
	 * @param playerNames
	 * @param playerNum
	 * @param totalPrice
	 * @return
	 * @throws Exception
	 */
	@Post("book")
	public String book(@Param("clubId") Integer clubId,@Param("teeTime") String teeTime,@Param("fairWays") String fairWays
			,@Param("playerNames") String playerNames,@Param("playerNum") Integer playerNum,@Param("totalPrice") Double totalPrice)
			throws Exception {
		
		if(clubId==null||playerNum==null||totalPrice==null||StringUtils.isBlank(teeTime)
				||StringUtils.isBlank(fairWays)||StringUtils.isBlank(playerNames)
				){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数不完整！"));
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date orderTime = df.parse(teeTime);
		
		User user = userHolder.getUserInfo();

		GolfClubOrder order = new GolfClubOrder();
		order.setClubId(clubId);
		order.setTeeTime(orderTime);
		order.setPlayerNum(playerNum);
		order.setPlayerNames(playerNames);
		order.setStatus(0);
		order.setUserId(user.getId());
		order.setTotalPrice(totalPrice);
		
		List<Integer> wayIds = new ArrayList<Integer>();
		if(fairWays.contains("，")){
			String[] fairWayArray = fairWays.split("，");
			for(String way:fairWayArray){
				Integer wayId = NumberUtils.toInt(way, 0);
				if(wayId!=0){
					wayIds.add(wayId);
				}
			}
		}else{
			Integer wayId = NumberUtils.toInt(fairWays, 0);
			if(wayId!=0){
				wayIds.add(wayId);
			}
		}
		order.setWayIdList(wayIds);
		
		boolean flag = golfClubHome.saveOrder(order);
		
		if(!flag){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "球道已被预订，请重新选择时间！"));
		}
		
		
		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "预订成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJson(result, type);

	}
	
	
	/**
	 * 球场列表
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
		
		if(offset==null){
			offset=0;
		}
		
	    if(limit==null){
	    	limit=10;
	    }

		offset = offset * limit;

		List<GolfClub> clubList = golfClubHome.getGolfClubList(city, offset, limit);

		if (clubList != null) {
			for (GolfClub club : clubList) {
				club.setLogo(GolfConstant.IMAGE_DOMAIN + club.getLogo());
			}
		}

		BaseResponseItem<List<GolfClub>> result = new BaseResponseItem<List<GolfClub>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfClub>>>() {
		}.getType();
		result.setData(clubList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}




}
