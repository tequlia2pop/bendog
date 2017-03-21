package com.gmail.tequlia2pop.bendog.connector.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.EOFException;
import org.apache.catalina.util.StringManager;

/**
 * 扩展自 InputStream，以便在处理 HTTP 请求头时可以更有效地读取行。
 * 
 * <p>该类实际上是 org.apache.catalina.connector.http.SocketInputStream 类的一个副本。
 * 该类提供了一些方法来获取请求行和请求头信息。
 * 
 * 类似 BufferedInputStream 的简化版本，支持缓冲输入，但是不支持 mark 和 reset 方法。
 *
 * @author tequlia2pop
 * @deprecated
 */
public class SocketInputStream extends InputStream {

	// -------------------------------------------------------------- Constants

	/**
	 * CR.
	 */
	private static final byte CR = (byte) '\r';

	/**
	 * LF.
	 */
	private static final byte LF = (byte) '\n';

	/**
	 * SP.
	 */
	private static final byte SP = (byte) ' ';

	/**
	 * HT.
	 */
	private static final byte HT = (byte) '\t';

	/**
	 * 冒号
	 */
	private static final byte COLON = (byte) ':';

	/**
	 * 小写字母的偏移量
	 */
	private static final int LC_OFFSET = 'A' - 'a';

	// -------------------------------------------------------------- Variables

	/**
	 * 用于该包的 string manager。
	 */
	protected static StringManager sm = StringManager.getManager(Constants.Package);

	/**
	 * 内部缓冲区。
	 */
	protected byte buf[];

	/**
	 * 缓冲区中的有效字节数。
	 * 这是比缓冲区中最后一个有效字节的索引大 1 的索引。
	 */
	protected int count;

	/**
	 * 缓冲区中的当前位置。
	 */
	protected int pos;

	/**
	 * 底层输入流。
	 */
	protected InputStream is;

	// ----------------------------------------------------------- Constructors

	/**
	 * 构造与指定套接字输入流相关联的 servlet 输入流。
	 *
	 * @param is 套接字输入流
	 * @param bufferSize 内部缓冲区的大小
	 */
	public SocketInputStream(InputStream is, int bufferSize) {
		this.is = is;
		buf = new byte[bufferSize];

	}

	// ----------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Public Methods

