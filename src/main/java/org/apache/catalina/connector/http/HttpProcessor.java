package org.apache.catalina.connector.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.util.FastHttpDateFormat;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.StringParser;

/**
 * 请求处理器的实现（及其相关线程），HttpConnector 可以使用它来处理单个请求。
 * 连接器将从处理器实例池中分配一个处理器，为其分配一个特定的套接字，然后处理器将对请求执行处理。处理器执行完成后将被自动回收。
 * 
 * @author tequlia2pop
 * @deprecated
 */
final class HttpProcessor implements Lifecycle, Runnable {

	// ----------------------------------------------------- Manifest Constants

	/**
	 * 此服务器的服务器信息字符串。
	 */
	private static final String SERVER_INFO = ServerInfo.getServerInfo()
			+ " (HTTP/1.1 Connector)";

	// ----------------------------------------------------- Class Variables

	/**
	 * The match string for identifying a session ID parameter.
	 */
	private static final String match = ";" + Globals.SESSION_PARAMETER_NAME + "=";

	/**
	 * The match string for identifying a session ID parameter.
	 */
	private static final char[] SESSION_ID = match.toCharArray();

	/**
	 * Ack string when pipelining HTTP requests.
	 */
	private static final byte[] ack = (new String("HTTP/1.1 100 Continue\r\n\r\n")).getBytes();

	/**
	 * CRLF.
	 */
	private static final byte[] CRLF = (new String("\r\n")).getBytes();

	// ----------------------------------------------------- Instance Variables

	/**
	 * 是否有新的可用套接字？
	 */
	private boolean available = false;

	/**
	 * 与此处理器关联的 HttpConnector。
	 */
	private HttpConnector connector = null;

	/**
	 * 该组件的调试详细级别。
	 */
	private int debug = 0;

	/**
	 * 该处理器的标识符，对于连接器来说它是唯一的。
	 */
	private int id = 0;

	/**
	 * The lifecycle event support for this component.
	 */
	private LifecycleSupport lifecycle = new LifecycleSupport(this);

	/**
	 * The string parser we will use for parsing request lines.
	 */
	private StringParser parser = new StringParser();

	/**
	 * 连接器的代理服务器名称。
	 */
	private String proxyName = null;

	/**
	 * 连接器的代理服务器的端口。
	 */
	private int proxyPort = 0;

	/**
	 * 将传递给关联容器的 HTTP 请求对象。
	 */
	private HttpRequestImpl request = null;

	/**
	 * 将传递给关联容器的 HTTP 响应对象。
	 */
	private HttpResponseImpl response = null;

	/**
	 * 连接器的实际服务器的端口。
	 */
	private int serverPort = 0;

	/**
	 * The string manager for this package.
	 */
	protected StringManager sm = StringManager.getManager(Constants.Package);

	/**
	 * 我们正在处理的请求的套接字。此对象仅用于 inter-thread 通信。
	 */
	private Socket socket = null;

	/**
	 * 这个组件是否已经启动？
	 */
	private boolean started = false;

	/**
	 * 关闭后台线程的信号
	 */
	private boolean stopped = false;

	/**
	 * 后台线程。
	 */
	private Thread thread = null;

	/**
	 * 后台线程的注册名称。
	 */
	private String threadName = null;

	/**
	 * The thread synchronization object.
	 */
	private Object threadSync = new Object();

	/**
	 * 表明该连接是否是持久连接。
	 */
	private boolean keepAlive = false;

	/**
	 * 表明 HTTP 请求是否是从支持 HTTP 1.1 的客户端发送来的。
	 */
	private boolean http11 = true;

	/**
	 * 如果客户要求收到请求确认，则为 true。
	 * 如果是这样的话，在服务器成功解析请求头之后，开始读取请求实体之前，服务器将发送一个初步的 100 Continue 的响应。
	 */
	private boolean sendAck = false;

	/**
	 * 请求行缓冲区。
	 */
	private HttpRequestLine requestLine = new HttpRequestLine();

	/**
	 * 处理器的状态。
	 */
	private int status = Constants.PROCESSOR_IDLE;

	// ----------------------------------------------------------- Constructors

