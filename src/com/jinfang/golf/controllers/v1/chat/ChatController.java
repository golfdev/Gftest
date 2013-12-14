package com.jinfang.golf.controllers.v1.chat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.api.utils.BaseResponseItem;
import com.jinfang.golf.api.utils.BeanJsonUtils;
import com.jinfang.golf.constants.GolfConstant;
import com.jinfang.golf.constants.ResponseStatus;
import com.jinfang.golf.interceptor.LoginRequired;
import com.jinfang.golf.utils.IdSeqUtils;
import com.jinfang.golf.utils.MathUtil;
import com.jinfang.golf.xmpp.model.Audio;
import com.jinfang.golf.xmpp.model.Photo;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.rest.Post;

@LoginRequired
public class ChatController {
	@Post("uploadPhoto")
	public String uploadPhoto(@Param("photo") MultipartFile file) throws Exception {
		if (file == null || file.getSize() <= 0) {
			return "@" + BeanJsonUtils.convertToJsonWithException(new GolfException(ResponseStatus.SERVER_ERROR,"图片文件为空"));
		}
		int size = (int)file.getSize();
		String absoluteName = "/chat/" + MathUtil.getImgFileDirectory() + "/" + generateFileName() + getFileSuffix(file.getOriginalFilename());
		String imageFileName = GolfConstant.HEAD_PATH + absoluteName;
		String url = GolfConstant.HEAD_DOMAIN + absoluteName;
        File image = savePhoto(file, imageFileName);
        BufferedImage bi = ImageIO.read(image);
        
		Photo photo = new Photo();
		photo.setPhotoId(IdSeqUtils.getNextPhotoId());
		photo.setHeight(bi.getHeight());
		photo.setWidth(bi.getWidth());
		photo.setPhotoUrl(url);
		photo.setSize(size);
		BaseResponseItem<Photo> result = new BaseResponseItem<Photo>(ResponseStatus.OK, "图片上传成功!");
		Type type = new TypeToken<BaseResponseItem<Photo>>() {}.getType();
		result.setData(photo);
	    
	    return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
    }
	@Post("uploadAudio")
	public String uploadAudio(@Param("audio") MultipartFile file, @Param("duration") double duration) throws Exception {
		if (file == null || file.getSize() <= 0) {
			return "@" + BeanJsonUtils.convertToJsonWithException(new GolfException(ResponseStatus.SERVER_ERROR,"音频文件为空"));
		}
		int size = (int) file.getSize();
		String absoluteName = "/chat/" + MathUtil.getImgFileDirectory() + "/" + generateFileName() + getFileSuffix(file.getOriginalFilename());
		String audioFileName = GolfConstant.AUDIO_PATH + absoluteName;
		String url = GolfConstant.AUDIO_DOMAIN + absoluteName;
        savePhoto(file, audioFileName);
        Audio audio = new Audio();
        audio.setAudioId(IdSeqUtils.getNextAudioId());
        audio.setAudioUrl(url);
        audio.setSize(size);
    	BaseResponseItem<Audio> result = new BaseResponseItem<Audio>(ResponseStatus.OK,"语音上传成功!");
	    Type type = new TypeToken<BaseResponseItem<Audio>>() {}.getType();
	    result.setData(audio);
	    
	    return "@" + BeanJsonUtils.convertToJsonWithGsonBuilder(result, type);
    }
	private File savePhoto(MultipartFile file, String fileName) throws Exception {
		File targetFile = new File(fileName);
		File parent = targetFile.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		file.transferTo(targetFile);
		return targetFile;
	}
	private String generateFileName() {
		return UUID.randomUUID().toString().substring(0, 8);
	}
	private String getFileSuffix(String name) {
		int index = name.lastIndexOf(".");
		return name.substring(index);
	}
	public static void main(String[] args) throws Exception {
		File file = new File("D:/temp/1.jpg");
		BufferedImage bi = ImageIO.read(file);
		System.out.println(file.length());
		System.out.println(bi.getHeight());
		System.out.println(bi.getWidth());
		System.out.println(UUID.randomUUID().toString().substring(0, 8));
	}
}
