package com.jerryshao.apinet.cluster.handler;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.wintim.common.util.LogFactory;

public class NodeManageHandler extends SimpleChannelUpstreamHandler {
	final private static Logger LOG = LogFactory.getLogger(NodeManageHandler.class);
	final private ClusterState clusterState;
	public NodeManageHandler(ClusterState clusterState) {
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		this.clusterState = clusterState;
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		ProxyNode proxyNode = clusterState.getProxyNode(e.getChannel());
		LOG.info("one proxyNode disconnected!:" + proxyNode);
		proxyNode.setDoneToAllTask(ResultCode.CLUSTER_ERROR_NEED_RETRY, new JSONObject(), "node is closed");
		e.getChannel().close();
		clusterState.deleteProxyNode(proxyNode);
	}
}
