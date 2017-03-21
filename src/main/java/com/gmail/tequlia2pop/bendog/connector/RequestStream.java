package com.gmail.tequlia2pop.bendog.connector;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.apache.catalina.util.StringManager;

import com.gmail.tequlia2pop.bendog.connector.http.Constants;
import com.gmail.tequlia2pop.bendog.connector.http.HttpRequest;

/**
 * <b>ServletInputStream</b>的便利实现，它与<b>Request</b>的标准实现一起工作。
 * 如果在我们关联的请求上已经设置了 content length，这个实现将保证不会读取底层流上超过 content length 的字节。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public class RequestStream extends ServletInputStream {

	/** 该包的本地化字符串 **/
	protected static StringManager sm = StringManager.getManager(Constants.Package);

	// ----------------------------------------------------- Instance Variable

	/** 此流是否已经关闭 **/
	protected boolean closed = false;

	/** 此流已返回的字节数 **/
	protected int count = 0;

	/** The content length past which we will not read, 如果没有定义 content length 则为 -1 **/
	protected int length = -1;

	/** 我们应该从中读取数据的底层输入流 **/
	protected InputStream stream = null;

	// ----------------------------------------------------------- Constructors

	/**
	 * 构造与指定的 Request 相关联的 servlet 输入流。
	 *
	 * @param request 关联的请求
	 */
	public RequestStream(HttpRequest request) {
		super();
		closed = false;
		count = 0;
		length = request.getContentLength();
		stream = request.getStream();
	}

	// --------------------------------------------------------- Public Methods

	/*
	 * 关闭此输入流。没有执行物理级 I-O，但任何进一步尝试读取此流将抛出 IOException。
	 * 如果已经设置了 content length，但是尚未读取所有字节，则剩余的字节将被吞噬（swallowed）。
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (closed)
			throw new IOException(sm.getString("requestStream.close.closed"));

		if (length > 0) {
			while (count < length) {
				int b = read();
				if (b < 0)
					break;
			}
		}

		closed = true;
	}

	/*
	 * 从此输入流读取并返回单个字节，如果遇到文件末尾，则返回 -1。
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		// 此流是否已经关闭？
		if (closed)
			throw new IOException(sm.getString("requestStream.read.closed"));

		// 我们已经读过指定的 content length 了吗？
		if ((length >= 0) && (count >= length))
			return -1; // 文件末尾指示符

		// 读取下一个字节并计数，然后返回该字节
		int b = stream.read();
		if (b >= 0)
			count++;
		return b;
	}

	/*
	 * 将输入流中最多 len 个数据字节读入 byte 数组。
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		int toRead = len;// 实际要读取的最大字节数
		if (length > 0) {
			if (count >= length)
				return -1;
			if ((count + len) > length)
				toRead = length - count;
		}
		int actuallyRead = super.read(b, off, toRead);
		return actuallyRead;
	}

	// ----------------------------------------------------------------

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		// TODO Auto-generated method stub

	}

}
