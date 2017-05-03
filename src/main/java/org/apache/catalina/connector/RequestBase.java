package org.apache.catalina.connector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;

/**
 * <b>Request</b>接口的便利基础实现，大多数连接器所需的请求实现都可以使用它。只需要实现连接器特定的方法即可。
 * 
 * @author tequlia2pop
 * @deprecated 
 */
public abstract class RequestBase implements ServletRequest, Request {

	/**
	 * 关于此请求实现的描述性信息。
	 */
	protected static final String info = "org.apache.catalina.connector.RequestBase/1.0";

	/**
	 * 默认的 Locale（如果没有指定的话）。
	 */
	protected static Locale defaultLocale = Locale.getDefault();

	/**
	 * 这个包的 string manager。
	 */
	protected static StringManager sm = StringManager.getManager(Constants.Package);

	// ----------------------------------------------------- Instance Variables

	/**
	 * 与此请求关联的属性，键是属性的名称。
	 */
	protected HashMap<String, Object> attributes = new HashMap<>();

	/**
	 * 与此请求一起发送的授权凭证（authorization credentials）。
	 */
	protected String authorization = null;

	/**
	 * 此请求的字符编码。
	 */
	protected String characterEncoding = null;

	/**
	 * 收到此请求的连接器。
	 */
	protected Connector connector = null;

	/**
	 * 与此请求相关联的内容长度。
	 */
	protected int contentLength = -1;

	/**
	 * 与此请求相关联的内容类型。
	 */
	protected String contentType = null;

	/**
	 * 正在处理此请求的上下文。
	 */
	protected Context context = null;

	/**
	 * 与此请求相关的外观。
	 */
	protected RequestFacade facade = new RequestFacade(this);

	/**
	 * 与此请求相关联的输入流。
	 */
	protected InputStream input = null;

	/**
	 * 与此请求相关联的首选 Locale。
	 */
	protected ArrayList<Locale> locales = new ArrayList<>();

	/**
	 * Internal notes associated with this request by Catalina components
	 * and event listeners.
	 */
	private transient HashMap<String, Object> notes = new HashMap<>();

	/**
	 * 与此请求相关联的协议名称和版本。
	 */
	protected String protocol = null;

	/**
	 * 由<code>getReader</code>返回的 reader（如果存在的话）。
	 */
	protected BufferedReader reader = null;

	/**
	 * 与此请求相关联的远程地址。
	 */
	protected String remoteAddr = null;

	/**
	 * 远程主机的完全限定名称。
	 */
	protected String remoteHost = null;

	/**
	 * 与此请求相关联的响应。
	 */
	protected Response response = null;

	/**
	 * 与此请求相关联的 scheme。
	 */
	protected String scheme = null;

	/**
	 * 是否在安全连接上收到此请求？
	 */
	protected boolean secure = false;

	/**
	 * 与此请求相关联的服务器名称。
	 */
	protected String serverName = null;

	/**
	 * 与此请求相关联的服务器端口。
	 */
	protected int serverPort = -1;

	/**
	 * 收到此请求的套接字。
	 */
	protected Socket socket = null;

	/** 
	 * 由<code>getInputStream()</code>返回的 ServletInputStream（如果存在的话）
	 *  **/
	protected ServletInputStream stream = null;

	/**
	 * The Wrapper within which this Request is being processed.
	 */
	protected Wrapper wrapper = null;

	// ------------------------------------------------------------- Properties

	@Override
	public String getAuthorization() {
		return this.authorization;
	}

	@Override
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	@Override
	public Connector getConnector() {
		return this.connector;
	}

	@Override
	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	@Override
	public Context getContext() {
		return this.context;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public ServletRequest getRequest() {
		return facade;
	}

	@Override
	public Response getResponse() {
		return this.response;
	}

	@Override
	public void setResponse(Response response) {
		this.response = response;
	}

	@Override
	public Socket getSocket() {
		return this.socket;
	}

	@Override
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	@Override
	public InputStream getStream() {
		return this.input;
	}

	@Override
	public void setStream(InputStream input) {
		this.input = input;
	}

	@Override
	public Wrapper getWrapper() {
		return this.wrapper;
	}

	@Override
	public void setWrapper(Wrapper wrapper) {
		this.wrapper = wrapper;
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Add a Locale to the set of preferred Locales for this Request.  The
	 * first added Locale will be the first one returned by getLocales().
	 *
	 * @param locale The new preferred Locale
	 */
	public void addLocale(Locale locale) {
		synchronized (locales) {
			locales.add(locale);
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

	/**
	 * Perform whatever actions are required to flush and close the input
	 * stream or reader, in a single operation.
	 *
	 * @exception IOException if an input/output error occurs
	 */
	@Override
	public void finishRequest() throws IOException {
		// If a Reader has been acquired, close it
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				;
			}
		}

		// If a ServletInputStream has been acquired, close it
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				;
			}
		}

		// The underlying input stream (perhaps from a socket)
		// is not our responsibility
	}

	/**
	 * Return the object bound with the specified name to the internal notes
	 * for this request, or <code>null</code> if no such binding exists.
	 *
	 * @param name Name of the note to be returned
	 */
	@Override
	public Object getNote(String name) {
		synchronized (notes) {
			return notes.get(name);
		}
	}

	/**
	 * Return an Iterator containing the String names of all notes bindings
	 * that exist for this request.
	 */
	@Override
	public Iterator<String> getNoteNames() {
		synchronized (notes) {
			return notes.keySet().iterator();
		}
	}

