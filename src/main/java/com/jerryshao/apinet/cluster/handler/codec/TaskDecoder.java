package com.jerryshao.apinet.cluster.handler.codec;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import com.jerryshao.apinet.cluster.Task;
import com.wintim.common.util.LogFactory;

public class TaskDecoder extends FrameDecoder {
	final static private Logger LOG = LogFactory.getLogger(TaskDecoder.class);
	
	final private Charset charset;
	
	public TaskDecoder(Charset charset) {
		if (null == charset) {
			throw new NullPointerException("charset");
		}
		
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
		Task task = (Task)decodeFromChannelBuffer(buffer.readBytes(length));
		LOG.info(String.format("task decoder get a task.id:%s, param:%s, time%s.", task.getId(), task.getParamJson(), task.getStartTime()));
		return task;
	}
	
	private Object decodeFromChannelBuffer(ChannelBuffer channelBuffer) {
		assert (channelBuffer != null);
		String taskJsonStr = channelBuffer.toString(charset);
		
		JSONObject json = JSONObject.fromObject(taskJsonStr);
		Task task = Task.fromJson(json);
		return task;
	}
	
	protected static final void parseRecivedData(InterfaceHttpData data, Map<String, List<String>> attrname2values) {
		assert (attrname2values != null);
		
		if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
            	LOG.error(String.format("Failed to reading the value for BODY Attribute:'%s':%s", 
            			attribute.getName(), attribute.getHttpDataType().name()), e1);
                return;
            }
            
            if (value.length() > 100) {
            	LOG.warn(String.format("BODY Attribute:%s:%s data too long", 
            			attribute.getHttpDataType().name(), attribute.getName()));
            }
            
            attrname2values.put(attribute.getName(), Arrays.asList(new String[]{value}));
        } else {
        	LOG.warn(String.format("Attribute:%s is not attribute, not surpport (%s) yet!", 
        			data.getName(), data.getHttpDataType().name()));
        }
	}
}
