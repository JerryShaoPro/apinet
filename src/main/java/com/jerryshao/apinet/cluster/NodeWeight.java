package com.jerryshao.apinet.cluster;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <code>NodeWeight</code>����������<code>ProxyNode</code>��Ȩ�ص����ݽṹ<br />
 * ��Ҫȡ����������:<br />
 * <li>�ɹ�����</li>
 * <li>����ִ�е�������</li>
 * <li>ƽ�����ѵ�ʱ��</li>
 * <li>��һ�ε�ִ�н��</li>
 * 
 * <code>NodeWeight</code>��Ϊ�����֣�һ������һ��ʱ���ڵ����ݣ�һ��������ʷ��¼������<br />
 * ʵ�ʷ�����Ϊ���󲿷֣�<code>baseWeight</code>�����֣���������һ��ʱ���Ȩ��
 * 
 * ע��һ����˵���̰߳�ȫ��ֻ����Եģ����ǲ�������м�״̬�������п���Ӧ�üӵģ�����û�мӡ���������<br />
 * ״̬����Ӱ��ϵͳ�����С�Ҳ����˵��Եİ�ȫ����Ϊ���Ƕ�����һ����Ҫ��û����ô�ߣ�
 * 
 * 
 * 
 */
public class NodeWeight {
	final static private long MAX_TIME_COST = 2500L;
	final static private int MAX_TASK_IN_PROGRESS = 500;
	final static private int RECENT_INTERVAL_NUM = 50;
	
	final static private int SUCCESS = 0x0;
	final static private int SYSTEM_ERROR = 0x1;
	final static private int CLUSTER_ERROR = 0x2;
	
	//��ǰ������
	private AtomicLong taskInProgress;//��ǰ�������е��������
	
	//���������
	private AtomicLong recentFinished;//�����ɵ���������
	private AtomicLong recentTimeCost;//�����ƽ������ʱ��
	private AtomicLong recentSuccessNum;//����ĳɹ�����
	private AtomicInteger recentResult;//��һ�εĽ��
	
	//��ʷ������
	private AtomicLong historyTimeCost;//��ʷ��ƽ������ʱ��
	private AtomicLong historyFinished;//��ʷ��ɵ�������
	private AtomicLong historySuccessNum;//��ʷ�ĳɹ�����
	private AtomicLong historySystemErrNum;//��ʷ��systemerror�Ĵ���
	private AtomicLong historyClusterErrNum;//��ʷ��clustererror�Ĵ���

	public NodeWeight() {
		historyTimeCost = new AtomicLong(0);
		historySuccessNum = new AtomicLong(1);
		historySystemErrNum = new AtomicLong(0);
		historyClusterErrNum = new AtomicLong(0);
		historyFinished = new AtomicLong(1);

		recentTimeCost = new AtomicLong(0);
		recentSuccessNum = new AtomicLong(1L);
		recentResult = new AtomicInteger(SUCCESS);
		recentFinished = new AtomicLong(1);
		
		taskInProgress = new AtomicLong(0);
	}
	
	/**
	 * ����Ҫ��weight���㺯��
	 * @return
	 */
	private int countWeight() {
		/**
		 * ��������������÷ּ������£�
		 * 
		 */
		//1. ����ɹ���
		double recentSuccessRate = (double)recentSuccessNum.get() / recentFinished.get();
		//2. ���㻨�ѵ�ʱ��
		double recentTimeRate = 1.0 - (double)(recentTimeCost.get() % MAX_TIME_COST) / (MAX_TIME_COST - 1);
		/**
		 * ��ʷ�����������÷ּ������£�
		 */
		//1. ����ɹ���
		double historySuccessRate = (double)historySuccessNum.get() / historyFinished.get();
		//2. ���㻨��ʱ��
		double historyTimeRate = 1.0 - (double) (historyTimeCost.get() % MAX_TIME_COST) / (MAX_TIME_COST - 1);
		
		/***
		 * ����ִ�е�����÷�������£�
		 */
		//1. ���������
		double progressRate = 1.0 - (double)(taskInProgress.get() % MAX_TASK_IN_PROGRESS) / (MAX_TASK_IN_PROGRESS - 1);
		
		//�����ռ����
		double w1 = 10 * recentSuccessRate + 20 * recentTimeRate;
		//��ʷ��ռ����
		double w2 = 5 * historySuccessRate + 5 * historyTimeRate;
		//��һ�εĽ����ռ����
		double w3 = 10;
		switch (recentResult.get()) {
		case SUCCESS:w3 = 10;break;
		case SYSTEM_ERROR: w3 = 5;break;
		case CLUSTER_ERROR:w3 = 0;break;
		}
		recentResult.set(SUCCESS);//���������һ�Σ��Ͱ������������
		
		//����ִ������ĸ�����ռ����
		double w4 = progressRate * 20;
		//�հ׷�����δʹ�ã�
		double w5 = 30;
		
		return (int) Math.round(w1 + w2 + w3 + w4 + w5);
	}
	
