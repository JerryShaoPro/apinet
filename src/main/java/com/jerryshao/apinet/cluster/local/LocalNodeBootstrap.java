package com.jerryshao.apinet.cluster.local;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;

import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.common.NodeAddress;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.jerryshao.apinet.cluster.exception.ConnectException;
import com.jerryshao.apinet.cluster.executor.TaskExecutor;
import com.jerryshao.apinet.cluster.handler.AuthenticationHandler;
import com.wintim.common.util.LogFactory;

class LocalNodeBootstrap {
	final static private Logger LOG = LogFactory.getLogger(LocalNodeBootstrap.class);
	protected ClientBootstrap requestAcceptBossBootstrap;
	private boolean isStarted = false;
	private Channel channel;
	final private LocalPipelineFactory localPipelineFactory;
	private InetSocketAddress proxyAddress;
	private NodeAddress nodeAddress;
	
	LocalNodeBootstrap(ExecutionHandler executionHandler, ScheduledExecutorService scheduledExecutorService, LocalNode localNode, TaskExecutor taskExecutor) {
		localPipelineFactory = new LocalPipelineFactory(executionHandler, scheduledExecutorService, localNode, taskExecutor);
	}
	
	void setNodeAddress(NodeAddress nodeAddress) {
		if (nodeAddress == null) {
			throw new NullPointerException("nodeAddress");
		}
		this.nodeAddress = nodeAddress;
	}
	
	void start() {
		if (! isStarted) {
			requestAcceptBossBootstrap = buildBootstrap();
			LOG.info("local node bootstrap is starting....");
			ChannelFuture future = requestAcceptBossBootstrap.connect(proxyAddress);
			future.awaitUninterruptibly();
			if (future.isSuccess()) {
				isStarted = true;
				channel = future.getChannel();
				channel.write(createAuthenResult()).awaitUninterruptibly();
				LOG.info("local node bootstrap start successfully!");
			} else {
				isStarted = false;
				LOG.error("local node bootstrap started failed!");
				throw new ConnectException(String.format("can't connect to proxyNode:%s",proxyAddress.toString()));
			}
		}
	}
	
	void stop() {
		if (isStarted) {
			LOG.info("local node bootstrap is stopping...");
			if (channel != null) {
				channel.close().awaitUninterruptibly();
			}
			if (requestAcceptBossBootstrap != null) {
				requestAcceptBossBootstrap.shutdown();
				requestAcceptBossBootstrap.releaseExternalResources();
			}
			isStarted = false;
			LOG.info("local node bootstrap stopped!");
		}
	}
	
	void setProxyAddress(InetSocketAddress address) {
		if (address == null) {
			throw new NullPointerException("address");
		}
		this.proxyAddress = address;
	}
	
	private ClientBootstrap buildBootstrap() {
		ClientBootstrap clientBootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), 
				Executors.newCachedThreadPool(), 
				NodeSettings.getLocalNodeWorkersCount()));
		clientBootstrap.setPipelineFactory(localPipelineFactory);
		clientBootstrap.setOption("localAddress", NodeSettings.getLocalNodeInetAddress());
		clientBootstrap.setOption("tcpNoDelay", false);
		clientBootstrap.setOption("keepAlive", true);
		clientBootstrap.setOption("soLinger", 0);
		return clientBootstrap;
	}
	
	private TaskResult createAuthenResult() {
		JSONObject json = new JSONObject();
		json.put(AuthenticationHandler.AUTHEN_HOST, nodeAddress.getHost());
		json.put(AuthenticationHandler.AUTHEN_PORT, nodeAddress.getPort());
		json.put(AuthenticationHandler.AUTHEN_PATH, nodeAddress.getPath());
		json.put(AuthenticationHandler.AUTHEN_TYPE, true);
		TaskResult result = new TaskResult(Task.FAKE_TASK_ID, ResultCode.SUCCESS, json);
		return result;
	}
}
