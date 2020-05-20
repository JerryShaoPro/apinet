package com.jerryshao.apinet.cluster.heartbeaat;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.jerryshao.apinet.cluster.TaskResult;
import com.wintim.common.util.LogFactory;

public class PongHeartBeatHandler extends SimpleChannelUpstreamHandler {
	final static private Logger LOG = LogFactory.getLogger(PongHeartBeatHandler.class);
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (! (e.getMessage() instanceof TaskResult)) {
			ctx.sendUpstream(e);
			return ;
		}
		
		assert (e.getMessage() instanceof TaskResult);
		TaskResult taskResult = (TaskResult) e.getMessage();
		if (! isPingTaskResult(taskResult)) {
			ctx.sendUpstream(e);
			return ;
		} 
		
		e.getChannel().write(new ClusterPongTask());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		LOG.error("pong heart beat handler get an exception.", e.getCause());
	}

	private boolean isPingTaskResult(TaskResult taskResult) {
		assert (taskResult != null);
		JSONObject result = taskResult.getResult();
		if (result.containsKey(ClusterPingTaskResult.PING_TASK_RESULT_FLAG_ATTRI)) {
			return true;
		} else {
			return false;
		}
	}
}
