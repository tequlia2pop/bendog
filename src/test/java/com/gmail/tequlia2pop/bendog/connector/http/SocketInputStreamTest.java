package com.gmail.tequlia2pop.bendog.connector.http;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;

public class SocketInputStreamTest {

	private static final String CRLF = "\r\n";

	private static String REQUEST = "POST /examples/default.jsp HTTP/1.1" + CRLF + //
			"Accept: text/plain; text/html" + CRLF + //
			"Accept-Language: en-gb" + CRLF + //
			"Connection: Keep-Alive" + CRLF + //
			"Host: localhost" + CRLF + //
			"User-Agent: Mozilla/4.0 (compatible; MSIE 4.01; Windows 98)" + CRLF + //
			"Content-Length: 33" + CRLF + //
			"Content-Type: application/x-www-form-urlencoded" + CRLF + //
			"Accept-Encoding: gzip, deflate" + CRLF + //
			CRLF + //
			"lastName=Franks&firstName=Michael";

	private SocketInputStream input;

	private HttpRequestLine requestLine;

	private HttpHeader header;

	@Before
	public void setUp() {
		InputStream is = new ByteArrayInputStream(REQUEST.getBytes());
		input = new SocketInputStream(is, 2048);

		requestLine = new HttpRequestLine();
	}

	@Test
	public void testReadRequestLine() throws IOException {
		input.readRequestLine(requestLine);
		String method = new String(requestLine.method, 0, requestLine.methodEnd);
		assertEquals("POST", method);
		String uri = new String(requestLine.uri, 0, requestLine.uriEnd);
		assertEquals("/examples/default.jsp", uri);
		String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);
		assertEquals("HTTP/1.1", protocol);
	}

	@Test
	public void testReadHeader() throws IOException, ServletException {
		input.readRequestLine(requestLine);

		HashMap<String, ArrayList<String>> headers = new HashMap<>();
		while (true) {
			header = new HttpHeader();

			input.readHeader(header);

			// 检查是否已经从输入流中读取了所有的请求头信息
			if (header.nameEnd == 0) {
				if (header.valueEnd == 0) {
					break;
				} else {
					throw new ServletException("HTTP 请求头格式无效");
				}
			}

			String name = new String(header.name, 0, header.nameEnd);
			String value = new String(header.value, 0, header.valueEnd);

			ArrayList<String> values = headers.get(name);
			if (values == null) {
				values = new ArrayList<>();
				headers.put(name, values);
			}
			values.add(value);
		}
		
		assertThat(headers, hasEntry("accept", Arrays.asList("text/plain; text/html")));
		assertThat(headers, hasEntry("accept-language", Arrays.asList("en-gb")));
		assertThat(headers, hasEntry("connection", Arrays.asList("Keep-Alive")));
		assertThat(headers, hasEntry("host", Arrays.asList("localhost")));
		assertThat(headers, hasEntry("user-agent",
				Arrays.asList("Mozilla/4.0 (compatible; MSIE 4.01; Windows 98)")));
		assertThat(headers, hasEntry("content-length", Arrays.asList("33")));
		assertThat(headers,
				hasEntry("content-type", Arrays.asList("application/x-www-form-urlencoded")));
		assertThat(headers, hasEntry("accept-encoding", Arrays.asList("gzip, deflate")));
	}
}
