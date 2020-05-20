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
 * <code>BootstrapForUserRequest</code>������<code>cluster</code>���û����н����Ľӿ�<br />
 * 
 */
class BootstrapForUserRequest {
	/**
	 * TODO
	 * ��Щ����������BootstrapForUser��ʹ�õĳ��������ʹ��ClusterSettingȥ��������õģ������Ҳ���ô��<br />
	 * �Ⱥ����ٿ�һ��
	 */
	final static private int DEFAULT_BOOTSTRAP_BOSS_NUM = 1;
	
	final static private Logger LOG = LogFactory.getLogger(BootstrapForUserRequest.class);
		
	private ServerBootstrap serverBootstrap;
	private boolean isStarted = false;
	//����pipelineFactory
	final private UserRequestPipelineFactory userRequestPipelineFactory;
	final private ChannelGroup channelGroup;
	
	BootstrapForUserRequest(ExecutionHandler executionHandler, ClusterState clusterState, MasterNode masterNode) {
		if (executionHandler == null) {
			throw new NullPointerException("executionHandler");
		}
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		if (masterNode == null) {
			throw new NullPointerException("masterNode");
		}
		channelGroup = new DefaultChannelGroup("groupForUser");
		userRequestPipelineFactory = new UserRequestPipelineFactory(channelGroup, executionHandler, clusterState, masterNode);
	}
	
	void start() {
		if (! isStarted) {
			//1. ��ʼ��bootstrap
			serverBootstrap = buildBootstrap();
			//2. ����bootstrap
			LOG.info("starting clusterBootstrap(serve for users)...");
			serverBootstrap.bind();
			LOG.info("clusterBootstrap(serve for users) started!");
			isStarted = true;
		}
	}

	void stop() {
		if (isStarted) {
			LOG.info("stopping clusterBootstrap(serve for users)...");
			channelGroup.close().awaitUninterruptibly();
			serverBootstrap.releaseExternalResources();
			LOG.info("clusterBootstrap(serve for users) is stopped!");
			isStarted = false;
		}
	}
	
	/**
	 * ����Bootstrap
	 * @return
	 */
	private ServerBootstrap buildBootstrap() {
		//����serverbootstrap
		ServerBootstrap bootstrap =  new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), DEFAULT_BOOTSTRAP_BOSS_NUM,
                Executors.newCachedThreadPool(), ClusterSettings.getClusterWorkersCount()));
		bootstrap.setPipelineFactory(userRequestPipelineFactory);
		//���û�������
		bootstrap.setOption("tcpNoDelay", false);
		bootstrap.setOption("localAddress", ClusterSettings.getClusterAddress());
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("soLinger", 1);
		return bootstrap;
	}
}
