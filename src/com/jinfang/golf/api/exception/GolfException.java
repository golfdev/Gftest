package com.jinfang.golf.api.exception;

/**
 * 数据校验异常类
 * 
 * @author en.wan 2012-4-5
 */
public class GolfException extends RuntimeException {
    private ErrorModel errorModel;
    /**
     * generation uid
     */
    private static final long serialVersionUID = 1L;

    /** not parameter constructor */
    public GolfException() {
        super();
        errorModel = new ErrorModel();
    }
    /** success constructor */
    public GolfException(Integer errorCode) {
        super();
        errorModel = new ErrorModel();
        errorModel.setStatus(errorCode);
    }

    /** error message parameter constructor */
    public GolfException(String errorMessage) {
        super(errorMessage);
        errorModel = new ErrorModel();
        errorModel.setMsg(errorMessage);
    }

    /** has tow parameter constructor */
    public GolfException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        errorModel = new ErrorModel();
        errorModel.setStatus(errorCode);
        errorModel.setMsg(errorMessage);
    }

    public ErrorModel getErrorModel() {
        return errorModel;
    }

}
