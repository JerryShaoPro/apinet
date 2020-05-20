package com.jerryshao.apinet.cluster.local;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.jerryshao.apinet.cluster.Node;
import com.jerryshao.apinet.cluster.common.NodeAddress;
import com.jerryshao.apinet.settings.Settings;
import com.jerryshao.apinet.settings.Settings.Builder;

/**
 * {@link Node}的配置信息
 */
public class NodeSettings {
	final static private String NODE_SETTINGS_FILE = "node.settings.yml";
	final static private Settings SETTINGS;
	
	static {
		Builder settingsBuilder = Settings.settingsBuilder().loadFromClasspath(NODE_SETTINGS_FILE);
		SETTINGS = settingsBuilder.build();
	}
		
	private NodeSettings() {}
	final static InetSocketAddress getLocalNodeInetAddress() {
		return new InetSocketAddress(localNodeAddress.getHost(), localNodeAddress.getPort());
	}
	
	static private NodeAddress localNodeAddress = null;
	final static NodeAddress getLocalNodeAddress() {
		if (localNodeAddress == null) {
			localNodeAddress = new NodeAddress(getLocalNodeHOST(), getLocalNodePort());
		}
		return localNodeAddress;
	}
	
	//for test
	final static public void setLocalNodeAddress(String host, int port) {
		if (host == null) {
			throw new NullPointerException("host");
		}
		localNodeAddress = new NodeAddress(host, port);
	}
	
	final static private int DEFAULT_LOCALNODE_MAX_RECONNECT_CLUSTER_TIMES = 10;
	final static int getLocalnodeMaxReconnectClusterTimes() {
		return SETTINGS.getAsInt(
				PropertyNames.MAX_RECONNECT_CLUSTER_TIMES, 
				DEFAULT_LOCALNODE_MAX_RECONNECT_CLUSTER_TIMES
			);
	}
	
	final static private int DEFAULT_LOCAL_NODE_TASK_EXECUTORS_SIZE = Runtime.getRuntime().availableProcessors() * 2;;
	final static int getLocalNodeTaskExecutorsSize() {
		return SETTINGS.getAsInt(PropertyNames.LOCAL_NODE_TASK_EXECUTORS_SIZE, DEFAULT_LOCAL_NODE_TASK_EXECUTORS_SIZE);
	}
	
	final static private long DEFAULT_LOCAL_NODE_CHANNEL_MAX_MEMORY_BYTE_SIZE = Runtime.getRuntime().totalMemory();
	final static long getLocalNodeChannelMaxMemoryByteSize() {
		return SETTINGS.getAsLong(PropertyNames.LOCAL_NODE_CHANNEL_MAX_MEMORY_BYTE_SIZE, DEFAULT_LOCAL_NODE_CHANNEL_MAX_MEMORY_BYTE_SIZE);
	}
	
	final static private long DEFAULT_LOCAL_NODE_TOTAL_MEMORY_BYTE_SIEZE = Runtime.getRuntime().totalMemory();
	final static long getLocalNodeTotalMemoryByteSize() {
		return SETTINGS.getAsLong(PropertyNames.LOCAL_NODE_TOTAL_MEMORY_BYTE_SIZE, DEFAULT_LOCAL_NODE_TOTAL_MEMORY_BYTE_SIEZE);
	}
	
	final static Charset getLocalNodeChannelCharset() {
		String charsetName = SETTINGS.get(PropertyNames.LOCAL_NODE_CHANNEL_CHARSET, "utf-8");
		return Charset.forName(charsetName);
	}
	
	final static private int DEFAULT_LOCAL_NODE_WORKERS_COUNT = Runtime.getRuntime().availableProcessors() * 2;
	final static int getLocalNodeWorkersCount() {
		return SETTINGS.getAsInt(PropertyNames.LOCAL_NODE_WORKERS_COUNT, DEFAULT_LOCAL_NODE_WORKERS_COUNT);
	}
	
	final static private String DEFAULT_LOCAL_NODE_HOST = "192.168.1.16";
	final static private String getLocalNodeHOST() {
		return SETTINGS.get(PropertyNames.LOCAL_NODE_HOST, DEFAULT_LOCAL_NODE_HOST);
	}
	
	final static private int DEFAULT_LOCAL_NODE_PORT = 8889;
	final static private int getLocalNodePort() {
		return SETTINGS.getAsInt(PropertyNames.LOCAL_NODE_PORT, DEFAULT_LOCAL_NODE_PORT);
	}
	
	abstract interface PropertyNames {
		final static String LOCAL_NODE_TASK_EXECUTORS_SIZE = "local.node.task.executors.size";
		final static String LOCAL_NODE_CHANNEL_MAX_MEMORY_BYTE_SIZE = "local.node.channel.max.memory.byte.size";
		final static String LOCAL_NODE_TOTAL_MEMORY_BYTE_SIZE = "local.node.total.memory.byte.size";
		final static String LOCAL_NODE_CHANNEL_CHARSET = "local.node.channel.charset";
		final static String LOCAL_NODE_WORKERS_COUNT = "local.node.workers.count";
		final static String MAX_RECONNECT_CLUSTER_TIMES = "local.node.max.reconnect.cluster.times";
		
		final static String LOCAL_NODE_PORT = "local.node.port";
		final static String LOCAL_NODE_HOST = "local.node.host";
	}
}
