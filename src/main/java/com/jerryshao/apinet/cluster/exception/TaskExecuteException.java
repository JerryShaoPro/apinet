package com.jerryshao.apinet.cluster.exception;

import com.jerryshao.apinet.cluster.Task;

public class TaskExecuteException extends ClusterException {
	private static final long serialVersionUID = -292417687623507111L;

	public TaskExecuteException(Task task, Throwable cause) {
		super("Failed to exeute task: " + task, cause);
	}
	
	public TaskExecuteException(Task task, String errorMessage) {
		super(String.format("Failed to execute the task('%s') cause to %s", 
				task, errorMessage));
	}
}