	/**
	 * 读取请求行，并将其复制到给定的缓冲区。
	 * 在解析 HTTP 请求头期间使用该函数。不要尝试使用它读取请求主体。
	 *
	 * @param requestLine 请求行对象
	 * @throws IOException 如果在底层套接字读取操作期间发生异常，或者给定的缓冲区不足以容纳整行
	 */
	public void readRequestLine(HttpRequestLine requestLine) throws IOException {
		// 回收检查
		if (requestLine.methodEnd != 0)
			requestLine.recycle();

		// 检查空行
		int chr = 0;
		do { // 跳过回车符或换行符
			try {
				chr = read();
			} catch (IOException e) {
				chr = -1;
			}
		} while ((chr == CR) || (chr == LF));
		
		if (chr == -1)
			throw new EOFException(sm.getString("requestStream.readline.error"));
		pos--;

		// 读取方法名称

		int maxRead = requestLine.method.length;// method[] 缓冲区的最大长度
		int readStart = pos;// Q: unuse
		int readCount = 0;// method[] 缓冲区中的当前位置

		boolean space = false;// 是否遇到空格符

		while (!space) {
			// 如果 method[] 缓冲区已满，扩展它
			if (readCount >= maxRead) {
				if ((2 * maxRead) <= HttpRequestLine.MAX_METHOD_SIZE) {
					char[] newBuffer = new char[2 * maxRead];
					System.arraycopy(requestLine.method, 0, newBuffer, 0, maxRead);
					requestLine.method = newBuffer;
					maxRead = requestLine.method.length;
				} else {
					throw new IOException(sm.getString("requestStream.readline.toolong"));
				}
			}
			
			// 我们在内部缓冲区的末尾
			if (pos >= count) {
				int val = read();
				if (val == -1) {
					throw new IOException(sm.getString("requestStream.readline.error"));
				}
				pos = 0;
				readStart = 0;
			}
			
			if (buf[pos] == SP) {// 跳出循环的条件
				space = true;
			}
			
			requestLine.method[readCount] = (char) buf[pos];
			readCount++;
			pos++;
		}

		requestLine.methodEnd = readCount - 1;// 剔除最后读取的字符（空格符）

		// 读取 URI

		maxRead = requestLine.uri.length;// uri[] 缓冲区的最大长度
		readStart = pos;
		readCount = 0;// uri[] 缓冲区中的当前位置
 
		space = false;

		boolean eol = false;// 是否遇到行结束符

		while (!space) {
			// 如果 uri[] 缓冲区已满，扩展它
			if (readCount >= maxRead) {
				if ((2 * maxRead) <= HttpRequestLine.MAX_URI_SIZE) {
					char[] newBuffer = new char[2 * maxRead];
					System.arraycopy(requestLine.uri, 0, newBuffer, 0, maxRead);
					requestLine.uri = newBuffer;
					maxRead = requestLine.uri.length;
				} else {
					throw new IOException(sm.getString("requestStream.readline.toolong"));
				}
			}
			
			// 我们在内部缓冲区的末尾
			if (pos >= count) {
				int val = read();
				if (val == -1)
					throw new IOException(sm.getString("requestStream.readline.error"));
				pos = 0;
				readStart = 0;
			}
			
			if (buf[pos] == SP) {
				space = true;
			} else if ((buf[pos] == CR) || (buf[pos] == LF)) {
				// HTTP/0.9 风格的请求
				eol = true;
				space = true;
			}
			
			requestLine.uri[readCount] = (char) buf[pos];
			readCount++;
			pos++;
		}

		requestLine.uriEnd = readCount - 1;

		// 读取协议

		maxRead = requestLine.protocol.length;// protocol[] 缓冲区的最大长度
		readStart = pos;
		readCount = 0;// protocol[] 缓冲区中的当前位置

		while (!eol) {
			// 如果 protocol[] 缓冲区已满，扩展它
			if (readCount >= maxRead) {
				if ((2 * maxRead) <= HttpRequestLine.MAX_PROTOCOL_SIZE) {
					char[] newBuffer = new char[2 * maxRead];
					System.arraycopy(requestLine.protocol, 0, newBuffer, 0, maxRead);
					requestLine.protocol = newBuffer;
					maxRead = requestLine.protocol.length;
				} else {
					throw new IOException(sm.getString("requestStream.readline.toolong"));
				}
			}
			
			// 我们在内部缓冲区的末尾
			if (pos >= count) {
				// 将内部缓冲区的一部分（或全部）复制到行缓冲区
				int val = read();
				if (val == -1)
					throw new IOException(sm.getString("requestStream.readline.error"));
				pos = 0;
				readStart = 0;
			}
			
			if (buf[pos] == CR) {
				// 跳过回车符。
			} else if (buf[pos] == LF) {
				eol = true;
			} else {
				requestLine.protocol[readCount] = (char) buf[pos];
				readCount++;
			}
			pos++;
		}

		requestLine.protocolEnd = readCount;
	}

