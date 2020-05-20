package com.jerryshao.apinet.cluster;

import net.sf.json.JSONObject;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.jerryshao.apinet.cluster.common.IJsonSerializable;
import com.jerryshao.apinet.cluster.constant.ResultCode;
import com.jerryshao.apinet.cluster.constant.ResultField;
import com.jerryshao.apinet.cluster.exception.TaskResultJsonFormatException;

/**
 * TaskExecuteResult�������jerryshao.apinet.cluster.Cluster}�ļ�����<br />
 * ��������:
 * <li>taskId�������result��jerryshao.apinet.cluster.Task}����</li>
 * <li>code,�����result���ڵķ�����,jerryshao.apinet.cluster.constant.ResultCode}</li>
 * <li>result,�����result���ڵļ�����</li>
 * <li>message,������result��������Ϣ</li>
 *
 */
public class TaskResult implements IJsonSerializable {
	final protected String taskId;
	protected ResultCode code;
	protected JSONObject result;
	protected String message;

	public TaskResult(String taskId) {
		this(taskId, ResultCode.SUCCESS);
	}

	public TaskResult(String taskId, ResultCode code) {
		this(taskId, code, new JSONObject());
	}

	public TaskResult(String taskId, ResultCode code, JSONObject result) {
		this(taskId, code, result, code.toString());
	}

	public TaskResult(String taskId, ResultCode code, String message) {
		this(taskId, code, new JSONObject(), message);
	}

	public TaskResult(String taskId, ResultCode code, JSONObject result, String message) {
		if (taskId == null) {
			throw new NullPointerException("taskId");
		}
		if (code == null) {
			throw new NullPointerException("code");
		}
		if (result == null) {
			throw new NullPointerException("result");
		}
		if (message == null) {
			throw new NullPointerException("message");
		}
		this.taskId = taskId;
		this.code = code;
		this.result = result;
		this.message = message;
	}

	public ResultCode getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	public void setCode(ResultCode code) {
		if (code == null) {
			throw new NullPointerException("code");
		}
		this.code = code;
	}

	public void setMessage(String message) {
		if (message == null) {
			throw new NullPointerException("message");
		}
		this.message = message;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public JSONObject getResult() {
		return this.result;
	}

	public void setResult(JSONObject result) {
		if (result == null) {
			throw new NullPointerException("result");
		}
		this.result = result;
	}

	/**
	 * ���л�����<br />
	 * �ǽ�<code>TaskResult</code>���л���һ��<code>Json</code>��ʽ <br />
	 *
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put(ResultField.JSON_FIELD.getContent(), result);
		json.put(ResultField.TASK_FIELD.getContent(), taskId);
		json.put(ResultField.CODE_FIELD.getContent(), code.getCode());
		json.put(ResultField.MESSAGE_FIELD.getContent(), message);
		return json;
	}

	/**
	 * �ǽ�<code>TaskResult</code>ת����<code>HttpResponse</code><br />
	 * ����ֻ�ǽ�{@link #result}�е���Ϣ��װ��<code>Response</code>��<br />
	 * @return
	 */
	public HttpResponse toHttpResponse() {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
		buf.writeBytes(result.toString().getBytes(ClusterSettings.getClusterChannelCharset()));
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.setContent(buf);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, String.format("application/json; charset=%s", ClusterSettings.getClusterChannelCharset()));
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
		return response;
	}

	/**
	 * ��<code>json</code>�����л���һ��<code>TaskResult</code>.
	 * <li>{@link com.jerryshao.apinet.cluster.constant.ResultField.TASK_FIELD}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.ResultField.CODE_FIELD}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.ResultField.JSON_FIELD}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.ResultField.MESSAGE_FIELD}</li>
	 *
	 * @param json
	 * @return
	 */
	final static public TaskResult fromJson(JSONObject json) {
		if (null == json) {
			throw new NullPointerException("json");
		}

		checkTaskJsonFormat(json);
		String taskId = json.getString(ResultField.TASK_FIELD.getContent());
		ResultCode code = ResultCode.toRESULT_CODE(json.getInt(ResultField.CODE_FIELD.getContent()));
		JSONObject result = JSONObject.fromObject(json.getJSONObject(ResultField.JSON_FIELD.getContent()));
		String message = json.getString(ResultField.MESSAGE_FIELD.getContent());
		return new TaskResult(taskId, code, result, message);
	}

	final static private void checkTaskJsonFormat(JSONObject json) {
		assert (json != null);

		if (! json.containsKey(ResultField.TASK_FIELD.getContent())) {
			throw new TaskResultJsonFormatException(json, "not contains taskId");
		}
		if (! json.containsKey(ResultField.CODE_FIELD.getContent())) {
			throw new TaskResultJsonFormatException(json, "not contains code");
		}
		if (! json.containsKey(ResultField.JSON_FIELD.getContent())) {
			throw new TaskResultJsonFormatException(json, "not contains json");
		}
		if (! json.containsKey(ResultField.JSON_FIELD.getContent())) {
			throw new TaskResultJsonFormatException(json, "not contains message");
		}
	}
}
