package com.gmail.tequlia2pop.bendog.connector.http;

/**
 * HTTP 请求行。
 * 
 * @author tequlia2pop
 * @deprecated
 */
final class HttpRequestLine {

	// -------------------------------------------------------------- Constants

	public static final int INITIAL_METHOD_SIZE = 8;
	public static final int INITIAL_URI_SIZE = 64;
	public static final int INITIAL_PROTOCOL_SIZE = 8;
	public static final int MAX_METHOD_SIZE = 1024;
	public static final int MAX_URI_SIZE = 32768;
	public static final int MAX_PROTOCOL_SIZE = 1024;

	// ----------------------------------------------------- Instance Variables

	public char[] method;
	public int methodEnd;
	public char[] uri;
	public int uriEnd;
	public char[] protocol;
	public int protocolEnd;

	// ----------------------------------------------------------- Constructors

	public HttpRequestLine() {
		this(new char[INITIAL_METHOD_SIZE], 0, new char[INITIAL_URI_SIZE], 0,
				new char[INITIAL_PROTOCOL_SIZE], 0);
	}

	public HttpRequestLine(char[] method, int methodEnd, char[] uri, int uriEnd, char[] protocol,
			int protocolEnd) {
		this.method = method;
		this.methodEnd = methodEnd;
		this.uri = uri;
		this.uriEnd = uriEnd;
		this.protocol = protocol;
		this.protocolEnd = protocolEnd;
	}

	// ------------------------------------------------------------- Properties

	// --------------------------------------------------------- Public Methods

	/**
	 * 释放所有的对象引用，并初始化实例变量，以便准备重用此对象。
	 */
	public void recycle() {
		methodEnd = 0;
		uriEnd = 0;
		protocolEnd = 0;
	}

	/**
	 * Test if the value of the header includes the given string.
	 * 测试头的值是否包含给定字符串。
	 */
	public int indexOf(String str) {
		return indexOf(str.toCharArray(), str.length());
	}

	/**
	 * Test if the uri includes the given char array.
	 */
	public int indexOf(char[] buf) {
		return indexOf(buf, buf.length);
	}

	/**
	 * Test if the value of the header includes the given char array.
	 * 测试头的值是否包含给定的 char 数组。
	 */
	public int indexOf(char[] buf, int end) {
		char firstChar = buf[0];
		int pos = 0;
		while (pos < uriEnd) {
			pos = indexOf(firstChar, pos);
			if (pos == -1)
				return -1;
			if ((uriEnd - pos) < end)
				return -1;
			for (int i = 0; i < end; i++) {
				if (uri[i + pos] != buf[i])
					break;
				if (i == (end - 1))
					return pos;
			}
			pos++;
		}
		return -1;
	}

	/**
	 * 返回给定字符在 uri[] 中的索引。
	 */
	public int indexOf(char c, int start) {
		for (int i = start; i < uriEnd; i++) {
			if (uri[i] == c)
				return i;
		}
		return -1;
	}

	// --------------------------------------------------------- Object Methods

	@Override
	public int hashCode() {
		// FIXME
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return false;
	}
}
