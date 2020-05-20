package com.jerryshao.apinet.settings;


public class SettingsException extends RuntimeException {

	private static final long serialVersionUID = 8202851569618039947L;

	public SettingsException(String message) {
        super(message);
    }

    public SettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
