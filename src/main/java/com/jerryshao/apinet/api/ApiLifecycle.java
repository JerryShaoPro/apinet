package com.jerryshao.apinet.api;

/**
 * {@link Api}生命周期，支持以下状态转移 ：<br>
 * <pre>
 *                 start()
 * * INITIALIZED ----------> STARTED
 *                 close()
 * * INITIALIZED ----------> CLOSED
 *             close()
 * * STARTED ----------> CLOSED
 * </pre>
 *
 * <p>
 * 初始状态为INITIALIZED，结束状态为CLOSED，当一个Api状态为CLOSED状态时不允许任何操作。
 * 状态转换中允许处于原来的状态，比如当调用close()操作，下面的逻辑是可以被应用的：
 *
 * <pre>
 * public void close() {
 * 	if (!lifeccycleState.moveToClosed()) {
 * 		return;
 * 	}
 * 	// continue with close logic
 * }
 * </pre>
 *
 * </p>
 *
 */
public class ApiLifecycle {

	public static enum State {
		INITIALIZED,
		STARTED,
		CLOSED
	}

	private volatile State state = State.INITIALIZED;

	public State state() {
		return this.state;
	}

	/**
	 * Returns <tt>true</tt> if the state is initialized.
	 */
	public boolean initialized() {
		return state == State.INITIALIZED;
	}

	/**
	 * Returns <tt>true</tt> if the state is started.
	 */
	public boolean started() {
		return state == State.STARTED;
	}

	/**
	 * Returns <tt>true</tt> if the state is closed.
	 */
	public boolean closed() {
		return state == State.CLOSED;
	}

	public boolean canMoveToStarted() throws IllegalApiStateException {
		State localState = this.state;
		if (localState == State.INITIALIZED) {
			return true;
		}

		if (localState == State.STARTED) {
			return false;
		}

		if (localState == State.CLOSED) {
			throw new IllegalApiStateException("Can't move to started state when closed");
		}

		throw new IllegalApiStateException("Can't move to started with unknown state");
	}

	public boolean moveToStarted() throws IllegalApiStateException {
		State localState = this.state;
		if (localState == State.INITIALIZED) {
			state = State.STARTED;
			return true;
		}

		if (localState == State.STARTED) {
			return false;
		}

		if (localState == State.CLOSED) {
			throw new IllegalApiStateException("Can't move to started state when closed");
		}

		throw new IllegalApiStateException("Can't move to started with unknown state");
	}

	public boolean canMoveToClosed() throws IllegalApiStateException {
		State localState = state;

		if (localState == State.INITIALIZED || localState == State.STARTED) {
			return true;
		}

		if (localState == State.CLOSED) {
			return false;
		}

		throw new IllegalApiStateException("Can't move to closed with unknown state");
	}

	public boolean moveToClosed() throws IllegalApiStateException {
		State localState = this.state;
		if (localState == State.INITIALIZED || localState == State.STARTED) {
			state = State.CLOSED;
			return true;
		}

		if (localState == State.CLOSED) {
			return false;
		}

		throw new IllegalApiStateException("Can't move to closed with unknown state");
	}

	@Override
	public String toString() {
		return state.toString();
	}

	public ApiLifecycle clone() {
		ApiLifecycle copy = new ApiLifecycle();
		copy.state = this.state;
		return copy;
	}

}
