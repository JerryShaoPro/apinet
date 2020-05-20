package com.jerryshao.apinet.cluster.event;

import java.util.EventListener;

import com.jerryshao.apinet.cluster.TaskResultFuture;

/**
 * <code>IResultListener</code>是事件模型中的监听器<br />
 * 用于实现cluster中异步操作，无阻塞的关键<br />
 * 
 */
public interface ResultListener extends EventListener {
	
	void operationComplete(TaskResultFuture future) throws Exception;
}
