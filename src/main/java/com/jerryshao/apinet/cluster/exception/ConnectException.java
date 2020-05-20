package com.jerryshao.apinet.cluster.exception;

public class ConnectException extends ClusterException {
	private static final long serialVersionUID = 5982046261051824145L;
	public ConnectException(String message) {
		super(message);
	}
	
	public ConnectException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
