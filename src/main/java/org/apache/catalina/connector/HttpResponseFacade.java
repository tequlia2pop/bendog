package org.apache.catalina.connector;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import org.apache.catalina.HttpResponse;

/**
 * 包装了 Catalina 内部的<b>HttpResponse</b>对象的外观类。
 * 所有方法都被委托给包装的响应。
 * 
 * @author tequlia2pop
 */
public final class HttpResponseFacade extends ResponseFacade implements HttpServletResponse {

	// ----------------------------------------------------------- Constructors

	/**
	 * Construct a wrapper for the specified response.
	 *
	 * @param response The response to be wrapped
	 */
	public HttpResponseFacade(HttpResponse response) {
		super(response);
	}

	// -------------------------------------------- HttpServletResponse Methods

	@Override
	public void addCookie(Cookie cookie) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).addCookie(cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return ((HttpServletResponse) response).containsHeader(name);
	}

	public String encodeURL(String url) {
		return ((HttpServletResponse) response).encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return ((HttpServletResponse) response).encodeRedirectURL(url);
	}

	@Override
	public String encodeUrl(String url) {
		return ((HttpServletResponse) response).encodeURL(url);
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return ((HttpServletResponse) response).encodeRedirectURL(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if (isCommitted())
			throw new IllegalStateException(/*sm.getString("responseBase.reset.ise")*/);

		resp.setAppCommitted(true);

		((HttpServletResponse) response).sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		if (isCommitted())
			throw new IllegalStateException(/*sm.getString("responseBase.reset.ise")*/);

		resp.setAppCommitted(true);

		((HttpServletResponse) response).sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		if (isCommitted())
			throw new IllegalStateException(/*sm.getString("responseBase.reset.ise")*/);

		resp.setAppCommitted(true);

		((HttpServletResponse) response).sendRedirect(location);
	}

	@Override
	public void setDateHeader(String name, long date) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).setDateHeader(name, date);
	}

	@Override
	public void addDateHeader(String name, long date) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).addDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).addHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).setIntHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).addIntHeader(name, value);
	}

	@Override
	public void setStatus(int sc) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).setStatus(sc);
	}

	@Override
	public void setStatus(int sc, String sm) {
		if (isCommitted())
			return;

		((HttpServletResponse) response).setStatus(sc, sm);
	}

}
