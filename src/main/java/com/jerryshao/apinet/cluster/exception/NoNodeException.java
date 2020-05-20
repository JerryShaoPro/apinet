package com.jerryshao.apinet.cluster.exception;

public class NoNodeException extends Exception {
	private static final long serialVersionUID = 7363018856549100698L;
	
	NoNodeException() {
		super();
	}
	
	public NoNodeException(String error) {
		super(error);
	}
}
