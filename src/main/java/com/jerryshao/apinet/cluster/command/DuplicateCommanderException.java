package com.jerryshao.apinet.cluster.command;

class DuplicateCommanderException extends RuntimeException {
	private static final long serialVersionUID = -1389121609055607601L;
	
	public DuplicateCommanderException(IClusterCommander commander) {
		super("Duplicate commander : " + commander.getCommandName());
	}
}
