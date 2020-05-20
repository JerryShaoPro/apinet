package com.jerryshao.apinet.cluster.exception;

import com.jerryshao.apinet.cluster.operation.IClusterOperator;


public class DuplicateOperatorException extends RuntimeException {
	private static final long serialVersionUID = -7731199511098514975L;

	public DuplicateOperatorException(IClusterOperator operator) {
		super("Duplicate operator " + operator.getOperationName());
	}
}
