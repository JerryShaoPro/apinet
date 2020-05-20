package com.jerryshao.apinet.cluster.constant;

/**
 * 用于描述{@link com.jerryshao.apinet.cluster.TaskResult}的返回码<br />
 * 定义如下:
 * <li>SUCCESS=200,表示执行成功</li>
 * <li>CLUSTER_ERROR,300-400,表示执行失败，失败的原因是cluster出现问题了</li>
 * <li>SYSTEM_ERROR,500-,表示执行失败，失败的原因是具体Task出现问题了</li>
 *
 * <code>CLUSTER_ERROR</code>又分为如下：<br />
 * <code>CLUSTER_ERROR_NEED_RETRY需要重试</code>
 * <code>CLUSTER_ERROR_NO_NEED_RETRY不需要重试</code>
 *
 * <code>SYSTEM_ERROR</code>又分为如下: <br />
 * <li>SYSTEM_ERROR_NEED_RETRY需要重试</li>
 * <li>SYSTEM_ERROR_NOT_NEED_RETRY不需要重试</li>
 *
 */
public enum ResultCode {
	SUCCESS(200),
	SYSTEM_ERROR_NEED_RETRY(500),
	SYSTEM_ERROR_NOT_NEED_RETRY(501),
	CLUSTER_ERROR_NEED_RETRY(300),
	CLUSTER_ERROR_NOT_NEED_RETRY(301);
	
	final private int code;
	private ResultCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public static ResultCode toRESULT_CODE(int code, ResultCode defaultCode) {
		if (defaultCode == null) {
			throw new NullPointerException("defaultCode");
		}
		switch (code) {
		case 200:
			return SUCCESS;
		case 300:
			return CLUSTER_ERROR_NEED_RETRY;
		case 301:
			return CLUSTER_ERROR_NOT_NEED_RETRY;
		case 500:
			return SYSTEM_ERROR_NEED_RETRY;
		case 501:
			return SYSTEM_ERROR_NOT_NEED_RETRY;
		default:
			return defaultCode;
		}
	}
	
	public static ResultCode toRESULT_CODE(int code) {
		switch (code) {
		case 200:
			return SUCCESS;
		case 300:
			return CLUSTER_ERROR_NEED_RETRY;
		case 301:
			return CLUSTER_ERROR_NOT_NEED_RETRY;
		case 500:
			return SYSTEM_ERROR_NEED_RETRY;
		case 501:
			return SYSTEM_ERROR_NOT_NEED_RETRY;
		default:
			throw new IllegalArgumentException("code");
		}
	}
}
