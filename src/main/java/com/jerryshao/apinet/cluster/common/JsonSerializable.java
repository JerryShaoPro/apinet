package com.jerryshao.apinet.cluster.common;

import net.sf.json.JSONObject;


public interface JsonSerializable {
	public JSONObject toJson() throws InterruptedException;
}
