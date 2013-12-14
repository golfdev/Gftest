package com.jinfang.golf.model;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class GroupUserModel implements Serializable {
	private static final long serialVersionUID = 8755097645959391291L;
	@Expose
	private int userId;
	@Expose
	private String userName;
	@Expose
	private String headurl;
	@Expose
	private int status;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getHeadurl() {
		return headurl;
	}
	public void setHeadurl(String headurl) {
		this.headurl = headurl;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
