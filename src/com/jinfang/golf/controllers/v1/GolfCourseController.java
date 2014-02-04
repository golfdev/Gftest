package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Post;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.api.utils.JsonUtil;
import com.jinfang.golf.club.home.GolfClubHome;
import com.jinfang.golf.club.model.GolfClubYard;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.course.home.GolfCourseHome;
import com.jinfang.golf.course.model.GolfCourse;
import com.jinfang.golf.course.model.GolfCourseComment;
import com.jinfang.golf.course.model.GolfCourseHoleScore;
import com.jinfang.golf.course.model.GolfCoursePlayer;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.UserHolder;

@Path("course")
@LoginRequired
public class GolfCourseController {

	@Autowired
	private Invocation inv;

	@Autowired
	private UserHome userHome;

	@Autowired
	private GolfCourseHome golfCourseHome;

	@Autowired
	private UserHolder userHolder;
	
	@Autowired
	private GolfClubHome golfClubHome;

	/**
	 * 创建个人比赛
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Post("create")
	public String create(@Param("clubId") Integer clubId,
			@Param("playerIds") String playerIds,
			@Param("teeNums") String teeNums,
			@Param("playerNames") String playerNames,
			@Param("isLive") Integer isLive) throws Exception {

		if (clubId == null || clubId == 0 || StringUtils.isBlank(playerIds)
				|| StringUtils.isBlank(playerNames)
				|| StringUtils.isBlank(teeNums) || isLive == null) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}

		List<GolfCoursePlayer> coursePlayerList = new ArrayList<GolfCoursePlayer>();

		String[] playerArray = playerIds.split("_");
		String[] nameArray = playerNames.split("_");
		String[] teeArray = teeNums.split("_");

		if (playerArray.length != nameArray.length
				|| playerArray.length != teeArray.length) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}

		int length = playerArray.length;

		GolfCourse course = new GolfCourse();
		course.setClubId(clubId);
		course.setIsLive(isLive);
		for (int i = 0; i < length; i++) {
			GolfCoursePlayer coursePlayer = new GolfCoursePlayer();
			course.setIsLive(isLive);
			Integer playerId = NumberUtils.toInt(playerArray[i], 0);
			coursePlayer.setPlayerId(playerId);
			coursePlayer.setPlayerName(nameArray[i]);
			coursePlayer.setSerialNum(i + 1);
			Integer teeNum = NumberUtils.toInt(teeArray[i], 0);
			coursePlayer.setTeeNum(teeNum);
			coursePlayerList.add(coursePlayer);
		}
		course.setPlayerList(coursePlayerList);
		course.setCreatorId(userHolder.getUserInfo().getId());
		Integer courseId = golfCourseHome.saveCourse(course);
	    Map<String,Object> result = new HashMap<String,Object>();
	    result.put("courseId", courseId);
		List<GolfClubYard> yardList = golfClubHome.getGolfClubYardList(course.getClubId());
		
		if(CollectionUtils.isEmpty(yardList)){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "球场码数卡未上传！"));
		}
		
		List<Integer> yardScoreList = new ArrayList<Integer>();
		
		for(GolfClubYard yard:yardList){
			yardScoreList.add(yard.getParScore());
		}
	    result.put("yardScoreList", yardScoreList);

	    JsonUtil.printResult(inv, ResponseStatus.OK, "success！", result);

        return "";
	}
	
	
	/**
	 * 比赛评论
	 * @param courseId
	 * @param content
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Post("comment")
	public String comment(@Param("courseId") Integer courseId,
			@Param("content") String content,
			@Param("type") Integer type) throws Exception {

		if (courseId == null || courseId == 0 || StringUtils.isBlank(content)
				|| type == null) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}
		Integer userId = userHolder.getUserInfo().getId();
		GolfCourseComment comment = new GolfCourseComment();
		comment.setContent(content);
		comment.setCourseId(courseId);
		comment.setType(type);
		comment.setUserId(userId);
		Integer commentId = golfCourseHome.saveComment(comment);
		comment.setId(commentId);
		BaseResponseItem<GolfCourseComment> result = new BaseResponseItem<GolfCourseComment>(
				ResponseStatus.OK, "成功！");
		Type commentType = new TypeToken<BaseResponseItem<GolfCourseComment>>() {
		}.getType();
		result.setData(comment);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, commentType);
	}
	
	
	/**
	 * 比赛评论列表
	 * @param courseId
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("commentList")
	public String commentList(@Param("courseId") Integer courseId,
			@Param("offset") Integer offset,
			@Param("limit") Integer limit) throws Exception {

		if (courseId == null || courseId == 0) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}

		List<GolfCourseComment> commentList = golfCourseHome.getCommentList(courseId, offset, limit);
		BaseResponseItem<List<GolfCourseComment>> result = new BaseResponseItem<List<GolfCourseComment>>(
				ResponseStatus.OK, "成功！");
		Type commentType = new TypeToken<BaseResponseItem<List<GolfCourseComment>>>() {
		}.getType();
		result.setData(commentList);
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, commentType);
	}
	
	
	/**
	 * 更改直播状态
	 * @param courseId
	 * @param isLive
	 * @return
	 * @throws Exception
	 */
	@Post("changeLiveStatus")
	public String changeLiveStatus(@Param("courseId") Integer courseId,
			@Param("isLive") Integer isLive) throws Exception {

		if (courseId == null || courseId == 0 || isLive == null) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}

		
		golfCourseHome.updateLiveStatus(courseId, isLive);
		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}

	@Post("holeScoring")
	public String holeScoring(@Param("courseId") Integer courseId,
			@Param("holeNum") Integer holeNum,
			@Param("playerIds") String playerIds,
			@Param("playerNames") String playerNames,
			@Param("parScore") Integer parScore,
			@Param("totalScores") String totalScores,
			@Param("putterScores") String putterScores) throws Exception {

		if (courseId == null || holeNum == null
				|| StringUtils.isBlank(playerIds)
				|| StringUtils.isBlank(playerNames)
				|| StringUtils.isBlank(totalScores)
				|| StringUtils.isBlank(putterScores)) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}

		String[] playerArray = playerIds.split("_");
		String[] nameArray = playerNames.split("_");
		String[] totalArray = totalScores.split("_");
		String[] putterArray = putterScores.split("_");

		if (playerArray.length != nameArray.length
				|| playerArray.length != totalArray.length
				|| playerArray.length != putterArray.length) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "参数非法！"));
		}

		int length = playerArray.length;
		for (int i = 0; i < length; i++) {
			GolfCourseHoleScore holeScore = new GolfCourseHoleScore();
			holeScore.setCourseId(courseId);
			holeScore.setHoleNum(holeNum);
			holeScore.setParScore(parScore);
			Integer playerId = NumberUtils.toInt(playerArray[i], 0);
			holeScore.setPlayerId(playerId);
			holeScore.setPlayerName(nameArray[i]);
			Integer totalScore = NumberUtils.toInt(totalArray[i], 0);
			holeScore.setTotalScore(totalScore);
			Integer putterScore = NumberUtils.toInt(putterArray[i], 0);
			holeScore.setPutterScore(putterScore);
			holeScore.setSerialNum(i + 1);
			holeScore.setSubScore(totalScore - parScore);
			golfCourseHome.saveHoleScore(holeScore);

		}
		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}

	/**
	 * 历史比赛
	 * 
	 * @param city
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("history")
	public String historyList(@Param("userId") Integer userId,
			@Param("offset") Integer offset, @Param("limit") Integer limit)
			throws Exception {

		if (offset == null) {
			offset = 0;
		}

		if (limit == null) {
			limit = 10;
		}

		if (userId == null) {
			userId = userHolder.getUserInfo().getId();
		}

		offset = offset * limit;

		List<GolfCourse> courseList = golfCourseHome.getHistoryCourseList(
				userId, offset, limit);

		BaseResponseItem<List<GolfCourse>> result = new BaseResponseItem<List<GolfCourse>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfCourse>>>() {
		}.getType();
		result.setData(courseList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	
	
	/**
	 * 返回比赛详情
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Post("show")
	public String show(@Param("courseId") Integer id) throws Exception {

		if (id == null || id == 0) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "打球的id为空！"));
		}
		
		Map<String,Object> result = new HashMap<String,Object>();
		
		GolfCourse course = golfCourseHome.getGolfCourseById(id);
		
		if(course==null){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "比赛不存在！"));
		}
		List<GolfClubYard> yardList = golfClubHome.getGolfClubYardList(course.getClubId());
		
		if(CollectionUtils.isEmpty(yardList)){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "球场码数卡未上传！"));
		}
		
		List<Integer> yardScoreList = new ArrayList<Integer>();
		
		for(GolfClubYard yard:yardList){
			yardScoreList.add(yard.getParScore());
		}
		
		List<GolfCourseHoleScore> holeScoreList = golfCourseHome.getHoleScoreList(id);
		Map<Integer,List<Integer>> holeTotalScoreMap = new HashMap<Integer,List<Integer>>();
		Map<Integer,List<Integer>> holePutterScoreMap = new HashMap<Integer,List<Integer>>();

		for(GolfCourseHoleScore holeScore:holeScoreList){
			List<Integer> holeTotalScore = null;
			List<Integer> holePutterScore = null;
			if(holeTotalScoreMap.containsKey(holeScore.getHoleNum())){
				holeTotalScore = holeTotalScoreMap.get(holeScore.getHoleNum());
				holeTotalScore.add(holeScore.getTotalScore());
				holeTotalScoreMap.put(holeScore.getHoleNum(), holeTotalScore);
			}else{
				holeTotalScore = new ArrayList<Integer>();
				holeTotalScore.add(holeScore.getTotalScore());
				holeTotalScoreMap.put(holeScore.getHoleNum(), holeTotalScore);
			}
			
			if(holePutterScoreMap.containsKey(holeScore.getHoleNum())){
				holePutterScore = holePutterScoreMap.get(holeScore.getHoleNum());
				holePutterScore.add(holeScore.getPutterScore());
				holePutterScoreMap.put(holeScore.getHoleNum(), holePutterScore);
			}else{
				holePutterScore = new ArrayList<Integer>();
				holePutterScore.add(holeScore.getPutterScore());
				holePutterScoreMap.put(holeScore.getHoleNum(), holePutterScore);
			}
		}
		
	
		
		List<Integer> totList = new ArrayList<Integer>();
		List<Integer> before9TotalList = new ArrayList<Integer>();
		List<Integer> after9TotalList = new ArrayList<Integer>();
		List<Integer> before9PutterList = new ArrayList<Integer>();
		List<Integer> after9PutterList = new ArrayList<Integer>();
		List<Integer> totalList = new ArrayList<Integer>();
		List<Integer> totalPutterList = new ArrayList<Integer>();



		
		if(holeScoreList!=null){
			Map<Integer,Integer> totScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> before9TotalScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> before9PutterScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> after9TotalScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> after9PutterScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> totalScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> totalPutterScoreMap = new HashMap<Integer,Integer>();

			for(GolfCourseHoleScore score:holeScoreList){
				if(totScoreMap.containsKey(score.getPlayerId())){
					Integer tot = totScoreMap.get(score.getPlayerId());
					Integer sum = score.getSubScore()+tot;
					totScoreMap.put(score.getPlayerId(), sum);
				}else{
					totScoreMap.put(score.getPlayerId(), score.getSubScore());
				}
				if(score.getHoleNum()<=9){
					if(before9TotalScoreMap.containsKey(score.getPlayerId())){
						Integer total = before9TotalScoreMap.get(score.getPlayerId());
						Integer sum = score.getTotalScore()+total;
						before9TotalScoreMap.put(score.getPlayerId(), sum);
					}else{
						before9TotalScoreMap.put(score.getPlayerId(), score.getTotalScore());
					}
					
					if(before9PutterScoreMap.containsKey(score.getPlayerId())){
						Integer total = before9PutterScoreMap.get(score.getPlayerId());
						Integer sum = score.getPutterScore()+total;
						before9PutterScoreMap.put(score.getPlayerId(), sum);
					}else{
						before9PutterScoreMap.put(score.getPlayerId(), score.getPutterScore());
					}
				}else{
					if(after9TotalScoreMap.containsKey(score.getPlayerId())){
						Integer total = after9TotalScoreMap.get(score.getPlayerId());
						Integer sum = score.getTotalScore()+total;
						after9TotalScoreMap.put(score.getPlayerId(), sum);
					}else{
						after9TotalScoreMap.put(score.getPlayerId(), score.getTotalScore());
					}
					
					if(after9PutterScoreMap.containsKey(score.getPlayerId())){
						Integer total = after9PutterScoreMap.get(score.getPlayerId());
						Integer sum = score.getPutterScore()+total;
						after9PutterScoreMap.put(score.getPlayerId(), sum);
					}else{
						after9PutterScoreMap.put(score.getPlayerId(), score.getPutterScore());
					}
				}
				
				
				
				if(totalScoreMap.containsKey(score.getPlayerId())){
					Integer total = totalScoreMap.get(score.getPlayerId());
					Integer sum = score.getTotalScore()+total;
					totalScoreMap.put(score.getPlayerId(), sum);
				}else{
					totalScoreMap.put(score.getPlayerId(), score.getTotalScore());
				}
				
				if(totalPutterScoreMap.containsKey(score.getPlayerId())){
					Integer total = totalPutterScoreMap.get(score.getPlayerId());
					Integer sum = score.getPutterScore()+total;
					totalPutterScoreMap.put(score.getPlayerId(), sum);
				}else{
					totalPutterScoreMap.put(score.getPlayerId(), score.getPutterScore());
				}
				
			}
			
			List<GolfCoursePlayer> playerList = course.getPlayerList();
			
			for(GolfCoursePlayer player:playerList){
				if(totScoreMap.containsKey(player.getPlayerId())){
					totList.add(totScoreMap.get(player.getPlayerId()));
				}
				if(before9TotalScoreMap.containsKey(player.getPlayerId())){
					before9TotalList.add(before9TotalScoreMap.get(player.getPlayerId()));
				}
				if(before9PutterScoreMap.containsKey(player.getPlayerId())){
					before9PutterList.add(before9PutterScoreMap.get(player.getPlayerId()));
				}
				
				if(after9TotalScoreMap.containsKey(player.getPlayerId())){
					after9TotalList.add(after9TotalScoreMap.get(player.getPlayerId()));
				}
				
				if(after9PutterScoreMap.containsKey(player.getPlayerId())){
					after9PutterList.add(after9PutterScoreMap.get(player.getPlayerId()));
				}
				
				if(totalScoreMap.containsKey(player.getPlayerId())){
					totalList.add(totalScoreMap.get(player.getPlayerId()));
				}
				
				if(totalPutterScoreMap.containsKey(player.getPlayerId())){
					totalPutterList.add(totalPutterScoreMap.get(player.getPlayerId()));
				}
			}
			
		}
		
		result.put("parList", yardScoreList);
		result.put("playerList", course.getPlayerList());
//		result.put("holeScoreList", holeScoreList);
		result.put("holeTotalScoreDetail", holeTotalScoreMap);
		result.put("holePutterScoreDetail", holePutterScoreMap);


		result.put("totList", totList);
		result.put("before9TotalScoreList", before9TotalList);
		result.put("before9PutterScoreList", before9PutterList);
		result.put("after9TotalScoreList", after9TotalList);
		result.put("after9PutterScoreList", after9PutterList);
		result.put("totalScoreList", totalList);
		result.put("totalPutterScoreList", totalPutterList);

	    JsonUtil.printResult(inv, ResponseStatus.OK, "success！", result);

        return "";
	}
	
	
	/**
	 * 直播
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Post("live/show")
	public String liveShow(@Param("courseId") Integer id) throws Exception {

		if (id == null || id == 0) {
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "打球的id为空！"));
		}
		
		Map<String,Object> result = new HashMap<String,Object>();
		
		GolfCourse course = golfCourseHome.getGolfCourseById(id);
		
		if(course==null){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "比赛不存在！"));
		}
		List<GolfClubYard> yardList = golfClubHome.getGolfClubYardList(course.getClubId());
		
		if(CollectionUtils.isEmpty(yardList)){
			return "@"
					+ BeanJsonUtils
							.convertToJsonWithException(new GolfException(
									ResponseStatus.SERVER_ERROR, "球场码数卡未上传！"));
		}
		
		List<Integer> yardScoreList = new ArrayList<Integer>();
		
		for(GolfClubYard yard:yardList){
			yardScoreList.add(yard.getParScore());
		}
		
		List<GolfCourseHoleScore> holeScoreList = golfCourseHome.getHoleScoreList(id);
		Map<Integer,List<Integer>> holeTotalScoreMap = new HashMap<Integer,List<Integer>>();
//		Map<Integer,List<Integer>> holePutterScoreMap = new HashMap<Integer,List<Integer>>();

		for(GolfCourseHoleScore holeScore:holeScoreList){
			List<Integer> holeTotalScore = null;
			if(holeTotalScoreMap.containsKey(holeScore.getHoleNum())){
				holeTotalScore = holeTotalScoreMap.get(holeScore.getHoleNum());
				holeTotalScore.add(holeScore.getTotalScore());
				holeTotalScoreMap.put(holeScore.getHoleNum(), holeTotalScore);
			}else{
				holeTotalScore = new ArrayList<Integer>();
				holeTotalScore.add(holeScore.getTotalScore());
				holeTotalScoreMap.put(holeScore.getHoleNum(), holeTotalScore);
			}
			
//			if(holePutterScoreMap.containsKey(holeScore.getHoleNum())){
//				holePutterScore = holePutterScoreMap.get(holeScore.getHoleNum());
//				holePutterScore.add(holeScore.getPutterScore());
//				holePutterScoreMap.put(holeScore.getHoleNum(), holePutterScore);
//			}else{
//				holePutterScore = new ArrayList<Integer>();
//				holePutterScore.add(holeScore.getPutterScore());
//				holePutterScoreMap.put(holeScore.getHoleNum(), holePutterScore);
//			}
		}
		
	
		
		List<Integer> totList = new ArrayList<Integer>();
		List<Integer> before9TotalList = new ArrayList<Integer>();
		List<Integer> after9TotalList = new ArrayList<Integer>();
//		List<Integer> before9PutterList = new ArrayList<Integer>();
//		List<Integer> after9PutterList = new ArrayList<Integer>();
		List<Integer> totalList = new ArrayList<Integer>();
//		List<Integer> totalPutterList = new ArrayList<Integer>();



		
		if(holeScoreList!=null){
			Map<Integer,Integer> totScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> before9TotalScoreMap = new HashMap<Integer,Integer>();
//			Map<Integer,Integer> before9PutterScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> after9TotalScoreMap = new HashMap<Integer,Integer>();
//			Map<Integer,Integer> after9PutterScoreMap = new HashMap<Integer,Integer>();
			Map<Integer,Integer> totalScoreMap = new HashMap<Integer,Integer>();
//			Map<Integer,Integer> totalPutterScoreMap = new HashMap<Integer,Integer>();

			for(GolfCourseHoleScore score:holeScoreList){
				if(totScoreMap.containsKey(score.getPlayerId())){
					Integer tot = totScoreMap.get(score.getPlayerId());
					Integer sum = score.getSubScore()+tot;
					totScoreMap.put(score.getPlayerId(), sum);
				}else{
					totScoreMap.put(score.getPlayerId(), score.getSubScore());
				}
				if(score.getHoleNum()<=9){
					if(before9TotalScoreMap.containsKey(score.getPlayerId())){
						Integer total = before9TotalScoreMap.get(score.getPlayerId());
						Integer sum = score.getTotalScore()+total;
						before9TotalScoreMap.put(score.getPlayerId(), sum);
					}else{
						before9TotalScoreMap.put(score.getPlayerId(), score.getTotalScore());
					}
					
//					if(before9PutterScoreMap.containsKey(score.getPlayerId())){
//						Integer total = before9PutterScoreMap.get(score.getPlayerId());
//						Integer sum = score.getPutterScore()+total;
//						before9PutterScoreMap.put(score.getPlayerId(), sum);
//					}else{
//						before9PutterScoreMap.put(score.getPlayerId(), score.getPutterScore());
//					}
				}else{
					if(after9TotalScoreMap.containsKey(score.getPlayerId())){
						Integer total = after9TotalScoreMap.get(score.getPlayerId());
						Integer sum = score.getTotalScore()+total;
						after9TotalScoreMap.put(score.getPlayerId(), sum);
					}else{
						after9TotalScoreMap.put(score.getPlayerId(), score.getTotalScore());
					}
					
//					if(after9PutterScoreMap.containsKey(score.getPlayerId())){
//						Integer total = after9PutterScoreMap.get(score.getPlayerId());
//						Integer sum = score.getPutterScore()+total;
//						after9PutterScoreMap.put(score.getPlayerId(), sum);
//					}else{
//						after9PutterScoreMap.put(score.getPlayerId(), score.getPutterScore());
//					}
				}
				
				
				
				if(totalScoreMap.containsKey(score.getPlayerId())){
					Integer total = totalScoreMap.get(score.getPlayerId());
					Integer sum = score.getTotalScore()+total;
					totalScoreMap.put(score.getPlayerId(), sum);
				}else{
					totalScoreMap.put(score.getPlayerId(), score.getTotalScore());
				}
				
//				if(totalPutterScoreMap.containsKey(score.getPlayerId())){
//					Integer total = totalPutterScoreMap.get(score.getPlayerId());
//					Integer sum = score.getPutterScore()+total;
//					totalPutterScoreMap.put(score.getPlayerId(), sum);
//				}else{
//					totalPutterScoreMap.put(score.getPlayerId(), score.getPutterScore());
//				}
				
			}
			
			List<GolfCoursePlayer> playerList = course.getPlayerList();
			
			for(GolfCoursePlayer player:playerList){
				if(totScoreMap.containsKey(player.getPlayerId())){
					totList.add(totScoreMap.get(player.getPlayerId()));
				}
				if(before9TotalScoreMap.containsKey(player.getPlayerId())){
					before9TotalList.add(before9TotalScoreMap.get(player.getPlayerId()));
				}
//				if(before9PutterScoreMap.containsKey(player.getPlayerId())){
//					before9PutterList.add(before9PutterScoreMap.get(player.getPlayerId()));
//				}
				
				if(after9TotalScoreMap.containsKey(player.getPlayerId())){
					after9TotalList.add(after9TotalScoreMap.get(player.getPlayerId()));
				}
				
//				if(after9PutterScoreMap.containsKey(player.getPlayerId())){
//					after9PutterList.add(after9PutterScoreMap.get(player.getPlayerId()));
//				}
				
				if(totalScoreMap.containsKey(player.getPlayerId())){
					totalList.add(totalScoreMap.get(player.getPlayerId()));
				}
				
//				if(totalPutterScoreMap.containsKey(player.getPlayerId())){
//					totalPutterList.add(totalPutterScoreMap.get(player.getPlayerId()));
//				}
			}
			
		}
		golfCourseHome.incViewCount(id);
		result.put("parList", yardScoreList);
		result.put("playerList", course.getPlayerList());
//		result.put("holeScoreList", holeScoreList);
		result.put("holeTotalScoreDetail", holeTotalScoreMap);
//		result.put("holePutterScoreDetail", holePutterScoreMap);


		result.put("totList", totList);
		result.put("before9TotalScoreList", before9TotalList);
//		result.put("before9PutterScoreList", before9PutterList);
		result.put("after9TotalScoreList", after9TotalList);
//		result.put("after9PutterScoreList", after9PutterList);
		result.put("totalScoreList", totalList);
		
		result.put("totalScoreList", totalList);
		
		List<GolfCourseComment> commentList = golfCourseHome.getCommentList(id, 0, 20);

		result.put("commentList", commentList);

		
//		result.put("totalPutterScoreList", totalPutterList);

	    JsonUtil.printResult(inv, ResponseStatus.OK, "success！", result);

        return "";
	}
	
	/**
	 * 直播列表
	 * @param offset
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@Post("live/list")
	public String liveList(
			@Param("offset") Integer offset, @Param("limit") Integer limit)
			throws Exception {

		if (offset == null) {
			offset = 0;
		}

		if (limit == null) {
			limit = 10;
		}

		offset = offset * limit;

		List<GolfCourse> courseList = golfCourseHome.getLiveCourseList(
				offset, limit);

		BaseResponseItem<List<GolfCourse>> result = new BaseResponseItem<List<GolfCourse>>(
				ResponseStatus.OK, "成功！");
		Type listType = new TypeToken<BaseResponseItem<List<GolfCourse>>>() {
		}.getType();
		result.setData(courseList);
		return "@"
				+ BeanJsonUtils.convertToJsonWithGsonBuilder(result, listType);

	}
	

}
