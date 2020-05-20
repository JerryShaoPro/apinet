package com.jerryshao.apiserver.api;

import java.util.concurrent.atomic.AtomicBoolean;

import com.jerryshao.apinet.api.ApiLifecycle.State;
import com.jerryshao.apinet.settings.Settings;
import com.jerryshao.apinet.settings.SettingsException;
import com.jerryshao.apinet.util.Strings;
import com.jerryshao.apinet.util.ObjectsFactory;

import net.sf.json.JSONObject;

/**
 * <i>Api</i>描述<br>
 *
 * 每个<i>Api</i>具有{@link ApiLifecycle 生命周期}}，支持如下操作:<br>
 * <ul>
 * <li>{@link #start() 启动}</li>
 * <li>{@link #close() 关闭}</li>
 * <li>{@link #run(JSONObject) 执行}</li>
 * </ul>
 *
 * 可通过{@link #lifecycleState()}查询<i>Api</i>当前状态
 *
 * notice: 如果需要重试，抛出的异常必须是<code>ResultCode.SYSTEM_ERROR_NEED_RETRY</code>
 * @see com.jerryshao.apinet.cluster.constant.ResultCode#SYSTEM_ERROR_NEED_RETRY
 * 否则报出的ApiException的Code必须是<code>ResultCode.SYSTEM_ERROR_NOT_NEED_RETRY</code>
 *
 */
//TODO 实现通过配置文件获取对返回结果的域名处理的处理器
public abstract class Api {
	final static public String REMOTE_HOST_ATTR = "remote";
	final static public String RESPONSE_FIELDS_PROPERTY = "response.fields"; //每个Api配置的其结果信息中需要包括的字段, 如果对该属性进
	//行了配置，那么返回结果只包括配置的字段信息，否则包括全部
	//对返回结果的域名称的处理器的类名称配置项名称
	final static public String RESPONSE_FIELD_NAME_PROCESSOR_CLASS_PROPERTY = "response.fields.processor.class";

	final protected ApiLifecycle lifecycle = new ApiLifecycle();

	protected Settings settings = Settings.EMPTY_SETTINGS;

	final private AtomicBoolean hasBuildSettings = new AtomicBoolean(false);

	/**
	 * 执行一次操作
	 * @param paramJson json格式的参数信息
	 * @param apiCallMetadata api调用元信息
	 * @return json格式的执行结果
	 */
	public abstract JSONObject run(JSONObject paramJson, ApiCallMetadata apiCallMetadata) throws ApiException;

	public synchronized void start() throws ApiException {
		synchronized (lifecycle) {
			if (lifecycle.canMoveToStarted()) {
				//如果还没有构建配置信息，首先进行配置信息的构建
				if (! hasBuildSettings.get()) {
					getSettings();
				}

				doStart();
				lifecycle.moveToStarted();
			} else if (State.STARTED == lifecycleState()) { //已经启动，不进行任何操作
			} else {
				throw new ApiException(this, "Can not move to be started");
			}
		}
	}

	public synchronized void close() throws ApiException {
		synchronized (lifecycle) {
			if (lifecycle.canMoveToClosed()) {
				doClose();
				lifecycle.moveToClosed();
			} else {
				throw new ApiException(this, "Can not moved to be closed");
			}
		}
	}

	/**
	 * 获取<i>Api</i>的key（一个<i>Api</i>的唯一标识），用于和<i>Api</i>进行关联<br>
	 * 默认使用类名的小写作为一个<i>Api</i>的唯一标识<br>
	 * 通过覆写该方法返回特定的标识
	 *
	 * @return
	 */
	public String getApiName() {
		String apiName = this.getClass().getSimpleName();
		if (apiName.toLowerCase().endsWith("api")) {
			apiName = apiName.substring(0, apiName.length()-"api".length());
		}
		return Strings.toUnderscoreCase(apiName);
	}

	public Settings getSettings() {
		if (! hasBuildSettings.get()) { //如果还没有构建配置则首先进行构建
			synchronized (this) {
				if (! hasBuildSettings.get()) { //再进行一次检查
					buildSettings();
					hasBuildSettings.set(true);
				}
			}
		}

		return settings;
	}

	public void resetSettings(Settings settings) {
		synchronized (this) {
			this.settings = settings;
			apiResponsePropertyNameProcessor = null;
		}
	}

	/**
	 * 获取处理结果中需要返回的字段信息
	 * @return
	 */
	public String[] getResponseFields() {
		synchronized (settings) {
			return settings.getAsArray(RESPONSE_FIELDS_PROPERTY);
		}
	}

	public State lifecycleState() {
		synchronized (lifecycle) {
			return lifecycle.state();
		}
	}

	public ApiLifecycle lifecycle() {
		synchronized (lifecycle) {
			return lifecycle.clone();
		}
	}

	private IApiResponsePropertyNameProcessor apiResponsePropertyNameProcessor = null;

	public IApiResponsePropertyNameProcessor getResponsePropertyNameProcessor() {
		synchronized (this) {
			if (null == apiResponsePropertyNameProcessor) {
				Class<IApiResponsePropertyNameProcessor> processorClass = (Class<IApiResponsePropertyNameProcessor>) settings.getAsClass(RESPONSE_FIELD_NAME_PROCESSOR_CLASS_PROPERTY, null);

				if (null == processorClass) {
					apiResponsePropertyNameProcessor = DummyApiResponsePropertyNameProcessor.get();
				} else {
					try {
						apiResponsePropertyNameProcessor = ObjectsFactory.createObject(processorClass);
					} catch (Exception e) {
						throw new SettingsException("Can not create the property name processor instance by class: " + processorClass , e);
					}
				}
			}
		}

		return apiResponsePropertyNameProcessor;
	}

	/**
	 * 进行配置对象的构建，如果Api需要进行配置，那么通过复写该方法来完成Api的配置,
	 * 在Api启动的时候才会调用此方法
	 */
	protected synchronized void buildSettings() {
	}

	/**
	 * 进行启动操作
	 * @throws ApiException
	 */
	protected abstract void doStart() throws ApiException;

	/**
	 * 进行关闭操作
	 * @throws ApiException
	 */
	protected abstract void doClose() throws ApiException;

	/**
	 * 检查请求参数是否合法
	 * @param paramJson
	 * @return
	 */
	public abstract void checkRequstParam(JSONObject paramJson, ApiOperateAction apiOperateAction) throws IllegalApiArugmentException;

}
