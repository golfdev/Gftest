package com.jinfang.golf.api.utils;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class BaseResponseItem<T> implements Serializable {


    /**
     * 状态
     */
    @Expose private int status;

    /**
     * 错误的描述信息
     */
    @Expose private String msg;

    @Expose private T data;
    
    public BaseResponseItem() {}
    
    // 返回200时调用
    public BaseResponseItem(int status) {
        this.status = status;
    }

    public BaseResponseItem(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
    

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

    
   
}
