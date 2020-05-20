package com.jerryshao.apinet.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.netty.channel.Channel;

import com.jerryshao.apinet.cluster.common.NodeAddress;

/**
 * <code>ClusterState</code>�����ڱ��jerryshao.apinetom.apiserver.cluster.Cluster}�е�״̬���ݵĽṹ<br />
 * <code>ClusterState</code>��Ҫ�Ǹ��𱣹�jerryshao.apinetm.apiserver.cluster.ProxyNode}�ļ���
 * <code>ClusterState</code>����������Ԫ��:<br />
 * ʹ���˵���ģʽ,��д�����룬CopyOnWrite����ʵ�ָ�Ч����ȫ��ClusterState<br />
 * <li>{@link #unAuthentNodes}û�б���Ȩ��Node���ϣ������Ѿ���������</li>
 * <li>{@link #activeNodes}��Ծ���õ�Node����</li>
 * <li>{@link #inactiveNodes}�����õ�Node����</li>
 * 
 */
//�̰߳�ȫ
public class ClusterState {
	
	//û����Ȩ��Node����
	final private CopyOnWriteArrayList<ProxyNode> unAuthentNodes;
	//����������Nodes
	final private List<ProxyNode> activeNodes;
	final private ReadWriteLock lock;
	//ʧЧ��Nodes
	final private CopyOnWriteArrayList<ProxyNode> inactiveNodes;
	
	static private ClusterState single = null;

	final static public ClusterState getInstance() {
		if (single == null) {
			single = new ClusterState();
		}
		return single;
	}
	
	private ClusterState() {
		lock = new ReentrantReadWriteLock();
		activeNodes = new ArrayList<ProxyNode>();
		inactiveNodes = new CopyOnWriteArrayList<ProxyNode>();
		unAuthentNodes = new CopyOnWriteArrayList<ProxyNode>();
	}
	
	/**
	 * ����ProxyNode,�����뵽UnAuthenNodes������<br />
	 * ������ڣ��ͷ���False<br />
	 * ���򷵻�True<br />
	 * ע��������������ò�Ƶ��
	 * ò���̰߳�ȫ��
	 * @param nodeAddress
	 * @return
	 */
	public boolean addToUnAuthen(NodeAddress nodeAddress, NodeGroup group) {
		if (nodeAddress == null) {
			throw new NullPointerException("nodeAddress");
		}
		if (group == null) {
			throw new NullPointerException("group");
		}
		ProxyNode proxyNode = new ProxyNode(nodeAddress);
		proxyNode.setGroup(group);
		synchronized (activeNodes) {
			if (activeNodes.contains(proxyNode) && unAuthentNodes.contains(proxyNode)) {
				return false;
			} else {
				unAuthentNodes.add(proxyNode);
				inactiveNodes.remove(proxyNode);
				return true;
			}
		}
	}
	
	/**
	 * ��activeNodes��ɾ��ProxyNode<br />
	 * ע��������������ò�Ƶ��
	 * ò���̰߳�ȫ��
	 * @param proxyNode
	 */
	public void deleteProxyNode(ProxyNode proxyNode) {
		if (proxyNode == null) {
			throw new NullPointerException("proxyNode");
		}
		synchronized (activeNodes) {
			if (activeNodes.remove(proxyNode)) {
				proxyNode.resetNodeWeight();
				inactiveNodes.add(proxyNode);
			}
		}
	}
	
	/**
	 * ������֤�ģ���Ҫ�ǣ��趨ProxyNode��ChannelId��Ӧ�Ĺ�ϵ<br />
	 * ���û�����ProxyNode���ͷ���false
	 * ��������ProxyNode������δ����֤�����ͷ���true
	 * ע�����ò�Ƶ��
	 * @param nodeAddress
	 * @param channelId
	 * @return
	 */
	public boolean authenticate(NodeAddress nodeAddress, final Channel channel) {
		if (nodeAddress == null) {
			throw new NullPointerException("nodeAddress");
		}
		for (int i = 0; i < unAuthentNodes.size(); i++) {
			ProxyNode proxyNode = unAuthentNodes.get(i);
			if (proxyNode.getAddress().equals(nodeAddress)) {
				proxyNode.setChannel(channel);
				unAuthentNodes.remove(i);
				addToActive(proxyNode);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ���ò�Ƶ��
	 * @param proxyNode
	 */
	private void addToActive(ProxyNode proxyNode) {
		assert (proxyNode != null);
		
		lock.writeLock().lock();
		try {
			activeNodes.add(proxyNode);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * ����<code>Channel</code>��ö�Ӧ��<code>ProxyNode</code><br />
	 * ����Ƶ��
	 * ò���̰߳�ȫ
	 * @param channel
	 * @return
	 */
	public ProxyNode getProxyNode(Channel channel) {
		if (channel == null) {
			throw new NullPointerException("channel");
		}
		lock.readLock().lock();
		try {
			for (ProxyNode proxyNode : activeNodes) {
				if (proxyNode.getChannel().getId() == channel.getId()) {
					return proxyNode;
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return null;
	}
	
	/**
	 * ����Ƶ��
	 * @return
	 */
	public int getActiveSize() {
		lock.readLock().lock();
		try {
			return activeNodes.size();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * ���index����ͻ᷵��Null<br />
	 * ����ͻ᷵��ProxyNode����
	 * ע������Ƶ��
	 * @param index
	 * @return
	 */
	public ProxyNode getProxyNode(int index) {
		lock.readLock().lock();
		try {
			if (index < 0 || index >= activeNodes.size()) {
				return null;
			} else {
				return activeNodes.get(index);
			}
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * ��õ�ǰ����ִ�е�<code>Task</code>����
	 * ���ò�Ƶ��
	 * @return
	 */
	public long getExecutingTasksCount() {
		long sum = 0L;

		lock.readLock().lock();
		try {
			for (ProxyNode proxyNode : activeNodes) {
				sum += proxyNode.getNodeWeight().getTaskInProgress();
			}
		} finally {
			lock.readLock().unlock();
		}
		return sum;
	}
	
	/**
	 * ��ȡ�����Ѽ����<code>Node</code>����
	 * ���ص��ǲ����޸ĵ���ͼ
	 * ע����֪����Ҫ����Ҫ����ͬ��
	 * @return
	 */
	public List<ProxyNode> getActiveNodes() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableList(activeNodes);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * ��ȡ����δ�����<code>Node</code>����
	 * ע����֪���費��Ҫ����ͬ��
	 * @return
	 */
	public List<ProxyNode> getInactiveNodes() {
		return Collections.unmodifiableList(inactiveNodes);
	}
}
