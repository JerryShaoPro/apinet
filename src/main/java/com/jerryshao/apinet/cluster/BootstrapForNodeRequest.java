package com.jerryshao.apinet.cluster;

import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.wintim.common.util.LogFactory;

/**
 * <code>BootstrapForNodeRequest</code>��ר�����ڴ���<code>Node</code>����Ľӿ�
 * ע����ʱֻ֧��ע��<br />
 *
 */
class BootstrapForNodeRequest {
	final static private Logger LOG = LogFactory.getLogger(BootstrapForNodeRequest.class);
	
	/**
	 * ������Կ���ʹ��ClusterSettings������
	 */
	final static private int DEFAULT_BOOTSTRAP_BOSS_NUM = 1;
	final static private int DEFAULT_BOOTSTRAP_WORKER_NUM = 5;
	
	private ServerBootstrap serverBootstrap;
	final private ChannelGroup channelGroup;
	private boolean isStarted = false;
	
	final private NodeRequestPipelineFactory nodeRequestPipelineFactory;
	
	public BootstrapForNodeRequest(ClusterState clusterState) {
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		channelGroup = new DefaultChannelGroup("groupForCommand");
		nodeRequestPipelineFactory = new NodeRequestPipelineFactory(clusterState, channelGroup);
	}
	
	void start() {
		if (! isStarted) {
			LOG.info("bootstrap for node request is starting...");
			serverBootstrap = buildBootstrap();
			serverBootstrap.bind();
			LOG.info("bootstrap for node request started!");
		}
	}
	
	void stop() {
		if (isStarted) {
			LOG.info("bootstrap for node request is stopping...");
			channelGroup.close().awaitUninterruptibly();
			serverBootstrap.releaseExternalResources();
			LOG.info("bootstrap for node request stopped!");
		}
	}
	
	private ServerBootstrap buildBootstrap() {
		//����serverbootstrap
		ServerBootstrap bootstrap =  new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), DEFAULT_BOOTSTRAP_BOSS_NUM,
                Executors.newCachedThreadPool(), DEFAULT_BOOTSTRAP_WORKER_NUM));
		bootstrap.setPipelineFactory(nodeRequestPipelineFactory);
		//���û�������
		bootstrap.setOption("tcpNoDelay", false);
		bootstrap.setOption("localAddress", ClusterSettings.getClusterRegisterAddress());
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("soLinger", 1);
		return bootstrap;
	}
}
