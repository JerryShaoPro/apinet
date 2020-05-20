package com.jerryshao.apinet.cluster.operation;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.TaskResult;

public interface IClusterOperator {
	public String getOperationName();
	public TaskResult operate(ClusterOperationTask operationTask, ClusterState clusterState);
}
