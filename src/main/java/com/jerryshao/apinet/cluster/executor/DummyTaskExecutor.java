package com.jerryshao.apinet.cluster.executor;

import net.sf.json.JSONObject;

import com.jerryshao.apinet.cluster.Task;
import com.jerryshao.apinet.cluster.TaskResult;
import com.jerryshao.apinet.cluster.exception.TaskExecuteException;

/**
 * ���ڲ��Ե�һ��ִ����.
 * ��{@link jerryshao.apinetrver.cluster.Task}�Ĳ�������ȥ
 * @author chuter & wuyadong
 *
 */
public class DummyTaskExecutor implements TaskExecutor {
	public TaskResult execute(Task task) throws TaskExecuteException {
		JSONObject result = task.getParamJson();
		TaskResult executeResult = new TaskResult(task.getId());
		executeResult.setResult(result);
		return executeResult;
	}
}
