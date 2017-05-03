package org.apache.catalina.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

/**
 * <b>HttpRequest</b>接口的便利基础实现，大多数实现了 HTTP 协议的连接器所需的请求实现都可以使用它。
 * 只需要实现连接器特定的方法。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public class HttpRequestBase extends RequestBase implements HttpRequest, HttpServletRequest {

	protected class PrivilegedGetSession implements PrivilegedAction<HttpSession> {
		private boolean create;

		PrivilegedGetSession(boolean create) {
			this.create = create;
		}

		@Override
		public HttpSession run() {
			return doGetSession(create);
		}
	}

	/**
	 * 用于返回空 Enumeration 的空集合。不要向此集合添加任何元素！
	 */
	protected static ArrayList<String> empty = new ArrayList<>();

	/**
	 * 有关此 HttpRequest 实现的描述性信息。
	 */
	protected static final String info = "org.apache.catalina.connector.HttpRequestBase/1.0";

	// ----------------------------------------------------- Instance Variables

	/**
	 * 用于此请求的身份验证类型。
	 */
	protected String authType = null;

	/**
	 * 这个请求的上下文路径。
	 */
	protected String contextPath = "";

	/**
	 * 与该请求相关联的一组 Cookie。
	 */
	protected ArrayList<Cookie> cookies = new ArrayList<>();

	/**
	 * The set of SimpleDateFormat formats to use in getDateHeader().
	 */
	protected SimpleDateFormat formats[] = {
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };

	/**
	 * 与此请求相关的外观。
	 */
	protected HttpRequestFacade facade = new HttpRequestFacade(this);

	/**
	 * 与该请求相关联的 HTTP 请求头，以名称为键，值为相应请求头值的 ArrayList。
	 */
	protected HashMap<String, ArrayList<String>> headers = new HashMap<>();

	/**
	 * 与此请求相关联的请求方法。
	 */
	protected String method = null;

	/**
	 * 解析得到的该请求的参数。
	 * 仅当通过调用<code>getParameter()</code>方法调用系列中的某个方法来请求参数信息时，才会填充此参数。
	 * 键是参数名称，而值是参数值的 String 数组。
	 * <p>
	 * <strong>IMPLEMENTATION NOTE</strong> - 一旦解析并存储特定请求的参数后，不会对其进行修改。
	 * 因此，应用程序级访问参数不需要同步。
	 */
	protected ParameterMap<String, String[]> parameters = null;

	/**
	 * 是否已解析此请求的参数
	 */
	protected boolean parsed = false;

	/**
	 * 此请求的路径信息。
	 */
	protected String pathInfo = null;

	/**
	 * 此请求的查询字符串。
	 */
	protected String queryString = null;

	/**
	 * 是在 cookie 中收到会话ID吗？
	 */
	protected boolean requestedSessionCookie = false;

	/**
	 * 请求的会话ID（如果有的话）。
	 */
	protected String requestedSessionId = null;

	/**
	 * 是在 URL 中收到会话ID吗？
	 */
	protected boolean requestedSessionURL = false;

	/**
	 * 与此请求相关联的请求 URI。
	 */
	protected String requestURI = null;

	/**
	 * 与此请求相关联的解码后的请求 URI。
	 */
	protected String decodedRequestURI = null;

	/**
	 * 这个请求是否是在安全的频道上收到的？
	 */
	protected boolean secure = false;

	/**
	 * 该请求的 servlet 路径。
	 */
	protected String servletPath = null;

	/**
	 * The currently active session for this request.
	 */
	protected Session session = null;

	/**
	 * The Principal who has been authenticated for this Request.
	 */
	protected Principal userPrincipal = null;

	// ------------------------------------------------------------- Properties

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public ServletRequest getRequest() {
		return facade;
	}

	// --------------------------------------------------------- Public Methods

	@Override
	public void addCookie(Cookie cookie) {
		synchronized (cookies) {
			cookies.add(cookie);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList<String> values = headers.get(name);
			if (values == null) {
				values = new ArrayList<>();
				headers.put(name, values);
			}
			values.add(value);
		}
	}

	@Override
	public void addParameter(String name, String values[]) {
		synchronized (parameters) {
			parameters.put(name, values);
		}
	}

	@Override
	public void clearCookies() {
		synchronized (cookies) {
			cookies.clear();
		}
	}

	@Override
	public void clearHeaders() {
		headers.clear();
	}

	@Override
	public void clearLocales() {
		locales.clear();
	}

	@Override
	public void clearParameters() {
		if (parameters != null) {
			parameters.setLocked(false);
			parameters.clear();
		} else {
			parameters = new ParameterMap<>();
		}
	}

	@Override
	public void recycle() {
		super.recycle();
		authType = null;
		contextPath = "";
		cookies.clear();
		headers.clear();
		method = null;
		if (parameters != null) {
			parameters.setLocked(false);
			parameters.clear();
		}
		parsed = false;
		pathInfo = null;
		queryString = null;
		requestedSessionCookie = false;
		requestedSessionId = null;
		requestedSessionURL = false;
		requestURI = null;
		decodedRequestURI = null;
		secure = false;
		servletPath = null;
		session = null;
		userPrincipal = null;
	}

	@Override
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	@Override
	public void setContextPath(String path) {
		if (path == null)
			this.contextPath = "";
		else
			this.contextPath = path;
	}

	@Override
	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public void setPathInfo(String path) {
		this.pathInfo = path;
	}

	@Override
	public void setQueryString(String query) {
		this.queryString = query;
	}

	@Override
	public void setRequestedSessionCookie(boolean flag) {
		this.requestedSessionCookie = flag;
	}

	@Override
	public void setRequestedSessionId(String id) {
		this.requestedSessionId = id;
	}

	@Override
	public void setRequestedSessionURL(boolean flag) {
		this.requestedSessionURL = flag;
	}

	@Override
	public void setRequestURI(String uri) {
		this.requestURI = uri;
	}

	@Override
	public void setSecure(boolean secure) {

	}

	@Override
	public void setServletPath(String path) {
		this.servletPath = path;
	}

	@Override
	public void setUserPrincipal(Principal principal) {
		this.userPrincipal = principal;
	}

	// ------------------------------------------------------ Protected Methods

	/**
	 * Parse the parameters of this request, if it has not already occurred.
	 * If parameters are present in both the query string and the request
	 * content, they are merged.
	 */
	protected void parseParameters() {
		if (parsed)
			return;

		ParameterMap<String, String[]> results = parameters;
		if (results == null)
			results = new ParameterMap<>();
		results.setLocked(false);

		String encoding = getCharacterEncoding();
		if (encoding == null)
			encoding = "ISO-8859-1";

		// 解析查询字符串中指定的任意参数
		String queryString = getQueryString();
		try {
			RequestUtil.parseParameters(results, queryString, encoding);
		} catch (UnsupportedEncodingException e) {
			;
		}

		// 解析输入流中指定的任意参数。
		// 如用户使用 POST 方法提交请求时，请求体会包含参数，
		// 则请求头"content-length"的值会大于0，"content-type"的值为"application/x-www-form-urlencoded"
		String contentType = getContentType();
		if (contentType == null)
			contentType = "";
		int semicolon = contentType.indexOf(';');
		if (semicolon >= 0) {
			contentType = contentType.substring(0, semicolon).trim();
		} else {
			contentType = contentType.trim();
		}
		if ("POST".equals(getMethod()) && (getContentLength() > 0) && (this.stream == null)
				&& "application/x-www-form-urlencoded".equals(contentType)) {
			try {
				int max = getContentLength();
				int len = 0;
				byte buf[] = new byte[getContentLength()];
				ServletInputStream is = getInputStream();
				while (len < max) {
					int next = is.read(buf, len, max - len);
					if (next < 0) {
						break;
					}
					len += next;
				}
				is.close();
				if (len < max) {
					// FIX ME, mod_jk when sending an HTTP POST will sometimes
					// have an actual content length received < content length.
					// Checking for a read of -1 above prevents this code from
					// going into an infinite loop.  But the bug must be in mod_jk.
					// Log additional data when this occurs to help debug mod_jk
					StringBuffer msg = new StringBuffer();
					msg.append("HttpRequestBase.parseParameters content length mismatch\n");
					msg.append("  URL: ");
					msg.append(getRequestURL());
					msg.append(" Content Length: ");
					msg.append(max);
					msg.append(" Read: ");
					msg.append(len);
					msg.append("\n  Bytes Read: ");
					if (len > 0) {
						msg.append(new String(buf, 0, len));
					}
					log(msg.toString());
					throw new RuntimeException(
							sm.getString("httpRequestBase.contentLengthMismatch"));
				}
				RequestUtil.parseParameters(results, buf, encoding);
			} catch (UnsupportedEncodingException ue) {
				;
			} catch (IOException e) {
				throw new RuntimeException(
						sm.getString("httpRequestBase.contentReadFail") + e.getMessage());
			}
		}

		// 存储最终的结果
		results.setLocked(true);
		parsed = true;
		parameters = results;
	}

	// ------------------------------------------------- ServletRequest Methods

	@Override
	public String getParameter(String name) {
		parseParameters();
		String values[] = parameters.get(name);
		if (values != null)
			return values[0];
		else
			return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		parseParameters();
		return new Enumerator<>(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		parseParameters();
		String values[] = parameters.get(name);
		if (values != null)
			return values;
		else
			return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		parseParameters();
		return this.parameters;
	}

	/**
	 * Return a RequestDispatcher that wraps the resource at the specified
	 * path, which may be interpreted as relative to the current request path.
	 * 
	 * @param path Path of the resource to be wrapped
	 */
	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		if (context == null)
			return null;

		// If the path is already context-relative, just pass it through
		if (path == null)
			return null;
		else if (path.startsWith("/"))
			return context.getServletContext().getRequestDispatcher(path);

		// Convert a request-relative path to a context-relative one
		String servletPath = (String) getAttribute(Globals.SERVLET_PATH_ATTR);
		if (servletPath == null)
			servletPath = getServletPath();

		int pos = servletPath.lastIndexOf('/');
		String relative = null;
		if (pos >= 0) {
			relative = RequestUtil.normalize(servletPath.substring(0, pos + 1) + path);
		} else {
			relative = RequestUtil.normalize(servletPath + path);
		}

		return context.getServletContext().getRequestDispatcher(relative);
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	// --------------------------------------------- HttpServletRequest Methods

	@Override
	public String getAuthType() {
		return authType;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public Cookie[] getCookies() {
		synchronized (cookies) {
			if (cookies.size() < 1)
				return null;
			Cookie results[] = new Cookie[cookies.size()];
			return cookies.toArray(results);
		}

	}

	@Override
	public long getDateHeader(String name) {
		String value = getHeader(name);
		if (value == null)
			return -1L;

		// Work around a bug in SimpleDateFormat in pre-JDK1.2b4
		// (Bug Parade bug #4106807)
		value += " ";

		// 尝试以各种格式转换日期请求头
		for (int i = 0; i < formats.length; i++) {
			try {
				Date date = formats[i].parse(value);
				return date.getTime();
			} catch (ParseException e) {
				;
			}
		}
		throw new IllegalArgumentException(value);
	}

	@Override
	public String getHeader(String name) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList<String> values = headers.get(name);
			if (values != null)
				return values.get(0);
			else
				return null;
		}
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList<String> values = headers.get(name);
			if (values != null)
				return new Enumerator<>(values);
			else
				return new Enumerator<>(empty);
		}
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		synchronized (headers) {
			return new Enumerator<>(headers.keySet());
		}
	}

	@Override
	public int getIntHeader(String name) {
		String value = getHeader(name);
		if (value == null)
			return -1;
		else
			return Integer.parseInt(value);
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	/**
	 * Return the extra path information for this request, translated
	 * to a real path.
	 */
	@Override
	public String getPathTranslated() {
		if (context == null)
			return null;

		if (pathInfo == null)
			return null;
		else
			return context.getServletContext().getRealPath(pathInfo);
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		if (userPrincipal != null)
			return userPrincipal.getName();
		else
			return null;
	}

	@Override
	public String getRequestedSessionId() {
		return requestedSessionId;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public void setDecodedRequestURI(String uri) {
		this.decodedRequestURI = uri;
	}

	@Override
	public String getDecodedRequestURI() {
		if (decodedRequestURI == null)
			decodedRequestURI = RequestUtil.URLDecode(getRequestURI());
		return decodedRequestURI;
	}

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer();
		String scheme = getScheme();
		int port = getServerPort();
		if (port < 0)
			port = 80; // Work around java.net.URL bug

		url.append(scheme);
		url.append("://");
		url.append(getServerName());
		if ((scheme.equals("http") && (port != 80))
				|| (scheme.equals("https") && (port != 443))) {
			url.append(':');
			url.append(port);
		}
		url.append(getRequestURI());
		return url;
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (System.getSecurityManager() != null) {
			PrivilegedGetSession dp = new PrivilegedGetSession(create);
			return AccessController.doPrivileged(dp);
		}
		return doGetSession(create);
	}

	private HttpSession doGetSession(boolean create) {
		// There cannot be a session if no context has been assigned yet
		if (context == null)
			return (null);

		// Return the current session if it exists and is valid
		if ((session != null) && !session.isValid())
			session = null;
		if (session != null)
			return (session.getSession());

		// Return the requested session if it exists and is valid
		Manager manager = null;
		if (context != null)
			manager = context.getManager();

		if (manager == null)
			return (null); // Sessions are not supported

		if (requestedSessionId != null) {
			try {
				session = manager.findSession(requestedSessionId);
			} catch (IOException e) {
				session = null;
			}
			if ((session != null) && !session.isValid())
				session = null;
			if (session != null) {
				return (session.getSession());
			}
		}

		// Create a new session if requested and the response is not committed
		if (!create)
			return (null);
		if ((context != null) && (response != null) && context.getCookies()
				&& response.getResponse().isCommitted()) {
			throw new IllegalStateException(sm.getString("httpRequestBase.createCommitted"));
		}

		session = manager.createSession();
		if (session != null)
			return (session.getSession());
		else
			return (null);

	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		if (requestedSessionId != null)
			return requestedSessionCookie;
		else
			return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		if (requestedSessionId != null)
			return requestedSessionURL;
		else
			return false;
	}

	/**
	 * Return <code>true</code> if the session identifier included in this
	 * request came from the request URI.
	 *
	 * @deprecated As of Version 2.1 of the Java Servlet API, use
	 *  <code>isRequestedSessionIdFromURL()</code> instead.
	 */
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		if (requestedSessionId == null)
			return false;
		if (context == null)
			return false;
		Manager manager = context.getManager();
		if (manager == null)
			return false;
		Session session = null;
		try {
			session = manager.findSession(requestedSessionId);
		} catch (IOException e) {
			session = null;
		}
		if ((session != null) && session.isValid())
			return true;
		else
			return false;

	}

	/**
	 * Return <code>true</code> if the authenticated user principal
	 * possesses the specified role name.
	 *
	 * @param role Role name to be validated
	 */
	@Override
	public boolean isUserInRole(String role) {
		// Have we got an authenticated principal at all?
		if (userPrincipal == null)
			return (false);

		// Identify the Realm we will use for checking role assignmenets
		if (context == null)
			return (false);
		Realm realm = context.getRealm();
		if (realm == null)
			return (false);

		// Check for a role alias defined in a <security-role-ref> element
		if (wrapper != null) {
			String realRole = wrapper.findSecurityReference(role);
			if ((realRole != null) && realm.hasRole(userPrincipal, realRole))
				return (true);
		}

		// Check for a role defined directly as a <security-role>
		return (realm.hasRole(userPrincipal, role));
	}

	@Override
	public Principal getUserPrincipal() {
		return userPrincipal;
	}

	private void log(String message) {
		Logger logger = context.getLogger();
		logger.log(message);
	}

	private void log(String message, Throwable throwable) {
		Logger logger = context.getLogger();
		logger.log(message, throwable);
	}

}
