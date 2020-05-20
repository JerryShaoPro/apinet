package com.jerryshao.apinet.cluster;

import java.util.UUID;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import com.jerryshao.apinet.cluster.common.IJsonSerializable;
import com.jerryshao.apinet.cluster.constant.TaskJsonNames;
import com.jerryshao.apinet.cluster.exception.TaskJsonFormatException;
/**
 * <code>Task</code>��������<code>Cluster</code>��<code>Node</code>֮�䴫�ݵ����ݽṹ<br />
 * ��������Ԫ��:<br />
 * <li>id��������Ψһ��Task�ı�ʶ</li>
 * <li>startTime��������taskִ�еĿ�ʼʱ��</li>
 * <li>retryCount��������task�Ѿ��ط��Ĵ���</li>
 * <li>param��������task�Ĳ���</li>
 * 
 */
public class Task implements IJsonSerializable {
	final static public Task EMPTY_TASK ;
	final static public String FAKE_TASK_ID = "fake_task_id";
	static {
		JSONObject json = new JSONObject();
		json.put("hello", "world");
		EMPTY_TASK = new Task("dummy_task" + UUID.randomUUID(), json, System.currentTimeMillis());
	}
	
	private String id;
	private long startTime;
	protected JSONObject param;
	private int retryCount;
	
	@SuppressWarnings("unchecked")
	public Task(String id,JSONObject param, long startTime) {
		if (null == id || null == param) {
			throw new NullPointerException("id or param");
		}
		
		this.id = new String(id);
		this.param = new JSONObject();
		this.startTime = startTime;
		for (Object object : param.entrySet()) {
			Entry<Object, Object> entry = (Entry<Object, Object>) object;
			this.param.put(entry.getKey(), entry.getValue());
		}
		this.retryCount = 0;
	}
	
	public String getId() {
		return id;
	}
	
	public long getStartTime() {
		return this.startTime;
	}
	
	public void setStartTime(long time) {
		this.startTime = time;
	}
	
	public int getRetryCount() {
		return this.retryCount;
	}
	
	public void increaseRetryCount() {
		this.retryCount ++;
	}
	
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	
	/**
	 * ����Task�Ĳ�����Ϣ<br />
	 * ע�����ǿ�¡���޸Ĳ���ı�task�Ĳ�������
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getParamJson() {
		JSONObject clonedParamJson = new JSONObject();
		for (Object entryObject : param.entrySet()) {
			Entry<String, Object> entry = (Entry<String, Object>) entryObject;
			clonedParamJson.put(entry.getKey(), entry.getValue());
		}
		return clonedParamJson;
	}
	
	@Override
	public String toString() {
		String str = String.format("id:'%s', param:'%s', startTime:'%d', retryCount:'%d'", id, param.toString(), startTime, retryCount);
		return str;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Task)) {
			return false;
		} else {
			Task task = (Task) obj;
			return task.getId().equals(id);
		}
	}
	
	/**
	 * �ǽ�<code>Task</code>���л���һ��<code>JSON</code>��ʽ <br />
	 * ��������Ԫ��<br />
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#Task_ID}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#TASK_ATTRIBUTES}}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#TASK_RETRY_COUNT}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#TASK_START_TIME}</li>
	 */
	final public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put(TaskJsonNames.TASK_ID.getContent(), id);
		json.put(TaskJsonNames.TASK_ATTRIBUTES.getContent(), param);
		json.put(TaskJsonNames.TASK_START_TIME.getContent(), startTime);
		json.put(TaskJsonNames.TASK_RETRY_COUNT.getContent(), retryCount);
		return json;
	}
	
	/**
	 * ��<code>Json</code>ת����Task
	 * �����ʽ����ȷ���׳�checkTaskJsonFormat
	 * JSON�б����������Ԫ��<br />
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#Task_ID}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#TASK_ATTRIBUTES}}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#TASK_RETRY_COUNT}</li>
	 * <li>{@link com.jerryshao.apinet.cluster.constant.TaskJsonNames#TASK_START_TIME}</li>
	 * @param taskJsonFormt
	 * @return
	 */
	final static public Task fromJson(JSONObject taskJsonFormt) {
		if (null == taskJsonFormt) {
			throw new NullPointerException("json");
		}
		
		checkTaskJsonFormat(taskJsonFormt);
		String taskId = taskJsonFormt.getString(TaskJsonNames.TASK_ID.getContent());
		JSONObject json = taskJsonFormt.getJSONObject(TaskJsonNames.TASK_ATTRIBUTES.getContent());
		long startTime = taskJsonFormt.getLong(TaskJsonNames.TASK_START_TIME.getContent());
		int retryCount = taskJsonFormt.getInt(TaskJsonNames.TASK_RETRY_COUNT.getContent());
		Task task = new Task(taskId, json, startTime);
		task.setRetryCount(retryCount);
		return task;
	}
	
	final static private void checkTaskJsonFormat(JSONObject json) {
		assert (json != null);
		
		if (! json.containsKey(TaskJsonNames.TASK_ID.getContent())) {
			throw new TaskJsonFormatException(json, "no task id");
		}
		
		if (! json.containsKey(TaskJsonNames.TASK_START_TIME.getContent())) {
			throw new TaskJsonFormatException(json, "no task start time");
		}
		
		if (! json.containsKey(TaskJsonNames.TASK_RETRY_COUNT.getContent())) {
			throw new TaskJsonFormatException(json, "no task retry count");
		}
		
		if (! json.containsKey(TaskJsonNames.TASK_ATTRIBUTES.getContent())) {
			throw new TaskJsonFormatException(json, "no task attributes");
		} else {
			Object attri = json.get(TaskJsonNames.TASK_ATTRIBUTES.getContent());
			try {
				JSONObject.fromObject(attri);
			} catch (Exception e) {
				throw new TaskJsonFormatException(json, "attributes format! error");
			}
		}
	}
}
