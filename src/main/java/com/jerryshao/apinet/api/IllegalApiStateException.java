package com.jerryshao.apinet.api;

import com.jerryshao.apinet.cluster.constant.ResultCode;

public class IllegalApiStateException extends ApiException {
	private static final long serialVersionUID = 6422394540739686340L;

	public IllegalApiStateException(String errorMsg) {
		super(ResultCode.SYSTEM_ERROR_NEED_RETRY.getCode(), errorMsg);
	}
	
	public IllegalApiStateException(Api api, String errorMsg) {
		super(ResultCode.SYSTEM_ERROR_NEED_RETRY.getCode(), api, errorMsg);
	}
	
	public IllegalApiStateException(Api api, Throwable cause) {
		super(ResultCode.SYSTEM_ERROR_NEED_RETRY.getCode(), api, cause);
	}

}