	/**
	 * 构造一个新的与指定连接器关联的 HttpProcessor。
	 *
	 * @param connector 拥有该处理器的 HttpConnector
	 * @param id 此 HttpProcessor 的标识（对于每个连接器来说是唯一的）
	 */
	public HttpProcessor(HttpConnector connector, int id) {
		super();
		this.connector = connector;
		this.debug = connector.getDebug();
		this.id = id;
		this.proxyName = connector.getProxyName();
		this.proxyPort = connector.getProxyPort();
		this.request = (HttpRequestImpl) connector.createRequest();
		this.response = (HttpResponseImpl) connector.createResponse();
		this.serverPort = connector.getPort();
		this.threadName = "HttpProcessor[" + connector.getPort() + "][" + id + "]";
	}

	// --------------------------------------------------------- Public Methods

	@Override
	public String toString() {
		return this.threadName;
	}

	// -------------------------------------------------------- Package Methods

	/**
	 * 处理指定套接字上传入的 TCP/IP 连接。
	 * 在处理过程中发生的任何异常都必须被记录和 swallowed。
	 * <b>注意</b>：是从 Connector 的线程中调用的这个方法。
	 * 我们必须将其分配给我们自己的线程，以便可以处理多个同时发送的请求。
	 *
	 * @param socket 要处理的 TCP 套接字
	 */
	synchronized void assign(Socket socket) {
		// 等待处理器获取上一个套接字
		while (available) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}

		// 存储新提供的套接字，并通知我们的线程
		this.socket = socket;
		available = true;
		notifyAll();

