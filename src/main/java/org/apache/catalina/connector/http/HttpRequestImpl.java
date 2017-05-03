package org.apache.catalina.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.servlet.ServletInputStream;
import org.apache.catalina.connector.HttpRequestBase;
import org.apache.catalina.util.Enumerator;

/**
 * 特定于 HTTP 连接器的<b>HttpRequest</b>实现。
 * 
 * @author tequlia2pop
 * @deprecated
 */
final class HttpRequestImpl extends HttpRequestBase {

	// -------------------------------------------------------------- Constants

	/**
	 * Initial pool size.
	 */
	protected static final int INITIAL_POOL_SIZE = 10;

	/**
	 * Pool size increment.
	 */
	protected static final int POOL_SIZE_INCREMENT = 5;

	/**
	 * Descriptive information about this Request implementation.
	 */
	protected static final String info = "org.apache.catalina.connector.http.HttpRequestImpl/1.0";

	// ----------------------------------------------------- Instance Variables

	/**
	 * 请求的远程客户端的 InetAddress。
	 */
	protected InetAddress inet = null;

	/**
	 * Headers pool.
	 */
	protected HttpHeader[] headerPool = new HttpHeader[INITIAL_POOL_SIZE];

	/**
	 * 下一个可用请求头在池中的位置。
	 */
	protected int nextHeader = 0;

	/**
	 * Connection header.
	 */
	protected HttpHeader connectionHeader = null;

	/**
	 * Transfer encoding header.
	 */
	protected HttpHeader transferEncodingHeader = null;

	// ------------------------------------------------------------- Properties

	// [Package Private]
	InetAddress getInet() {
		return inet;
	}

	// [Package Private]
	void setInet(InetAddress inet) {
		this.inet = inet;
	}

	@Override
	public String getInfo() {
		return info;
	}

	// --------------------------------------------------------- Public Methods

	@Override
	public void recycle() {
		super.recycle();
		inet = null;
		nextHeader = 0;
		connectionHeader = null;
	}

	/**
	 * Create and return a ServletInputStream to read the content
	 * associated with this Request.  The default implementation creates an
	 * instance of RequestStream associated with this request, but this can
	 * be overridden if necessary.
	 *
	 * @exception IOException if an input/output error occurs
	 */
	@Override
	public ServletInputStream createInputStream() throws IOException {
		return new HttpRequestStream(this, (HttpResponseImpl) response);
	}

	/**
	 * 分配新的请求头。
	 *
	 * @return an HttpHeader buffer allocated from the pool
	 */
	HttpHeader allocateHeader() {
		if (nextHeader == headerPool.length) {
			// Grow the pool
			HttpHeader[] newHeaderPool = new HttpHeader[headerPool.length + POOL_SIZE_INCREMENT];
			for (int i = 0; i < nextHeader; i++) {
				newHeaderPool[i] = headerPool[i];
			}
			headerPool = newHeaderPool;
		}
		if (headerPool[nextHeader] == null)
			headerPool[nextHeader] = new HttpHeader();
		return headerPool[nextHeader];
	}

	/**
	 * Go to the next header.
	 */
	void nextHeader() {
		nextHeader++;
	}

	/**
	 * Add a Header to the set of Headers associated with this Request.
	 *
	 * @param name The new header name
	 * @param value The new header value
	 * @deprecated Don't use
	 */
	@Override
	public void addHeader(String name, String value) {
		if (nextHeader == headerPool.length) {
			// Grow the pool
			HttpHeader[] newHeaderPool = new HttpHeader[headerPool.length + POOL_SIZE_INCREMENT];
			for (int i = 0; i < nextHeader; i++) {
				newHeaderPool[i] = headerPool[i];
			}
			headerPool = newHeaderPool;
		}
		headerPool[nextHeader++] = new HttpHeader(name, value);
	}

	/**
	 * Clear the collection of Headers associated with this Request.
	 */
	public void clearHeaders() {
		nextHeader = 0;
	}

	/**
	 * Return the first value of the specified header, if any; otherwise,
	 * return <code>null</code>
	 *
	 * @param header Header we want to retrieve
	 */
	public HttpHeader getHeader(HttpHeader header) {
		for (int i = 0; i < nextHeader; i++) {
			if (headerPool[i].equals(header))
				return headerPool[i];
		}
		return null;

	}

	/**
	 * Return the first value of the specified header, if any; otherwise,
	 * return <code>null</code>
	 *
	 * @param headerName Name of the requested header
	 */
	public HttpHeader getHeader(char[] headerName) {
		for (int i = 0; i < nextHeader; i++) {
			if (headerPool[i].equals(headerName))
				return headerPool[i];
		}
		return null;
	}

	/**
	 * Perform whatever actions are required to flush and close the input
	 * stream or reader, in a single operation.
	 *
	 * @exception IOException if an input/output error occurs
	 */
	public void finishRequest() throws IOException {
		// If neither a reader or an is have been opened, do it to consume
		// request bytes, if any
		if ((reader == null) && (stream == null) && (getContentLength() != 0)
				&& (getProtocol() != null) && (getProtocol().equals("HTTP/1.1")))
			getInputStream();

		super.finishRequest();
	}

	// ------------------------------------------------- ServletRequest Methods

	@Override
	public String getRemoteAddr() {
		return inet.getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		if (connector.getEnableLookups())
			return inet.getHostName();
		else
			return getRemoteAddr();
	}

	// --------------------------------------------- HttpServletRequest Methods

	/**
	 * Return the first value of the specified header, if any; otherwise,
	 * return <code>null</code>
	 *
	 * @param name Name of the requested header
	 */
	@Override
	public String getHeader(String name) {
		name = name.toLowerCase();
		for (int i = 0; i < nextHeader; i++) {
			if (headerPool[i].equals(name))
				return new String(headerPool[i].value, 0, headerPool[i].valueEnd);
		}
		return null;
	}

	/**
	 * Return all of the values of the specified header, if any; otherwise,
	 * return an empty enumeration.
	 *
	 * @param name Name of the requested header
	 */
	@Override
	public Enumeration getHeaders(String name) {
		name = name.toLowerCase();
		ArrayList tempArrayList = new ArrayList();
		for (int i = 0; i < nextHeader; i++) {
			if (headerPool[i].equals(name))
				tempArrayList.add(new String(headerPool[i].value, 0, headerPool[i].valueEnd));
		}
		return (Enumeration) new Enumerator(tempArrayList);
	}

	/**
	 * Return the names of all headers received with this request.
	 */
	@Override
	public Enumeration getHeaderNames() {
		ArrayList tempArrayList = new ArrayList();
		for (int i = 0; i < nextHeader; i++) {
			tempArrayList.add(new String(headerPool[i].name, 0, headerPool[i].nameEnd));
		}
		return (Enumeration) new Enumerator(tempArrayList);
	}

}
