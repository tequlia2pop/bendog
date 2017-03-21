package com.gmail.tequlia2pop.bendog.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

import com.gmail.tequlia2pop.bendog.connector.RequestStream;

/**
 * 这个类从 org.apache.catalina.connector.HttpRequestBase 
 * 和 org.apache.catalina.connector.http.HttpRequestImpl 复制方法。
 * HttpRequestImpl 类使用 HttpHeader对象池来提高性能。
 *
 * 其中很多方法都是空方法，但是已经可以从中获取引入的 HTTP 请求的请求头、Cookie 信息和请求参数等信息了。
 *
 * @author tequlia2pop
 */
public class HttpRequest implements HttpServletRequest {
	
	/**
	 * 用于返回空 Enumeration 的空 collection。不要向此集合添加任何元素！
	 */
	protected static ArrayList<String> empty = new ArrayList<>();

	/** content type（来自请求头） **/
	private String contentType;
	/** content length（来自请求头） **/
	private int contentLength;
	private InetAddress inetAddress;
	/** 包装了底层套接字输入流的 SocketInputStream **/
	private InputStream input;
	/** 请求方法 **/
	private String method;
	/** 请求协议和版本 **/
	private String protocol;
	/** 查询字符串 **/
	private String queryString;
	/** 请求 URI **/
	private String requestURI;
	private String serverName;
	private int serverPort;
	private Socket socket;
	/** 设置一个标志，它指示此请求的会话 ID 是否通过 Cookie 引入。这通常由 HTTP 连接器在解析请求头时调用 **/
	private boolean requestedSessionCookie;
	/** 会话 ID **/
	private String requestedSessionId;
	private boolean requestedSessionURL;

	/** 此请求的请求属性 **/
	protected HashMap<String, Object> attributes = new HashMap<>();
	/** 与此请求一起发送的授权凭据（authorization credentials） **/
	protected String authorization = null;
	/** 此请求的上下文路径 **/
	protected String contextPath = "";
	/** 与该请求相关联的一组 Cookie **/
	protected ArrayList<Cookie> cookies = new ArrayList<>();
	/** 在 getDateHeader() 中使用的 SimpleDateFormat 格式集 **/
	protected SimpleDateFormat formats[] = {
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };
	/** 与该请求相关联的 HTTP 请求头，以名称为键，值为相应请求头值的 ArrayList **/
	protected HashMap<String, ArrayList<String>> headers = new HashMap<>();

	/**
	 * 解析得到的该请求的参数。
	 * 仅当通过调用<code>getParameter()</code>方法调用系列中的某个方法来请求参数信息时，才会填充此参数。
	 * 键是参数名称，而值是参数值的 String 数组。
	 * 
	 * <p><strong>IMPLEMENTATION NOTE</strong> - 一旦解析并存储特定请求的参数后，不会对其进行修改。
	 * 因此，应用程序级访问参数不需要同步。
	 */
	protected ParameterMap<String, String[]> parameters = null;

	/** 是否已解析此请求的参数 **/
	protected boolean parsed = false;
	protected String pathInfo = null;
	/** 由<code>getReader</code>返回的 reader（如果存在的话） **/
	protected BufferedReader reader = null;
	/** 由<code>getInputStream()</code>返回的 ServletInputStream（如果存在的话） **/
	protected ServletInputStream stream = null;

	public HttpRequest(InputStream input) {
		this.input = input;
	}

