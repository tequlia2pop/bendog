package org.apache.catalina;

/**
 * 组件生命周期方法的公共接口。 
 * Catalina 组件可以（但不是必须）实现该接口（以及它们支持的适当的功能接口），以便提供一致的机制来启动和停止组件。
 * 
 * @author tequlia2pop
 */
public interface Lifecycle {

	// ----------------------------------------------------- Manifest Constants

	/**
	 * LifecycleEvent 类型：“组件启动”事件。
	 */
	public static final String START_EVENT = "start";

	/**
	 * The LifecycleEvent type for the "component before start" event.
	 */
	public static final String BEFORE_START_EVENT = "before_start";

	/**
	 * The LifecycleEvent type for the "component after start" event.
	 */
	public static final String AFTER_START_EVENT = "after_start";

	/**
	 * The LifecycleEvent type for the "component stop" event.
	 */
	public static final String STOP_EVENT = "stop";

	/**
	 * The LifecycleEvent type for the "component before stop" event.
	 */
	public static final String BEFORE_STOP_EVENT = "before_stop";

	/**
	 * The LifecycleEvent type for the "component after stop" event.
	 */
	public static final String AFTER_STOP_EVENT = "after_stop";

	// --------------------------------------------------------- Public Methods

	/**
	 * Add a LifecycleEvent listener to this component.
	 *
	 * @param listener The listener to add
	 */
	public void addLifecycleListener(LifecycleListener listener);

	/**
	 * Get the lifecycle listeners associated with this lifecycle. If this 
	 * Lifecycle has no listeners registered, a zero-length array is returned.
	 */
	public LifecycleListener[] findLifecycleListeners();

	/**
	 * Remove a LifecycleEvent listener from this component.
	 *
	 * @param listener The listener to remove
	 */
	public void removeLifecycleListener(LifecycleListener listener);

	/**
	 * 准备开始积极使用这个组件的公共方法。 
	 * 在使用此组件的任何公共方法之前，应该调用此方法。
	 * 它也应该向任意注册的监听器发送一个类型为 START_EVENT 的 LifecycleEvent。
	 *
	 * @exception LifecycleException 如果此组件检测到一个致命的错误，将阻止使用此组件
	 */
	public void start() throws LifecycleException;

	/**
	 * Gracefully terminate the active use of the public methods of this
	 * component.  This method should be the last one called on a given
	 * instance of this component.  It should also send a LifecycleEvent
	 * of type STOP_EVENT to any registered listeners.
	 *
	 * @exception LifecycleException if this component detects a fatal error
	 *  that needs to be reported
	 */
	public void stop() throws LifecycleException;

}
