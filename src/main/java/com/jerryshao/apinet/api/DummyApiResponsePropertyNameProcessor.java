package com.jerryshao.apinet.api;

/**
 * 对<i>Api</i>处理结果的域名不进行任何处理
 */
public class DummyApiResponsePropertyNameProcessor implements ApiResponsePropertyNameProcessor {
	private static final IApiResponsePropertyNameProcessor INSTANCE = new DummyApiResponsePropertyNameProcessor();
	
	public static IApiResponsePropertyNameProcessor get() {
		return INSTANCE;
	}
	
	public String process(String name) {
		return name;
	}
	
	private DummyApiResponsePropertyNameProcessor() {	}
}
