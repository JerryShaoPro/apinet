package com.jerryshao.apinet.cluster.heartbeaat;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.local.LocalNode;
import com.wintim.common.util.LogFactory;

public class PingHeartBeatHandler extends SimpleChannelUpstreamHandler {
	final static private Logger LOG = LogFactory.getLogger(PingHeartBeatHandler.class);
	
	final private ScheduledExecutorService scheduledExecutorService;
	final private long pingTime;
	final private long timeOut;
	final private TimeUnit timeUnit;
	final private LocalNode localNode;
	private Channel channel;
	private boolean isResponsed;
	private volatile boolean isGoOn = true;
	
	public PingHeartBeatHandler(ScheduledExecutorService scheduledExecutorService, LocalNode localNode, long pingTime, long timeOut, TimeUnit timeUnit) {
		if (scheduledExecutorService == null) {
			throw new NullPointerException("scheduledExecutorService");
		}
		this.scheduledExecutorService = scheduledExecutorService;
		this.pingTime = pingTime;
		this.timeOut = timeOut;
		this.timeUnit = timeUnit;
		this.isResponsed = false;
		this.localNode = localNode;
	}
	

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
		this.channel = e.getChannel();
		
		scheduledExecutorService.scheduleWithFixedDelay(buildPingActor(), pingTime, pingTime, timeUnit);
		scheduledExecutorService.scheduleWithFixedDelay(buildTimeOutMonitor(), timeOut, timeOut, timeUnit);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (! (e.getMessage() instanceof Task)) {
			ctx.sendUpstream(e);
			return ;
		}
		
		assert (e.getMessage() instanceof Task);
		Task task = (Task) e.getMessage();
		
		//����Ƿ���PongTask
		if (! isPongTask(task)) {
			ctx.sendUpstream(e);
			return ;
		} 
		LOG.debug("ping heart beat handler get a pong response!");
		isResponsed = true;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		LOG.error("ping heart beat handler get an exception.", e.getCause());
	}

	private Runnable buildPingActor() {
		Runnable pingActor = new Runnable() {
			public void run() {
				if (isGoOn && channel != null && channel.isWritable()) {
					channel.write(new ClusterPingTaskResult());
					LOG.debug("ping heart beat handler send a ping task!");
				}
			}
		};
		return pingActor;
	}

	private Runnable buildTimeOutMonitor() {
		Runnable timeOutMonitor = new Runnable() {
			public void run() {
				if (isGoOn) {
					if (isResponsed) {
						isResponsed = false;
						return;
					} else {
						LOG.info("local node haven't got a pong response!");
						localNode.setBeatHeartError(true);
						isGoOn = false;
					}
				}
			}
		};
		return timeOutMonitor;
	}
	
	private boolean isPongTask(Task task) {
		assert (task != null);
		JSONObject json = task.getParamJson();
		if (json.containsKey(ClusterPongTask.PONG_TASK_FLAG_ATTR)) {
			return true;
		} else {
			return false;
		}
	}
}
