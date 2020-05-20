package com.jerryshao.apinet.cluster;

import com.jerryshao.apinet.cluster.common.NodeAddress;

/**
 * ��Ⱥ��һ���ڵ������
 * 
 */
public abstract class Node {
	protected NodeAddress nodeAddress;
	
	public Node(NodeAddress nodeAddress) {
		if (null == nodeAddress) {
			throw new NullPointerException("node address");
		}
		
		this.nodeAddress = nodeAddress;
	}
	
	public NodeAddress getAddress() {
		return nodeAddress;
	}
	
	boolean isMaster() {
		return false;
	}
	
	boolean isLocal() {
		return false;
	}
	
	public String getId() {
		return nodeAddress.hashStr();
	}
	
	public abstract TaskResult execute(Task task) throws Exception;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node node = (Node) obj;
			return node.getAddress().equals(nodeAddress);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return String.format("host:%s,port:%d,path:%s", nodeAddress.getHost(), nodeAddress.getPort(), nodeAddress.getPath()).hashCode();
	}
}
