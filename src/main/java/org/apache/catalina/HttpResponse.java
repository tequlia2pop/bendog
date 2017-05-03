package org.apache.catalina;

import javax.servlet.http.Cookie;

/**
 * <b>HttpResponse</b>是<code>HttpServletResponse</code>的 Catalina 内部的外观，
 * 它是基于对应的<code>HttpRequest</code>的处理而生成的。
 * 
 * @author tequlia2pop
 */
public interface HttpResponse extends Response {

	// --------------------------------------------------------- Public Methods

	/**
	 * 返回为此响应设置的所有 Cookie 的数组，如果未设置 Cookie，则返回零长度的数组。
	 */
	public Cookie[] getCookies();

	/**
	 * 返回指定响应头的值，如果未设置此响应头则返回<code>null</code>。
	 * 如果为此名称添加了多个值，则只返回第一个值; 使用getHeaderValues() 来检索所有的值。
	 *
	 * @param name 要查找的响应头名称
	 */
	public String getHeader(String name);

	/**
	 * 返回为此响应设置的所有响应头的名称的数组，如果未设置响应头，则返回零长度的数组。
	 */
	public String[] getHeaderNames();

	/**
	 * 返回与指定的响应头名称相关联的所有响应头的值的数组，如果没有这样的响应头的值，则返回零长度数组。
	 *
	 * @param name 要查找的响应头的名称
	 */
	public String[] getHeaderValues(String name);

	/**
	 * 返回该响应的错误消息，它是通过<code>sendError()</code>设置的。
	 */
	public String getMessage();

	/**
	 * 返回与此响应关联的 HTTP 状态码。
	 */
	public int getStatus();

	/**
	 * 重置此响应，并指定 HTTP 状态码和相应消息的值。
	 *
	 * @exception IllegalStateException 如果响应已经提交了
	 */
	public void reset(int status, String message);

}
