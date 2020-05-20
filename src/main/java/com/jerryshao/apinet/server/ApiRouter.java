
package com.jerryshao.apinet.server;

import org.apache.log4j.Logger;

import com.jerryshao.apinet.api.Api;
import com.jerryshao.apinet.util.Strings;
import com.wintim.common.util.LogFactory;

/**
 * API路由, 根据请求路径，自动路由到对应的Api<br>
 * 访问API路径规范为"/api/${api_name}/../?param1=value1&param2=value2..."
 *
 */
public class ApiRouter {
	
	private static final Logger LOG = LogFactory.getLogger(ApiRouter.class);
	
	private ApiRouter() {	}
	
	public static Api route(String path) {
		if (null == path) {
			LOG.warn("Can not route the path null");
			return null;
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Routing api by path " + path);
		}
		
		String apiName = extractApiName(path);
		if (null == apiName) {
			LOG.warn(String.format("Can not route the path '%s', because the api name extracted is null", path));
			return null;
		}
		Api api = ApiManager.get().get(apiName);
		if (null == api) {
			api = ApiManager.get().get(Strings.toUnderscoreCase(apiName));
		}
		return api;
	}

	public static final String extractApiName(String path) {
		if (! path.startsWith("/")) {
			return null;
		}
		
		int apiNameStartPos = "/".length();
		int apiNameEndPos = path.indexOf("/", apiNameStartPos);
		if (apiNameEndPos == -1) {
			apiNameEndPos = path.indexOf("?", apiNameStartPos);
			if (apiNameEndPos == -1) { //����"/api/apiname"������·��
				apiNameEndPos = path.length();
			} //��������"/api/apiname?key=value"������·��
		} 
		
		return path.substring(apiNameStartPos, apiNameEndPos);
	}
	
}
