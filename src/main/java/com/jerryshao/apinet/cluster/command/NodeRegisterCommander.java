/**
 * Copyright    : Copyright (c) 2006. Wintim Corp. All rights reserved
 * File Summary : 
 * Create time  : 2013-5-28
 */
package com.jerryshao.apinet.cluster.command;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.jerryshao.apinet.cluster.ClusterSettings;
import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.NodeGroup;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.common.NodeAddress;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.wintim.common.util.LogFactory;

/**
 * <code>NodeRegisterCommander</code>是专门用于处理注册请求的Commander
 * 注册的流程<br />
 * <li>获得注册信息</li>
 * <li>查询可否注册</li>
 * <li>创建ProxyNode，并进行一些处理</li>
 * <li>发送成功回复</li>
 */
public class NodeRegisterCommander implements IClusterCommander {
	final static private Logger LOG = LogFactory.getLogger(NodeRegisterCommander.class);

	final static public String COMMAND_NAME = "register_command";
	final static public String REGISTER_HOST = "register_host";
	final static public String REGISTER_PORT = "register_port";
	final static public String REGISTER_PATH = "register_path";
	final static public String REGISTER_GROUP = "register_group";

	final static public String PROXY_HOST = "proxy_host";
	final static public String PROXY_PORT = "proxy_port";

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public TaskResult operateCommand(ClusterCommandTask task,
									 ClusterState clusterState) {
		if (task == null) {
			throw new NullPointerException("task");
		}
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		//1.获得注册信息
		JSONObject param = task.getParamJson();
		String host = param.getString(REGISTER_HOST);
		int port = param.getInt(REGISTER_PORT);
		String path = param.getString(REGISTER_PATH);
		NodeGroup group = NodeGroup.valueOf(param.getString(REGISTER_GROUP));

		//2.进行注册
		NodeAddress nodeAddress = new NodeAddress(host, port, path);
		boolean isSuccess = clusterState.addToUnAuthen(nodeAddress, group);

		TaskResult result = null;
		//3. 返回注册的结果
		if (isSuccess) {
			LOG.info(nodeAddress + ", register successfully!");
			JSONObject json = new JSONObject();
			json.put(PROXY_HOST, ClusterSettings.getClusterDataExchangeAddress().getAddress().getHostAddress());
			json.put(PROXY_PORT, ClusterSettings.getClusterDataExchangeAddress().getPort());
			result = new TaskResult(task.getId(), ResultCode.SUCCESS, json);
		} else {
			LOG.info(nodeAddress + ", register failed!");
			result = new TaskResult(task.getId(), ResultCode.SYSTEM_ERROR_NOT_NEED_RETRY);
		}

		return result;
	}
}
