package com.jerryshao.apinet.cluster;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;

import com.jerryshao.apinet.cluster.handler.ChannelManageHandler;
import com.jerryshao.apinet.cluster.handler.ExecuteHandlerForUser;
import com.jerryshao.apinet.cluster.handler.codec.HttpTaskDecoder;
import com.jerryshao.apinet.cluster.handler.codec.HttpTaskResultEncoder;
import com.jerryshao.apinet.cluster.operation.ClusterOperationHandler;

/**
 * <code>UserRequestPipelineFactory</code>主要是用于<code>BootstrapForUserRequest</code>的channelFactory<br />
 * 主要包括如下Handler<br />
 * <li>{@link com.jerryshao.apinet.cluster.handler.ChannelManageHandler}</li>用于管理<code>channel</code>的<code>handler</code>
 * <li>{@link HttpRequestDecoder}</li>解码<code>Http</code>请求的<code>handler</code>
 * <li>{@link HttpChunkAggregator}</li>用于<code>Http</code>chunk的合并
 * <li>{@link com.jerryshao.apinet.cluster.handler.codec.HttpTaskDecoder}</li>用于从<code>http</code>中解析出<code>Task</code>
 * <li>{@link ExecutionHandler}</li>
 * <li>{@link com.jerryshao.apinet.cluster.operation.ClusterOperationHandler}</li>用于从处理<code>Operation</code>请求
 * <li>{@link com.jerryshao.apinet.cluster.handler.ExecuteHandlerForUser}</li>用于处理任务
 * <li>{@link HttpResponseEncoder}</li>用于加码的
 * <li>{@link com.jerryshao.apinet.cluster.handler.codec.HttpTaskResultEncoder}</li>用于加码的
 *
 */
public class UserRequestPipelineFactory implements ChannelPipelineFactory {
	final private static int DEFAULT_HTTP_AGGREGATOR_BUFFER_SIZE = 1024 * 10;
	final private ChannelGroup channelGroup;
	final private ExecutionHandler executionHandler;
	final private ClusterState clusterState;
	final private MasterNode masterNode;

	UserRequestPipelineFactory(ChannelGroup channelGroup, ExecutionHandler executionHandler, ClusterState clusterState, MasterNode masterNode) {
		if (channelGroup == null) {
			throw new NullPointerException("channelGroup");
		}
		if (executionHandler == null) {
			throw new NullPointerException("executionhandler");
		}
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		if (masterNode == null) {
			throw new NullPointerException("masterNode");
		}
		this.channelGroup = channelGroup;
		this.executionHandler = executionHandler;
		this.clusterState = clusterState;
		this.masterNode = masterNode;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline channelPipeline = Channels.pipeline();
		channelPipeline.addLast("system-channel-manage-handler", new ChannelManageHandler(channelGroup));
		channelPipeline.addLast("system-http-request-decoder", new HttpRequestDecoder());
		channelPipeline.addLast("system-http-chunk-aggregator", new HttpChunkAggregator(DEFAULT_HTTP_AGGREGATOR_BUFFER_SIZE));
		channelPipeline.addLast("system-task-decoder", new HttpTaskDecoder(ClusterSettings.getClusterChannelCharset()));
		channelPipeline.addLast("execution-handler", executionHandler);
		channelPipeline.addLast("operation-handler", new ClusterOperationHandler(clusterState));
		channelPipeline.addLast("master-execute-handler", new ExecuteHandlerForUser(masterNode));
		channelPipeline.addLast("system-http-response", new HttpResponseEncoder());
		channelPipeline.addLast("system-http-task-result-encoder", new HttpTaskResultEncoder());
		return channelPipeline;
	}
}
