package org.apache.catalina.connector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import org.apache.catalina.Response;

/**
 * 包装了 Catalina 内部的 <b>Response</b>对象的外观类。
 * 所有方法都被委托给包装的响应。
 * 
 * @author tequlia2pop
 */
public class ResponseFacade implements ServletResponse {

	// ----------------------------------------------------- Instance Variables

	/**
	 * The wrapped response.
	 */
	protected ServletResponse response = null;

	/**
	 * The wrapped response.
	 */
	protected Response resp = null;

	// ----------------------------------------------------------- Constructors

	/**
	 * Construct a wrapper for the specified response.
	 *
	 * @param response The response to be wrapped
	 */
	public ResponseFacade(Response response) {
		this.resp = response;
		this.response = (ServletResponse) response;
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * 清理外观。
	 */
	public void clear() {
		response = null;
		resp = null;
	}

	
	public void finish() {
		resp.setSuspended(true);
	}
	
	public boolean isFinished() {
		return resp.isSuspended();
	}

	// ------------------------------------------------ ServletResponse Methods

	@Override
	public String getCharacterEncoding() {
		return response.getCharacterEncoding();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		ServletOutputStream sos = response.getOutputStream();
		if (isFinished())
			resp.setSuspended(true);
		return sos;

	}

	@Override
	public PrintWriter getWriter() throws IOException {
		PrintWriter writer = response.getWriter();
		if (isFinished())
			resp.setSuspended(true);
		return writer;

	}

	@Override
	public void setContentLength(int len) {
		if (isCommitted())
			return;

		response.setContentLength(len);
	}

	@Override
	public void setContentType(String type) {
		if (isCommitted())
			return;

		response.setContentType(type);
	}

	@Override
	public void setBufferSize(int size) {
		if (isCommitted())
			throw new IllegalStateException(/*sm.getString("responseBase.reset.ise")*/);

		response.setBufferSize(size);
	}

	@Override
	public int getBufferSize() {
		return response.getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		if (isFinished())
			return;

		resp.setAppCommitted(true);
		response.flushBuffer();
	}

	@Override
	public void resetBuffer() {
		if (isCommitted())
			throw new IllegalStateException(/*sm.getString("responseBase.reset.ise")*/);

		response.resetBuffer();
	}

	@Override
	public boolean isCommitted() {
		return resp.isAppCommitted();
	}

	@Override
	public void reset() {
		if (isCommitted())
			throw new IllegalStateException(/*sm.getString("responseBase.reset.ise")*/);

		response.reset();
	}

	@Override
	public void setLocale(Locale loc) {
		if (isCommitted())
			return;

		response.setLocale(loc);
	}

	@Override
	public Locale getLocale() {
		return response.getLocale();
	}

}
