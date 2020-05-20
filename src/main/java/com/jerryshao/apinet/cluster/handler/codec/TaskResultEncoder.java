package com.jerryshao.apinet.cluster.handler.codec;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.jerryshao.apinet.cluster.TaskResult;
import com.wintim.common.util.LogFactory;

public class TaskResultEncoder extends OneToOneEncoder {
	final static private Logger LOG = LogFactory.getLogger(TaskResultEncoder.class);
	private final Charset charset;
	
	public TaskResultEncoder(Charset charset) {
		this.charset = charset;
	}
	
	@Override
	protected Object encode(ChannelHandlerContext ctx,
			Channel channel, Object msg) throws Exception {
		if (! (msg instanceof TaskResult)) {
			return msg;
		}
		
		assert (msg instanceof TaskResult);

		TaskResult result = (TaskResult) msg; 
		LOG.info("task result encoder get a result:" + result.toJson().toString());
		
		ChannelBuffer buffer = dynamicBuffer();
		byte[] bytes = result.toJson().toString().getBytes(charset);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes, 0, bytes.length);
		return buffer;
	}
}
