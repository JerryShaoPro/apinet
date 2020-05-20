package com.jerryshao.apinet.apis.buildin;

import com.jerryshao.apinet.api.Api;
import com.jerryshao.apinet.api.ApiCallMetadata;
import com.jerryshao.apinet.api.ApiException;
import com.jerryshao.apinet.api.ApiOperateAction;
import com.jerryshao.apinet.api.IllegalApiArugmentException;

import net.sf.json.JSONObject;

public class DummyApi extends Api {

	@Override
	public JSONObject run(JSONObject paramJson, ApiCallMetadata apiCallMetadata) throws ApiException {
		return paramJson;
	}

	@Override
	protected void doStart() throws ApiException {
		System.out.println("I am starting...");
	}

	@Override
	protected void doClose() throws ApiException {
		System.out.println("I am closing...");
	}

	@Override
	public void checkRequstParam(JSONObject paramJson, ApiOperateAction apiOperateAction) throws IllegalApiArugmentException {
	}

}
