package com.gmail.tequlia2pop.bendog.connector.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.util.CookieTools;

import com.gmail.tequlia2pop.bendog.connector.ResponseStream;
import com.gmail.tequlia2pop.bendog.connector.ResponseWriter;

/**
 * HTTP 响应。
 * 
 * @author tequlia2pop
 */
public class HttpResponse implements HttpServletResponse {

	// 默认的缓冲区大小
	private static final int BUFFER_SIZE = 1024;

	/** HTTP 请求对象 **/
	HttpRequest request;
	/** 用于输出响应的 OutputStream **/
	OutputStream output;
	/** 用于输出响应的 PrintWriter **/
	PrintWriter writer;

	protected byte[] buffer = new byte[BUFFER_SIZE];
	/** 缓冲区中的数据大小 **/
	protected int bufferCount = 0;
	/** 此响应是否已提交？已提交的响应已经写入了其状态码和响应头 **/
	protected boolean committed = false;
	/**
	 * 写入此响应的实际的字节数。
	 */
	protected int contentCount = 0;
	/**
	 * 与此响应关联的 content length。
	 */
	protected int contentLength = -1;
	/**
	 * 与此响应关联的 content type。
	 */
	protected String contentType = null;
	/**
	 * 与此响应相关联的字符编码。
	 */
	protected String encoding = null;

	/**
	 * 与该响应相关联的一组 Cookie
	 */
	protected ArrayList<Cookie> cookies = new ArrayList<>();
	/**
	 * 通过 addHeader() 显式添加的 HTTP 响应头，但不包括使用 setContentLength()、setContentType() 等添加的响应头。
	 * 该 Map 以响应头的名称作为键，值元素包含已设置的相关值的 ArrayList。
	 */
	protected HashMap<String, ArrayList<String>> headers = new HashMap<>();
	/**
	 * 我们用于创建日期响应头的日期格式。
	 */
	protected final SimpleDateFormat format = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
	/**
	 * 通过<code>sendError()</code>所设置的错误消息。
	 */
	protected String message = getStatusMessage(HttpServletResponse.SC_OK);
	/**
	 * 与此响应相关联的 HTTP 状态码。
	 */
	protected int status = HttpServletResponse.SC_OK;

