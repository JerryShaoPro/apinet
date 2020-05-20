package com.jerryshao.apinet.apis.buildin;

import com.jerryshao.apinet.api.Api;
import com.jerryshao.apinet.api.ApiCallMetadata;
import com.jerryshao.apinet.api.ApiException;
import com.jerryshao.apinet.api.ApiOperateAction;
import com.jerryshao.apinet.api.IllegalApiArugmentException;
import com.jerryshao.apinet.api.IllegalApiOperateActionException;
import com.jerryshao.apinet.server.ApiManager;
import com.jerryshao.apinet.settings.Settings.Builder;
import com.jerryshao.apinet.util.Strings;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Settings extends Api {

	private static final String OPERATE_API_NAME_ATTR = "apiname";
	private static final String GET_SETTINGS_FIELDS_ATTR = "fields"; 
	
	@Override
	public JSONObject run(JSONObject paramJson, ApiCallMetadata apiCallMetadata) throws ApiException {
		String targetApiName = paramJson.getString(OPERATE_API_NAME_ATTR);
		
		Api targetApi = ApiManager.get().get(targetApiName);
		if (null == targetApi) {
			targetApi = ApiManager.get().get(Strings.toUnderscoreCase(targetApiName));
		} 
		
		if (null == targetApi) {
			throw new ApiException(String.format("can't find api %s", targetApiName));
		}
		
	
		paramJson.remove(OPERATE_API_NAME_ATTR);
		
		if (ApiOperateAction.GET == apiCallMetadata.getOperateAction()) {
			return get(targetApi, paramJson);
		} else if (ApiOperateAction.CREATE == apiCallMetadata.getOperateAction() || ApiOperateAction.MODIFY == apiCallMetadata.getOperateAction()) {
			return modify(targetApi, paramJson);
		} else {
			throw new IllegalApiOperateActionException(this, apiCallMetadata.getOperateAction());
		}
	}

	private JSONObject get(Api api, JSONObject paramJson) {
		if (paramJson.has(GET_SETTINGS_FIELDS_ATTR)) { 
			JSONObject retJson = new JSONObject();
			String[] fields = Strings.splitStringByCommaToArray(paramJson.getString(GET_SETTINGS_FIELDS_ATTR));
			for (String field : fields) {
				String value = api.getSettings().get(field);
				if (value == null) {
					String[] values = api.getSettings().getAsArray(field);
					if (values != null) {
						retJson.put(field.trim(), JSONArray.fromObject(values));
					} else {
						retJson.put(field.trim(), "");
					}
				} else {
					retJson.put(field.trim(), value);
				}
			}
			return retJson;
		} else { 
			return JSONObject.fromObject(com.jerryshao.apinet.settings.Settings.settingsBuilder().put(api.getSettings()).internalMap());
		}
	}
	
	private JSONObject modify(Api api, JSONObject paramJson) {
		if (paramJson.size() > 0) {
			Builder builder = com.jerryshao.apinet.settings.Settings.settingsBuilder().put(api.getSettings());
			for (Object field : paramJson.keySet()) {
				String value = paramJson.getString(field.toString());
				if (value.indexOf(",") > 0) {
					builder.putArray(field.toString(), value.split(","));
				} else {
					builder.put(field.toString(), value);
				}
			}
			
			api.resetSettings(builder.build());
		}
		
		return new JSONObject();
	}
	
	@Override
	protected void doStart() throws ApiException {	}

	@Override
	protected void doClose() throws ApiException {	}

	@Override
	public void checkRequstParam(JSONObject paramJson, ApiOperateAction apiOperateAction) throws IllegalApiArugmentException {
		if (ApiOperateAction.GET != apiOperateAction && ApiOperateAction.MODIFY != apiOperateAction && ApiOperateAction.CREATE != apiOperateAction) {
			throw new IllegalApiOperateActionException(this, apiOperateAction);
		}
		if (! paramJson.has(OPERATE_API_NAME_ATTR)) {
			throw new IllegalApiArugmentException("have no this api name");
		}
	}

}
