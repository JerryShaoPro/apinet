
package com.jerryshao.apinet.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jerryshao.apinet.api.Api;
import com.jerryshao.apinet.api.DuplicateApiKeyException;
import com.wintim.common.util.LogFactory;

/**
 * 进行{@link Api}的管理类（线程安全），包括以下操作：
 * <ul>
 * <li>{@link #register(Api) 注册一个<i>Api</i>}</li>
 * <li>{@link #get(String) 通过<i>Api</i>名称获取一个<i>Api</i>}</li>
 * </ul>
 * <br>
 * 该管理类会在启动时自动注册所有系统中已经实现的<i>Api</i>
 *
 */
public class ApiManager {

	private static final Logger LOG = LogFactory.getLogger(ApiManager.class);

	//TODO 自动在内存紧张的时候回收处于CLOSED状态的Api
	private static final Map<String, Api> name2api = new HashMap<String, Api>();

	private static final ApiManager SINGLETON = new ApiManager();

	public static ApiManager get() {
		return SINGLETON;
	}

	private ApiManager() {	}

	boolean contains(String apiName) {
		synchronized (name2api) {
			return name2api.containsKey(apiName);
		}
	}

	public Api get(String apiName) {
		synchronized (name2api) {
			return name2api.get(apiName);
		}
	}

	public void register(Api api) {
		register(api, false);
	}

	public void register(Api api, boolean isOverWrite) {
		if (null == api) {
			throw new IllegalArgumentException("The api can not be null");
		}

		if (! isOverWrite) {
			synchronized (name2api) {
				if (name2api.containsKey(api.getApiName())) {
					throw new DuplicateApiKeyException(api.getApiName());
				}
			}
		}

		LOG.info(String.format("Register api:'%s' as '%s'", api.getClass().getName(), api.getApiName()));

		synchronized (name2api) {
			name2api.put(api.getApiName(), api);
		}
	}

	void unRegister(Api api) {
		if (null == api) {
			return;
		}

		synchronized (name2api) {
			if (name2api.containsKey(api.getApiName())) {
				name2api.remove(api.getApiName());
			}
		}
	}

	void unRegister(String apiName) {
		if (null == apiName) {
			return;
		}

		synchronized (name2api) {
			if (name2api.containsKey(apiName)) {
				name2api.remove(apiName);
			}
		}
	}
}