	@Override
	public void recycle() {
		attributes.clear();
		authorization = null;
		characterEncoding = null;
		// 回收时不需重置连接器
		contentLength = -1;
		contentType = null;
		context = null;
		input = null;
		locales.clear();
		notes.clear();
		protocol = null;
		reader = null;
		remoteAddr = null;
		remoteHost = null;
		response = null;
		scheme = null;
		secure = false;
		serverName = null;
		serverPort = -1;
		socket = null;
		stream = null;
		wrapper = null;
	}

	/**
	 * Remove any object bound to the specified name in the internal notes
	 * for this request.
	 *
	 * @param name Name of the note to be removed
	 */
	@Override
	public void removeNote(String name) {
		synchronized (notes) {
			notes.remove(name);
		}
	}

	/**
	 * Set the content length associated with this Request.
	 *
	 * @param length The new content length
	 */
	@Override
	public void setContentLength(int length) {
		this.contentLength = length;
	}

	/**
	 * Set the content type (and optionally the character encoding)
	 * associated with this Request.  For example,
	 * <code>text/html; charset=ISO-8859-4</code>.
	 *
	 * @param type The new content type
	 */
	@Override
	public void setContentType(String type) {
		this.contentType = type;
		if (type.indexOf(';') >= 0)
			characterEncoding = RequestUtil.parseCharacterEncoding(type);
	}

	/**
	 * Bind an object to a specified name in the internal notes associated
	 * with this request, replacing any existing binding for this name.
	 *
	 * @param name Name to which the object should be bound
	 * @param value Object to be bound to the specified name
	 */
	@Override
	public void setNote(String name, Object value) {
		synchronized (notes) {
			notes.put(name, value);
		}
	}

	/**
	 * Set the protocol name and version associated with this Request.
	 *
	 * @param protocol Protocol name and version
	 */
	@Override
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Set the IP address of the remote client associated with this Request.
	 *
	 * @param remoteAddr The remote IP address
	 */
	@Override
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	/**
	 * Set the fully qualified name of the remote client associated with this
	 * Request.
	 *
	 * @param remoteHost The remote host name
	 */
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	/**
	 * Set the name of the scheme associated with this request.  Typical values
	 * are <code>http</code>, <code>https</code>, and <code>ftp</code>.
	 *
	 * @param scheme The scheme
	 */
	@Override
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * Set the value to be returned by <code>isSecure()</code>
	 * for this Request.
	 *
	 * @param secure The new isSecure value
	 */
	@Override
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * Set the name of the server (virtual host) to process this request.
	 *
	 * @param name The server name
	 */
	@Override
	public void setServerName(String name) {
		this.serverName = name;
	}

	@Override
	public void setServerPort(int port) {
		this.serverPort = port;
	}

	// ------------------------------------------------- ServletRequest Methods

	@Override
	public Object getAttribute(String name) {
		synchronized (attributes) {
			return attributes.get(name);
		}
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		synchronized (attributes) {
			return new Enumerator<>(attributes.keySet());
		}
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		// 确保指定的编码有效
		byte buffer[] = new byte[1];
		buffer[0] = (byte) 'a';
		String dummy = new String(buffer, enc);

		// 保存验证过的编码
		this.characterEncoding = enc;
	}

	@Override
	public int getContentLength() {
		return this.contentLength;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (reader != null)
			throw new IllegalStateException(sm.getString("requestBase.getInputStream.ise"));

		if (stream == null)
			stream = createInputStream();
		return stream;
	}

	@Override
	public abstract String getParameter(String name);

	@Override
	public abstract Enumeration<String> getParameterNames();

	@Override
	public abstract String[] getParameterValues(String name);

	@Override
	public abstract Map<String, String[]> getParameterMap();

	@Override
	public String getProtocol() {
		return this.protocol;
	}

	@Override
	public String getScheme() {
		return this.scheme;
	}

	@Override
	public String getServerName() {
		return this.serverName;
	}

	@Override
	public int getServerPort() {
		return this.serverPort;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (stream != null)
			throw new IllegalStateException(sm.getString("requestBase.getReader.ise"));

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
		return this.remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return this.remoteHost;
	}

	@Override
	public void setAttribute(String name, Object value) {
		// 名称不能为空
		if (name == null)
			throw new IllegalArgumentException(sm.getString("requestBase.setAttribute.namenull"));

		// 空的 value 等效于 removeAttribute()
		if (value == null) {
			removeAttribute(name);
			return;
		}

		synchronized (attributes) {
			attributes.put(name, value);
		}
	}

	@Override
	public void removeAttribute(String name) {
		synchronized (attributes) {
			attributes.remove(name);
		}
	}

	@Override
	public Locale getLocale() {
		synchronized (locales) {
			if (locales.size() > 0)
				return locales.get(0);
			else
				return defaultLocale;
		}
	}

	@Override
	public Enumeration<Locale> getLocales() {
		synchronized (locales) {
			if (locales.size() > 0)
				return new Enumerator<>(locales);
		}
		ArrayList<Locale> results = new ArrayList<>();
		results.add(defaultLocale);
		return new Enumerator<>(results);
	}

	@Override
	public boolean isSecure() {
		return this.secure;
	}

	@Override
	public abstract RequestDispatcher getRequestDispatcher(String path);

	/**
	 * @deprecated  As of Version 2.1 of the Java Servlet API,
	 *    use {@link ServletContext#getRealPath} instead.
	 */
	@Override
	public String getRealPath(String path) {
		if (context == null)
			return null;

		ServletContext servletContext = context.getServletContext();
		if (servletContext == null)
			return null;
		else {
			try {
				return servletContext.getRealPath(path);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

}