	public HttpResponse(OutputStream output) {
		this.output = output;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public OutputStream getStream() {
		return this.output;
	}

	protected String getProtocol() {
		return request.getProtocol();
	}

	/**
	 * 调用此方法来将响应头和响应发送到输出。
	 * @throws IOException 
	 */
	public void finishResponse() {
		// sendHeaders();
		// 刷新并关闭相应的输出机制。
		if (writer != null) {
			writer.flush();
			writer.close();
		}
	}

	/**
	 * 返回指定 HTTP 状态码的默认状态消息。
	 *
	 * @param status 需要消息的状态码
	 */
	protected String getStatusMessage(int status) {
		switch (status) {
		case SC_OK:
			return ("OK");
		case SC_ACCEPTED:
			return ("Accepted");
		case SC_BAD_GATEWAY:
			return ("Bad Gateway");
		case SC_BAD_REQUEST:
			return ("Bad Request");
		case SC_CONFLICT:
			return ("Conflict");
		case SC_CONTINUE:
			return ("Continue");
		case SC_CREATED:
			return ("Created");
		case SC_EXPECTATION_FAILED:
			return ("Expectation Failed");
		case SC_FORBIDDEN:
			return ("Forbidden");
		case SC_GATEWAY_TIMEOUT:
			return ("Gateway Timeout");
		case SC_GONE:
			return ("Gone");
		case SC_HTTP_VERSION_NOT_SUPPORTED:
			return ("HTTP Version Not Supported");
		case SC_INTERNAL_SERVER_ERROR:
			return ("Internal Server Error");
		case SC_LENGTH_REQUIRED:
			return ("Length Required");
		case SC_METHOD_NOT_ALLOWED:
			return ("Method Not Allowed");
		case SC_MOVED_PERMANENTLY:
			return ("Moved Permanently");
		case SC_MOVED_TEMPORARILY:
			return ("Moved Temporarily");
		case SC_MULTIPLE_CHOICES:
			return ("Multiple Choices");
		case SC_NO_CONTENT:
			return ("No Content");
		case SC_NON_AUTHORITATIVE_INFORMATION:
			return ("Non-Authoritative Information");
		case SC_NOT_ACCEPTABLE:
			return ("Not Acceptable");
		case SC_NOT_FOUND:
			return ("Not Found");
		case SC_NOT_IMPLEMENTED:
			return ("Not Implemented");
		case SC_NOT_MODIFIED:
			return ("Not Modified");
		case SC_PARTIAL_CONTENT:
			return ("Partial Content");
		case SC_PAYMENT_REQUIRED:
			return ("Payment Required");
		case SC_PRECONDITION_FAILED:
			return ("Precondition Failed");
		case SC_PROXY_AUTHENTICATION_REQUIRED:
			return ("Proxy Authentication Required");
		case SC_REQUEST_ENTITY_TOO_LARGE:
			return ("Request Entity Too Large");
		case SC_REQUEST_TIMEOUT:
			return ("Request Timeout");
		case SC_REQUEST_URI_TOO_LONG:
			return ("Request URI Too Long");
		case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
			return ("Requested Range Not Satisfiable");
		case SC_RESET_CONTENT:
			return ("Reset Content");
		case SC_SEE_OTHER:
			return ("See Other");
		case SC_SERVICE_UNAVAILABLE:
			return ("Service Unavailable");
		case SC_SWITCHING_PROTOCOLS:
			return ("Switching Protocols");
		case SC_UNAUTHORIZED:
			return ("Unauthorized");
		case SC_UNSUPPORTED_MEDIA_TYPE:
			return ("Unsupported Media Type");
		case SC_USE_PROXY:
			return ("Use Proxy");
		case 207: // WebDAV
			return ("Multi-Status");
		case 422: // WebDAV
			return ("Unprocessable Entity");
		case 423: // WebDAV
			return ("Locked");
		case 507: // WebDAV
			return ("Insufficient Storage");
		default:
			return ("HTTP Response Status " + status);
		}
	}

	/**
	 * 发送 HTTP 响应头（如果尚未发生的话）。
	 */
	protected void sendHeaders() throws IOException {
		if (isCommitted())
			return;

		// 准备一个合适的用于输出的 writer
		OutputStreamWriter osr = null;
		try {
			osr = new OutputStreamWriter(getStream(), getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			osr = new OutputStreamWriter(getStream());
		}
		
		final PrintWriter outputWriter = new PrintWriter(osr);

		// Send the "Status:" header
		outputWriter.print(this.getProtocol());
		outputWriter.print(" ");
		outputWriter.print(status);
		if (message != null) {
			outputWriter.print(" ");
			outputWriter.print(message);
		}
		outputWriter.print("\r\n");

		// 发送 content-length 和 content-type 响应头（如果存在的话）。
		if (getContentType() != null) {
			outputWriter.print("Content-Type: " + getContentType() + "\r\n");
		}
		if (getContentLength() >= 0) {
			outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
		}

		// 发送所有指定的响应头（如果存在的话）。
		synchronized (headers) {
			Iterator<String> names = headers.keySet().iterator();
			while (names.hasNext()) {
				String name = names.next();
				ArrayList<String> values = headers.get(name);
				Iterator<String> items = values.iterator();
				while (items.hasNext()) {
					String value = items.next();
					outputWriter.print(name);
					outputWriter.print(": ");
					outputWriter.print(value);
					outputWriter.print("\r\n");
				}
			}
		}

		// Add the session ID cookie if necessary
		/*    HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
		    HttpSession session = hreq.getSession(false);
		    if ((session != null) && session.isNew() && (getContext() != null)
		      && getContext().getCookies()) {
		      Cookie cookie = new Cookie("JSESSIONID", session.getId());
		      cookie.setMaxAge(-1);
		      String contextPath = null;
		      if (context != null)
		contextPath = context.getPath();
		      if ((contextPath != null) && (contextPath.length() > 0))
		cookie.setPath(contextPath);
		      else
		
		      cookie.setPath("/");
		      if (hreq.isSecure())
		cookie.setSecure(true);
		      addCookie(cookie);
		    }
		*/

		// 发送所有指定的 Cookie（如果存在）
		synchronized (cookies) {
			Iterator<Cookie> items = cookies.iterator();
			while (items.hasNext()) {
				Cookie cookie = items.next();
				outputWriter.print(CookieTools.getCookieHeaderName(cookie));
				outputWriter.print(": ");
				outputWriter.print(CookieTools.getCookieHeaderValue(cookie));
				outputWriter.print("\r\n");
			}
		}

		// 发送表示终止的空白行，它标明了响应头的结束。
		outputWriter.print("\r\n");
		outputWriter.flush();

		committed = true;
	}

	/**
	 * 发送静态资源（如 HTML 文件）。
	 * 
	 * @throws IOException
	 */
	public void sendStaticResource() throws IOException {
		byte[] bytes = new byte[BUFFER_SIZE];
		File file = new File(Constants.WEB_ROOT, request.getRequestURI());
		try (FileInputStream fis = new FileInputStream(file)) {
			/* request.getUri 已被 request.getRequestURI 取代 */
			/*
			   HTTP Response = Status-Line
			     *(( general-header | response-header | entity-header ) CRLF)
			     CRLF
			     [ message-body ]
			   Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
			*/
			int ch = fis.read(bytes, 0, BUFFER_SIZE);
			while (ch != -1) {
				output.write(bytes, 0, ch);
				ch = fis.read(bytes, 0, BUFFER_SIZE);
			}
		} catch (FileNotFoundException e) {
			String errorMessage = "HTTP/1.1 404 File Not Found\r\n"
					+ "Content-Type: text/html\r\n" + "Content-Length: 23\r\n" + "\r\n"
					+ "<h1>File Not Found</h1>";
			output.write(errorMessage.getBytes());
		}
	}

	public void write(int b) throws IOException {
		if (bufferCount >= buffer.length)
			flushBuffer();
		buffer[bufferCount++] = (byte) b;
		contentCount++;
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		// If the whole thing fits in the buffer, just put it there
		if (len == 0)
			return;

		if (len <= (buffer.length - bufferCount)) {
			System.arraycopy(b, off, buffer, bufferCount, len);
			bufferCount += len;
			contentCount += len;
			return;
		}

		// Flush the buffer and start writing full-buffer-size chunks
		flushBuffer();
		int iterations = len / buffer.length;
		int leftoverStart = iterations * buffer.length;
		int leftoverLen = len - leftoverStart;
		for (int i = 0; i < iterations; i++)
			write(b, off + (i * buffer.length), buffer.length);

		// Write the remainder (guaranteed to fit in the buffer)
		if (leftoverLen > 0)
			write(b, off + leftoverStart, leftoverLen);
	}

	// --------------------------------------------------------------
	// implementation of ServletResponse
	// --------------------------------------------------------------

	@Override
	public String getCharacterEncoding() {
		if (encoding == null)
			return "ISO-8859-1";
		else
			return encoding;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		ResponseStream newStream = new ResponseStream(this);
		newStream.setCommit(false);
		OutputStreamWriter osr = new OutputStreamWriter(newStream, getCharacterEncoding());
		writer = new ResponseWriter(osr);
		return writer;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentLength(int length) {
		if (isCommitted())
			return;
		//if (included)
		//	return;     // Ignore any call from an included servlet
		this.contentLength = length;
	}

	@Override
	public void setContentLengthLong(long len) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentType(String type) {
	}

	@Override
	public void setBufferSize(int size) {
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		//committed = true;
		if (bufferCount > 0) {
			try {
				output.write(buffer, 0, bufferCount);
			} finally {
				bufferCount = 0;
			}
		}
	}

	@Override
	public void resetBuffer() {
	}

	@Override
	public void reset() {
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}

	@Override
	public void setLocale(Locale locale) {
		if (isCommitted())
			return;

		//if (included)
		//return;     // Ignore any call from an included servlet

		// super.setLocale(locale);

		String language = locale.getLanguage();
		if ((language != null) && (language.length() > 0)) {
			String country = locale.getCountry();
			StringBuffer value = new StringBuffer(language);
			if ((country != null) && (country.length() > 0)) {
				value.append('-');
				value.append(country);
			}
			setHeader("Content-Language", value.toString());
		}
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	// --------------------------------------------------------------
	// implementation of HttpServletResponse
	// --------------------------------------------------------------

	@Override
	public void addCookie(Cookie cookie) {
		if (isCommitted())
			return;

		//  if (included)
		//        return;     // Ignore any call from an included servlet

		synchronized (cookies) {
			cookies.add(cookie);
		}
	}

	@Override
	public boolean containsHeader(String name) {
		synchronized (headers) {
			return headers.get(name) != null;
		}
	}

	@Override
	public String encodeURL(String url) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		return null;
	}

	@Override
	@Deprecated
	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	@Override
	@Deprecated
	public String encodeRedirectUrl(String url) {
		return encodeRedirectURL(url);
	}

	@Override
	public void sendError(int sc) throws IOException {
	}

	@Override
	public void sendError(int sc, String message) throws IOException {
	}

	@Override
	public void sendRedirect(String location) throws IOException {
	}

	@Override
	public void setDateHeader(String name, long value) {
		if (isCommitted())
			return;

		//    if (included)
		//    return;     // Ignore any call from an included servlet

		setHeader(name, format.format(new Date(value)));
	}

	@Override
	public void addDateHeader(String name, long value) {
		if (isCommitted())
			return;

		//    if (included)
		//          return;     // Ignore any call from an included servlet

		addHeader(name, format.format(new Date(value)));
	}

	@Override
	public void setHeader(String name, String value) {
		if (isCommitted())
			return;

		//    if (included)
		//    return;     // Ignore any call from an included servlet

		ArrayList<String> values = new ArrayList<>();
		values.add(value);
		synchronized (headers) {
			headers.put(name, values);
		}

		String match = name.toLowerCase();
		if (match.equals("content-length")) {
			int contentLength = -1;
			try {
				contentLength = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				;
			}
			if (contentLength >= 0)
				setContentLength(contentLength);
		} else if (match.equals("content-type")) {
			setContentType(value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		if (isCommitted())
			return;

		//        if (included)
		//          return;     // Ignore any call from an included servlet

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
	public void setIntHeader(String name, int value) {
		if (isCommitted())
			return;

		//if (included)
		//return;     // Ignore any call from an included servlet

		setHeader(name, "" + value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		if (isCommitted())
			return;

		//    if (included)
		//    return;     // Ignore any call from an included servlet

		addHeader(name, "" + value);
	}

	@Override
	public void setStatus(int sc) {
	}

	@Override
	@Deprecated
	public void setStatus(int sc, String message) {
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
