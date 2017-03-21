package com.gmail.tequlia2pop.bendog.connector;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import com.gmail.tequlia2pop.bendog.connector.http.HttpResponse;

/**
 * <b>ServletOutputStream</b>的便利实现，它与 ResponseBase 的标准实现<b>Response</b>协同工作。
 * 如果在我们关联的响应上已经设置了 content length，这个实现将保证不会在底层流上写入超过 content length 的字节。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public class ResponseStream extends ServletOutputStream {

	// ----------------------------------------------------- Instance Variables

	/** 此流是否已经关闭？ **/
	protected boolean closed = false;

	/** 刷新时是否应该提交响应？ **/
	protected boolean commit = false;

	/** 已经写入此流的字节数 **/
	protected int count = 0;

	/** The content length past which we will not write, 如果没有定义 content length 则为 -1 **/
	protected int length = -1;

	/** 与此输出流相关联的响应 **/
	protected HttpResponse response = null;

	/** 我们应该向其写入数据的底层输出流 **/
	protected OutputStream stream = null;

	// ----------------------------------------------------------- Constructors

	/**
	 * 构造与指定的 Response 相关联的 servlet 输出流。
	 *
	 * @param response 关联的响应
	 */
	public ResponseStream(HttpResponse response) {
		super();
		closed = false;
		commit = false;
		count = 0;
		this.response = response;
		//  this.stream = response.getStream();
	}

	// ------------------------------------------------------------- Properties

	public boolean getCommit() {
		return this.commit;
	}

	public void setCommit(boolean commit) {
		this.commit = commit;
	}

	// --------------------------------------------------------- Public Methods

	/*
	 * 关闭此输出流，导致任何缓冲数据被刷新和任何进一步的输出数据都会抛出 IOException。
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (closed)
			throw new IOException("responseStream.close.closed");
		response.flushBuffer();
		closed = true;
	}

	/*
	 * 刷新此输出流的任何缓冲数据，这也会使得响应被提交。
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		if (closed)
			throw new IOException("responseStream.flush.closed");
		if (commit)
			response.flushBuffer();

	}

	/*
	 * 将指定的字节写入此输出流。
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		if (closed)
			throw new IOException("responseStream.write.closed");

		if ((length > 0) && (count >= length))
			throw new IOException("responseStream.write.count");

		response.write(b);
		count++;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (closed)
			throw new IOException("responseStream.write.closed");

		int actual = len;// 实际要写入的字节数
		if ((length > 0) && ((count + len) >= length))
			actual = length - count;
		response.write(b, off, actual);
		count += actual;
		if (actual < len)
			throw new IOException("responseStream.write.count");

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

	// ----------------------------------------------------------- 

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// TODO Auto-generated method stub
	}
}
