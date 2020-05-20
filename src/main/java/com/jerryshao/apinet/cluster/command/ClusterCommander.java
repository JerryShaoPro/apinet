package com.jerryshao.apinet.cluster.command;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.TaskResult;

public interface ClusterCommander {
	/**
	 * 获得commander的名字
	 * @return
	 */
	public String getCommandName();

	/**
	 *  执行某一个command，并返回结果<br />
	 * @param task
	 * @param clusterState
	 * @return
	 */
	public TaskResult operateCommand(ClusterCommandTask task, ClusterState clusterState);
}
