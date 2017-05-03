package org.apache.catalina.connector;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

import org.apache.catalina.Request;
import org.apache.catalina.util.StringManager;

/**
 * <b>ServletInputStream</b>的便利实现，它与<b>Request</b>的标准实现一起工作。
 * 如果在我们关联的请求上已经设置了 content length，这个实现将保证不会读取底层流上超过 content length 的字节。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public class RequestStream extends ServletInputStream {
	
	/** 
	 * 该包的本地化字符串 
	 */
	protected static StringManager sm = StringManager.getManager(Constants.Package);

	// ----------------------------------------------------- Instance Variables

	/**
	 * 此流是否已经关闭？
	 */
	protected boolean closed = false;

	/**
	 * 此流已返回的字节数。
	 */
	protected int count = 0;

	/**
	 * The content length past which we will not read, or -1 if there is
	 * no defined content length.
	 */
	protected int length = -1;

	/**
	 * 我们应该从中读取数据的底层输入流。
	 */
	protected InputStream stream = null;

	// ----------------------------------------------------------- Constructors

	/**
	 * 构造与指定的 Request 相关联的 servlet 输入流。
	 *
	 * @param request 关联的请求
	 */
	public RequestStream(Request request) {
		super();
		closed = false;
		count = 0;
		length = request.getRequest().getContentLength();
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
	 * 从输入流读取一些字节数，并将它们存储到缓冲区数组 b 中。
	 * 实际读取的字节数作为整数返回。
	 * 该方法会阻塞，直到输入数据可用、检测到文件结束或抛出异常。
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	/*
	 * 从输入流读取<code>len</code>个字节数据到一个字节数组。
	 * 尝试读取<code>len</code>个字节，但可能读取不了那么多，也可能为读取零个。
	 * 实际读取的字节数作为整数返回。
	 * 该方法会阻塞，直到输入数据可用、检测到文件结束或抛出异常。
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

}
