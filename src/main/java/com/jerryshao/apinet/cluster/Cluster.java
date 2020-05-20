package com.jerryshao.apinet.cluster;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import com.jerryshao.apinet.cluster.balance.IBalancingStrategy;
import com.wintim.common.util.LogFactory;

/**
 * <code>cluster</code>是对整个集群的抽象描述。<br />
 * <code>cluster</code>包含以下元素：<br />
 * <li>{@link com.jerryshao.apinet.cluster.BootstrapForUserRequest}是面向用户的接口，只处理用户的请求</li>
 * <li>{@link com.jerryshao.apinet.cluster.BootstrapForDataExchange}是面向节点和master的通信接口，只用于节点之间的数据交换</li>
 * <li>{@link com.jerryshao.apinet.cluster.BootstrapForNodeRequest}是面向节点的接口，只处理节点注册等请求</li>
 * <li>{@link com.jerryshao.apinet.cluster.MasterNode}是逻辑上的主节点</li>
 * <li>{@link com.jerryshao.apinet.cluster.ClusterState}是描述集群的状态的结构</li><br />
 * 典型的<code>Cluster</code>是这样使用的<br />
 * <pre>
 * Cluster cluster = new Cluster();
 * cluster.start();
 * .....
 * cluster.stop();
 * </pre>
 * <code>Cluster</code>的所有配置，都是从ClusterSetting中读取的，不存在中间传递变量，都是在最后需要使用时给出的<br />-->final 约定
 */
public class Cluster {
	final static private Logger LOG = LogFactory.getLogger(Cluster.class);

	private boolean isStarted = false;
	private BootstrapForUserRequest bootstrapForUserRequest;
	private BootstrapForNodeRequest bootstrapForNodeRequest;
	private BootstrapForDataExchange bootstrapForDataExchange;
	private ExecutionHandler executionHandler;

	final protected ClusterState clusterState;
	final protected MasterNode masterNode;

	public Cluster() {
		clusterState = ClusterState.getInstance();
		masterNode = new MasterNode(clusterState);
	}

	/**
	 * 支持stop之后进行start
	 */
	public void start() {
		if (! isStarted) {
			LOG.info("cluster is starting...");
			executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(ClusterSettings.getClusterTaskExecutorsSize(),
					ClusterSettings.getClusterChannelMaxMemoryByteSize(), ClusterSettings.getClusterTotalMemoryByteSize()));
			bootstrapForNodeRequest = new BootstrapForNodeRequest(clusterState);
			bootstrapForUserRequest = new BootstrapForUserRequest(executionHandler, clusterState, masterNode);
			bootstrapForDataExchange = new BootstrapForDataExchange(clusterState, executionHandler);

			bootstrapForDataExchange.start();
			bootstrapForNodeRequest.start();
			bootstrapForUserRequest.start();
			LOG.info("cluster is started!");
			isStarted = true;
		}
	}

	/**
	 * stop之后，可以start
	 */
	public void stop() {
		if (isStarted) {
			LOG.info("cluster is stopping...");
			bootstrapForDataExchange.stop();
			bootstrapForNodeRequest.stop();
			bootstrapForUserRequest.stop();
			executionHandler.releaseExternalResources();
			LOG.info("cluster is stopped!");
			isStarted = false;
		}
	}

	public boolean isStarted() {
		return isStarted;
	}

	/**
	 * 设置masterNode选择的策略
	 * @param strategy
	 */
	void setBalanceStrategy(IBalancingStrategy strategy) {
		if (strategy == null) {
			throw new NullPointerException("strategy");
		}
		masterNode.setBalanceStrategy(strategy);
	}
}
