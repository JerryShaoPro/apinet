package com.jerryshao.apinet.api;

public enum ApiOperateAction {
	GET(null), //获取资源
	CREATE("POST"), //创建资源
	DELETE(null), //删除资源
	MODIFY("PUT"), //修改资源
	NOT_SURPPORT(null); //不支持的操作

	private String alias = null;

	ApiOperateAction(String alias) {
		this.alias = alias;
	}

	public static ApiOperateAction parse(String opStr) {
		if (null == opStr) {
			return NOT_SURPPORT;
		}

		for (ApiOperateAction action : values()) {
			if (action.name().equalsIgnoreCase(opStr) || opStr.equalsIgnoreCase(action.alias)) {
				return action;
			}
		}

		return NOT_SURPPORT;
	}

}
