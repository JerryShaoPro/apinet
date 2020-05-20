package com.jerryshao.apinet.cluster.balance;

import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.exception.NoNodeException;

class NodeWeightStrategy implements BalancingStrategy {
    private ClusterState clusterState;

    final public ProxyNode selectNode(Task task) throws NoNodeException {
        if (null == task) {
            throw new NullPointerException("task");
        }
        if (clusterState.getActiveSize() <= 0) {
            throw new NoNodeException("no avaliabel node");
        }

        int maxWeight = -1;
        ProxyNode node = null;

        for (int i = 0; i < clusterState.getActiveSize(); i++) {
            ProxyNode proxyNode = clusterState.getProxyNode(i);
            if (proxyNode != null) {
                int weight = proxyNode.getNodeWeight().getWeight();
                if (weight >= maxWeight) {
                    maxWeight = weight;
                    node = proxyNode;
                }
            }
        }
        return node;
    }

    final public void setClusterState(ClusterState clusterState) {
        if (clusterState == null) {
            throw new NullPointerException("clusterState");
        }
        this.clusterState = clusterState;
    }
}
