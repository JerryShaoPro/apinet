import com.jerryshao.apinet.cluster.ClusterState;
import com.jerryshao.apinet.cluster.ProxyNode;
import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.exception.NoNodeException;

/**
 * {@link com.jerryshao.apinet.cluster.balance.Balancing}负载均衡使用的策略的接口<br />
 */
public interface BalancingStrategy {
	/**
	 * 设置<code>strategy</code>中所使用的{@link com.jerryshao.apinet.cluster.ClusterState}
	 * @param clusterState
	 */
	void setClusterState(ClusterState clusterState);

	/**
	 * 根据<code>Task</code>选择一个合适的<code>ProxyNode</code>
	 * 如果没有可用的<code>ProxyNode</code>就会抛出<code>NoNodeException</code>
	 * 有可能返回一个Null，这是因为多线程的缘故
	 * @param task
	 * @return
	 * @throws NoNodeException
	 */
	ProxyNode selectNode(Task task) throws NoNodeException;
}
