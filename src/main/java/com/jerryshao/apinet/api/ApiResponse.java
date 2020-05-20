package com.jerryshao.apinet.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Api调用响应结果类
 *
 */
public final class ApiResponse {
	public static final int succeedCode = 200;
	
	public static final ApiResponse Succeed = new ApiResponse(200, "");
	public static final ApiResponse IllegalArgument = new ApiResponse(400, "");
	public static final ApiResponse SystemError = new ApiResponse(500, "");
	
	public static final String RESPONSE_CODE_FIELD = "code";
	public static final String RESPONSE_DATA_FIELD = "data";
	public static final String RESPONSE_ERRMSG_FIELD = "errMsg";
	public static final String RESPONSE_ISSUCCESS_FIELD = "success";
	public static final String RESPONSE_ERRMSG_DETAIL_FIELD = "innerErrMsg";
	
	int code = succeedCode;
	String errMsg = "";
	String innerErrMsg = "";
	boolean success = true;
	JSONObject data = null;
	
	public ApiResponse(JSONObject data) {
		this(data, true);
	}
	
	public ApiResponse(JSONObject data, boolean success) {
		this.code = Succeed.code;
		this.data = data;
		this.success = success;
	}
	
	public ApiResponse(int code, String errMsg) {
		this.code = code;
		this.errMsg = (null == errMsg) ? "" : errMsg;
		
		if (code != succeedCode) {
			success = false;
		}
	}
	
	public ApiResponse(int code, String errMsg, String innerErrMsg) {
		this.code = code;
		this.errMsg = (null == errMsg) ? "" : errMsg;
		this.innerErrMsg = (null == innerErrMsg) ? "" : innerErrMsg;
		
		if (code != succeedCode) {
			success = false;
		}
	}
	
	public ApiResponse(ApiException apiException) {
		this.code = SystemError.getCode();
		this.errMsg = apiException.getDetailedMessage();
		this.innerErrMsg = dumpException(apiException);
		
		success = false;
	}
	
	public ApiResponse(Throwable cause) {
		this.code = SystemError.getCode();
		this.errMsg = (null == cause.getMessage()) ? SystemError.getErrMsg() : cause.getMessage();
		this.innerErrMsg = dumpException(cause);
		
		success = false;
	}
	
	public ApiResponse appendMessage(String messageDetail) {
		if (null == messageDetail) {
			return this;
		}
		
		String newMessage = String.format("%s - %s", errMsg, messageDetail);
		return new ApiResponse(code, newMessage);
	}
	
	public ApiResponse setException(Throwable cause) {
		if (null == cause) {
			return this;
		}
		
		this.innerErrMsg = dumpException(cause);
		return this;
	}
	
	public JSONObject toJsonObject() {
		JSONObject jsonObject = new JSONObject();
		
		try {
			jsonObject.put(RESPONSE_CODE_FIELD, Integer.valueOf(code));
			jsonObject.put(RESPONSE_ISSUCCESS_FIELD, success);
			jsonObject.put(RESPONSE_ERRMSG_FIELD, errMsg);
			jsonObject.put(RESPONSE_ERRMSG_DETAIL_FIELD, innerErrMsg);
			if (data != null) {
				jsonObject.put(RESPONSE_DATA_FIELD, data);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			jsonObject = SystemError.appendMessage("failed to build json response").setException(e).toJsonObject();
		}
		
		return jsonObject;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setSuccess(boolean isSuccess) {
		this.success = isSuccess;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public String getErrMsg() {
		return errMsg;
	}
	
	public String getInnerErrMsg() {
		return innerErrMsg;
	}
	
	private String dumpException(Throwable cause) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		cause.printStackTrace(printWriter);
		return stringWriter.toString();
	}
	
}