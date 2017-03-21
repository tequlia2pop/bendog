package com.gmail.tequlia2pop.bendog.connector.http;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Test;

public class HttpProcessorTest {

	@Test
	public void testNormalize() throws UnsupportedEncodingException {
		HttpProcessor httpProcessor = new HttpProcessor(null);
		assertNull("防止对 '%'、'/'、'.' 和 '\' 进行编码，这些是特殊的保留字符",
				httpProcessor.normalize("/myApp/xxx" + URLEncoder.encode("%", "UTF-8") + "yyy"));
		assertEquals("", "/", httpProcessor.normalize("/."));
		assertEquals("规范化斜线，如有必要添加前导斜线", "/myApp/ModernServlet",
				httpProcessor.normalize("myApp\\ModernServlet"));
		assertEquals("对规范化路径中出现的 \"//\" 进行解析", "/myApp/model/ModernServlet",
				httpProcessor.normalize("/myApp//model//ModernServlet"));
		assertEquals("对规范化路径中出现的 \"/./\" 进行解析", "/myApp/model/ModernServlet",
				httpProcessor.normalize("/myApp/./model/./ModernServlet"));
		assertNull("对规范化路径中出现的 \"/../\" 进行解析", httpProcessor.normalize("/../ModernServlet"));
		assertEquals("对规范化路径中出现的 \"/../\" 进行解析", "/myApp/ModernServlet",
				httpProcessor.normalize("/myApp/some/../ModernServlet"));
		assertNull("声明 \"/...\"（三个或更多个点）的出现是无效的（在某些Windows平台上，这会遍历目录树!!!）",
				httpProcessor.normalize("/myApp/.../ModernServlet"));
	}
}