	/**
	 * 读取请求头，并将其复制到给定的缓冲区。
	 * 在解析 HTTP 请求头期间使用该函数。不要尝试使用它读取请求主体。
	 *
	 * @param requestLine 请求行对象
	 * @throws IOException 如果在底层套接字读取操作期间发生异常，或者给定的缓冲区不足以容纳整行
	 */
	public void readHeader(HttpHeader header) throws IOException {
		// 回收检查
		if (header.nameEnd != 0)
			header.recycle();

		// 检查空行
		int chr = read();
		if ((chr == CR) || (chr == LF)) { // 跳过回车符
			if (chr == CR)
				read(); // 跳过换行符
			header.nameEnd = 0;
			header.valueEnd = 0;
			return;
		} else {
			pos--;
		}

		// 读取请求头的名称

		int maxRead = header.name.length;// name[] 缓冲区的最大长度
		int readStart = pos;// Q: unuse
		int readCount = 0;// name[] 缓冲区中的当前位置

		boolean colon = false;// 是否遇到了冒号（:）

		while (!colon) {
			// 如果 name[] 缓冲区已满，扩展它
			if (readCount >= maxRead) {
				if ((2 * maxRead) <= HttpHeader.MAX_NAME_SIZE) {
					char[] newBuffer = new char[2 * maxRead];
					System.arraycopy(header.name, 0, newBuffer, 0, maxRead);
					header.name = newBuffer;
					maxRead = header.name.length;
				} else {
					throw new IOException(sm.getString("requestStream.readline.toolong"));
				}
			}
			
			// 我们在内部缓冲区的末尾
			if (pos >= count) {
				int val = read();
				if (val == -1) {
					throw new IOException(sm.getString("requestStream.readline.error"));
				}
				pos = 0;
				readStart = 0;
			}
			
			if (buf[pos] == COLON) {
				colon = true;
			}
			
			char val = (char) buf[pos];
			if ((val >= 'A') && (val <= 'Z')) {// 大写字母转小写字母
				val = (char) (val - LC_OFFSET);
			}
			
			header.name[readCount] = val;
			readCount++;
			pos++;
		}

		header.nameEnd = readCount - 1;

		// 读取请求头的值（可跨越多行）

		maxRead = header.value.length;
		readStart = pos;
		readCount = 0;

		int crPos = -2;//

		boolean eol = false; // 是否遇到了行结束符
		boolean validLine = true;// Q

		while (validLine) {

			boolean space = true;

			// 跳过空格
			// 注意：只删除前导空格，不会删除尾部空格。
			while (space) {
				// 我们在内部缓冲区的末尾
				if (pos >= count) {
					// 将内部缓冲区的一部分（或全部）复制到行缓冲区
					int val = read();
					if (val == -1)
						throw new IOException(sm.getString("requestStream.readline.error"));
					pos = 0;
					readStart = 0;
				}
				if ((buf[pos] == SP) || (buf[pos] == HT)) {
					pos++;
				} else {
					space = false;
				}
			}

			while (!eol) {
				// 如果 value[] 缓冲区已满，扩展它
				if (readCount >= maxRead) {
					if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
						char[] newBuffer = new char[2 * maxRead];
						System.arraycopy(header.value, 0, newBuffer, 0, maxRead);
						header.value = newBuffer;
						maxRead = header.value.length;
					} else {
						throw new IOException(sm.getString("requestStream.readline.toolong"));
					}
				}
				// 我们在内部缓冲区的末尾
				if (pos >= count) {
					// 将内部缓冲区的一部分（或全部）复制到行缓冲区
					int val = read();
					if (val == -1)
						throw new IOException(sm.getString("requestStream.readline.error"));
					pos = 0;
					readStart = 0;
				}
				
				if (buf[pos] == CR) {
				} else if (buf[pos] == LF) {
					eol = true;
				} else {
					// FIXME : Check if binary conversion is working fine
					int ch = buf[pos] & 0xff;
					header.value[readCount] = (char) ch;
					readCount++;
				}
				pos++;
			}

			int nextChr = read();

			if ((nextChr != SP) && (nextChr != HT)) {
				pos--;
				validLine = false;
			} else {
				eol = false;
				// if the buffer is full, extend it
				if (readCount >= maxRead) {
					if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
						char[] newBuffer = new char[2 * maxRead];
						System.arraycopy(header.value, 0, newBuffer, 0, maxRead);
						header.value = newBuffer;
						maxRead = header.value.length;
					} else {
						throw new IOException(sm.getString("requestStream.readline.toolong"));
					}
				}
				header.value[readCount] = ' ';
				readCount++;
			}

		}

		header.valueEnd = readCount;
	}

	/*
	 * 读取下一个字节。
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		// 若已经读完缓冲区中的数据，则调用 fill() 从输入流读取下一部分数据来填充缓冲区
		if (pos >= count) {
			fill();
			if (pos >= count)
				return -1;
		}
		// 从缓冲区中读取指定的字节。
		return buf[pos++] & 0xff;
	}

	/**
	 *
	 */
	/*
	public int read(byte b[], int off, int len)
	    throws IOException {
	
	}
	*/

	/**
	 *
	 */
	/*
	public long skip(long n)
	    throws IOException {
	
	}
	*/

	/*
	 * 返回可以从此输入流读取但不阻塞的字节数。
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		int n = count - pos;
		int avail = is.available();
		return n > (Integer.MAX_VALUE - avail) ? Integer.MAX_VALUE : n + avail;
	}

	/*
	 * 关闭此输入流并释放与该流关联的所有系统资源。
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (is == null)
			return;
		is.close();
		is = null;
		buf = null;
	}

	// ------------------------------------------------------ Protected Methods

	/**
	 * 使用来自底层输入流的数据填充内部缓冲区。
	 * 
	 * 在不支持 mark 的情况下，只需要丢弃原缓冲区即可，也就是重新填充缓冲区。
	 * 
	 * @throws IOException
	 */
	protected void fill() throws IOException {
		pos = 0;
		count = 0;
		int nRead = is.read(buf, 0, buf.length);
		if (nRead > 0) {
			count = nRead;
		}
	}
}
