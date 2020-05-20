package com.jerryshao.apinet.cluster.exception;

import com.jerryshao.apinet.cluster.Task;

import net.sf.json.JSONObject;

public class TaskJsonFormatException extends RuntimeException {
	private static final long serialVersionUID = -1315692550522750026L;
	
	TaskJsonFormatException(Task task, String errorMesssage) {
		super(String.format("Failed to format task(%s) to json(%s)", task, errorMesssage));
	}
	
	public TaskJsonFormatException(JSONObject taskJson, String errorMesssage) {
		super(String.format("Failed to build task from json format(%s)(%s)", taskJson, errorMesssage));
	}
}
