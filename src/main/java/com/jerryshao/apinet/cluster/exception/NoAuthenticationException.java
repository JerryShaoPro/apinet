package com.jerryshao.apinet.cluster.exception;

import com.jerryshao.apinet.cluster.TaskResult;

public class NoAuthenticationException extends Exception {
	private static final long serialVersionUID = 1237172186749524573L;
	
	public NoAuthenticationException(TaskResult result) {
		super(String.format("taskId:%s, code:%s, message:%s, result:%s", result.getTaskId(), result.getCode(), result.getMessage(), result.getResult()));
	}
}
