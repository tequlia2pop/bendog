package org.apache.catalina;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/**
 * <b>Response</b>是<code>ServletResponse</code>的 Catalina 内部的外观，
 * 它是基于对应的<code>Request</code>的处理而生成的。
 * 
 * @author tequlia2pop
 */
public interface Response {

	// ------------------------------------------------------------- Properties

	/**
	 * 返回连接器，通过这个连接器来返回响应。
	 */
	public Connector getConnector();

	/**
	 * Set the Connector through which this Response is returned.
	 * 
	 * 设置连接器，通过这个连接器来返回响应。
	 *
	 * @param connector 新的连接器
	 */
	public void setConnector(Connector connector);

	/**
	 * 返回实际写入输出流的字节数。
	 */
	public int getContentCount();

	/**
	 * 返回与此响应相关联的上下文。
	 */
	public Context getContext();

	/**
	 * 设置与此响应相关联的上下文。
	 * 一旦确定了适当的上下文，就应该调用该方法。
	 *
	 * @param context 关联的上下文
	 */
	public void setContext(Context context);

	/**
	 * 设置应用程序级的提交标志。
	 *
	 * @param appCommitted 新的应用程序级的提交标志值
	 */
	public void setAppCommitted(boolean appCommitted);

	/**
	 * 应用程序级的提交标志访问方法。
	 */
	public boolean isAppCommitted();

	/**
	 * Return the "processing inside an include" flag.
	 */
	public boolean getIncluded();

	/**
	 * Set the "processing inside an include" flag.
	 *
	 * @param included <code>true</code> if we are currently inside a
	 *  RequestDispatcher.include(), else <code>false</code>
	 */
	public void setIncluded(boolean included);

	/**
	 * 返回有关此响应实现和相应版本号的描述信息，格式为<code>&lt;description&gt;/&lt;version&gt;</code>。
	 */
	public String getInfo();

	/**
	 * 返回与此响应相关联的请求。
	 */
	public Request getRequest();

	/**
	 * 设置与此响应相关联的请求。
	 *
	 * @param request 新的关联的请求
	 */
	public void setRequest(Request request);

	/**
	 * Return the <code>ServletResponse</code> for which this object
	 * is the facade.
	 */
	public ServletResponse getResponse();

	/**
	 * 返回与此响应关联的输出流。
	 */
	public OutputStream getStream();

	/**
	 * 设置与此响应关联的输出流。
	 *
	 * @param stream 新的输出流
	 */
	public void setStream(OutputStream stream);

	/**
	 * 设置挂起标志。
	 *
	 * @param suspended 新的挂起标志值
	 */
	public void setSuspended(boolean suspended);

	/**
	 * 挂起标志访问方法。
	 */
	public boolean isSuspended();

	/**
	 * 设置错误标志。
	 */
	public void setError();

	/**
	 * 错误标志访问方法。
	 */
	public boolean isError();

	// --------------------------------------------------------- Public Methods

	/**
	 * 创建并返回一个 ServletOutputStream 来写入与此响应关联的内容。
	 *
	 * @exception IOException 如果发生了输入/输出错误
	 */
	public ServletOutputStream createOutputStream() throws IOException;

	/**
	 * 在单个操作中，执行所需的任何操作来刷新和关闭输出流或 writer。
	 * 
	 * @throws IOException 如果发生了输入/输出错误
	 */
	public void finishResponse() throws IOException;

	/**
	 * 返回为此响应设置或计算的内容长度。
	 */
	public int getContentLength();

	/**
	 * 返回为此响应设置或计算的内容类型，如果没有设置内容类型，则返回<code>null</code>。
	 */
	public String getContentType();

	/**
	 * 返回可用于呈现错误消息的 PrintWriter，无论是否已经获取了流或 writer。
	 *
	 * @return Writer which can be used for error reports. If the response is
	 * not an error report returned using sendError or triggered by an
	 * unexpected exception thrown during the servlet processing
	 * (and only in that case), null will be returned if the response stream
	 * has already been used.
	 */
	public PrintWriter getReporter();

	/**
	 * 释放所有对象引用，并初始化实例变量，准备重新使用此对象。
	 */
	public void recycle();

	/**
	 * 重置数据缓冲区，但不包含任何状态或头信息。
	 */
	public void resetBuffer();

	/**
	 * 发送一个请求的确认。
	 *
	 * @exception IOException 如果发生输入/输出错误
	 */
	public void sendAcknowledgement() throws IOException;

}
