package com.jerryshao.apinet.cluster.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONObject;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.constant.ResultCode;

/**
 * <code>ClusterCommandHandler</code>用于执行commandTask的Handler
 *
 */
//TODO 错误处理
public class ClusterCommandHandler extends SimpleChannelUpstreamHandler {
	final static private Map<String, IClusterCommander> name2commander = new ConcurrentHashMap<String, IClusterCommander>();
	final private ClusterState clusterState;
	
	public ClusterCommandHandler(ClusterState clusterState) {
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		this.clusterState = clusterState;
	}
	
	private void fileExceptionEvent(ClusterCommandTask task, ChannelHandlerContext ctx, ExceptionEvent event) {
		assert (task != null);
		TaskResult result = new TaskResult(task.getId(), ResultCode.SYSTEM_ERROR_NOT_NEED_RETRY, event.getCause().toString());
		Channel channel = ctx.getChannel();
		if (channel != null && channel.isWritable()) {
			channel.write(result).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (!(e.getMessage() instanceof Task)) {
			ctx.sendUpstream(e);
			return ;
		}
		Task task = (Task) e.getMessage();
		if (!isCommand(task)) {
			ctx.sendUpstream(e);
			return ;
			
		}
		ClusterCommandTask commandTask = buildCommandTask(task);
		try {
			TaskResult result = executeCommandTask(commandTask);
			Channel channel = ctx.getChannel();
			if (channel != null && channel.isWritable()) {
				channel.write(result).addListener(ChannelFutureListener.CLOSE);
			}
		} catch (Exception cause) {
			ExceptionEvent event = new DefaultExceptionEvent(ctx.getChannel(), cause);
			fileExceptionEvent(commandTask, ctx, event);
		}
	}

	public void registerCommander(IClusterCommander commander, boolean isOverWrite) {
		if (isOverWrite) {
			name2commander.put(commander.getCommandName(), commander);
		} else {
			if (name2commander.containsKey(commander.getCommandName())) {
				throw new DuplicateCommanderException(commander);
			} else {
				name2commander.put(commander.getCommandName(), commander);
			}
		}
	}
	
	private boolean isCommand(Task task) {
		assert (task != null);
		
		boolean isOperationTask = false;
		Object isCommandTaskValueObj = task.getParamJson().get(ClusterCommandTask.COMMAND_TASK_FLAG_ATTR);
		if (null == isCommandTaskValueObj) {
			return isOperationTask;
		}
		
		if (isCommandTaskValueObj instanceof Boolean) {
			return (Boolean) isCommandTaskValueObj;
		} else if (isCommandTaskValueObj instanceof String) {
			return "yes".equals(isCommandTaskValueObj) || "true".equals(isCommandTaskValueObj);
		} else if (isCommandTaskValueObj instanceof Number) {
			return ((Number) isCommandTaskValueObj).intValue() > 0;
		} else {
			return false;
		}
	}
	
	private ClusterCommandTask buildCommandTask(Task task) {
		assert (task != null);
		JSONObject commandTaskParamJson = task.getParamJson();
		commandTaskParamJson.remove(ClusterCommandTask.COMMAND_TASK_FLAG_ATTR);
		
		String commandName = (String) commandTaskParamJson.remove(ClusterCommandTask.COMMAND_NAME_ATTR);
		return new ClusterCommandTask(task.getId(), commandName, commandTaskParamJson, task.getStartTime());
	}
	
	TaskResult executeCommandTask(ClusterCommandTask commandTask) {
		IClusterCommander commander = name2commander.get(commandTask.getCommandName());
		if (null == commander) {
			throw new NullPointerException("No cluster command for commander " + commandTask.getCommandName());
		}
		
		TaskResult commandResult = commander.operateCommand(commandTask, clusterState);
		return commandResult;
	}
}
