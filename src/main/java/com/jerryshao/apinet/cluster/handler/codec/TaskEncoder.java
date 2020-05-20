package com.jerryshao.apinet.cluster.handler.codec;

import static org.jboss.netty.buffer.ChannelBuffers.*;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.jerryshao.apinet.cluster.Task;
import com.wintim.common.util.LogFactory;

public class TaskEncoder extends OneToOneEncoder {
	final static private Logger LOG = LogFactory.getLogger(TaskEncoder.class);
	final private Charset charset;
	
	public TaskEncoder(Charset charset) {
		this.charset = charset;
	}
	
	@Override
	protected Object encode(ChannelHandlerContext ctx,
			Channel channel, Object object) throws Exception {
		if (! (object instanceof Task)) {
			return object;
		}
		
		assert (object instanceof Task);
		Task task = (Task) object;
		LOG.info(String.format("task encoder encode a task to channelBuffer: taskid: %s, param: %s", task.getId(), task.getParamJson()));
		String taskStr = task.toJson().toString();
		ChannelBuffer buffer = dynamicBuffer();
		byte[] bytes = taskStr.getBytes(charset);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes, 0, bytes.length);
		return buffer;
	}
}
