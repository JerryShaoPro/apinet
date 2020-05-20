package com.jerryshao.apinet.cluster.handler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import com.wintim.common.util.LogFactory;

/**
 * <code>channelManageHandler</code>主要是负责管理已连接的channel<br />
 * 目前已实现：<br />
 * <li>优雅的关闭</li>
 */
public class ChannelManageHandler extends SimpleChannelUpstreamHandler {
	final static private Logger LOG = LogFactory.getLogger(ChannelManageHandler.class);
	final private ChannelGroup channelGroup;
	
	public ChannelManageHandler(ChannelGroup channelGroup) {
		if (channelGroup == null) {
			throw new NullPointerException("channel group");
		}
		this.channelGroup = channelGroup;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		LOG.error("channel manage handler get an exception.", e.getCause());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
		LOG.info("one channel connected to cluster: " + e.getChannel());
		channelGroup.add(e.getChannel());
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		channelGroup.remove(e.getChannel());
		LOG.info("one channel closed: " + e.getChannel());
	}
}
