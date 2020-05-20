/**
 * Copyright    : Copyright (c) 2006. Wintim Corp. All rights reserved
 * File Summary : 
 * Create time  : 2013-5-28
 */
package com.jerryshao.apinet.cluster.command;

import java.util.UUID;

import com.jerryshao.apinet.cluster.Task;

import net.sf.json.JSONObject;

/**
 * <code>ClusterCommandTask</code>是用于描述集群中节点命令通信的Task类型.
 * 一般包括以下类型：<br />
 * <li>注册请求</li>
 * <li>注销请求（未实现）</li>
 */
public class ClusterCommandTask extends Task {
	//Task参数中指明是否为commmand类型的域信息
	final static public String COMMAND_TASK_FLAG_ATTR = "is.command";
	final static public String COMMAND_NAME_ATTR = "command.name";
	
	final private String commandName;
	
	public ClusterCommandTask(String commandName, JSONObject param, long startTime) {
		this(generateId(param), commandName, param, startTime);
	}
	
	public ClusterCommandTask(String id, String commandName, JSONObject param, long startTime) {
		super(id, param, startTime);
		if (commandName == null) {
			throw new NullPointerException("command name");
		}
		this.commandName = commandName;
	}
	
	String getCommandName() {
		return this.commandName;
	}
	
	JSONObject getCommandParam() {
		return param;
	}
	
	static private String generateId(JSONObject param) {
		assert (param != null);
		String id = param.toString() + UUID.randomUUID().toString();
		return id;
	}
}
