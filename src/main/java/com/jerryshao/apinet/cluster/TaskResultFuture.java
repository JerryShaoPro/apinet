package com.jerryshao.apinet.cluster;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.jerryshao.apinet.cluster.event.ResultListener;
import com.wintim.common.util.LogFactory;

/**
 * <code>TaskResultFuture</code>��<code>TaskResult</code>��<code>Future</code>����<br />
 * ִ�к������صĽ������һ��<code>TaskResultFuture</code>
 * ���б�{@ljerryshao.apinetpiserver.cluster.TaskResult}������<code>Channel</code>�͹۲����б�<br />
 * ����<code>Channel</code>�����������Result�������ߵ�ͨ��<br />
 * �۲����б���ʹ�����첽��ִ�еĹ���<br />
 * 
 */
public class TaskResultFuture extends TaskResult {
	final static private Logger LOG = LogFactory.getLogger(TaskResultFuture.class);
	
	private boolean isDone;
	final private CopyOnWriteArrayList<ResultListener> listeners;
	private Channel channel;
	
	final private ReentrantLock lock;
	final private Condition doneCondition;
	
	public TaskResultFuture(String taskId) {
		this(taskId, ResultCode.SUCCESS);
	}
	
	TaskResultFuture(String taskId, ResultCode code) {
		this(taskId, code, new JSONObject());
	}
	
	private TaskResultFuture(String taskId, ResultCode code, JSONObject result) {
		this(taskId, code, result, code.toString());
	}

	private TaskResultFuture(String taskId, ResultCode code, JSONObject result, String message) {
		super(taskId, code, result, message);
		listeners = new CopyOnWriteArrayList<ResultListener>();
		lock = new ReentrantLock();
		doneCondition = lock.newCondition();
		isDone = false;
	}

	/**
	 * ����Ϊ���״̬<br />
	 * ���ô˺����ᴥ�����ж���<br />
	 * <li>֪ͨ���м�������TaskResult�Ѿ�����ó������</li>
	 * <li>���е�get��������������</li>
	 */
	public void setDone() {
		lock.lock();
		try {
			if (isDone) {
				doneCondition.signalAll();
				return;
			}
			isDone = true;
			notifyListeners();
			doneCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}
	
	public boolean isDone() {
		lock.lock();
		try {
			return isDone;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * �����ȴ�������ɣ�ֱ��{@link #setDone()}������<br />
	 */
	public void awaitUninterruptibly() {
		boolean interrupted = false;
		lock.lock(); 
		try {
			while (! isDone) {
				try {
					doneCondition.await();
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		} finally {
			lock.unlock();
	        if (interrupted) {
	            Thread.currentThread().interrupt();
	        }
		}
	}
	
	/**
	 * �����ȴ�������ɣ�֪��{@link #setDone()}������<br />
	 * ���߳�ʱ<br />
	 * �������ѵó�,����true<br />
	 * �����ʱ������false<br />
	 * @param time
	 * @param unit
	 * @return
	 */
	public boolean awaitUninterruptibly(long time, TimeUnit unit) {
		boolean interrupted = false;
		lock.lock();
		try {
			long nanoTime = unit.toNanos(time);
			while (! isDone) {
				if (nanoTime > 0) {
					try {
						nanoTime = doneCondition.awaitNanos(nanoTime);
					} catch (InterruptedException e) {
						interrupted = true;
					}
				} else {
					return false;
				}
			}
			return true;
		} finally {
			lock.unlock();
	        if (interrupted) {
	            Thread.currentThread().interrupt();
	        }
		}
	}
	
	//TODO �жϴ���
	@Override
	public ResultCode getCode() {
		lock.lock();
		try {
			try {
				while (! isDone) {
					doneCondition.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return this.code;
		} finally {
			lock.unlock();
		}
	}
	
	//TODO �жϴ���
	@Override
	public String getMessage() {
		lock.lock();
		try {
			try {
				while (! isDone) {
					doneCondition.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return this.message;
		} finally {
			lock.unlock();
		}
	}

	//TODO �жϴ���
	@Override
	public JSONObject getResult() {
		lock.lock();
		try {
			try {
				while (! isDone) {
					doneCondition.await();
				}
			} catch (InterruptedException e) {
				// TODO: handle exception
			}
			return this.result;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void setCode(ResultCode code) {
		if (code == null) {
			throw new NullPointerException("codes");
		}
		lock.lock();
		try {
			this.code = code;
		} finally {
			lock.unlock();
		}
	}
	
	public void setMessage(String message) {
		if (message == null) {
			throw new NullPointerException("message");
		}
		lock.lock();
		try {
			this.message = message;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void setResult(JSONObject result) {
		if (result == null) {
			throw new NullPointerException("result");
		}
		lock.lock();
		try {
			this.result = result;
		} finally {
			lock.unlock();
		}
	}

	//TODO �жϴ���(���ٷ���)
	@Override
	public HttpResponse toHttpResponse() {
		lock.lock();
		try {
			try {
				while (! isDone) {
					doneCondition.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return super.toHttpResponse();
		} finally {
			lock.unlock();
		}
	}
	
	//TODO �жϴ���
	@Override
	public JSONObject toJson() {
		lock.lock();
		try {
			try {
				while (! isDone) {
					doneCondition.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return super.toJson();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * �����¼�������
	 * @param listener
	 */
	public void addListener(ResultListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}
		listeners.add(listener);
	}
	
    private void notifyListeners() {
    	for (ResultListener listener : listeners) {
    		try {
				listener.operationComplete(this);
			} catch (Exception e) {
				LOG.error("task result future notify get a exception: ", e);
				e.printStackTrace();
			}
    	}
    }
    
    public void setChannel(Channel channel) {
    	if (channel == null) {
    		throw new NullPointerException("channel");
    	}
    	this.channel = channel;
    }
    
    public Channel getChannel() {
    	return this.channel;
    }
}
