package com.jerryshao.apinet.api;

import com.jerryshao.apinet.settings.Settings;
import com.jerryshao.apinet.settings.Settings.Builder;

/**
 * Api调用响应结果类
 */
public class ApiSettings {
	private ApiSettings() {	}
	
	private static Settings ALL_API_COMMON_SETTINGS = null;
	
	static {
		Builder settingsBuilder = Settings.settingsBuilder().loadFromClasspath("apis.settings.yml");
		ALL_API_COMMON_SETTINGS = settingsBuilder.build();
	}
	
	public static final Settings getSettings() {
		return ALL_API_COMMON_SETTINGS;
	}
}
