package com.jerryshao.apinet.cluster.operation;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.Node;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.constant.ResultCode;

/**
 * {@link com.jerryshao.apinet.cluster.Cluster}״̬�ſ�����άʵ��<br>
 * ��ʵ����ֻ֧�ֻjerryshao.apinetoom.apiserver.cluster.Cluster}�ĸſ���Ϣ<br>
 * ����������Ϣ:<br>
 * <ul>
 * <li>��ֹ��ǰ<i>Cluster</i>���ܹ�������<i>Task</i>����</li>
 * <li>������Ч<i>Node</i>��Ϣ</li>
 * <li>������ʧЧ<i>Node</i>��Ϣ</li>
 * </ul>
 * 
 * <b>����ÿ��<i>Node</i>��Ϣ��ֻ������<i>Node</i>�ĵ�ַ��Ϣ</b>
 * 
 */
class ClusterSimpleStatusOperator implements IClusterOperator {
	final static private String OPERATION_NAME = "simple_status";
	
	public String getOperationName() {
		return OPERATION_NAME;
	}

	public TaskResult operate(ClusterOperationTask operationTask,
			ClusterState clusterState) {
		if (null == operationTask || null == clusterState) {
			throw new NullPointerException("operation task or cluster");
		}
		
		if (operationTask.getOperateType() != OperateType.GET) {
			throw new IllegalArgumentException(getOperationName() + " ֻ֧�ֻ�ȡ����");
		}
		
		TaskResult executeResult = new TaskResult(operationTask.getId(),ResultCode.SUCCESS, buildCluterSimpleStatusInfo(clusterState));
		return executeResult;
	}

	private JSONObject buildCluterSimpleStatusInfo(ClusterState clusterState) {
		assert (clusterState != null);
		JSONObject statusInfoJson = new JSONObject();
		statusInfoJson.put("executing_tasks_count", clusterState.getExecutingTasksCount());
		statusInfoJson.put("isactive_nodes", buildNodesInfo(clusterState));
		
		return statusInfoJson;
	}
	
	private JSONArray buildNodesInfo(ClusterState clusterState) {
		assert (clusterState != null);
		JSONArray nodesInfoArray = new JSONArray();
		
		List<ProxyNode> activeNodes = clusterState.getActiveNodes();
		List<ProxyNode> inactiveNodes = clusterState.getInactiveNodes();
		if (activeNodes != null) {
			for (Node node : activeNodes) {
				nodesInfoArray.add(buildNodeInfo(node, true));
			}
		}
		
		if (inactiveNodes != null) {
			for (Node node : inactiveNodes) {
				nodesInfoArray.add(buildNodeInfo(node, false));
			}
		}
		
		return nodesInfoArray;
	}
	
	private JSONObject buildNodeInfo(Node node, boolean isActive) {
		assert (node != null);
		
		JSONObject nodeInfo = new JSONObject();
		nodeInfo.put("host", node.getAddress().getHost());
		nodeInfo.put("port", node.getAddress().getPort());
		nodeInfo.put("path", node.getAddress().getPath());
		nodeInfo.put("is_active", isActive ? 1 : 0);
		return nodeInfo;
	}
}
