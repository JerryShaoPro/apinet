package com.jerryshao.apinet.cluster.executor;

import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.exception.TaskExecuteException;

/**
 * 一个执行器接口<br />
 * 所有的执行器，都必须实现这个接口<br />
 */
 public interface TaskExecutor {
	public TaskResult execute(Task task) throws TaskExecuteException;
}
