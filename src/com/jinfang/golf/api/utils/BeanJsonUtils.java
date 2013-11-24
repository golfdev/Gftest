package com.jinfang.golf.api.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jinfang.golf.api.exception.GolfException;
import com.jinfang.golf.constants.ResponseStatus;



public class BeanJsonUtils {
    private static Gson gson = new Gson();
    
    private static Gson gsonBulider = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @SuppressWarnings("unchecked")
    public static <T> T convertToObject(String string, Class<T> clazz) {
        return (T) convertToObject(string, (Type) clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertToObject(String json, Type type) {
        try {
            return (T) gson.fromJson(json, type);
        } catch (Exception e) {
            return (T) ResponseStatus.SERVER_ERROR;
        }
    }

    public static String convertToJson(Object obj) {
        return gson.toJson(obj);
    }

    public static String convertToJson(Object obj, Type type) {
        return gson.toJson(obj, type);
    }

    public static String convertToJsonWithGsonBuilder(Object obj) {
        return gsonBulider.toJson(obj);
    }

    public static String convertToJsonWithGsonBuilder(Object obj, Type type) {
        return gsonBulider.toJson(obj, type);
    }

    public static String convertToJsonWithException(GolfException oe) {
        BaseResponseItem<String> obj = new BaseResponseItem<String>(oe.getErrorModel().getStatus(), oe.getErrorModel().getMsg());
        Type type = new TypeToken<BaseResponseItem<String>>() {}.getType();

        return convertToJson(obj, type);
    }
    
    public static String getJson(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer("");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            br.close();
        } catch (Exception e) {
            return "服务器内部错误！";
        }
        return sb.toString();
    }

}
