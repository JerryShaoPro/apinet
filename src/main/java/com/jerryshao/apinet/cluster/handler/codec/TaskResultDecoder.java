package com.jerryshao.apinet.cluster.handler.codec;

import java.nio.charset.Charset;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.jerryshao.apinet.cluster.TaskResult;
import com.wintim.common.util.LogFactory;

public class TaskResultDecoder extends FrameDecoder {
	final static private Logger LOG = LogFactory.getLogger(TaskResultDecoder.class);
	private final Charset charset;
	
	public TaskResultDecoder(Charset charset) {
		this.charset = charset;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() < 4) {
			return null;
		}
		buffer.markReaderIndex();
		int length = buffer.readInt();
		if (buffer.readableBytes() < length) {
			buffer.resetReaderIndex();
			return null;
		}
		TaskResult result = decodeFromChannelBuffer(buffer.readBytes(length));
		LOG.info(String.format("task result decoder get a result.taskId:%s, result:%s, code:%s", result.getTaskId(), result.getResult(), result.getCode()));
		return result;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
	}
	
	private TaskResult decodeFromChannelBuffer(ChannelBuffer channelBuffer) {
		assert (channelBuffer != null);
		String resultJsonStr = channelBuffer.toString(charset);
		
		JSONObject json = JSONObject.fromObject(resultJsonStr);
		TaskResult result = TaskResult.fromJson(json);
		return result;
	}
}
