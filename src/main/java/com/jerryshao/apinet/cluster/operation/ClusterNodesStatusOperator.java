package com.jerryshao.apinet.cluster.operation;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.NodeWeight;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.constant.ResultCode;

/**
 * ���ڲ�ѯ����Nodes��ʵʱ�����operator
 * ����������Ϣ:<br />
 * <li>host</li>
 * <li>port</li>
 * <li>path</li>
 * <li>executing_task_number</li>
 * <li>finished_task_number</li>
 * <li>success_task_number</li>
 * <li>system_error_task_number</li>
 * <li>system_error_task_number</li>
 * <li>cluster_error_task_number</li>
 * <li>average_time_cost</li>
 *
 */
class ClusterNodesStatusOperator implements IClusterOperator {
	final static private String OPERATION_NAME = "nodes_status";
	
	public ClusterNodesStatusOperator() {}
	
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
		TaskResult executeResult = new TaskResult(operationTask.getId(), ResultCode.SUCCESS, buildNodesStatus(clusterState));
		return executeResult;
	}
	
	private JSONObject buildNodesStatus(ClusterState clusterState) {
		assert (clusterState != null);
		
		JSONObject jsonObject = new JSONObject();
		List<ProxyNode> activeNodes = clusterState.getActiveNodes();
		JSONArray jsonArray = new JSONArray();
		for (ProxyNode proxyNode : activeNodes) {
			JSONObject json = buildNodeInfo(proxyNode, proxyNode.getNodeWeight());
			jsonArray.add(json);
		}
		
		jsonObject.put("nodes_status", jsonArray);
		return jsonObject;
	}
	
	private JSONObject buildNodeInfo(ProxyNode proxyNode, NodeWeight weight) {
		JSONObject json = new JSONObject();
		json.put("host", proxyNode.getAddress().getHost());
		json.put("port", proxyNode.getAddress().getPort());
		json.put("path", proxyNode.getAddress().getPath());
		json.put("executing_task_number", weight.getTaskInProgress());
		json.put("finished_task_number", weight.getTaskHasFinished());
		json.put("success_task_number", weight.getSuccessNumber());
		json.put("system_error_task_number", weight.getSystemErrorNumber());
		json.put("cluster_error_task_number", weight.getClusterErrorNumber());
		json.put("average_time_cost", weight.getAverageTimeCost());
		
		return json;
	}
}
