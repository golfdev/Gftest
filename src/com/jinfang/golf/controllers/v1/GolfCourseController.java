package com.jinfang.golf.controllers.v1;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.course.home.GolfCourseHome;
import com.jinfang.golf.course.model.GolfCourse;
import com.jinfang.golf.course.model.GolfCourseHoleScore;
import com.jinfang.golf.course.model.GolfCoursePlayer;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.user.home.UserHome;
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
		golfCourseHome.saveCourse(course);
		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}
	
	
	@Post("holeScoring")
	public String holeScoring(@Param("courseId") Integer courseId,@Param("holeNum") Integer holeNum,
			@Param("playerIds") String playerIds,
			@Param("playerNames") String playerNames,
			@Param("parScore") Integer parScore
		   ,@Param("totalScores") String totalScores,@Param("putterScores") String putterScores) throws Exception {

		if (courseId == null||holeNum==null|| StringUtils.isBlank(playerIds)
				|| StringUtils.isBlank(playerNames)
				|| StringUtils.isBlank(totalScores)|| StringUtils.isBlank(putterScores)) {
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
			holeScore.setSerialNum(i+1);
			holeScore.setSubScore(totalScore-parScore);
			golfCourseHome.saveHoleScore(holeScore);

		}
		BaseResponseItem<String> result = new BaseResponseItem<String>(
				ResponseStatus.OK, "成功！");
		Type type = new TypeToken<BaseResponseItem<String>>() {
		}.getType();
		return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
	}

}
