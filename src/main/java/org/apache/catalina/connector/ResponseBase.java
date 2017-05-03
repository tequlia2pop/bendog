package org.apache.catalina.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.util.CharsetMapper;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;

/**
 * <b>Response</b>接口的便利基础实现，可用于大多数连接器所需的响应实现。
 * 只需要实现连接器特定的方法即可。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public abstract class ResponseBase implements Response, ServletResponse {

	// ----------------------------------------------------- Instance Variables

	/**
	 * Has this response been committed by the application yet?
	 */
	protected boolean appCommitted = false;

	/**
	 * The buffer through which all of our output bytes are passed.
	 */
	protected byte[] buffer = new byte[1024];

	/**
	 * 当前缓冲区中的数据的字节数。
	 */
	protected int bufferCount = 0;

	/**
	 * 此响应是否已提交？
	 */
	protected boolean committed = false;

	/**
	 * The Connector through which this Response is returned.
	 */
	protected Connector connector = null;

	/**
	 * 写入此响应的实际字节数。
	 */
	protected int contentCount = 0;

	/**
	 * The content length associated with this Response.
	 */
	protected int contentLength = -1;

	/**
	 * 与此响应关联的内容类型。
	 */
	protected String contentType = null;

	/**
	 * The Context within which this Response is being produced.
	 */
	protected Context context = null;

	/**
	 * 与此响应相关联的字符编码。
	 */
	protected String encoding = null;

	/**
	 * The facade associated with this response.
	 */
	protected ResponseFacade facade = new ResponseFacade(this);

	/**
	 * Are we currently processing inside a RequestDispatcher.include()?
	 */
	protected boolean included = false;

	/**
	 * Descriptive information about this Response implementation.
	 */
	protected static final String info = "org.apache.catalina.connector.ResponseBase/1.0";

	/**
	 * The Locale associated with this Response.
	 */
	protected Locale locale = Locale.getDefault();

	/**
	 * The output stream associated with this Response.
	 */
	protected OutputStream output = null;

	/**
	 * The Request with which this Response is associated.
	 */
	protected Request request = null;

	/**
	 * The string manager for this package.
	 */
	protected static StringManager sm = StringManager.getManager(Constants.Package);

	/**
	 * 由<code>getOutputStream()</code>返回的 ServletOutputStream（如果有的话）。
	 */
	protected ServletOutputStream stream = null;

	/**
	 * Has this response output been suspended?
	 */
	protected boolean suspended = false;

	/**
	 * 已由<code>getWriter()</code>返回的 PrintWriter（如果有的话）。
	 */
	protected PrintWriter writer = null;

	/**
	 * Error flag. True if the response is an error report.
	 */
	protected boolean error = false;

	// ------------------------------------------------------------- Properties

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getConnector()
	 */
	@Override
	public Connector getConnector() {
		return this.connector;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#setConnector(org.apache.catalina.Connector)
	 */
	@Override
	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getContentCount()
	 */
	@Override
	public int getContentCount() {
		return this.contentCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getContext()
	 */
	@Override
	public Context getContext() {
		return this.context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#setContext(org.apache.catalina.Context)
	 */
	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	/*
	* (non-Javadoc)
	* @see org.apache.catalina.Response#setAppCommitted(boolean)
	*/
	@Override
	public void setAppCommitted(boolean appCommitted) {
		this.appCommitted = appCommitted;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#isAppCommitted()
	 */
	@Override
	public boolean isAppCommitted() {
		return this.appCommitted || this.committed;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getIncluded()
	 */
	@Override
	public boolean getIncluded() {
		return this.included;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#setIncluded(boolean)
	 */
	@Override
	public void setIncluded(boolean included) {
		this.included = included;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getInfo()
	 */
	@Override
	public String getInfo() {
		return this.info;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getRequest()
	 */
	@Override
	public Request getRequest() {
		return this.request;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#setRequest(org.apache.catalina.Request)
	 */
	@Override
	public void setRequest(Request request) {
		this.request = request;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getResponse()
	 */
	@Override
	public ServletResponse getResponse() {
		return facade;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getStream()
	 */
	@Override
	public OutputStream getStream() {
		return this.output;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#setStream(java.io.OutputStream)
	 */
	@Override
	public void setStream(OutputStream stream) {
		this.output = stream;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#setSuspended(boolean)
	 */
	@Override
	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
		if (stream != null)
			((ResponseStream) stream).setSuspended(suspended);

	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		return this.suspended;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#setError()
	 */
	@Override
	public void setError() {
		this.error = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#isError()
	 */
	@Override
	public boolean isError() {
		return this.error;
	}

	// --------------------------------------------------------- Public Methods

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#createOutputStream()
	 */
	@Override
	public ServletOutputStream createOutputStream() throws IOException {
		return new ResponseStream(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#finishResponse()
	 */
	@Override
	public void finishResponse() throws IOException {
		// 如果还没有请求流，请获得一个，以便我们可以刷新必需的标头
		if (this.stream == null) {
			ServletOutputStream sos = getOutputStream();
			sos.flush();
			sos.close();
			return;
		}

		// 如果流已关闭，什么都不用做
		if (((ResponseStream) stream).closed())
			return;

		// 刷新并关闭相应的输出机制
		if (writer != null) {
			writer.flush();
			writer.close();
		} else {
			stream.flush();
			stream.close();
		}

		// 底层输出流（可能来自套接字）不是我们的责任
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getContentLength()
	 */
	@Override
	public int getContentLength() {
		return this.contentLength;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getContentType()
	 */
	@Override
	public String getContentType() {
		return this.contentType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#getReporter()
	 */
	@Override
	public PrintWriter getReporter() {
		if (isError()) {
			try {
				if (this.stream == null)
					this.stream = createOutputStream();
			} catch (IOException e) {
				return null;
			}
			return new PrintWriter(this.stream);

		} else {
			if (this.stream != null) {
				return null;
			} else {
				try {
					return new PrintWriter(getOutputStream());
				} catch (IOException e) {
					return null;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#recycle()
	 */
	@Override
	public void recycle() {
		// 回收时不会重置缓冲区
		bufferCount = 0;
		committed = false;
		appCommitted = false;
		suspended = false;
		// 回收时不会重置连接器
		contentCount = 0;
		contentLength = -1;
		contentType = null;
		context = null;
		encoding = null;
		included = false;
		locale = Locale.getDefault();
		output = null;
		request = null;
		stream = null;
		writer = null;
		error = false;
	}

	// -------------------------------------------------------- Package Methods

	/**
	 * Write the specified byte to our output stream, flushing if necessary.
	 *
	 * @param b The byte to be written
	 *
	 * @exception IOException if an input/output error occurs
	 */
	public void write(int b) throws IOException {
		if (suspended)
			throw new IOException(sm.getString("responseBase.write.suspended"));

		if (bufferCount >= buffer.length)
			flushBuffer();
		buffer[bufferCount++] = (byte) b;
		contentCount++;
	}

	/**
	 * Write <code>b.length</code> bytes from the specified byte array
	 * to our output stream.  Flush the output stream as necessary.
	 *
	 * @param b The byte array to be written
	 *
	 * @exception IOException if an input/output error occurs
	 */
	public void write(byte b[]) throws IOException {
		if (suspended)
			throw new IOException(sm.getString("responseBase.write.suspended"));

		write(b, 0, b.length);
	}

	/**
	 * Write <code>len</code> bytes from the specified byte array, starting
	 * at the specified offset, to our output stream.  Flush the output
	 * stream as necessary.
	 *
	 * @param b The byte array containing the bytes to be written
	 * @param off Zero-relative starting offset of the bytes to be written
	 * @param len The number of bytes to be written
	 *
	 * @exception IOException if an input/output error occurs
	 */
	public void write(byte b[], int off, int len) throws IOException {
		if (suspended)
			throw new IOException(sm.getString("responseBase.write.suspended"));

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

	// ------------------------------------------------ ServletResponse Methods

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	@Override
	public void flushBuffer() throws IOException {
		committed = true;
		if (bufferCount > 0) {
			try {
				output.write(buffer, 0, bufferCount);
			} finally {
				bufferCount = 0;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		return buffer.length;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	@Override
	public String getCharacterEncoding() {
		if (encoding == null)
			return "ISO-8859-1";
		else
			return encoding;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null)
			throw new IllegalStateException(sm.getString("responseBase.getOutputStream.ise"));

		if (stream == null)
			stream = createOutputStream();
		((ResponseStream) stream).setCommit(true);
		return (stream);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return locale;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer != null)
			return writer;

		if (stream != null)
			throw new IllegalStateException(sm.getString("responseBase.getWriter.ise"));

		ResponseStream newStream = (ResponseStream) createOutputStream();
		newStream.setCommit(false);
		OutputStreamWriter osr = new OutputStreamWriter(newStream, getCharacterEncoding());
		writer = new ResponseWriter(osr, newStream);
		stream = newStream;
		return writer;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	@Override
	public boolean isCommitted() {
		return committed;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	@Override
	public void reset() {
		if (committed)
			throw new IllegalStateException(sm.getString("responseBase.reset.ise"));

		if (included)
			return; // Ignore any call from an included servlet

		if (stream != null)
			((ResponseStream) stream).reset();
		bufferCount = 0;
		contentLength = -1;
		contentType = null;

	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.Response#resetBuffer()
	 */
	@Override
	public void resetBuffer() {
		if (committed)
			throw new IllegalStateException(sm.getString("responseBase.resetBuffer.ise"));

		bufferCount = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	@Override
	public void setBufferSize(int size) {
		if (committed || (bufferCount > 0))
			throw new IllegalStateException(sm.getString("responseBase.setBufferSize.ise"));

		if (buffer.length >= size)
			return;
		buffer = new byte[size];

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	@Override
	public void setContentLength(int length) {
		if (isCommitted())
			return;

		if (included)
			return; // Ignore any call from an included servlet

		this.contentLength = length;

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String type) {
		if (isCommitted())
			return;

		if (included)
			return; // Ignore any call from an included servlet

		this.contentType = type;
		if (type.indexOf(';') >= 0) {
			encoding = RequestUtil.parseCharacterEncoding(type);
			if (encoding == null)
				encoding = "ISO-8859-1";
		} else {
			if (encoding != null)
				this.contentType = type + ";charset=" + encoding;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale(Locale locale) {
		if (isCommitted())
			return;

		if (included)
			return; // Ignore any call from an included servlet

		this.locale = locale;
		if (this.context != null) {
			CharsetMapper mapper = context.getCharsetMapper();
			this.encoding = mapper.getCharset(locale);
			if (contentType != null) {
				if (contentType.indexOf(';') < 0) {
					contentType = contentType + ";charset=" + encoding;
				} else {
					// Replace the previous charset
					int i = contentType.indexOf(';');
					contentType = contentType.substring(0, i) + ";charset=" + encoding;
				}
			}
		}
	}

}