		if ((debug >= 1) && (socket != null))
			log(" An incoming request is being assigned");
	}

	// -------------------------------------------------------- Private Methods

	/**
	 * 等待由连接器分配的新套接字，如果我们应该关闭它的话，则返回 <code>null</code>。
	 * 
	 * 这里使用局部变量 socket，而不直接将成员变量 socket 返回。
	 * 因为使用局部变量可以在当前 Socket 对象处理完之前，继续接收下一个 Socket 对象。
	 */
	private synchronized Socket await() {
		// 等待连接器提供一个新的套接字
		while (!available) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}

		// 通知连接器，告知它我们已经收到此套接字
		Socket socket = this.socket;
		available = false;
		notifyAll();

		if ((debug >= 1) && (socket != null))
			log("  The incoming request has been awaited");

		return socket;
	}

	/**
	 * Log a message on the Logger associated with our Container (if any)
	 *
	 * @param message Message to be logged
	 */
	private void log(String message) {
		Logger logger = connector.getContainer().getLogger();
		if (logger != null)
			logger.log(threadName + " " + message);
	}

	/**
	 * Log a message on the Logger associated with our Container (if any)
	 *
	 * @param message Message to be logged
	 * @param throwable Associated exception
	 */
	private void log(String message, Throwable throwable) {
		Logger logger = connector.getContainer().getLogger();
		if (logger != null)
			logger.log(threadName + " " + message, throwable);
	}

	/**
	 * Parse the value of an <code>Accept-Language</code> header, and add
	 * the corresponding Locales to the current request.
	 *
	 * @param value The value of the <code>Accept-Language</code> header.
	 */
	private void parseAcceptLanguage(String value) {
		// Store the accumulated languages that have been requested in
		// a local collection, sorted by the quality value (so we can
		// add Locales in descending order).  The values will be ArrayLists
		// containing the corresponding Locales to be added
		TreeMap locales = new TreeMap();

		// Preprocess the value to remove all whitespace
		int white = value.indexOf(' ');
		if (white < 0)
			white = value.indexOf('\t');
		if (white >= 0) {
			StringBuffer sb = new StringBuffer();
			int len = value.length();
			for (int i = 0; i < len; i++) {
				char ch = value.charAt(i);
				if ((ch != ' ') && (ch != '\t'))
					sb.append(ch);
			}
			value = sb.toString();
		}

		// Process each comma-delimited language specification
		parser.setString(value); // ASSERT: parser is available to us
		int length = parser.getLength();
		while (true) {

			// Extract the next comma-delimited entry
			int start = parser.getIndex();
			if (start >= length)
				break;
			int end = parser.findChar(',');
			String entry = parser.extract(start, end).trim();
			parser.advance(); // For the following entry

			// Extract the quality factor for this entry
			double quality = 1.0;
			int semi = entry.indexOf(";q=");
			if (semi >= 0) {
				try {
					quality = Double.parseDouble(entry.substring(semi + 3));
				} catch (NumberFormatException e) {
					quality = 0.0;
				}
				entry = entry.substring(0, semi);
			}

			// Skip entries we are not going to keep track of
			if (quality < 0.00005)
				continue; // Zero (or effectively zero) quality factors
			if ("*".equals(entry))
				continue; // FIXME - "*" entries are not handled

			// Extract the language and country for this entry
			String language = null;
			String country = null;
			String variant = null;
			int dash = entry.indexOf('-');
			if (dash < 0) {
				language = entry;
				country = "";
				variant = "";
			} else {
				language = entry.substring(0, dash);
				country = entry.substring(dash + 1);
				int vDash = country.indexOf('-');
				if (vDash > 0) {
					String cTemp = country.substring(0, vDash);
					variant = country.substring(vDash + 1);
					country = cTemp;
				} else {
					variant = "";
				}
			}

			// Add a new Locale to the list of Locales for this quality level
			Locale locale = new Locale(language, country, variant);
			Double key = new Double(-quality); // Reverse the order
			ArrayList values = (ArrayList) locales.get(key);
			if (values == null) {
				values = new ArrayList();
				locales.put(key, values);
			}
			values.add(locale);

		}

		// Process the quality values in highest->lowest order (due to
		// negating the Double value when creating the key)
		Iterator keys = locales.keySet().iterator();
		while (keys.hasNext()) {
			Double key = (Double) keys.next();
			ArrayList list = (ArrayList) locales.get(key);
			Iterator values = list.iterator();
			while (values.hasNext()) {
				Locale locale = (Locale) values.next();
				if (debug >= 1)
					log(" Adding locale '" + locale + "'");
				request.addLocale(locale);
			}
		}

	}

	/**
	 * 解析并记录与此请求相关的连接参数。
	 *
	 * @param socket 连接的套接字
	 *
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a parsing error occurs
	 */
	private void parseConnection(Socket socket) throws IOException, ServletException {
		if (debug >= 2)
			log("  parseConnection: address=" + socket.getInetAddress() + ", port="
					+ connector.getPort());
		((HttpRequestImpl) request).setInet(socket.getInetAddress());
		if (proxyPort != 0)
			request.setServerPort(proxyPort);
		else
			request.setServerPort(serverPort);
		request.setSocket(socket);
	}

	/**
	 * 解析传入的 HTTP 请求头，并设置相应的请求头。
	 *
	 * @param input 连接到套接字的输入流
	 *
	 * @exception IOException 如果发生输入/输出错误
	 * @exception ServletException 如果发生解析错误
	 */
	private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
		// 循环地从 SocketInputStream 中读取请求头信息。
		while (true) {
			HttpHeader header = request.allocateHeader();

			// 读取下一个请求头
			input.readHeader(header);

			// 检查是否已经从输入流中读取了所有的请求头信息
			// 若所有请求头都已经读取过了，则退出该方法。
			if (header.nameEnd == 0) {
				if (header.valueEnd == 0) {
					return;
				} else {
					throw new ServletException(sm.getString("httpProcessor.parseHeaders.colon"));
				}
			}

			String value = new String(header.value, 0, header.valueEnd);
			if (debug >= 1)
				log(" Header " + new String(header.name, 0, header.nameEnd) + " = " + value);

			// 设置相应的请求头
			if (header.equals(DefaultHeaders.AUTHORIZATION_NAME)) {
				request.setAuthorization(value);
			} else if (header.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)) {
				parseAcceptLanguage(value);
			} 
			// cookie
			else if (header.equals(DefaultHeaders.COOKIE_NAME)) {
				Cookie cookies[] = RequestUtil.parseCookieHeader(value);
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals(Globals.SESSION_COOKIE_NAME)) {
						// Override anything requested in the URL
						if (!request.isRequestedSessionIdFromCookie()) {
							// // 只接受第一个 会话标识 cookie
							request.setRequestedSessionId(cookies[i].getValue());
							request.setRequestedSessionCookie(true);
							request.setRequestedSessionURL(false);
							if (debug >= 1)
								log(" Requested cookie session id is "
										+ ((HttpServletRequest) request.getRequest())
												.getRequestedSessionId());
						}
					}
					if (debug >= 1)
						log(" Adding cookie " + cookies[i].getName() + "="
								+ cookies[i].getValue());
					request.addCookie(cookies[i]);
				}
			} 
			// content-length
			else if (header.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
				int n = -1;
				try {
					n = Integer.parseInt(value);
				} catch (Exception e) {
					throw new ServletException(
							sm.getString("httpProcessor.parseHeaders.contentLength"));
				}
				request.setContentLength(n);
			} 
			// content-type
			else if (header.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
				request.setContentType(value);
			} else if (header.equals(DefaultHeaders.HOST_NAME)) {
				int n = value.indexOf(':');
				if (n < 0) {
					if (connector.getScheme().equals("http")) {
						request.setServerPort(80);
					} else if (connector.getScheme().equals("https")) {
						request.setServerPort(443);
					}
					if (proxyName != null)
						request.setServerName(proxyName);
					else
						request.setServerName(value);
				} else {
					if (proxyName != null)
						request.setServerName(proxyName);
					else
						request.setServerName(value.substring(0, n).trim());
					if (proxyPort != 0)
						request.setServerPort(proxyPort);
					else {
						int port = 80;
						try {
							port = Integer.parseInt(value.substring(n + 1).trim());
						} catch (Exception e) {
							throw new ServletException(
									sm.getString("httpProcessor.parseHeaders.portNumber"));
						}
						request.setServerPort(port);
					}
				}
			} else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
				if (header.valueEquals(DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
					keepAlive = false;
					response.setHeader("Connection", "close");
				}
				//request.setConnection(header);
				/*
				  if ("keep-alive".equalsIgnoreCase(value)) {
				  keepAlive = true;
				  }
				*/
			} else if (header.equals(DefaultHeaders.EXPECT_NAME)) {
				if (header.valueEquals(DefaultHeaders.EXPECT_100_VALUE))
					sendAck = true;
				else
					throw new ServletException(
							sm.getString("httpProcessor.parseHeaders.unknownExpectation"));
			} else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {
				//request.setTransferEncoding(header);
			}

			request.nextHeader();
		}
	}

	/**
	 * 解析传入的 HTTP 请求，并设置相应的 HTTP 请求属性。
	 * 
	 * 关于请求 URI，有以下注意事项：
	 * （1）请求 URI 后面可以加上可选的查询字符串。当浏览器禁用 Cookie 时，也可以将会话标识（jsessionid）嵌入到查询字符串中。
	 * （2）当请求 URI 是一个绝对路径中的值时，需要删除其协议和主机名部分。
	 * （3）对请求 URI 要进行规范化。
	 *
	 * @param input The input stream attached to our socket
	 * @param output The output stream of the socket
	 *
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a parsing error occurs
	 */
	private void parseRequest(SocketInputStream input, OutputStream output)
			throws IOException, ServletException {
		// 解析传入的请求行
		input.readRequestLine(requestLine);

		// 当前一个方法返回时，我们实际上正在处理一个请求
		status = Constants.PROCESSOR_ACTIVE;

		String method = new String(requestLine.method, 0, requestLine.methodEnd);
		String uri = null;
		String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

		//System.out.println(" Method:" + method + "_ Uri:" + uri
		//                   + "_ Protocol:" + protocol);

		if (protocol.length() == 0)
			protocol = "HTTP/0.9";

		// 现在检查是否应该在解析请求后保持持久连接。
		if (protocol.equals("HTTP/1.1")) {
			http11 = true;
			sendAck = false;
		} else {
			http11 = false;
			sendAck = false;
			// 对于 HTTP/1.0，默认情况下连接不会持久，除非使用 Connection: Keep-Alive 头进行指定。
			keepAlive = false;
		}

		// 验证传入的请求行
		if (method.length() < 1) {
			throw new ServletException(sm.getString("httpProcessor.parseRequest.method"));
		} else if (requestLine.uriEnd < 1) {
			throw new ServletException(sm.getString("httpProcessor.parseRequest.uri"));
		}

		// 从请求 URI 中解析查询字符串
		// 查询字符串与 URI 用一个"?"分隔
		int question = requestLine.indexOf("?");
		if (question >= 0) {
			request.setQueryString(
					new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1));
			if (debug >= 1)
				log(" Query string is "
						+ ((HttpServletRequest) request.getRequest()).getQueryString());
			uri = new String(requestLine.uri, 0, question);
		} else {
			request.setQueryString(null);
			uri = new String(requestLine.uri, 0, requestLine.uriEnd);
		}

		// 检查是否为绝对路径的 URI（使用 HTTP 协议）
		// URI 可以指向一个相对路径中的资源，也可以是一个绝对路径中的值，例如：
		// 相对 URI —— /myApp/ModernServlet?userName=tarzan&password=pwd
		// 绝对路径 —— http://www.brainysoftware.com/index.html?name=Tarzan
		if (!uri.startsWith("/")) {
			int pos = uri.indexOf("://");
			// 解析协议和主机名
			if (pos != -1) {
				pos = uri.indexOf('/', pos + 3);
				if (pos == -1) {
					uri = "";
				} else {
					uri = uri.substring(pos);
				}
			}
		}

		// 从请求 URI 中解析请求的会话 ID
		// 若存在参数 jsessionid，则表明会话标识符在查询字符串中，而不在 Cookie 中
		int semicolon = uri.indexOf(match);
		if (semicolon >= 0) {
			String rest = uri.substring(semicolon + match.length());
			int semicolon2 = rest.indexOf(';');
			if (semicolon2 >= 0) {
				request.setRequestedSessionId(rest.substring(0, semicolon2));
				rest = rest.substring(semicolon2);
			} else {
				request.setRequestedSessionId(rest);
				rest = "";
			}
			request.setRequestedSessionURL(true);
			uri = uri.substring(0, semicolon) + rest;
			if (debug >= 1)
				log(" Requested URL session id is "
						+ ((HttpServletRequest) request.getRequest()).getRequestedSessionId());
		} else {
			request.setRequestedSessionId(null);
			request.setRequestedSessionURL(false);
		}

		// 规范化 URI（使用字符串操作）
		String normalizedUri = normalize(uri);
		if (debug >= 1)
			log("Normalized: '" + uri + "' to '" + normalizedUri + "'");

		// 设置相应的请求属性
		((HttpRequest) request).setMethod(method);
		request.setProtocol(protocol);
		if (normalizedUri != null) {
			((HttpRequest) request).setRequestURI(normalizedUri);
		} else {
			((HttpRequest) request).setRequestURI(uri);
		}
		request.setSecure(connector.getSecure());
		request.setScheme(connector.getScheme());

		// 如果 normalizedUri 为 null，将会抛出异常。
		if (normalizedUri == null) {
			log(" Invalid request URI: '" + uri + "'");
			throw new ServletException("Invalid URI: " + uri + "'");
		}

		if (debug >= 1)
			log(" Request is '" + method + "' for '" + uri + "' with protocol '" + protocol
					+ "'");
	}

	/**
	 * 返回一个上下文相对路径，以"/"开头，表示解析得到的".."和"."元素之后的指定路径的规范版本。 
	 * 如果指定的路径试图超出当前上下文的边界（即存在太多的".."路径元素），则返回<code>null</ code>。
	 *  
	 * <p>对非正常的 URL 进行修正。例如，出现"\"的地方会被替换为"/"。
	 * 若 URI 本身是正常的，则返回相同的 URI；否则，返回修正过的 URI。若 URI 无法修正，则会认为它是无效的，将会返回 null。
	 *
	 * @param path 要规范化的路径
	 */
	protected String normalize(String path) {
		if (path == null)
			return null;

		// Create a place for the normalized path
		String normalized = path;

		// Normalize "/%7E" and "/%7e" at the beginning to "/~"
		if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
			normalized = "/~" + normalized.substring(4);

		// Prevent encoding '%', '/', '.' and '\', which are special reserved
		// characters
		if ((normalized.indexOf("%25") >= 0) || (normalized.indexOf("%2F") >= 0)
				|| (normalized.indexOf("%2E") >= 0) || (normalized.indexOf("%5C") >= 0)
				|| (normalized.indexOf("%2f") >= 0) || (normalized.indexOf("%2e") >= 0)
				|| (normalized.indexOf("%5c") >= 0)) {
			return null;
		}

		if (normalized.equals("/."))
			return "/";

		// Normalize the slashes and add leading slash if necessary
		if (normalized.indexOf('\\') >= 0)
			normalized = normalized.replace('\\', '/');
		if (!normalized.startsWith("/"))
			normalized = "/" + normalized;

		// Resolve occurrences of "//" in the normalized path
		while (true) {
			int index = normalized.indexOf("//");
			if (index < 0)
				break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 1);
		}

		// Resolve occurrences of "/./" in the normalized path
		while (true) {
			int index = normalized.indexOf("/./");
			if (index < 0)
				break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 2);
		}

		// Resolve occurrences of "/../" in the normalized path
		while (true) {
			int index = normalized.indexOf("/../");
			if (index < 0)
				break;
			if (index == 0)
				return (null); // Trying to go outside our context
			int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
		}

		// Declare occurrences of "/..." (three or more dots) to be invalid
		// (on some Windows platforms this walks the directory tree!!!)
		if (normalized.indexOf("/...") >= 0)
			return (null);

		// Return the normalized path that we have completed
		return (normalized);

	}

	/**
	 * 在 pipelining 时，发送一个请求已被处理的确认。
	 * 将向客户端发送 HTTP/1.1 100 Continue。
	 *
	 * @param output Socket output stream
	 */
	private void ackRequest(OutputStream output) throws IOException {
		if (sendAck)
			output.write(ack);
	}

	/**
	 * 对于分配给该处理器的 Socket，处理其 HTTP 请求。
	 * Any exceptions that occur during processing must be swallowed and dealt with.
	 * 
	 * `process()` 执行以下三个操作：解析连接、解析请求、解析请求头。
	 * 在完成解析后，`process()` 将 request 和 response 对象作为参数传入 Servlet 容器的 `invoke()`。
	 *
	 * @param socket 连接到客户端的套接字
	 */
	private void process(Socket socket) {
		boolean ok = true;// 表示在处理过程中是否有错误发生
		boolean finishResponse = true;// 表示是否应该调用 Response 接口的 finishResponse()
		SocketInputStream input = null;// 套接字的底层输入流
		OutputStream output = null;// 套接字的底层输出流

		// 构建和初始化我们需要的对象
		try {
			input = new SocketInputStream(socket.getInputStream(), connector.getBufferSize());
		} catch (Exception e) {
			log("process.create", e);
			ok = false;
		}

		keepAlive = true;

		// 循环读取输入流，直到 HttpProcessor 实例终止，抛出一个异常，或连接断开。
		while (!stopped && ok && keepAlive) {
			finishResponse = true;
			// 执行 reqeust 和 response 对象的一些初始化操作。
			try {
				request.setStream(input);
				request.setResponse(response);
				output = socket.getOutputStream();
				response.setStream(output);
				response.setRequest(request);
				((HttpServletResponse) response.getResponse()).setHeader("Server", SERVER_INFO);
			} catch (Exception e) {
				log("process.create", e);
				ok = false;
			}

			// 解析传入的请求
			try {
				if (ok) {
					parseConnection(socket);
					parseRequest(input, output);
					if (!request.getRequest().getProtocol().startsWith("HTTP/0"))
						parseHeaders(input);

					if (http11) {
						// 向客户端发送一个请求确认。
						ackRequest(output);
						// 如果协议是 HTTP/1.1，则允许块编码。
						if (connector.isChunkingAllowed())
							response.setAllowChunking(true);
					}
				}
			} catch (EOFException e) {
				// It's very likely to be a socket disconnect on either the
				// client or the server
				ok = false;
				finishResponse = false;
			} catch (ServletException e) {
				ok = false;
				try {
					((HttpServletResponse) response.getResponse())
							.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} catch (Exception f) {
					;
				}
			} catch (InterruptedIOException e) {
				if (debug > 1) {
					try {
						log("process.parse", e);
						((HttpServletResponse) response.getResponse())
								.sendError(HttpServletResponse.SC_BAD_REQUEST);
					} catch (Exception f) {
						;
					}
				}
				ok = false;
			} catch (Exception e) {
				try {
					log("process.parse", e);
					((HttpServletResponse) response.getResponse())
							.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} catch (Exception f) {
					;
				}
				ok = false;
			}

			// 请求容器来处理此请求
			try {
				((HttpServletResponse) response).setHeader("Date",
						FastHttpDateFormat.getCurrentDate());
				if (ok) {
					connector.getContainer().invoke(request, response);
				}
			} catch (ServletException e) {
				log("process.invoke", e);
				try {
					((HttpServletResponse) response.getResponse())
							.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (Exception f) {
					;
				}
				ok = false;
			} catch (InterruptedIOException e) {
				ok = false;
			} catch (Throwable e) {
				log("process.invoke", e);
				try {
					((HttpServletResponse) response.getResponse())
							.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (Exception f) {
					;
				}
				ok = false;
			}

			// 完成对请求的处理
			if (finishResponse) {
				try {
					response.finishResponse();
				} catch (IOException e) {
					ok = false;
				} catch (Throwable e) {
					log("process.invoke", e);
					ok = false;
				}
				try {
					request.finishRequest();
				} catch (IOException e) {
					ok = false;
				} catch (Throwable e) {
					log("process.invoke", e);
					ok = false;
				}
				try {
					if (output != null)
						output.flush();
				} catch (IOException e) {
					ok = false;
				}
			}

			// 我们必须检查应用程序或响应流是否请求了连接关闭（在 HTTP/1.0 启用持久连接的情况下）。
			if ("close".equals(response.getHeader("Connection"))) {
				keepAlive = false;
			}

			// 请求处理结束
			status = Constants.PROCESSOR_IDLE;

			// 回收请求和响应对象
			request.recycle();
			response.recycle();
		}

		try {
			shutdownInput(input);
			socket.close();
		} catch (IOException e) {
			;
		} catch (Throwable e) {
			log("process.invoke", e);
		}
		socket = null;
	}

	protected void shutdownInput(InputStream input) {
		try {
			int available = input.available();
			// skip any unread (bogus) bytes
			if (available > 0) {
				input.skip(available);
			}
		} catch (Throwable e) {
			;
		}
	}

	// ---------------------------------------------- Background Thread Methods

	/**
	 * 后台线程，它监听传入的 TCP/IP 连接，并将它们移交给适当的处理器。
	 */
	@Override
	public void run() {
		// 处理请求，直到我们收到关闭信号
		while (!stopped) {
			// 等待下一个分配过来的套接字
			Socket socket = await();
			if (socket == null)
				continue;

			// 处理来自此套接字的请求
			try {
				process(socket);
			} catch (Throwable t) {
				log("process.invoke", t);
			}

			// 完成此请求
			connector.recycle(this);
		}

		// Tell threadStop() we have shut ourselves down successfully
		synchronized (threadSync) {
			threadSync.notifyAll();
		}
	}

	/**
	 * 启动后台处理线程。
	 */
	private void threadStart() {
		log(sm.getString("httpProcessor.starting"));

		thread = new Thread(this, threadName);
		thread.setDaemon(true);
		thread.start();

		if (debug >= 1)
			log(" Background thread has been started");
	}

	/**
	 * Stop the background processing thread.
	 */
	private void threadStop() {
		log(sm.getString("httpProcessor.stopping"));

		stopped = true;
		assign(null);

		if (status != Constants.PROCESSOR_IDLE) {
			// Only wait if the processor is actually processing a command
			synchronized (threadSync) {
				try {
					threadSync.wait(5000);
				} catch (InterruptedException e) {
					;
				}
			}
		}
		thread = null;

	}

	// ------------------------------------------------------ Lifecycle Methods

	/**
	 * Add a lifecycle event listener to this component.
	 *
	 * @param listener The listener to add
	 */
	public void addLifecycleListener(LifecycleListener listener) {
		lifecycle.addLifecycleListener(listener);
	}

	/**
	 * Get the lifecycle listeners associated with this lifecycle. If this
	 * Lifecycle has no listeners registered, a zero-length array is returned.
	 */
	public LifecycleListener[] findLifecycleListeners() {
		return lifecycle.findLifecycleListeners();
	}

	/**
	 * Remove a lifecycle event listener from this component.
	 *
	 * @param listener The listener to add
	 */
	public void removeLifecycleListener(LifecycleListener listener) {
		lifecycle.removeLifecycleListener(listener);
	}

	/**
	 * 启动用于请求处理的后台线程。
	 *
	 * @exception LifecycleException if a fatal startup error occurs
	 */
	public void start() throws LifecycleException {
		if (started)
			throw new LifecycleException(sm.getString("httpProcessor.alreadyStarted"));
		lifecycle.fireLifecycleEvent(START_EVENT, null);
		started = true;

		threadStart();
	}

	/**
	 * Stop the background thread we will use for request processing.
	 *
	 * @exception LifecycleException if a fatal shutdown error occurs
	 */
	public void stop() throws LifecycleException {
		if (!started)
			throw new LifecycleException(sm.getString("httpProcessor.notStarted"));
		lifecycle.fireLifecycleEvent(STOP_EVENT, null);
		started = false;

		threadStop();
	}

}
