package com.jerryshao.apinet.cluster.handler.codec;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.constant.TaskParamNames;
import com.wintim.common.util.LogFactory;

public class HttpTaskDecoder extends OneToOneDecoder {
	final static private Logger LOG = LogFactory.getLogger(HttpTaskDecoder.class);
	
	final private Charset charset;
	
	public HttpTaskDecoder(Charset charset) {
		this.charset = charset;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel dchannel,
			Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			//����ico
			if (httpRequest.getUri().endsWith(".ico")) {
				dchannel.close();
				return null;
			} else {
				Task task = (Task)decodeFromHttpRequest(httpRequest);
				System.err.println(httpRequest);
				return task;
			}
		} else {
			return msg;
		}
	}
	
	private Object decodeFromHttpRequest(HttpRequest httpRequest) throws Exception {
		assert (httpRequest != null);
		
		String taskId = generateTaskId(httpRequest);
		JSONObject taskParamJson = parseTaskAttributes(httpRequest);
		//����path��method��romote
		taskParamJson.put(TaskParamNames.TASK_PARAM_METHOD.getContent(), httpRequest.getMethod().toString());
		taskParamJson.put(TaskParamNames.TASK_PARAM_REMOTE.getContent(), httpRequest.getHeader(HttpHeaders.Names.FROM));
		LOG.info("Http Task Decoder decode a http request: " + httpRequest);
		return new Task(taskId, taskParamJson, System.currentTimeMillis());
	}
	
	final static private String generateTaskId(HttpRequest httpRequest) {
		assert (httpRequest != null);
		
		return String.format("%s_%s", httpRequest.getUri(), UUID.randomUUID().toString());
	}
	
	final static private HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed MINSIZE
	final private JSONObject parseTaskAttributes(HttpRequest httpRequest) throws Exception {
		assert (httpRequest != null);
		
		QueryStringDecoder decoderQuery = new QueryStringDecoder(httpRequest.getUri(), charset);
		Map<String, List<String>> attriName2values = colone(decoderQuery.getParameters());
		//����path
		attriName2values.put(TaskParamNames.TASK_PARAM_URI.getContent(), Arrays.asList(processPath(decoderQuery.getPath())));
		
		if (httpRequest.getMethod() == HttpMethod.POST) {
			parseAttributesFromPostData(httpRequest, attriName2values);
		}
		JSONObject json = new JSONObject();
		for (Entry<String, List<String>> entry : attriName2values.entrySet()) {
			if (entry.getValue().size() == 1) {
				json.put(entry.getKey(), entry.getValue().get(0));
			} else {
				JSONArray valueArray = JSONArray.fromObject(entry.getValue());
				json.put(entry.getKey(), valueArray);
			}
		}
		
		return json;
	}
	
	final private void parseAttributesFromPostData(HttpRequest httpRequest, Map<String, List<String>> attriName2values) throws Exception {
		assert (httpRequest != null);

		try {
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, httpRequest, charset);
			List<InterfaceHttpData> listOfData = decoder.getBodyHttpDatas();
			
			for (InterfaceHttpData data : listOfData) {
				parseRecivedData(data, attriName2values);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	final static private Map<String, List<String>> colone(Map<String, List<String>> map) {
		Map<String, List<String>> newMap = new HashMap<String, List<String>>();
		
		if (map == null || map.size() == 0) {
			return newMap;
		}
		
		for (Entry<String, List<String>> entry : map.entrySet()) {
			newMap.put(entry.getKey(), entry.getValue());
		}
		
		return newMap;
	}
	
	protected static final void parseRecivedData(InterfaceHttpData data, Map<String, List<String>> attrname2values) {
		assert (attrname2values != null);
		
		if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
            	LOG.error(String.format("Failed to reading the value for BODY Attribute:'%s':%s", 
            			attribute.getName(), attribute.getHttpDataType().name()), e1);
                return;
            }
            
            if (value.length() > 100) {
            	LOG.warn(String.format("BODY Attribute:%s:%s data too long", 
            			attribute.getHttpDataType().name(), attribute.getName()));
            }
            
            attrname2values.put(attribute.getName(), Arrays.asList(new String[]{value}));
        } else {
        	LOG.warn(String.format("Attribute:%s is not attribute, not surpport (%s) yet!", 
        			data.getName(), data.getHttpDataType().name()));
        }
	}
	
	final private String processPath(String path) {
		if (path == null) {
			throw new NullPointerException("path");
		}
		if (path.startsWith("/api")) {
			String str = path.substring(4);
			return str;
		} else {
			return path;
		}
	}
}
