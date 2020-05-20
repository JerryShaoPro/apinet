package com.jerryshao.apinet.cluster;

import net.sf.json.JSONObject;

import org.jboss.netty.channel.Channel;

import com.jerryshao.apinet.cluster.common.NodeAddress;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.jerryshao.apinet.cluster.exception.ConnectException;

/**
 * <code>ProxyNode</code>��Ϊ<code>LocalNode</code>��<code>Cluster</code>�˵�����Node<br />
 * ��LocalNode�����в���������ͨ��ProxyNode��ִ�У�ͬʱProxyNode����������LocalNode�йص���Ϣ��
 * <code>ProxyNode</code>��LocalNode�ľ���.
 * ProxyNode��������Ԫ��<br />
 * <li>Channel���ProxyNode��Ӧ��Channel,������Ϣ����ͨ�����Channel�����LocalNode��</li>
 * <li>NodeWeight��������Node��Ȩ����Ϣ(Ȩ����Ϣ�а���һЩͳ����Ϣ)</li>
 * <li>NodeGroup��������Node�����ڵ���</li>
 * <li>TaskResultFuture��������Node����ִ�������Future�б�</li>
 * 
 */
public class ProxyNode extends Node {
	private Channel channel;
	final private TaskResultFutureTable table;
	private NodeWeight nodeWeight;
	private NodeGroup group;
	
	public ProxyNode(NodeAddress nodeAddress) {
		super(nodeAddress); 
		table = new TaskResultFutureTable();
		nodeWeight = new NodeWeight();
	}
	
	/**
	 * �������е�taskΪ�����״̬�������ý��<br />
	 * �����Ӱ��Weight
	 */
	public void setDoneToAllTask(ResultCode resultCode, JSONObject result, String message) {
		if (resultCode == null) {
			throw new NullPointerException("result code");
		}
		if (result == null) {
			throw new NullPointerException("result");
		}
		if (message == null) {
			throw new NullPointerException("message");
		}
		
		table.clearAllTasks(resultCode, result, message);
	}
	
	/**
	 * ����taskId��Ӧ��task������ɣ�������result
	 * ������task�����ڣ��Ͳ����κδ���
	 * @param taskId
	 * @param result
	 */
	public void setDone(String taskId, TaskResult result) {
		if (taskId == null) {
			throw new NullPointerException("task ID");
		}
		if (result == null) {
			throw new NullPointerException("result");
		}
		updateNodeWeight(result);
		this.table.setDone(taskId, result);
	}
	
	@Override
	public TaskResultFuture execute(Task task) throws ConnectException {
		if (task == null) {
			throw new NullPointerException("task");
		}
		assert (channel != null);
		//����NodeWeight
		nodeWeight.increaseInProgress();
		
		channel.write(task);
		TaskResultFuture resultFuture = new TaskResultFuture(task.getId());
		table.putTaskResultFuture(task, resultFuture);
		return resultFuture;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public void setGroup(NodeGroup group) {
		if (group == null) {
			throw new NullPointerException("group");
		}
		this.group = group;
	}
	
	public NodeGroup getGroup() {
		return this.group;
	}
	
	public NodeWeight getNodeWeight() {
		return nodeWeight;
	}
	
	/**
	 * ����<code>ProxyNode</code>��Ȩ��
	 */
	public void resetNodeWeight() {
		this.nodeWeight = new NodeWeight();
	}
	
	/**
	 * ����Result�Ľ��������ProxyNode��Ȩ����Ϣ,������Task,�����ڣ��Ͳ����κ���
	 * @param result
	 */
	private void updateNodeWeight(TaskResult result) {
		assert (result != null);
		String taskId = result.getTaskId();
		
		switch (result.getCode()) {
		case SUCCESS:
			Task task = table.getTask(taskId);
			nodeWeight.increaseSuccess(System.currentTimeMillis() - task.getStartTime());
			break;
		case SYSTEM_ERROR_NEED_RETRY:
		case SYSTEM_ERROR_NOT_NEED_RETRY:
			nodeWeight.increaseSystemError();
			break;
		case CLUSTER_ERROR_NEED_RETRY:
		case CLUSTER_ERROR_NOT_NEED_RETRY:
			nodeWeight.increaseClusterError();
			break;
		default:
			break;
		}
	}
}
