package com.jerryshao.apinet.api;

import java.util.HashMap;

/**
 * 调用{@link Api}的元数据<br>
 * 目前只包括{@link #OPERATE_ACTION 操作信息}
 */
public class ApiCallMetadata extends HashMap<String, Object> {
	public static final String OPERATE_ACTION = "op";

	private static final long serialVersionUID = 6009176624961990648L;

	public ApiOperateAction getOperateAction() {
		return ApiOperateAction.parse(getString(OPERATE_ACTION));
	}

	private String getString(String key) {
		Object value = get(key);
		if (value != null) {
			return value.toString();
		} else {
			return null;
		}
	}

}
