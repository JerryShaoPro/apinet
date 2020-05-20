package com.jerryshao.apinet.cluster.handler;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.group.ChannelGroup;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.wintim.common.util.LogFactory;

/**
 * <code>TaskChannelManageHandler</code>是用于在BootstrapForTask中控制channelGroup<br />
 * 同时，清除已断开连接的所有Task
 *
 */
public class TaskChannelManageHandler extends ChannelManageHandler {
	final static Logger LOG = LogFactory.getLogger(TaskChannelManageHandler.class);
	final private ClusterState clusterState;
	
	public TaskChannelManageHandler(ChannelGroup channelGroup, ClusterState clusterState) {
		super(channelGroup);
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		this.clusterState = clusterState;
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		ProxyNode proxyNode = clusterState.getProxyNode(e.getChannel());
		proxyNode.setDoneToAllTask(ResultCode.CLUSTER_ERROR_NEED_RETRY, new JSONObject(), "node is closed");
		e.getChannel().close();
		clusterState.deleteProxyNode(proxyNode);
		super.channelDisconnected(ctx, e);
	}
}
