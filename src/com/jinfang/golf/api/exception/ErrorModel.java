package com.jinfang.golf.api.exception;

public class ErrorModel {
    private Integer status;// 内部编码
    private String msg;// 错误的描述信息

    public ErrorModel(Integer status, String msg) {
        super();
        this.status = status;
        this.msg = msg;
    }

    public ErrorModel() {
        super();
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
    
    

}
