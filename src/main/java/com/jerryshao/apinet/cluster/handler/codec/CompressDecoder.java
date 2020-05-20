package com.jerryshao.apinet.cluster.handler.codec;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;
import org.jboss.netty.handler.codec.compression.ZlibWrapper;

import com.wintim.common.util.LogFactory;

public class CompressDecoder extends ZlibDecoder {
	final static private Logger LOG = LogFactory.getLogger(CompressDecoder.class);
	
	public CompressDecoder() {
		super(ZlibWrapper.ZLIB);
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		LOG.info("Compress decoder receive : " + msg);
		return super.decode(ctx, channel, msg);
	}
}
