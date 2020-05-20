package com.jerryshao.apinet.apis.buildin;

import net.sf.json.JSONObject;

import com.jerryshao.apinet.api.Api;
import com.jerryshao.apinet.api.ApiCallMetadata;
import com.jerryshao.apinet.api.ApiException;
import com.jerryshao.apinet.api.ApiOperateAction;
import com.jerryshao.apinet.api.IllegalApiArugmentException;

public class HelloApi extends Api {

	@Override
	public void checkRequstParam(JSONObject paramJson,
			ApiOperateAction apiOperateAction)
			throws IllegalApiArugmentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doClose() throws ApiException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doStart() throws ApiException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject run(JSONObject paramJson, ApiCallMetadata apiCallMetadata)
			throws ApiException {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		for (int i = 0; i < 100; i ++) {
			json.put("hello" + i, "world");
		}
		return json;
	}
}
