package com.gmail.tequlia2pop.bendog.connector.http;

/**
 * HTTP 请求头。
 * 
 * 要获取请求头的名字和值，可以使用如下的方法：
 * <code>
 * String name = new String(header.name, 0, header.nameEnd);
 * String value = new String(header.value, 0, header.valueEnd);
 * </code>
 * 
 * 若没有请求头信息，则 HttpHeader 实例的 nameEnd 和 valueEnd 字段都会是 0。
 * 
 * @author tequlia2pop
 * @deprecated
 */
final class HttpHeader {

	// -------------------------------------------------------------- Constants

	public static final int INITIAL_NAME_SIZE = 32;
	public static final int INITIAL_VALUE_SIZE = 64;
	public static final int MAX_NAME_SIZE = 128;
	public static final int MAX_VALUE_SIZE = 4096;

	// ----------------------------------------------------- Instance Variables

	public char[] name;
	public int nameEnd;
	public char[] value;
	public int valueEnd;
	
	protected int hashCode = 0;

	// ----------------------------------------------------------- Constructors

	public HttpHeader() {
		this(new char[INITIAL_NAME_SIZE], 0, new char[INITIAL_VALUE_SIZE], 0);
	}

	public HttpHeader(char[] name, int nameEnd, char[] value, int valueEnd) {
		this.name = name;
		this.nameEnd = nameEnd;
		this.value = value;
		this.valueEnd = valueEnd;
	}

	public HttpHeader(String name, String value) {
		this.name = name.toLowerCase().toCharArray();
		this.nameEnd = name.length();
		this.value = value.toCharArray();
		this.valueEnd = value.length();
	}

	// ------------------------------------------------------------- Properties

	// --------------------------------------------------------- Public Methods

	/**
	 * Release all object references, and initialize instance variables, in
	 * preparation for reuse of this object.
	 */
	public void recycle() {
		nameEnd = 0;
		valueEnd = 0;
		hashCode = 0;
	}

	/**
	 * 检查请求头的名称是否等于给定的 char 数组。所有字符必须为小写字母。
	 */
	public boolean equals(char[] buf) {
		return equals(buf, buf.length);
	}

	/**
	 * 检查请求头的名称是否等于给定的 char 数组。所有字符必须为小写字母。
	 */
	public boolean equals(char[] buf, int end) {
		if (end != nameEnd)
			return false;
		for (int i = 0; i < end; i++) {
			if (buf[i] != name[i])
				return false;
		}
		return true;
	}

	/**
	 * 检查请求头的名称是否等于给定的字符串。给定的字符串必须由小写字母组成。
	 */
	public boolean equals(String str) {
		return equals(str.toCharArray(), str.length());
	}

	/**
	 * Test if the value of the header is equal to the given char array.
	 */
	public boolean valueEquals(char[] buf) {
		return valueEquals(buf, buf.length);
	}

	/**
	 * 检查请求头的值是否等于给定的 char 数组。
	 */
	public boolean valueEquals(char[] buf, int end) {
		if (end != valueEnd)
			return false;
		for (int i = 0; i < end; i++) {
			if (buf[i] != value[i])
				return false;
		}
		return true;
	}

	/**
	 * 检查请求头的值是否等于给定的字符串。
	 */
	public boolean valueEquals(String str) {
		return valueEquals(str.toCharArray(), str.length());
	}

	/**
	 * 测试请求头的值是否包含给定的 char 数组。
	 */
	public boolean valueIncludes(char[] buf) {
		return valueIncludes(buf, buf.length);
	}

	/**
	 * 测试请求头的值是否包含给定的 char 数组。
	 */
	public boolean valueIncludes(char[] buf, int end) {
		char firstChar = buf[0];
		int pos = 0;
		while (pos < valueEnd) {
			pos = valueIndexOf(firstChar, pos);
			if (pos == -1)
				return false;
			if ((valueEnd - pos) < end)
				return false;
			for (int i = 0; i < end; i++) {
				if (value[i + pos] != buf[i])
					break;
				if (i == (end - 1))
					return true;
			}
			pos++;
		}
		return false;
	}

	/**
	 * 测试请求头的值是否包含给定的字符串。
	 */
	public boolean valueIncludes(String str) {
		return valueIncludes(str.toCharArray(), str.length());
	}

	/**
	 * 返回指定字符在请求头值中的索引。
	 */
	public int valueIndexOf(char c, int start) {
		for (int i = start; i < valueEnd; i++) {
			if (value[i] == c)
				return i;
		}
		return -1;
	}

	/**
	 * 检查请求头的名称是否和给定的请求头相同。
	 * 名称中的所有字符必须为小写字母。
	 */
	public boolean equals(HttpHeader header) {
		return equals(header.name, header.nameEnd);
	}

	/**
	 * 检查请求头的名称和值是否和给定的请求头相同。
	 * 名称中的所有字符必须为小写字母。
	 */
	public boolean headerEquals(HttpHeader header) {
		return (equals(header.name, header.nameEnd))
				&& (valueEquals(header.value, header.valueEnd));
	}

	// --------------------------------------------------------- Object Methods

	/*
	 * 返回哈希码。
	 * HttpHeader 对象的哈希码与 new String(name, 0, nameEnd).hashCode() 返回的哈希码相同。
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int h = hashCode;
		if (h == 0) {
			int off = 0;
			char val[] = name;
			int len = nameEnd;
			for (int i = 0; i < len; i++)
				h = 31 * h + val[off++];
			hashCode = h;
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			return equals(((String) obj).toLowerCase());
		} else if (obj instanceof HttpHeader) {
			return equals((HttpHeader) obj);
		}
		return false;
	}

	@Override
	public String toString() {
		return "HttpHeader [name=" + new String(name, 0, nameEnd) + ", value="
				+ new String(value, 0, valueEnd) + "]";
	}
}
