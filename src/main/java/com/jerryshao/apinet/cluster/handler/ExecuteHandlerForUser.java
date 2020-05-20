package com.jerryshao.apinet.cluster.handler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.jerryshao.apinet.cluster.MasterNode;
import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.TaskResultFuture;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.jerryshao.apinet.cluster.event.ResultListener;
import com.wintim.common.util.LogFactory;

/**
 * <code>ExecuteHandlerForUser</code>是为user提供执行服务的Handler<br />
 * 主要用于<code>BootstrapForUser</code>中
 * 实际上：是调用MasterNode执行
 *
 */
//TODO 增加错误处理
public class ExecuteHandlerForUser extends SimpleChannelUpstreamHandler {
	final static public int MAX_RETRY_COUNT = 3;

	final static private Logger LOG = LogFactory.getLogger(ExecuteHandlerForUser.class);
	final private MasterNode masterNode;

	public ExecuteHandlerForUser(MasterNode masterNode) {
		if (masterNode == null) {
			throw new NullPointerException("masterNode");
		}
		this.masterNode = masterNode;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		LOG.error("execute handler for user get an exception.", e.getCause());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object object = e.getMessage();
		if (! (object instanceof Task)) {
			return ;
		}

		assert (object instanceof Task);
		Task task = (Task) object;
		LOG.info(String.format("execute handler get a task: id:%s, param:%s, time:%s", task.getId(), task.getParamJson(), task.getStartTime()));

		sendAndExecute(task, e.getChannel(), masterNode);
	}

	final static public void sendAndExecute(final Task task, final Channel channel,final MasterNode masterNode) {
		if (task == null) {
			throw new NullPointerException("task");
		}
		if (channel == null) {
			throw new NullPointerException("channel");
		}
		if (masterNode == null) {
			throw new NullPointerException("masterNode");
		}

		TaskResultFuture resultFuture = masterNode.execute(task);
		resultFuture.setChannel(channel);

		resultFuture.addListener(new IResultListener() {
			public void operationComplete(TaskResultFuture future) throws Exception {
				if (future.isDone()) {
					if (needRetry(task, future.getCode())) {//需要重试
						updateTask(task);
						sendAndExecute(task, channel, masterNode);
					} else {//不需要重试
						Channel channel = future.getChannel();
						if (channel != null && channel.isWritable()) {
							channel.write(TaskResult.fromJson(future.toJson())).addListener(ChannelFutureListener.CLOSE);
						}
					}
				}
			}
		});
	}

	/**
	 * 检查是否需要重试
	 * @return
	 */
	final static public boolean needRetry(Task task, ResultCode code) {
		if (task == null) {
			throw new NullPointerException("task");
		}
		if (code == null) {
			throw new NullPointerException("code");
		}
		switch (code) {
			case SUCCESS:
			case SYSTEM_ERROR_NOT_NEED_RETRY:
			case CLUSTER_ERROR_NOT_NEED_RETRY:
				return false;
			case SYSTEM_ERROR_NEED_RETRY:
			case CLUSTER_ERROR_NEED_RETRY:
				if (task.getRetryCount() < MAX_RETRY_COUNT) {
					return true;
				} else {
					return false;
				}
		}
		return false;
	}

	/**
	 * 更新Task的重试次数
	 * 更新Task的startTime
	 * @param task
	 */
	final static public void updateTask(Task task) {
		if (task == null) {
			throw new NullPointerException("task");
		}
		task.increaseRetryCount();
		task.setStartTime(System.currentTimeMillis());
	}
}
