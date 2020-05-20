package com.jerryshao.apinet.cluster.heartbeaat;

import net.sf.json.JSONObject;

import com.jerryshao.apinet.cluster.Task;

class ClusterPongTask extends Task {
	final static public String PONG_TASK_FLAG_ATTR = "is.pong";
	final static private JSONObject json;
	static {
		json = new JSONObject();
		json.put(PONG_TASK_FLAG_ATTR, true);
	}
	
	public ClusterPongTask() {
		this(Task.FAKE_TASK_ID, json, System.currentTimeMillis());
	}
	
	private ClusterPongTask(String id, JSONObject param, long startTime) {
		super(id, param, startTime);
	}
}
