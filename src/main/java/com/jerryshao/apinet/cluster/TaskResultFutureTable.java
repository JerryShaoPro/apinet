package com.jerryshao.apinet.cluster;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.json.JSONObject;

import com.jerryshao.apinet.cluster.constant.ResultCode;

/**
 * ���ڱ���ĳһjerryshao.apinetm.apiserver.cluster.ProxyNode}��jerryshao.apinetm.apiserver.cluster.TaskResultFuture}���б�<br />
 * ����б�һ�����ڸ���<code>TaskResult</code>��<code>TaskId</code>�ҵ���Ӧ��<code>TaskResultFuture</code>������Ϊ���״̬<br />
 * 
 */
public class TaskResultFutureTable {
	final private ConcurrentHashMap<Task, TaskResultFuture> taskResultFutureTable;
	final private ConcurrentHashMap<String, Task> id2Task;
	final private ReentrantLock lock;
	
	public TaskResultFutureTable() {
		taskResultFutureTable = new ConcurrentHashMap<Task, TaskResultFuture>();
		id2Task = new ConcurrentHashMap<String, Task>();
		lock = new ReentrantLock();
	}
	
	/**
	 * ������<code>table</code>����δ���״̬��<code>TaskResultFuture</code>
	 * �����ǰ�ʹ����ˣ��ͻᱨ������<br />
	 * @param task
	 * @param future
	 */
	public void putTaskResultFuture(Task task, TaskResultFuture future) {
		if (null == future) {
			throw new NullPointerException("future");
		}
		if (task == null) {
			throw new NullPointerException("task");
		}
		
		if (id2Task.containsKey(task.getId()) || taskResultFutureTable.containsKey(task)) {
			throw new IllegalArgumentException("has same id!");
		}
		lock.lock();
		try {
			id2Task.put(task.getId(), task);
			taskResultFutureTable.put(task, future);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * ���<code>TaskResult</code>�Ľ����������Ӧ��taskExecuteFuture����Ϊ���״̬.
	 * ������task�����ڣ������κ����飬����false<br />
	 * ������task���ڣ�����true
	 * @param taskId
	 * @param result
	 * @return
	 */
	public boolean setDone(String taskId, TaskResult result) {
		Task task = id2Task.get(taskId);
		if (task != null) {
			TaskResultFuture future = taskResultFutureTable.get(task);
			if (future != null) {
				future.setResult(result.getResult());
				future.setCode(result.getCode());
				future.setMessage(result.getMessage());
				future.setDone();
				
				lock.lock();
				try {
					id2Task.remove(taskId);
					taskResultFutureTable.remove(task);
				} finally {
					lock.unlock();
				}
				return true;
			} else {
				//TODO something
				//maybe will never arrive here
				throw new NullPointerException("fuck");
			}
		} else {
			//TODO something
			return false;
		}
	}
	
	/**
	 * �������ĳһ<code>ProxyNode</code>��Ӧ����������<br />
	 * һ�㶼����ĳ<code>ProxyNode</code>ʧЧʱ��Ĳ���<br />
	 * ע���������û�о����ر����صĿ��ǣ������ߣ���ע������һ��<br />
	 * @param resultCode
	 * @param result
	 * @param message
	 */
	public void clearAllTasks(ResultCode resultCode, JSONObject result, String message) {
		if (resultCode == null) {
			throw new NullPointerException("resultCode");
		}
		if (result == null) {
			throw new NullPointerException("result");
		}
		if (message == null) {
			throw new NullPointerException("message");
		}
		for (TaskResultFuture future : taskResultFutureTable.values()) {
			future.setCode(resultCode);
			future.setResult(result);
			future.setMessage(message);
			future.setDone();
		}
		taskResultFutureTable.clear();
	}
	
	public Task getTask(String taskId) {
		if (taskId == null) {
			throw new NullPointerException("taskId");
		}
		return id2Task.get(taskId);
	}
	
	/**
	 * ��������ִ�е��������
	 * @return
	 */
	public int size() {
		return taskResultFutureTable.size();
	}
}
