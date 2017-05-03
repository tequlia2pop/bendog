package org.apache.catalina.connector.http;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

public class HttpRequestLineTest {

	private HttpRequestLine requestLine;

	@Before
	public void setUp() throws IOException {
		InputStream is = new ByteArrayInputStream(
				"GET /myApp/ModernServlet?userName=tarzan&password=pwd HTTP/1.1\r\n".getBytes());
		SocketInputStream input = new SocketInputStream(is, 2048);
		requestLine = new HttpRequestLine();

		input.readRequestLine(requestLine);
	}

	@Test
	public void testIndexOf() {
		int question = requestLine.indexOf("?");
		assertEquals(20, question);
		assertEquals("userName=tarzan&password=pwd",
				new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1));
		assertEquals("/myApp/ModernServlet", new String(requestLine.uri, 0, question));
	}
}
