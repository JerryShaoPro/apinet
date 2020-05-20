package com.jerryshao.apinet.cluster.exception;

public class DuplicateHandlerException extends ClusterException {
	private static final long serialVersionUID = 2900975939592685383L;
	
	public DuplicateHandlerException(String error) {
		super(error);
	}
}
