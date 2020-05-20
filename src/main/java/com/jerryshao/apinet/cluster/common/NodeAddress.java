package com.jerryshao.apinet.cluster.common;

/**
 * {@link com.jerryshao.apinet.cluster.Node}的地址信息
 * 其中包含了一下信息:<br>
 * <dl>
 * <dt>host</dt>
 * <dd>节点的ip或域名</dd>
 *
 * <dt>port</dt>
 * <dd>节点监听的端口号信息</dd>
 *
 * <dt>path</dt>
 * <dd>节点的部署路径</dd>
 * </dl>
 */
public class NodeAddress {
	final static private String DEFAULT_PATH = System.getProperty("user.dir");
	final static private int DEFAULT_PORT = 31152;
	final static private String DEFAULT_HOST = "127.0.0.1";
	final static public NodeAddress FAKE_ADDRESS = new NodeAddress(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PATH);
	
	private String host;
	private int port;
	private String path;
	
	public NodeAddress(String host, int port) {
		this(host, port, DEFAULT_PATH);
	}
	
	public NodeAddress(String host, int port, String path) {
		if (null == host) {
			throw new NullPointerException("host");
		}

		this.host = host;
		this.port = port;
		this.path = (null == path) ? DEFAULT_PATH : path;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getPath() {
		return path;
	}
	
	public String hashStr() {
		return String.format("%s:%d/%s", host, port, path);
	}
	
	@Override
	public int hashCode() {
		return hashStr().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (null == o) {
			return false;
		}
		
		if (! (o instanceof NodeAddress)) {
			return false;
		}
		
		NodeAddress that = (NodeAddress) o;
		return this.hashCode() == that.hashCode();
	}
}
