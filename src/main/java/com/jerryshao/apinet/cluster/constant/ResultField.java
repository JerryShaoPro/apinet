package com.jerryshao.apinet.cluster.constant;

/**
 * 用于将{@link com.jerryshao.apinet.cluster.TaskResult}序列化时，所对应的
 * field区域
 * 包括如下field:
 * <li>TASK_FIELD:result_task描述的是所对应的task的field</li>
 * <li>CODE_FIELD:result_code描述的是所对应的code的field</li>
 * <li>JSON_FIELD:result_json描述的是所对应的json的field</li>
 * <li>MESSAGE_FIELD:result_message描述的是所对应的messge的field</li>
 *
 */
public enum ResultField {
	TASK_FIELD("result_task"),
	CODE_FIELD("result_code"),
	MESSAGE_FIELD("result_message"),
	JSON_FIELD("result_json");
	
	final private String content;
	private ResultField(String content) {
		assert(content != null);
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
}
