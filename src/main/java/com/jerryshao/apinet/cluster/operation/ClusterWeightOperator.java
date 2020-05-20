package com.jerryshao.apinet.cluster.operation;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.constant.ResultCode;

class ClusterWeightOperator implements IClusterOperator {
	final static private String OPERATION_NAME = "weight_status";
	
	public String getOperationName() {
		return OPERATION_NAME;
	}

	public TaskResult operate(ClusterOperationTask operationTask,
			ClusterState clusterState) {
		if (null == operationTask || null == clusterState) {
			throw new NullPointerException("operation task or clusterState");
		}
		
		if (operationTask.getOperateType() != OperateType.GET) {
			throw new IllegalArgumentException(getOperationName() + " ֻ֧�ֻ�ȡ����");
		}

		TaskResult executeResult = new TaskResult(operationTask.getId(),ResultCode.SUCCESS, buildCluterWeightStatusInfo(clusterState));
		return executeResult;
	}

	private JSONObject buildCluterWeightStatusInfo(ClusterState clusterState) {
		JSONObject jsonObject = new JSONObject();
		
		assert (clusterState != null);
		JSONArray jsonArray = new JSONArray();
		
		List<ProxyNode> activeNodes = clusterState.getActiveNodes();
		for (ProxyNode proxyNode : activeNodes) {
			JSONObject json = new JSONObject();
			json.put("host", proxyNode.getAddress().getHost());
			json.put("port", proxyNode.getAddress().getPort());
			json.put("path", proxyNode.getAddress().getPath());
			json.put("weight", proxyNode.getNodeWeight().getWeight());
			jsonArray.add(json);
		}
		
		jsonObject.put("status", jsonArray);
		
		return jsonObject;
	}
}
