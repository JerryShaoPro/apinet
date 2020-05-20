package com.jerryshao.apinet.cluster.balance;

import java.util.concurrent.atomic.AtomicInteger;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.exception.NoNodeException;

class RoundRobinStrategy implements IBalancingStrategy {
	private ClusterState clusterState;

	private AtomicInteger nextIndex = new AtomicInteger(0);

	final public ProxyNode selectNode(Task task) throws NoNodeException {
		if (clusterState == null) {
			throw new IllegalStateException("cluster state can't be null!");
		}
		if (task == null) {
			throw new NullPointerException("task");
		}
		int size = clusterState.getActiveSize();
		if (size <= 0) {
			throw new NoNodeException("no avaliable node!");
		}
		nextIndex.set(nextIndex.get() % size);
		int index = nextIndex.get();
		ProxyNode proxyNode = clusterState.getProxyNode(index);
		nextIndex.incrementAndGet();
		return proxyNode;
	}

	final public void setClusterState(ClusterState clusterState) {
		nextIndex.set(0);
		this.clusterState = clusterState;
	}
}
