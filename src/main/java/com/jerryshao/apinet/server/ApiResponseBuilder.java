package com.jerryshao.apinet.server;

import java.util.HashSet;
import java.util.Set;

import com.jerryshao.apinet.api.Api;
import com.jerryshao.apinet.api.IApiResponsePropertyNameProcessor;
import com.jerryshao.apinet.util.Strings;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Api处理结果数据的构建，需要进行的处理包括:<br>
 * <pre>
 * 1. 用Api所配置的域名处理器对处理结果中的域名进行处理
 * 2. 根据Api配置所返回的域名对处理结果进行过滤
 *
 * 嵌套的域名用.进行连接，比如对于如下的处理结果：
 * {
 * 	"code":200,
 *  "user":{
 *  	"name":"test"
 *  	"status":{
 *  		"id":1
 *  	}
 *  	...
 *  }
 * }
 * 如果需要返回user中的name字段，那么配置的处理结果返回字段信息中需包括"user.name",
 * 同理，status中的id字段对应user.status.id, 如果需要user中的所有字段，只需
 * 配置user即可
 * </pre>
 * @author chuter
 *
 */
public class ApiResponseBuilder {

	private ApiResponseBuilder() {	}

	public static JSONObject build(JSONObject rawResultJson, Api api) {
		//首先进行需返回的Json的域的过滤
		JSONObject retJsonObject;
		Set<String> retFieldsSet = buildApiResponseFieldsSet(api);
		if (retFieldsSet.isEmpty()) {
			retJsonObject = rawResultJson;
		} else {
			retJsonObject = new JSONObject();
			filterRetJsonFields(retFieldsSet, retJsonObject, rawResultJson, "");
		}

		//然后进行返回结果中的域名处理
		return processJsonPropertyName(retJsonObject, api.getResponsePropertyNameProcessor());
	}

	private static final Set<String> EMPTY_SET = new HashSet<String>();
	private static Set<String> buildApiResponseFieldsSet(Api api) {
		if (api.getResponseFields().length == 0) {
			return EMPTY_SET;
		}

		Set<String> fieldsSet = new HashSet<String>();
		for (String field : api.getResponseFields()) {
			fieldsSet.add(Strings.toUnderscoreCase(field));
		}
		return fieldsSet;
	}

	/**
	 * 对返回的Json结果中的域名称进行处理
	 * @param origJsonObject 返回的Json数据
	 * @param processor 域名称处理器
	 * @return 对域名称进行处理之后的Json数据
	 */
	private static JSONObject processJsonPropertyName(JSONObject origJsonObject, IApiResponsePropertyNameProcessor processor) {
		if (null == processor) {
			return origJsonObject;
		}

		JSONObject processedJsonObject = new JSONObject();
		for (Object key : origJsonObject.keySet()) {
			if (key instanceof String) {
				Object value = origJsonObject.get(key);
				if (value instanceof JSONObject) {
					processedJsonObject.put(processor.process((String)key), processJsonPropertyName((JSONObject)origJsonObject.get(key), processor));
				} else if (value instanceof JSONArray) {
					JSONArray newJsonArray = new JSONArray();
					for (Object item : (JSONArray)value) {
						if (item instanceof JSONObject) {
							item = processJsonPropertyName((JSONObject)item, processor);
						}
						newJsonArray.add(item);
					}
					processedJsonObject.put(processor.process((String)key), newJsonArray);
				} else { //JSONArray
					processedJsonObject.put(processor.process((String)key), origJsonObject.get(key));
				}
			} else {
				processedJsonObject.put(key, origJsonObject.get(key));
			}
		}

		return processedJsonObject;
	}

	/**
	 * 递归地对<i>JSONObject</i>中的域进行过滤，过滤出返回结果中需要包括的域
	 *
	 * @param retFieldsSet 配置的需要返回的域名称集合
	 * @param retJsonObject 最终返回的Json结果
	 * @param rawJsonObject 过滤前的Json结果
	 * @param fieldNamePrefix Json中域名的前缀(嵌套域名称之间用.进行连接), 类比java类名和其源文件所在目录的关系
	 */
	private static void filterRetJsonFields(Set<String> retFieldsSet, JSONObject retJsonObject, JSONObject rawJsonObject, String fieldNamePrefix) {
		for (Object key : rawJsonObject.keySet()) {
			if (key instanceof String) {
				Object value = rawJsonObject.get(key);

				String nextfieldNamePrefix;
				if (fieldNamePrefix.length() == 0) {
					nextfieldNamePrefix = Strings.toUnderscoreCase((String)key);
				} else {
					nextfieldNamePrefix = String.format("%s.%s", fieldNamePrefix, Strings.toUnderscoreCase((String)key));
				}

				if (retFieldsSet.contains(nextfieldNamePrefix)) {
					retJsonObject.put(key, value);
				} else if (shouldSelectCauseToSubfields(retFieldsSet, nextfieldNamePrefix)) {
					if (value instanceof JSONObject) {
						JSONObject subJsonObject = new JSONObject();
						filterRetJsonFields(retFieldsSet, subJsonObject, (JSONObject) value, nextfieldNamePrefix);
						retJsonObject.put(key, subJsonObject);
					} else if (value instanceof JSONArray) {
						JSONArray subJsonArray = new JSONArray();
						filterRetJsonFields(retFieldsSet, subJsonArray, (JSONArray) value, nextfieldNamePrefix);
						retJsonObject.put(key, subJsonArray);
					}
				}
			} else {
				retJsonObject.put(key, rawJsonObject.get(key));
			}
		}
	}

	/**
	 * 递归地对<i>JSONArray</i>中的域进行过滤
	 */
	private static void filterRetJsonFields(Set<String> retFieldsSet, JSONArray retJsonArray, JSONArray rawJsonArray, String fieldNamePrefix) {
		for (Object item : rawJsonArray) {
			if (item instanceof JSONObject) {
				JSONObject subJsonObject = new JSONObject();
				filterRetJsonFields(retFieldsSet, subJsonObject, (JSONObject)item, fieldNamePrefix);
				if (! subJsonObject.isEmpty()) {
					retJsonArray.add(subJsonObject);
				}
			} else {
				retJsonArray.add(item);
			}
		}
	}

	/**
	 * 是否由于其子域被选择而导致一个域被选择，例如对于如下的结果：<br>
	 * <pre>
	 * {
	 * "user":{
	 * 	"name":"chuter"
	 * 	...
	 * }
	 * }
	 * </pre>
	 * 配置需要返回的字段信息中包括了user.name，但是没包括user，这种情况下由于user的子域
	 * 被选择，因此也需要选择user域
	 *
	 * @param retFieldsSet 需要返回的域信息
	 * @param prefix 需要检查的域的前缀
	 * @param fieldName 待检查的域名称
	 * @return 是否在返回结果中需要包含该域
	 */
	private static boolean shouldSelectCauseToSubfields(Set<String> retFieldsSet, String prefix) {
		String fieldPrefix = prefix + ".";
		for (String retField : retFieldsSet) {
			if (retField.startsWith(fieldPrefix)) {
				return true;
			}
		}

		return false;
	}
	
}