	// --------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public void setContentLength(int length) {
		this.contentLength = length;
	}

	public void setContentType(String type) {
		this.contentType = type;
	}

	public void setContextPath(String path) {
		if (path == null)
			this.contextPath = "";
		else
			this.contextPath = path;
	}

	public void setInet(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setPathInfo(String path) {
		this.pathInfo = path;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public void setRequestedSessionId(String requestedSessionId) {
		this.requestedSessionId = requestedSessionId;
	}

	public void setRequestedSessionCookie(boolean flag) {
		this.requestedSessionCookie = flag;
	}

	public void setRequestedSessionURL(boolean flag) {
		requestedSessionURL = flag;
	}

	/**
	 * Set the name of the server (virtual host) to process this request.
	 *
	 * @param name The server name
	 */
	public void setServerName(String name) {
		this.serverName = name;
	}

	/**
	* Set the port number of the server to process this request.
	*
	* @param port The server port
	*/
	public void setServerPort(int port) {
		this.serverPort = port;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public InputStream getStream() {
		return input;
	}

	// --------------------------------------------------------------
	// Public Method
	// --------------------------------------------------------------

	/**
	 * 将请求头信息添加到 HttpRequest 的 HashMap 请求头中
	 * 
	 * @param name
	 * @param value
	 */
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

	/**
	* 解析此请求的参数（如果尚未发生）。如果查询字符串和请求内容中都存在参数，则会合并这些参数。
	*/
	protected void parseParameters() {
		if (parsed)// 若已完成对参数的解析，直接返回
			return;

		ParameterMap<String, String[]> results = parameters;
		if (results == null)
			results = new ParameterMap<>();

		results.setLocked(false);// 打开锁，使其可写

		String encoding = getCharacterEncoding();// return null
		if (encoding == null)// 使用默认编码
			encoding = "ISO-8859-1";

		// 解析查询字符串中指定的任意参数
		String queryString = getQueryString();
		try {
			RequestUtil.parseParameters(results, queryString, encoding);
		} catch (UnsupportedEncodingException e) {
			;
		}

		// 解析输入流中指定的任意参数
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
		if ("POST".equals(getMethod()) && (getContentLength() > 0)
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
					throw new RuntimeException("Content length mismatch");
				}
				RequestUtil.parseParameters(results, buf, encoding);
			} catch (UnsupportedEncodingException ue) {
				;
			} catch (IOException e) {
				throw new RuntimeException("Content read fail");
			}
		}

		// 存储最终的结果
		results.setLocked(true);
		parsed = true;
		parameters = results;
	}

	/**
	 * 将 Cookie 添加到  Cookie List 中。
	 * @param cookie
	 */
	public void addCookie(Cookie cookie) {
		synchronized (cookies) {
			cookies.add(cookie);
		}
	}

	/**
	 * 创建并返回一个 ServletInputStream 以读取与此请求相关联的内容。
	 * 默认实现会创建与此请求相关联的 RequestStream 实例，如果必要的话可以覆盖该方法。
	 *
	 * @exception IOException 如果发生输入/输出错误
	 */
	public ServletInputStream createInputStream() throws IOException {
		return new RequestStream(this);
	}

	// --------------------------------------------------------------
	// implementation of the ServletRequest
	// --------------------------------------------------------------

	@Override
	public Object getAttribute(String name) {
		synchronized (attributes) {
			return attributes.get(name);
		}
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		synchronized (attributes) {
			return new Enumerator(attributes.keySet());
		}
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
	}

	@Override
	public int getContentLength() {
		return contentLength;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (reader != null)
			throw new IllegalStateException("getInputStream has been called");

		if (stream == null)
			stream = createInputStream();
		return stream;
	}

	@Override
	public String getParameter(String name) {
		parseParameters();
		String values[] = (String[]) parameters.get(name);
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

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (stream != null)
			throw new IllegalStateException("getInputStream has been called.");

		if (reader == null) {
			String encoding = getCharacterEncoding();
			if (encoding == null)
				encoding = "ISO-8859-1";
			InputStreamReader isr = new InputStreamReader(createInputStream(), encoding);
			reader = new BufferedReader(isr);
		}
		return reader;
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public void setAttribute(String key, Object value) {
	}

	@Override
	public void removeAttribute(String attribute) {
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return null;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	@Deprecated
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	// --------------------------------------------------------------
	// implementation of the HttpServletRequest
	// --------------------------------------------------------------

	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		synchronized (cookies) {
			if (cookies.size() < 1)
				return null;
			Cookie[] results = new Cookie[cookies.size()];
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

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return null;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean authenticate(HttpServletResponse response)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}
}
