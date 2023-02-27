/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package org.opengauss.batman.common.exception;

import org.opengauss.batman.common.ErrorCode;

/**
 * 自定义异常
 *
 * @author Mark sunlightcs@gmail.com
 */
public class BatException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
    private String msg;

    private int code = 500;
    
    public BatException(String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public BatException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public BatException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.msg = errorCode.getMessage();
		this.code = errorCode.getCode();
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
