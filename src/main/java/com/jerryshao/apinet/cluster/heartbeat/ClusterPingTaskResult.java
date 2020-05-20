package com.jerryshao.apinet.cluster.heartbeaat;

import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.TaskResult;

class ClusterPingTaskResult extends TaskResult {
	final static public String PING_TASK_RESULT_FLAG_ATTRI = "is.ping";
	
	ClusterPingTaskResult() {
		super(Task.FAKE_TASK_ID);
		result.put(PING_TASK_RESULT_FLAG_ATTRI, true);
	}
}
