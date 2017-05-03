package org.apache.catalina.connector;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import org.apache.catalina.Response;
import org.apache.catalina.util.StringManager;

/**
 * <b>ServletOutputStream</b>的便利实现，它与<b>Response</b> 的标准实现 ResponseBase 协同工作。
 * 如果在我们关联的响应上已经设置了 content length，这个实现将保证不会在底层流上写入超过 content length 的字节。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public class ResponseStream extends ServletOutputStream {

	/**
	 * The localized strings for this package.
	 */
	protected static StringManager sm = StringManager.getManager(Constants.Package);

	// ----------------------------------------------------- Instance Variables

	/**
	 * 此流是否已经关闭？
	 */
	protected boolean closed = false;

	/**
	 * 刷新时是否应该提交响应？
	 */
	protected boolean commit = false;

	/**
	 * 已经写入此流的字节数。
	 */
	protected int count = 0;

	/**
	 * The content length past which we will not write, or -1 if there is
	 * no defined content length.
	 */
	protected int length = -1;

	/**
	 * 与此输入流相关联的响应。
	 */
	protected Response response = null;

	/**
	 * 我们应该向其写入数据的底层输出流。
	 */
	protected OutputStream stream = null;

	/**
	 * 此响应输出是否暂停？
	 */
	protected boolean suspended = false;

	// ----------------------------------------------------------- Constructors

	/**
	 * Construct a servlet output stream associated with the specified Request.
	 *
	 * @param response The associated response
	 */
	public ResponseStream(Response response) {
		super();
		closed = false;
		commit = false;
		count = 0;
		this.response = response;
		this.stream = response.getStream();
		this.suspended = response.isSuspended();
	}

	// ------------------------------------------------------------- Properties

	/**
	 * [Package Private]
	 */
	boolean getCommit() {
		return this.commit;
	}

	/**
	 * [Package Private]
	 */
	void setCommit(boolean commit) {
		this.commit = commit;
	}

	/**
	 * Set the suspended flag.
	 */
	void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	boolean isSuspended() {
		return this.suspended;
	}

	// --------------------------------------------------------- Public Methods

	/*
	 * 关闭此输出流，导致任何缓冲数据被刷新和任何进一步的输出数据都会抛出 IOException。
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (suspended)
			throw new IOException(sm.getString("responseStream.suspended"));

		if (closed)
			throw new IOException(sm.getString("responseStream.close.closed"));

		response.getResponse().flushBuffer();
		closed = true;
	}

	/*
	 * 刷新此输出流的任何缓冲数据，这也会使得响应被提交。
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		if (suspended)
			throw new IOException(sm.getString("responseStream.suspended"));

		if (closed)
			throw new IOException(sm.getString("responseStream.flush.closed"));

		if (commit)
			response.getResponse().flushBuffer();
	}

	@Override
	public void write(int b) throws IOException {
		if (suspended)
			return;

		if (closed)
			throw new IOException(sm.getString("responseStream.write.closed"));

		if ((length > 0) && (count >= length))
			throw new IOException(sm.getString("responseStream.write.count"));

		((ResponseBase) response).write(b);
		count++;
	}

	@Override
	public void write(byte b[]) throws IOException {
		if (suspended)
			return;

		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (suspended)
			return;

		if (closed)
			throw new IOException(sm.getString("responseStream.write.closed"));

		int actual = len;
		if ((length > 0) && ((count + len) >= length))
			actual = length - count;
		((ResponseBase) response).write(b, off, actual);
		count += actual;
		if (actual < len)
			throw new IOException(sm.getString("responseStream.write.count"));
	}

	// -------------------------------------------------------- Package Methods

	/**
	 * 此响应流是否已关闭？
	 */
	boolean closed() {
		return this.closed;
	}

	/**
	 * 将写入此流的字节计数重置为零。
	 */
	void reset() {
		count = 0;
	}

}
