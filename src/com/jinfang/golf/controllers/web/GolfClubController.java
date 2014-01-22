package com.jinfang.golf.controllers.web;

import java.lang.reflect.Type;

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
import com.jinfang.golf.club.home.GolfClubHome;
import com.jinfang.golf.club.model.GolfClub;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.utils.FormatCheckUtil;
import com.jinfang.golf.utils.UserHolder;

@Path("club")
public class GolfClubController {

    @Autowired
    private Invocation inv;

    @Autowired
    private UserHome userHome;

    @Autowired
    private GolfClubHome golfClubHome;

    @Autowired
    private Passport passport;

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
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "球场id为空！"));
        }
        GolfClub club = golfClubHome.getGolfClubById(id);

        club.setLogo(GolfConstant.IMAGE_DOMAIN + club.getLogo());
        BaseResponseItem<GolfClub> result = new BaseResponseItem<GolfClub>(ResponseStatus.OK,
                "返回球场信息！");
        Type type = new TypeToken<BaseResponseItem<GolfClub>>() {
        }.getType();
        result.setData(club);
        return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);

    }

    /**
     * 登录
     * 
     * @param account
     * @param pwd
     * @return
     * @throws Exception
     */
    @Post("login")
    public String login(@Param("email") String email, @Param("pwd") String pwd) throws Exception {

        if (StringUtils.isBlank(email) || !FormatCheckUtil.isEmail(email)) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "邮箱格式不正确！"));
        }

        GolfClub club = golfClubHome.getGolfClubLoginInfoByEmail(email);

        if (club == null) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "用户不存在！"));
        }

        if (StringUtils.isNotBlank(pwd) && pwd.equals(club.getPassword())) {
            passport.createInCookie(club.getId(), inv.getResponse(), null);
            BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK,
                    "登录成功！");
            return "@" + BeanJsonUtils.convertToJson(result);
        } else {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "用户名或密码不正确！"));
        }

    }

   /**
    * 注册成功
    * @param email
    * @param pwd
    * @param name
    * @param phone
    * @param contacts
    * @param city
    * @return
    * @throws Exception
    */
    @Post("register")
    public String register(@Param("email") String email, @Param("pwd") String pwd,
            @Param("name") String name, @Param("phone") String phone,
            @Param("contacts") String contacts, @Param("city") String city) throws Exception {

        if (StringUtils.isBlank(email) || StringUtils.isBlank(pwd)) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "请输入用户名或密码！"));
        }

        GolfClub club = golfClubHome.getGolfClubLoginInfoByEmail(email);

        if (club != null) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "该邮箱已经被注册！"));
        } else {
            club = new GolfClub();
            club.setEmail(email);
            club.setPassword(pwd);
            club.setName(name);
            club.setContacts(contacts);
            club.setPhone(phone);
            club.setCity(city);
            golfClubHome.registerClub(club);
        }

        BaseResponseItem<String> result = new BaseResponseItem<String>(ResponseStatus.OK, "注册成功！");
        Type type = new TypeToken<BaseResponseItem<String>>() {
        }.getType();
        return "@" + BeanJsonUtils.convertToJson(result, type);

    }

}
