package com.jerryshao.apinet.cluster.executor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.jerryshao.apinet.api.Api;
import com.jerryshao.apinet.api.ApiCallMetadata;
import com.jerryshao.apinet.api.ApiException;
import com.jerryshao.apinet.api.ApiLifecycle;
import com.jerryshao.apinet.api.ApiResponse;
import com.jerryshao.apinet.api.IllegalApiArugmentException;
import com.jerryshao.apinet.apis.buildin.Settings;
import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.jerryshao.apinet.cluster.constant.TaskParamNames;
import com.jerryshao.apinet.cluster.exception.TaskExecuteException;
import com.jerryshao.apinet.server.ApiManager;
import com.jerryshao.apinet.server.ApiResponseBuilder;
import com.jerryshao.apinet.server.ApiRouter;
import com.jerryshao.apinet.server.ServerConfig;
import com.jerryshao.apinet.util.ObjectsFactory;
import com.wintim.common.util.LogFactory;

public class ApTaskExecutor implements TaskExecutor {
	final static private Logger LOG = LogFactory.getLogger(ApTaskExecutor.class);

	public ApTaskExecutor() {
		registerBuildinApis();
		try {
			registerCustomerApis();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public TaskResult execute(Task task) throws TaskExecuteException {
		//0. 获取api的路径
		String path = task.getParamJson().getString(TaskParamNames.TASK_PARAM_URI.getContent());
		assert (path != null);
		ApiResponse apiResponse = null;

		ResultCode resultCode = ResultCode.SUCCESS;
		boolean isOver = false;

		Api targetApi = null;
		JSONObject paramJson = null;
		ApiCallMetadata apiCallMetadata = null;
		JSONObject apiRunJsonResult = null;

		//1. 路由api
		if (! isOver) {
			try {
				targetApi = routeToApi(path);
			} catch (ApiException e) {
				e.printStackTrace();
				apiResponse = buildApiResponse(e);
				isOver =true;
				resultCode = ResultCode.SYSTEM_ERROR_NOT_NEED_RETRY;
			}
		}

		//2. 检查api的状态
		if (! isOver) {
			if (targetApi.lifecycleState() == ApiLifecycle.State.CLOSED) {
				isOver = true;
				resultCode = ResultCode.SYSTEM_ERROR_NEED_RETRY;
				apiResponse = new ApiResponse(ApiResponse.SystemError.getCode(), "api is closed!");
			}

			if (targetApi.lifecycleState() != ApiLifecycle.State.STARTED) {
				targetApi.start();
			}
		}

		//3. 解析Api的参数
		if (! isOver) {
			paramJson = buildParamJson(task.getParamJson());
			apiCallMetadata = parseApiCallMetadata(task.getParamJson());
			LOG.info(String.format("op:'%s', params:'%s'", apiCallMetadata.getOperateAction(), paramJson.toString()));
		}

		//4. 检查请求参数
		if (! isOver) {
			try {
				targetApi.checkRequstParam(paramJson, apiCallMetadata.getOperateAction());
			} catch (IllegalApiArugmentException e) {
				e.printStackTrace();
				isOver = true;
				resultCode = ResultCode.SYSTEM_ERROR_NOT_NEED_RETRY;
				apiResponse = buildApiResponse(e);
			}
		}

		//5. 执行api
		if (! isOver) {
			try {
				apiRunJsonResult = targetApi.run(paramJson, apiCallMetadata);
			} catch (ApiException e) {
				e.printStackTrace();
				isOver = true;
				resultCode = ResultCode.toRESULT_CODE(e.getErrorCode(), ResultCode.SYSTEM_ERROR_NEED_RETRY);
				apiResponse = buildApiResponse(e);
			}
		}

		if (! isOver) {
			apiResponse = buildApiResponse(targetApi, apiRunJsonResult);
			resultCode = ResultCode.SUCCESS;
			isOver = true;
		}

		TaskResult taskExecuteResult = buildApiTaskResult(task, apiResponse, resultCode);
		return taskExecuteResult;
	}

	/**
	 * 根据ApiName，路由到相应的Api
	 * @param path
	 * @return
	 * @throws ApiException
	 */
	private Api routeToApi(String path) throws ApiException {
		Api targetApi = ApiRouter.route(path);
		if (null == targetApi) {
			throw new ApiException("Can not route to any api for " + path);
		}
		return targetApi;
	}

	@SuppressWarnings("unchecked")
	private JSONObject buildParamJson(JSONObject param) {
		JSONObject jsonObject = new JSONObject();
		for (Object obj : param.entrySet()) {
			assert (obj instanceof Entry);
			Entry<String, Object> entry = (Entry<String, Object>) obj;
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;
	}


	/**
	 * 根据url的path的<i>Request</i>解析Api调用的元信息
	 * @param path
	 * @param baseRequest
	 * @return Api调用的元信息
	 */
	private ApiCallMetadata parseApiCallMetadata(JSONObject params) {
		ApiCallMetadata apiCallMetadata = new ApiCallMetadata();
		String operation = extractOperationFromUrlPath(params.getString(TaskParamNames.TASK_PARAM_URI.getContent()));
		if (null == operation || operation.trim().length() == 0) {
			LOG.warn("Can not exact operation of the url path " + params.getString(TaskParamNames.TASK_PARAM_URI.getContent()));
			operation = params.getString(TaskParamNames.TASK_PARAM_METHOD.getContent());
		}
		apiCallMetadata.put(ApiCallMetadata.OPERATE_ACTION, operation);
		return apiCallMetadata;
	}

	public static String extractOperationFromUrlPath(String path) {
		String operation = null;

		if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
		String[] urlPathParts = path.split("/");
		if (urlPathParts != null && urlPathParts.length >= 2) {
			operation = urlPathParts[1];
		}
		return operation;
	}

	/**
	 * 构建相应结果，需要先对Api的结果数据的Json格式进行属性的处理
	 * @param targetApi 是访问的api
	 * @param apiRunJsonResult api处理结果数据
	 * @return
	 */
	private ApiResponse buildApiResponse(Api targetApi, JSONObject apiRunJsonResult) {
		if (null == apiRunJsonResult) {
			apiRunJsonResult = new JSONObject();
		}

		//构建需要返回的Json数据
		JSONObject retApiJsonResult = ApiResponseBuilder.build(apiRunJsonResult, targetApi);
		return new ApiResponse(retApiJsonResult);
	}

	private ApiResponse buildApiResponse(Throwable cause) {
		ApiResponse apiResponse = ApiResponse.SystemError;
		if (cause instanceof IllegalApiArugmentException) {
			apiResponse = ApiResponse.IllegalArgument.appendMessage(cause.getMessage());
		} else if (cause instanceof ApiException) {
			int errorCode = ((ApiException) cause).getErrorCode();
			if (errorCode == -1) {
				errorCode = ApiResponse.SystemError.getCode();
			}
			apiResponse = new ApiResponse(errorCode, cause.getMessage());
		}

		LOG.error("", cause);

		apiResponse = apiResponse.setException(cause);

		return apiResponse;
	}

	private TaskResult buildApiTaskResult(Task task, ApiResponse apiResponse, ResultCode resultCode) {
		if (apiResponse == null) {
			throw new NullPointerException("api response");
		}
		if (task == null) {
			throw new NullPointerException("task");
		}

		TaskResult result = new TaskResult(task.getId());
		if (apiResponse.isSuccess()) {
			result.setCode(ResultCode.SUCCESS);
		} else {
			result.setCode(resultCode);
		}
		result.setResult(apiResponse.toJsonObject());
		return result;
	}

	private void registerBuildinApis() {
		ApiManager.get().register(new Settings(),true);
	}

	@SuppressWarnings("unchecked")
	private void registerCustomerApis() throws Exception {
		String[] customerApiClassStrs = ServerConfig.getCustomerizedApis();
		Collection<Api> customerApis = (Collection<Api>) ObjectsFactory.createObjects(Arrays.asList(customerApiClassStrs), Api.class);
		for (Api api : customerApis) {
			ApiManager.get().register(api, true);
		}
	}
}
