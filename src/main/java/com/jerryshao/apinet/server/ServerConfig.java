/**
 * Copyright    : Copyright (c) 2006. Wintim Corp. All rights reserved
 * File Summary : 
 * Create time  : 2012-8-2
 */
package com.jerryshao.apinet.server;

import com.jerryshao.apinet.settings.Settings.Builder;

public class ServerConfig {
	private static final String SERVER_SETTINGS_FILE_NAME = "server.settings.yml";
	private static final String serverPortConfName = "server.port";
	private static final String serverMonitorPortConfName = "server.monitor.port";
	private static final String customerizedApis = "server.customerized.apis";
	
	private static com.jerryshao.apinet.settings.Settings SERVER_SETTINGS = com.jerryshao.apinet.settings.Settings.EMPTY_SETTINGS;

	static {
		try {
			Builder settingsBuilder = com.jerryshao.apinet.settings.Settings.settingsBuilder().loadFromClasspath(SERVER_SETTINGS_FILE_NAME);
			SERVER_SETTINGS = settingsBuilder.build();
		} catch (Exception e) {
			System.err.println("Failed to parse the server settings. Please check it.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	static final int getServerPort() {
		return SERVER_SETTINGS.getAsInt(serverPortConfName, 8888);
	}
	
	public static final int getServerMonitorPort() {
		return SERVER_SETTINGS.getAsInt(serverMonitorPortConfName, 8889);
	}
	
	private static final String[] EMPTY_STRING_ARRAY = new String[]{};
	public static final String[] getCustomerizedApis() {
		return SERVER_SETTINGS.getAsArray(customerizedApis, EMPTY_STRING_ARRAY);
	}
	
	private ServerConfig() {	}
	
}
