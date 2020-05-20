package com.jerryshao.apinet.api;

import com.jerryshao.apinet.util.Strings;

/**
 * 默认的Api返回的Json结果的属性名称处理实现，该实现完成如下转换:<br>
 * "UndercoreCase" -> "undercore_case"
 */
public class DefaultApiResponsePropertyNameProcessor implements ApiResponsePropertyNameProcessor {

	public static DefaultApiResponsePropertyNameProcessor get() {
		if (null == PROCESSOR) {
			PROCESSOR = new DefaultApiResponsePropertyNameProcessor();
		}
		return PROCESSOR;
	}

	private static DefaultApiResponsePropertyNameProcessor PROCESSOR = null;

	private DefaultApiResponsePropertyNameProcessor() {	}

	/**
	 * 对所有处理结果的字段名做如下处理:<br>
	 * "UndercoreCase" -> "undercore_case"
	 *
	 */
	public String process(String name) {
		return Strings.toUnderscoreCase(name);
	}

}
