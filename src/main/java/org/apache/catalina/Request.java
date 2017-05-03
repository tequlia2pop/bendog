package org.apache.catalina;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Iterator;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

/**
 * <b>Request</b>是要处理的<code>ServletRequest</code>的 Catalina 内部的外观（facade），
 * 用于生成相应的 <code>Response</code>。
 * 
 * @author tequlia2pop
 */
public interface Request {

	// ------------------------------------------------------------- Properties

	/**
	 * 返回与此请求一起发送的授权凭证（authorization credentials）。
	 */
	public String getAuthorization();

	/**
	 * 设置与此请求一起发送的授权凭据（authorization credentials）。
	 *
	 * @param authorization 新的授权凭据（authorization credentials）
	 */
	public void setAuthorization(String authorization);

	/**
	 * 返回接收到此请求的连接器。
	 */
	public Connector getConnector();

	/**
	 * 设置接收该请求的连接器。
	 *
	 * @param connector 新的连接器
	 */
	public void setConnector(Connector connector);

	/**
	 * 返回正在处理此请求的上下文。
	 */
	public Context getContext();

	/**
	 * Set the Context within which this Request is being processed.  This
	 * must be called as soon as the appropriate Context is identified, because
	 * it identifies the value to be returned by <code>getContextPath()</code>,
	 * and thus enables parsing of the request URI.
	 *
	 * @param context The newly associated Context
	 */
	public void setContext(Context context);

	/**
	 * 返回有关此请求实现和相应版本号的描述性信息，格式为<code>&lt;description&gt;/&lt;version&gt;</code>。
	 */
	public String getInfo();

	/**
	 * 返回<code>ServletRequest</code>的外观。
	 */
	public ServletRequest getRequest();

	/**
	 * Return the Response with which this Request is associated.
	 */
	public Response getResponse();

	/**
	 * Set the Response with which this Request is associated.
	 *
	 * @param response The new associated response
	 */
	public void setResponse(Response response);

	/**
	 * Return the Socket (if any) through which this Request was received.
	 * This should <strong>only</strong> be used to access underlying state
	 * information about this Socket, such as the SSLSession associated with
	 * an SSLSocket.
	 */
	public Socket getSocket();

	/**
	 * Set the Socket (if any) through which this Request was received.
	 *
	 * @param socket The socket through which this request was received
	 */
	public void setSocket(Socket socket);

	/**
	 * 返回与此请求相关联的输入流。
	 */
	public InputStream getStream();

	/**
	 * Set the input stream associated with this Request.
	 *
	 * @param stream The new input stream
	 */
	public void setStream(InputStream stream);

	/**
	 * Return the Wrapper within which this Request is being processed.
	 */
	public Wrapper getWrapper();

	/**
	 * Set the Wrapper within which this Request is being processed.  This
	 * must be called as soon as the appropriate Wrapper is identified, and
	 * before the Request is ultimately passed to an application servlet.
	 *
	 * @param wrapper The newly associated Wrapper
	 */
	public void setWrapper(Wrapper wrapper);

	// --------------------------------------------------------- Public Methods

	/**
	 * Create and return a ServletInputStream to read the content
	 * associated with this Request.
	 *
	 * @exception IOException if an input/output error occurs
	 */
	public ServletInputStream createInputStream() throws IOException;

	/**
	 * Perform whatever actions are required to flush and close the input
	 * stream or reader, in a single operation.
	 *
	 * @exception IOException if an input/output error occurs
	 */
	public void finishRequest() throws IOException;

	/**
	 * Return the object bound with the specified name to the internal notes
	 * for this request, or <code>null</code> if no such binding exists.
	 *
	 * @param name Name of the note to be returned
	 */
	public Object getNote(String name);

	/**
	 * Return an Iterator containing the String names of all notes bindings
	 * that exist for this request.
	 */
	public Iterator getNoteNames();

	/**
	 * 释放所有对象引用，并初始化实例变量，准备重新使用此对象。
	 */
	public void recycle();

	/**
	 * Remove any object bound to the specified name in the internal notes
	 * for this request.
	 *
	 * @param name Name of the note to be removed
	 */
	public void removeNote(String name);

	/**
	 * Set the content length associated with this Request.
	 *
	 * @param length The new content length
	 */
	public void setContentLength(int length);

	/**
	 * Set the content type (and optionally the character encoding)
	 * associated with this Request.  For example,
	 * <code>text/html; charset=ISO-8859-4</code>.
	 *
	 * @param type The new content type
	 */
	public void setContentType(String type);

	/**
	 * Bind an object to a specified name in the internal notes associated
	 * with this request, replacing any existing binding for this name.
	 *
	 * @param name Name to which the object should be bound
	 * @param value Object to be bound to the specified name
	 */
	public void setNote(String name, Object value);

	/**
	 * Set the protocol name and version associated with this Request.
	 *
	 * @param protocol Protocol name and version
	 */
	public void setProtocol(String protocol);

	/**
	 * Set the remote IP address associated with this Request.  NOTE:  This
	 * value will be used to resolve the value for <code>getRemoteHost()</code>
	 * if that method is called.
	 *
	 * @param remote The remote IP address
	 */
	public void setRemoteAddr(String remote);

	/**
	 * Set the name of the scheme associated with this request.  Typical values
	 * are <code>http</code>, <code>https</code>, and <code>ftp</code>.
	 *
	 * @param scheme The scheme
	 */
	public void setScheme(String scheme);

	/**
	 * Set the value to be returned by <code>isSecure()</code>
	 * for this Request.
	 *
	 * @param secure The new isSecure value
	 */
	public void setSecure(boolean secure);

	/**
	 * Set the name of the server (virtual host) to process this request.
	 *
	 * @param name The server name
	 */
	public void setServerName(String name);

	/**
	 * 设置要处理此请求的服务器的端口号。
	 *
	 * @param port 服务器端口
	 */
	public void setServerPort(int port);

}