	/**
	 * ���recent����
	 */
	private void checkAndReset() {
		if (recentFinished.get() > RECENT_INTERVAL_NUM) {
			recentFinished.set(1);
			recentSuccessNum.set(1);
			recentTimeCost.set(0);
		}
	}
	
	
	/**
	 * �����̰߳�ȫ
	 * @param timeCost
	 */
	public void increaseSuccess(long taskTimeCost) {
		//����history����
		long historyTimeTemp = historyTimeCost.get();
		long historySuccessTemp = historySuccessNum.get();
		historyTimeCost.set((historyTimeTemp * historySuccessTemp + taskTimeCost) / (historySuccessTemp + 1));
		historySuccessNum.incrementAndGet();
		historyFinished.incrementAndGet();
		//����recent����
		long recentTimeTemp = recentTimeCost.get();
		long recentSuccessTemp = recentSuccessNum.get();
		recentTimeCost.set((recentTimeTemp * recentSuccessTemp + taskTimeCost) / (recentSuccessTemp + 1));
		recentSuccessNum.incrementAndGet();
		recentFinished.incrementAndGet();
		//����taskInProgress
		taskInProgress.decrementAndGet();
		//����recentResult
		recentResult.set(SUCCESS);
		//����
		checkAndReset();
	}
	
	/**
	 * �����̰߳�ȫ
	 */
	public void increaseSystemError() {
		//����history����
		historySystemErrNum.incrementAndGet();
		historyFinished.incrementAndGet();
		//����recent����
		recentFinished.incrementAndGet();
		//����taskInProgress
		taskInProgress.decrementAndGet();
		//����recentResult
		recentResult.set(SYSTEM_ERROR);
		//����
		checkAndReset();
	}

	/**
	 * �����̰߳�ȫ
	 */
	public void increaseClusterError() {
		//����history����
		historyClusterErrNum.incrementAndGet();
		historyFinished.incrementAndGet();
		//����recent����
		recentFinished.incrementAndGet();
		//����taskInProgress
		taskInProgress.decrementAndGet();
		//����recentResult
		recentResult.set(CLUSTER_ERROR);
		//����
		checkAndReset();
	}
	
	/**
	 * �̰߳�ȫ��
	 */
	public void increaseInProgress() {
		taskInProgress.incrementAndGet();
	}
	
	/**
	 * Ȩ�ط�Ϊ:<br />
	 * �����֣�baseWeight���ܷ�Ϊ50��
	 * ��һ�ν����Ӱ��֣����ܷ�Ϊ10��
	 * ��ʷ��¼�ķ��������ܷ�Ϊ10��-----���Ҳ�ʹ��
	 * �հ׷֣�30
	 * �̰߳�ȫ��
	 * @return
	 */
	public int getWeight() {
		return countWeight();
	}
	
	/**
	 * �̰߳�ȫ
	 * @return
	 */
	public long getTaskInProgress() {
		return taskInProgress.get();
	}
	
	/**
	 * �̰߳�ȫ
	 * @return
	 */
	public long getTaskHasFinished() {
		return historyFinished.get() - 1;//��Ϊ��ʼ����ʱ�򣬶�����1
	}
	
	/**
	 * �̰߳�ȫ
	 * @return
	 */
	public long getSuccessNumber() {
		return historySuccessNum.get() - 1;//��Ϊ��ʼ����ʱ�򣬶�����1
	}
	
	/**
	 * �̰߳�ȫ
	 * @return
	 */
	public long getSystemErrorNumber() {
		return historySystemErrNum.get();
	}
	
	/**
	 * �̰߳�ȫ
	 * @return
	 */
	public long getClusterErrorNumber() {
		return historyClusterErrNum.get();
	}
	
	/**
	 * �̰߳�ȫ
	 * @return
	 */
	public long getAverageTimeCost() {
		return historyTimeCost.get();
	}
}
