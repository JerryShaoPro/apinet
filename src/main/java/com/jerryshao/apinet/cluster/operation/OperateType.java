package com.jerryshao.apinet.cluster.operation;

enum OperateType {
	GET,
	MODIFY,
	DELETE,
	UNKOWN;
	
	static OperateType parse(String type) {
		if (null == type) {
			return UNKOWN;
		}
		
		for (OperateType action : values()) {
			if (action.name().equalsIgnoreCase(type)) {
				return action;
			}
		}
		
		return UNKOWN;
	}
}