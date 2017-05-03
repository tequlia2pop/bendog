package org.apache.catalina;

import java.security.Principal;
import java.util.Locale;

import javax.servlet.http.Cookie;

/**
 * <b>HttpRequest</b>是要处理的<code>HttpServletRequest</code>的 Catalina 内部的外观，
 * 用于产生相应的<code>HttpResponse</code>。
 * 
 * @author tequlia2pop
 *
 */
public interface HttpRequest extends Request {

	// --------------------------------------------------------- Public Methods

	/**
	 * 将一个 Cookie 添加到与请求相关的  Cookie 集合中。
	 *
	 * @param cookie 新 cookie
	 */
	public void addCookie(Cookie cookie);

	/**
	 * 将请求头信息添加到与请求相关的请求头集合中。
	 *
	 * @param name 请求头的名称
	 * @param value 请求头的值
	 */
	public void addHeader(String name, String value);

	/**
	 * Add a Locale to the set of preferred Locales for this Request.  The
	 * first added Locale will be the first one returned by getLocales().
	 *
	 * @param locale The new preferred Locale
	 */
	public void addLocale(Locale locale);

	/**
	 * 向此请求添加一个参数的名称和相应的值集合（在基于表单的登录上恢复原始请求时使用）。
	 *
	 * @param name 请求参数的名称
	 * @param values 请求参数相应的值
	 */
	public void addParameter(String name, String values[]);

	/**
	 * 清除与此请求相关联的 Cookie 集合。
	 */
	public void clearCookies();

	/**
	 * 清除与此请求相关联的请求头集合。
	 */
	public void clearHeaders();

	/**
	 * 清除与此请求相关联的 Locale 集合。
	 */
	public void clearLocales();

	/**
	 * 清除与此请求相关联的参数集合。
	 */
	public void clearParameters();

	/**
	 * 设置用于此请求的身份验证类型（如果有的话）;
	 * 否则将类型设置为<code>null</code>。
	 * 典型的值包括 "BASIC","DIGEST", 或 "SSL"。
	 *
	 * @param type The authentication type used
	 */
	public void setAuthType(String type);

	/**
	 * 设置此请求的上下文路径。
	 * 当相关联的上下文将请求映射到特定的包装器时，通常将调用该方法。
	 *
	 * @param path The context path
	 */
	public void setContextPath(String path);

	/**
	 * 设置用于此请求的 HTTP 请求方法。
	 *
	 * @param method The request method
	 */
	public void setMethod(String method);

	/**
	 * 设置此请求的查询字符串。当 HTTP 连接器解析请求头时通常会调用这个方法。
	 *
	 * @param query The query string
	 */
	public void setQueryString(String query);

	/**
	 * 设置此请求的路径信息。当相关联的上下文将请求映射到特定的包装器时，这通常将被调用。
	 *
	 * @param path The path information
	 */
	public void setPathInfo(String path);

	/**
	 * 设置一个标志，它指示了请求的会话ID是否通过 cookie 引入。这通常由 HTTP 连接器在解析请求头时调用。
	 *
	 * @param flag The new flag
	 */
	public void setRequestedSessionCookie(boolean flag);

	/**
	 * 设置请求的会话ID。这通常由 HTTP 连接器在解析请求头时调用。
	 *
	 * @param id The new session id
	 */
	public void setRequestedSessionId(String id);

	/**
	 * 设置一个标志，它指示了请求的会话ID是否通过 URL 引入。这通常由 HTTP 连接器在解析请求头时调用。
	 *
	 * @param flag The new flag
	 */
	public void setRequestedSessionURL(boolean flag);

	/**
	 * 设置此请求的未解析的请求 URI。当 HTTP 连接器解析请求头时通常会调用这个方法。
	 *
	 * @param uri The request URI
	 */
	public void setRequestURI(String uri);

	/**
	 * Set the decoded request URI.
	 * 
	 * @param uri The decoded request URI
	 */
	public void setDecodedRequestURI(String uri);

	/**
	 * Get the decoded request URI.
	 * 
	 * @return the URL decoded request URI
	 */
	public String getDecodedRequestURI();

	/**
	 * 设置此请求的 servlet 路径。当相关联的上下文将请求映射到特定的包装器时，通常会调用该方法。
	 *
	 * @param path The servlet path
	 */
	public void setServletPath(String path);

	/**
	 * Set the Principal who has been authenticated for this Request.  This
	 * value is also used to calculate the value to be returned by the
	 * <code>getRemoteUser()</code> method.
	 * 
	 * @param principal The user Principal
	 */
	public void setUserPrincipal(Principal principal);

}
