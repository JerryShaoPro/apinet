package com.jerryshao.apinet.cluster.constant;

/**
 *
 * 用于{@link com.jerryshao.apinet.cluster.local.RegisterChannel}中.
 * 描述注册时，传递给{@link com.jerryshaoa.apinet.cluster.Cluster}<br />
 * 包括如下元素<br />
 * <li>NODE_HOST, host，即node的主机地址</li>
 * <li>NODE_PORT, port, 即node的主机端口</li>
 * <li>NODE_PATH, path, 即node的路径</li>
 * <li>CLUSTER_ACK, ack, 即cluster返回注册是的回复</li>
 *
 */
public enum RegisterCommunicatePropertyNames {
	NODE_HOST("host"),
	NODE_PORT("port"),
	NODE_PATH("path"),
	CLUSTER_ACK("ack");
	
	private String content;
	private RegisterCommunicatePropertyNames(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
}
