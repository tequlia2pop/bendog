package org.apache.catalina.connector.http;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.HttpResponseBase;

/**
 * 特定于 HTTP 连接器的<b>HttpResponse</b>实现。
 * 
 * @author tequlia2pop
 * @deprecated
 */
final class HttpResponseImpl extends HttpResponseBase {

	// ----------------------------------------------------- Instance Variables

	/**
	 * Descriptive information about this Response implementation.
	 */
	protected static final String info = "org.apache.catalina.connector.http.HttpResponseImpl/1.0";

	/**
	 * 是否允许分块。
	 */
	protected boolean allowChunking;

	/**
	 * Associated HTTP response stream.
	 */
	protected HttpResponseStream responseStream;

	// ------------------------------------------------------------- Properties

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.ResponseBase#getInfo()
	 */
	@Override
	public String getInfo() {
		return info;
	}

	/**
	 * 设置分块标志。
	 */
	void setAllowChunking(boolean allowChunking) {
		this.allowChunking = allowChunking;
	}

	public boolean isChunkingAllowed() {
		return allowChunking;
	}

	// ------------------------------------------------------ Protected Methods

	/**
	 * Return the HTTP protocol version implemented by this response
	 * object.
	 *
	 * @return The &quot;HTTP/1.1&quot; string.
	 */
	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.HttpResponseBase#getProtocol()
	 */
	@Override
	protected String getProtocol() {
		return "HTTP/1.1";
	}

	// --------------------------------------------------------- Public Methods

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.HttpResponseBase#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();
		responseStream = null;
		allowChunking = false;
	}

	/**
	 * Send an error response with the specified status and message.
	 *
	 * @param status HTTP status code to send
	 * @param message Corresponding message to send
	 *
	 * @exception IllegalStateException if this response has
	 *  already been committed
	 * @exception IOException if an input/output error occurs
	 */
	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.HttpResponseBase#sendError(int, java.lang.String)
	 */
	@Override
	public void sendError(int status, String message) throws IOException {
		addHeader("Connection", "close");
		super.sendError(status, message);
	}

	/**
	 * Clear any content written to the buffer.  In addition, all cookies
	 * and headers are cleared, and the status is reset.
	 *
	 * @exception IllegalStateException if this response has already
	 *  been committed
	 */
	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.HttpResponseBase#reset()
	 */
	@Override
	public void reset() {
		// Saving important HTTP/1.1 specific headers
		String connectionValue = (String) getHeader("Connection");
		String transferEncodingValue = (String) getHeader("Transfer-Encoding");
		super.reset();
		if (connectionValue != null)
			addHeader("Connection", connectionValue);
		if (transferEncodingValue != null)
			addHeader("Transfer-Encoding", transferEncodingValue);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.ResponseBase#createOutputStream()
	 */
	@Override
	public ServletOutputStream createOutputStream() throws IOException {
		responseStream = new HttpResponseStream(this);
		return responseStream;
	}

	/**
	 * Tests is the connection will be closed after the processing of the
	 * request.
	 */
	public boolean isCloseConnection() {
		String connectionValue = (String) getHeader("Connection");
		return (connectionValue != null && connectionValue.equals("close"));
	}

	/**
	 * Removes the specified header.
	 *
	 * @param name Name of the header to remove
	 * @param value Value to remove
	 */
	public void removeHeader(String name, String value) {
		if (isCommitted())
			return;

		if (included)
			return; // Ignore any call from an included servlet

		synchronized (headers) {
			ArrayList<String> values = headers.get(name);
			if ((values != null) && (!values.isEmpty())) {
				values.remove(value);
				if (values.isEmpty())
					headers.remove(name);
			}
		}
	}

	/**
	 * 已创建流？
	 */
	public boolean isStreamInitialized() {
		return responseStream != null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.HttpResponseBase#finishResponse()
	 */
	@Override
	public void finishResponse() throws IOException {
		if (getStatus() < HttpServletResponse.SC_BAD_REQUEST) {
			if ((!isStreamInitialized()) && (getContentLength() == -1) && (getStatus() >= 200)//
					&& (getStatus() != SC_NOT_MODIFIED) && (getStatus() != SC_NO_CONTENT))
				setContentLength(0);
		} else {
			setHeader("Connection", "close");
		}
		super.finishResponse();
	}

	// -------------------------------------------- HttpServletResponse Methods

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.HttpResponseBase#setStatus(int)
	 */
	@Override
	public void setStatus(int status) {
		super.setStatus(status);

		if (responseStream != null)
			responseStream.checkChunking(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.catalina.connector.HttpResponseBase#setContentLength(int)
	 */
	@Override
	public void setContentLength(int length) {
		if (isCommitted())
			return;

		if (included)
			return; // Ignore any call from an included servlet

		super.setContentLength(length);

		if (responseStream != null)
			responseStream.checkChunking(this);
	}

}
