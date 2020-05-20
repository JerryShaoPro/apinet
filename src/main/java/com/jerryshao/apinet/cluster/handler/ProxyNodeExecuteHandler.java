package com.jerryshao.apinet.cluster.handler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.TaskResult;
import com.wintim.common.util.LogFactory;

/**
 * 用于{@link com.jerryshao.apinet.cluster.ProxyNode}的pipeline中.<br />
 * 主要负责接收{@link com.jerryshao.apinet.cluster.local.LocalNode}的taskResult.<br />
 * 并将{@link com.jerryshao.apinet.cluster.TaskResult}设置到{@link com.jerryshaoa.apinet.cluster.TaskResultFuture}<br />
 * 中，并设置该任务已完成
 */
public class ProxyNodeExecuteHandler extends SimpleChannelUpstreamHandler {
	final static private Logger LOG = LogFactory.getLogger(ProxyNodeExecuteHandler.class);

	final private ClusterState clusterState;

	public ProxyNodeExecuteHandler(ClusterState clusterState) {
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		this.clusterState = clusterState;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		LOG.error("proxy node execute handler get an exception.", e.getCause());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (! (e.getMessage() instanceof TaskResult)) {
			ctx.sendUpstream(e);
			return ;
		}
		assert (e.getMessage() instanceof TaskResult);
		TaskResult result = (TaskResult) e.getMessage();
		LOG.info("proxy node execute handler get a result." + result.toJson());
		//1. 获得对应的ProxyNode
		ProxyNode proxyNode = clusterState.getProxyNode(e.getChannel());
		assert (proxyNode != null);

		//2. 设置为完成状态
		proxyNode.setDone(result.getTaskId(), result);
	}
}
