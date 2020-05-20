package com.jerryshao.apinet.cluster.handler.codec;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.jerryshao.apinet.cluster.TaskResult;
import com.wintim.common.util.LogFactory;

public class HttpTaskResultEncoder extends OneToOneEncoder {
	final static private Logger LOG = LogFactory.getLogger(HttpTaskResultEncoder.class);
	
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		if (! (msg instanceof TaskResult)) {
			return msg;
		}
		assert (msg instanceof TaskResult);
		TaskResult executeResult = (TaskResult) msg;
		LOG.info("get a task execute result: " + executeResult.toJson());
		return executeResult.toHttpResponse();
	}
}
