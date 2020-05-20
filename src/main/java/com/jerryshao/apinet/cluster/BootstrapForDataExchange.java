package com.jerryshao.apinet.cluster;

import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;

import com.wintim.common.util.LogFactory;

/**
 * <code>BootstrapForDataExchange</code>�����ڼ�Ⱥ�нڵ���<code>Cluster</code>��ͨ��<br />
 * ��Ҫ�����jerryshao.apinetom.apiserver.cluster.Task}�jerryshao.apinetom.apiserver.cluster.TaskResult}�Ľ���<br />
 * 
 */
class BootstrapForDataExchange {
	final static private Logger LOG = LogFactory.getLogger(BootstrapForDataExchange.class);

	//TODO ʹ��ͳһ���ù���ģ�飬����apinet��Ⱥ������(������Կ���ʹ��ClusterSettings)
	final static private int DEFAULT_BOOTSTRAP_BOSS_NUM = 1;
	final static private int DEFAULT_BOOTSTRAP_WORKER_NUM = 5;
	
	private ServerBootstrap serverBootstrap;
	private boolean isStarted = false;
	
	final private DataExchangePipelineFactory dataExchangePipelineFactory;
	final private ChannelGroup channelGroup;
	
	BootstrapForDataExchange(ClusterState clusterState, ExecutionHandler executionHandler) {
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		if (executionHandler == null) {
			throw new NullPointerException("executionHandler");
		}
		channelGroup = new DefaultChannelGroup("groupForTask");
		dataExchangePipelineFactory = new DataExchangePipelineFactory(channelGroup, clusterState, executionHandler);
	}
	
	void start() {
		if (! isStarted) {
			LOG.info("bootstrap for task is starting...");
			serverBootstrap = buildBootstrap();
			serverBootstrap.bind();
			LOG.info("bootstrap for task started!");
		}
	}
	
	/**
	 * stop֮�󣬿���start
	 */
	void stop() {
		if (isStarted) {
			LOG.info("bootstrap for task is stopping...");
			channelGroup.close().awaitUninterruptibly();
			serverBootstrap.releaseExternalResources();
			LOG.info("bootstrap for task stopped!");
		}
	}
	
	private ServerBootstrap buildBootstrap() {
		//����serverbootstrap
		ServerBootstrap bootstrap =  new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), DEFAULT_BOOTSTRAP_BOSS_NUM,
                Executors.newCachedThreadPool(), DEFAULT_BOOTSTRAP_WORKER_NUM));
		bootstrap.setPipelineFactory(dataExchangePipelineFactory);
		//���û�������
		bootstrap.setOption("tcpNoDelay", false);
		bootstrap.setOption("localAddress", ClusterSettings.getClusterDataExchangeAddress());
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("soLinger", 1);
		return bootstrap;
	}
}
