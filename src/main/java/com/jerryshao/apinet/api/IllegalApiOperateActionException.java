package com.jerryshao.apinet.api;

import com.jerryshao.apinet.cluster.constant.ResultCode;

public class IllegalApiOperateActionException extends ApiException {
	public IllegalApiOperateActionException(Api api, ApiOperateAction operateAction) {
		super(ResultCode.SYSTEM_ERROR_NOT_NEED_RETRY.getCode(), api, "不支持操作:"+operateAction.name());
	}
	
	private static final long serialVersionUID = 1780276410745370309L;
}
