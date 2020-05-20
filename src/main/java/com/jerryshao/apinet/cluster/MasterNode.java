package com.jerryshao.apinet.cluster;

import org.apache.log4j.Logger;

import com.jerryshao.apinet.cluster.balance.Balancing;
import com.jerryshao.apinet.cluster.balance.IBalancingStrategy;
import com.jerryshao.apinet.cluster.common.NodeAddress;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.jerryshao.apinet.cluster.exception.NoNodeException;
import com.wintim.common.util.LogFactory;

/**
 * <code>MasterNode</code>是在<code>Cluster</code>中的主节点<br />
 * 主要是完成<code>ProxyNode</code>的选择问题<br />
 * 其中选择的策略，由{@link #balancing}来负责<br />
 *
 * 注意：
 * 默认MasterNode不能处理请求<br />
 * 即当没有可选的<code>ProxyNode</code>时抛出异常
 *
 */
public class MasterNode extends Node {
	final static private Logger LOG = LogFactory.getLogger(MasterNode.class);

	protected Balancing balancing = null;

	public MasterNode(ClusterState clusterState) {
		super(NodeAddress.FAKE_ADDRESS);
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		this.balancing = new Balancing(clusterState);
	}

	/**
	 * 设置MasterNode选择ProxyNode的策略<br />
	 * @param strategy
	 */
	void setBalanceStrategy(IBalancingStrategy strategy) {
		if (strategy == null) {
			throw new NullPointerException("strategy");
		}
		this.balancing.setBanlanceStrategy(strategy);
	}

	@Override
	boolean isMaster() {
		return true;
	}

	/**
	 * 貌似当没有可用的<code>Node</code>的时候，会出现问题<br />
	 * 后来者，请注意
	 */
	@Override
	public TaskResultFuture execute(Task task) {
		if (task == null) {
			throw new NullPointerException("task");
		}
		LOG.info("master node executing a task!id: " + task.getId() + ", param: "+ task.getParamJson() + ", time: " + task.getStartTime());
		try {
			ProxyNode proxyNode = balancing.selectProxyNode(task);
			LOG.debug("master node select one proxyNode to execute this task.node: " + proxyNode);
			TaskResultFuture resultFuture = proxyNode.execute(task);
			return resultFuture;
		} catch (NoNodeException e) {
			LOG.error("no avaliable node to use!", e);
			return createNoNodeResult(task.getId(), e);
		}
	}

	private TaskResultFuture createNoNodeResult(String taskId, NoNodeException e) {
		assert (taskId != null);
		TaskResultFuture resultFuture = new TaskResultFuture(taskId, ResultCode.CLUSTER_ERROR_NOT_NEED_RETRY);
		resultFuture.setDone();
		return resultFuture;
	}
}
