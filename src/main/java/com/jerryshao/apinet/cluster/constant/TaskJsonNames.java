package com.jerryshao.apinet.cluster.constant;

/**
 * 用于描述{@link com.jerryshao.apinet.cluster.Task#toJson()}中的json的字段<br />
 * 包括如下字段:<br />
 * <li>TASK_ID, id描述的是task的id号</li>
 * <li>TASK_ATTRIBUTES, attrs描述的是task的参数表</li>
 * <li>TASK_START_TIME, startTime描述的是task的开始时间</li>
 * <li>TASK_RETRY_COUNT, retryCount描述的是task被重试的次数</li>
 */
public enum TaskJsonNames {
	TASK_ID("id"),
	TASK_ATTRIBUTES("attrs"),
	TASK_START_TIME("startTime"),
	TASK_RETRY_COUNT("retryCount");

	private String content;

	private TaskJsonNames(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}
}
