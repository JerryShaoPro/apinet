package com.jerryshao.apinet.cluster;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.jerryshao.apinet.settings.Settings;
import com.jerryshao.apinet.settings.Settings.Builder;

public class ClusterSettings {
	final static private String CLUSTER_SETTINGS_FILE = "cluster.settings.yml";
	final static private Settings SETTINGS;
	
	static {
		Builder settingsBuilder = Settings.settingsBuilder().loadFromClasspath(CLUSTER_SETTINGS_FILE);
		SETTINGS = settingsBuilder.build();
	}
		
	private ClusterSettings() {}
	
	//for test
	static private String clusterAddressHost = null;
	static private int clusterAddressPort;
	final static public void setClusterAddress(String host, int port) {
		if (host == null) {
			throw new NullPointerException("host");
		}
		clusterAddressHost = host;
		clusterAddressPort = port;
	}
	
	//for test
	static private String clusterRegisterHost = null;
	static private int clusterRegisterPort;
	final static public void setClusterRegisterAddress(String host, int port) {
		if (host == null) {
			throw new NullPointerException("host");
		}
		clusterRegisterHost = host;
		clusterRegisterPort = port;
	}
	
	//for test
	static private String clusterDataExchangeHost = null;
	static private int clusterDataExchangePort;
	final static public void setClusterDataExchangeAddress(String host, int port) {
		if (host == null) {
			throw new NullPointerException("host");
		}
		clusterDataExchangeHost = host;
		clusterDataExchangePort = port;
	}
	
	/**
	 * ��ȡ�����û���cluster�ĵ�ַ
	 * @return
	 */
	final static public InetSocketAddress getClusterAddress() {
		if (clusterAddressHost == null) {
			clusterAddressHost = SETTINGS.get(PropertyNames.CLUSTER_HOST, "127.0.0.1");
			clusterAddressPort = SETTINGS.getAsInt(PropertyNames.CLUSTER_PORT, 6936);
		} 
		return new InetSocketAddress(clusterAddressHost, clusterAddressPort);
	}
	
	/**
	 * ��ȡ����node��ע��ĵ�ַ
	 * @return
	 */
	final static public InetSocketAddress getClusterRegisterAddress() {
		if (clusterRegisterHost == null) {
			clusterRegisterHost = SETTINGS.get(PropertyNames.CLUSTER_REGISTER_HOST, "127.0.0.1");
			clusterRegisterPort = SETTINGS.getAsInt(PropertyNames.CLUSTER_REGISTER_PORT, 3344);
		}
		return new InetSocketAddress(clusterRegisterHost, clusterRegisterPort);
	}
	
	/**
	 * ��ȡ����node�����ݽ����ĵ�ַ
	 * @return
	 */
	final static public InetSocketAddress getClusterDataExchangeAddress() {
		if (clusterDataExchangeHost == null) {
			clusterDataExchangeHost = SETTINGS.get(PropertyNames.CLUSTER_DATA_EXCHANGE_HOST, "127.0.0.1");
			clusterDataExchangePort = SETTINGS.getAsInt(PropertyNames.CLUSTER_DATA_EXCHANGE_PORT, 3115);
		}
		return new InetSocketAddress(clusterDataExchangeHost, clusterDataExchangePort);
	}
	
	final static private int DEFAULT_CLUSTER_WORKERS_COUNT = 512;
	final static public int getClusterWorkersCount() {
		return SETTINGS.getAsInt(PropertyNames.CLUSTER_WORKERS_COUNT, DEFAULT_CLUSTER_WORKERS_COUNT);
	}
	
	final static private int DEFAULT_CLUSTER_TASK_EXECUTORS_SIZE = Runtime.getRuntime().availableProcessors() * 2;;
	final static public int getClusterTaskExecutorsSize() {
		return SETTINGS.getAsInt(PropertyNames.CLUSTER_TASK_EXECUTORS_SIZE, DEFAULT_CLUSTER_TASK_EXECUTORS_SIZE);
	}
	
	final static private long DEFAULT_CLUSTER_CHANNEL_MAX_MEMORY_BYTE_SIZE = Runtime.getRuntime().totalMemory();
	final static public long getClusterChannelMaxMemoryByteSize() {
		return SETTINGS.getAsLong(PropertyNames.CLUSTER_CHANNEL_MAX_MEMORY_BYTE_SIZE, DEFAULT_CLUSTER_CHANNEL_MAX_MEMORY_BYTE_SIZE);
	}
	
	final static private long DEFAULT_CLUSTER_TOTAL_MEMORY_BYTE_SIEZE = Runtime.getRuntime().totalMemory();
	final static public long getClusterTotalMemoryByteSize() {
		return SETTINGS.getAsLong(PropertyNames.CLUSTER_TOTAL_MEMORY_BYTE_SIZE, DEFAULT_CLUSTER_TOTAL_MEMORY_BYTE_SIEZE);
	}
	
	final static public Charset getClusterChannelCharset() {
		String charsetName = SETTINGS.get(PropertyNames.CLUSTER_CHANNEL_CHARSET, "utf-8");
		return Charset.forName(charsetName);
	}
	
	final static private String[] EMPTY_STRING_ARRAY = new String[]{};
	final static public String[] getCustomerizedOperators() {
		return SETTINGS.getAsArray(PropertyNames.CLUSTER_CUSTOMERIZED_OPERATORS, EMPTY_STRING_ARRAY);
	}
	
	final static private long DEFAULT_CLUSTER_TIMEOUT_MS = 5000;
	final static public long getClusterTimeOut() {
		return SETTINGS.getAsLong(PropertyNames.CLUSTER_EXECUTE_TIMEOUT_MS, DEFAULT_CLUSTER_TIMEOUT_MS);
	}
	
	abstract interface PropertyNames {
		//�����û��������ַ
		final static String CLUSTER_HOST = "cluster.host";
		final static String CLUSTER_PORT = "cluster.port";
		
		//����Node��ע��ĵ�ַ
		final static String CLUSTER_REGISTER_HOST = "cluster.register.host";
		final static String CLUSTER_REGISTER_PORT = "cluster.register.port";
		
		//����node��task��result���ݽ����ĵ�ַ
		final static String CLUSTER_DATA_EXCHANGE_HOST = "cluster.data.exchange.host";
		final static String CLUSTER_DATA_EXCHANGE_PORT = "cluster.data.exchange.port";

		final static String CLUSTER_CHANNEL_CHARSET = "cluster.channel.charset";
		final static String CLUSTER_WORKERS_COUNT = "cluster.workers.count";
		final static String CLUSTER_TASK_EXECUTORS_SIZE = "cluster.task.executors.size";
		final static String CLUSTER_CHANNEL_MAX_MEMORY_BYTE_SIZE = "cluster.channel.max.memory.byte.size";
		final static String CLUSTER_TOTAL_MEMORY_BYTE_SIZE = "cluster.total.memory.byte.size";
		final static String CLUSTER_CUSTOMERIZED_OPERATORS = "cluster.customerized.operators";
		final static String CLUSTER_EXECUTE_TIMEOUT_MS = "cluster.execute.timeout.ms";
	}
}
