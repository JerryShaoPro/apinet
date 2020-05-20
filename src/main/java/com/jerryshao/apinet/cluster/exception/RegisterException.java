package com.jerryshao.apinet.cluster.exception;

import java.net.InetSocketAddress;

public class RegisterException extends ClusterException {
	private static final long serialVersionUID = 6352665509088045983L;

	public RegisterException(InetSocketAddress address) {
		super(String.format("failed to register to cluster:%s", address.toString()));
	}
}
