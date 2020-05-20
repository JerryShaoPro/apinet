package com.jerryshao.apinet.api;

public class DuplicateApiKeyException extends ApiException {
	private static final long serialVersionUID = -7345427027521982411L;

	public DuplicateApiKeyException(String apiKey) {
		super("Already contains the api with key " + apiKey);
	}
	
}
