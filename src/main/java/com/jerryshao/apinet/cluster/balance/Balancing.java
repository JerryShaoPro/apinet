package com.jerryshao.apinet.cluster.balance;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.Node;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.exception.NoNodeException;

/**
 * 负载均衡，通过{@link #getNodeToExute(Task, TaskExecutingContext)}获取
 * 一个{@link Node}执行任务<br>
 */
public class Balancing {
	private IBalancingStrategy balancingStrategy;
	private ClusterState clusterState;
	
	public Balancing(ClusterState clusterState) {
		if (clusterState == null) {
			throw new NullPointerException("clusterState");
		}
		this.balancingStrategy = new RoundRobinStrategy();
		this.clusterState = clusterState;
		this.balancingStrategy.setClusterState(clusterState);
	}
	
	public void setBanlanceStrategy(IBalancingStrategy strategy) {
		if (strategy == null) {
			throw new NullPointerException("strategy");
		}
		this.balancingStrategy = strategy;
		this.balancingStrategy.setClusterState(clusterState);
	}
	
	public ProxyNode selectProxyNode(Task task) throws NoNodeException {
		return this.balancingStrategy.selectNode(task);
	}
}
