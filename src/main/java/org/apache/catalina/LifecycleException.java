package org.apache.catalina;

/**
 * 抛出的通用异常表示生命周期相关的问题。这种异常通常被认为对包含该组件的应用程序的操作是致命的。
 * 
 * @author tequlia2pop
 */
public final class LifecycleException extends Exception {
	
	private static final long serialVersionUID = 1L;

	//------------------------------------------------------ Instance Variables

	/**
	 * 传递给构造函数的错误消息（如果有的话）
	 */
	protected String message = null;

	/**
	 * 传递给构造函数的底层异常或错误（如果有的话）
	 */
	protected Throwable throwable = null;

	//------------------------------------------------------------ Constructors

	/**
	 * 构造一个新的不带其他信息的 LifecycleException。
	 */
	public LifecycleException() {
		this(null, null);
	}

	/**
	 * 为指定的消息构造一个新的 LifecycleException。
	 *
	 * @param message 描述此异常的消息
	 */
	public LifecycleException(String message) {
		this(message, null);
	}

	/**
	 * 为指定的 throwable 构造一个新的 LifecycleException。
	 *
	 * @param throwable 引发这种异常的 Throwable
	 */
	public LifecycleException(Throwable throwable) {
		this(null, throwable);
	}

	/**
	 * 为指定的消息和 throwable 构造一个新的 LifecycleException。
	 *
	 * @param message 描述此异常的消息
	 * @param throwable 引发这种异常的 Throwable
	 */
	public LifecycleException(String message, Throwable throwable) {
		super();
		this.message = message;
		this.throwable = throwable;
	}

	//---------------------------------------------------------- Public Methods

	/**
	 * 返回与此异常关联的消息（如果有的话）。
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 返回引起此异常的 throwable（如果有的话）。
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * 返回描述此异常的格式化字符串。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("LifecycleException:  ");
		if (message != null) {
			sb.append(message);
			if (throwable != null) {
				sb.append(":  ");
			}
		}
		if (throwable != null) {
			sb.append(throwable.toString());
		}
		return sb.toString();
	}

}
