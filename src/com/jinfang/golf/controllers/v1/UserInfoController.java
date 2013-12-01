package com.jinfang.golf.controllers.v1;

import java.io.IOException;
import java.lang.reflect.Type;
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
import com.jinfang.golf.passport.model.Passport;
import com.jinfang.golf.sms.home.SmsHome;
import com.jinfang.golf.user.home.UserHome;
import com.jinfang.golf.user.home.VerifyCodeHome;
import com.jinfang.golf.user.model.User;
import com.jinfang.golf.utils.MathUtil;
import com.jinfang.golf.utils.UploadUtil;
import com.jinfang.golf.utils.UserHolder;

@LoginRequired
@Path("user")
public class UserInfoController {

    @Autowired
    private Invocation inv;

    @Autowired
    private UserHome userHome;

    @Autowired
    private VerifyCodeHome verifyCodeHome;

    @Autowired
    private SmsHome smsHome;

    @Autowired
    private Passport passport;
    
    @Autowired
    private UserHolder userHolder;

    /**
     * 返回用户基本信息
     * @param id
     * @return
     * @throws Exception
     */
    @Post("show")
    public String show(@Param("id") Integer id) throws Exception {

        if (id==null||id==0) {
            return "@"
                    + BeanJsonUtils.convertToJsonWithException(new GolfException(
                            ResponseStatus.SERVER_ERROR, "用户id为空！"));
        }
        
        User user = userHome.getById(id);
        
		BaseResponseItem<User> result = new BaseResponseItem<User>(ResponseStatus.OK,"返回用户信息！");
	    Type type = new TypeToken<BaseResponseItem<User>>() {}.getType();
	    result.setData(user);
	    return "@" + BeanJsonUtils.convertToJson(result,type);

    }
    
   /**
    * 上传头像
    * @param userHead
    * @return
    * @throws Exception
    */
    @Post("uploadPhoto")
    public String uploadPhoto(@Param("userHead")MultipartFile userHead) throws Exception {

        String path = processUserHead(userHead);
        User user = userHolder.getUserInfo();
        user.setHeadUrl(path);
        userHome.updateHeadUrl(user);
        
		BaseResponseItem<User> result = new BaseResponseItem<User>(ResponseStatus.OK,"上传图片成功！");
	    Type type = new TypeToken<BaseResponseItem<User>>() {}.getType();
	    result.setData(user);
	    return "@" + BeanJsonUtils.convertToJson(result,type);

    }
    
    
  /**
   * 编辑用户信息
   * @param userHead
   * @return
   * @throws Exception
   */
    @Post("edit")
    public String edit(@Param("userName")String userName,@Param("gender")Integer gender,@Param("city")String city,@Param("description")String description) throws Exception {

        User user = userHolder.getUserInfo();
        
        if(StringUtils.isNotBlank(userName)){
        	user.setUserName(userName);
        }
        
        if(gender!=null){
        	user.setGender(gender);
        }
        
        if(StringUtils.isNotBlank(city)){
        	user.setCity(city);;
        }
    	
        if(StringUtils.isNotBlank(description)){
        	user.setDescription(description);
        }
        
        userHome.updateUser(user);
        
		BaseResponseItem<User> result = new BaseResponseItem<User>(ResponseStatus.OK,"修改成功！");
	    Type type = new TypeToken<BaseResponseItem<User>>() {}.getType();
	    result.setData(user);
	    return "@" + BeanJsonUtils.convertToJson(result,type);

    }
    
    /**
     * 球手列表
     * @param type
     * @param offset
     * @param limit
     * @return
     * @throws Exception
     */
      @Post("list")
      public String list(@Param("type")Integer type,@Param("offset")Integer offset,@Param("limit")Integer limit) throws Exception {

          User user = userHolder.getUserInfo();
          List<User> userList = null;
          
          offset = offset*limit;
          
          if(type==0){
        	  userList = userHome.getAllUserList(offset, limit);
          }else if(type==1){
        	  String city = user.getCity();
        	  userList = userHome.getAllUserListByCity(offset, limit, city);
          }else if(type==2){
        	  
          }else if(type==3){
        	  userList = userHome.getAllUserListByStatus(offset, limit,1);
          }else if(type == 4 ){
        	  userList = userHome.getAllUserListByStatus(offset, limit,0);
          }
          
  		BaseResponseItem< List<User>> result = new BaseResponseItem< List<User>>(ResponseStatus.OK,"上传图片成功！");
  	    Type listType = new TypeToken<BaseResponseItem< List<User>>>() {}.getType();
  	    result.setData(userList);
  	    return "@" + BeanJsonUtils.convertToJson(result,listType);

      }
    
    /**
     * 处理用户头像
     * @param imgFile
     * @return
     */
    private String processUserHead(MultipartFile imgFile){
        if(imgFile == null){
            return null;
        }
        String fileName = imgFile.getOriginalFilename();
        String name = String.valueOf(System.currentTimeMillis());
        String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        String saveFilePath = GolfConstant.HEAD_PATH + "/head/" + MathUtil.getImgFileDirectory() + "/" ;
        String saveDBPath = "/head/" + MathUtil.getImgFileDirectory() + "/";
        try {
            UploadUtil.saveFile(imgFile, saveFilePath, name + suffix);
//            String fileSrcPath = saveFilePath + name + suffix;
//            String tinyPath = saveFilePath + name + "50x50" + suffix;
//            String mainPath = saveFilePath + name + "200x200" + suffix;
//            EasyImage.resize(fileSrcPath, tinyPath, 50, 50);
//            EasyImage.resize(fileSrcPath, mainPath, 200, 200);
            return saveDBPath+name+suffix;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

   

}
