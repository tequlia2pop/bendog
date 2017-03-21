package org.apache.catalina.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.junit.Test;

public class RequestUtilTest {

	@Test
	public void testParseCookieHeader() {
		String data = "userName=budi; password=pwd";

		// Cookie 没有覆盖 equals()
		/*ArrayList<Cookie> cookies = new ArrayList<>();
		cookies.add(new Cookie("userName", "budi"));
		cookies.add(new Cookie("password", "pwd"));*/
		/*assertArrayEquals(cookies.toArray(new Cookie[cookies.size()]),
				RequestUtil.parseCookieHeader(cookieHeader));*/

		Cookie[] cookies = RequestUtil.parseCookieHeader(data);
		assertEquals(2, cookies.length);
		assertTrue(cookieEquals(new Cookie("userName", "budi"), cookies[0]));
		assertTrue(cookieEquals(new Cookie("password", "pwd"), cookies[1]));
	}

	@Test
	public void testParseParameters() throws UnsupportedEncodingException {
		String data = "lastName=Franks&firstName=Michael&hobbies=basketball&hobbies=game";
		String encoding = "ISO-8859-1";

		Map<String, String[]> map = new HashMap<>();
		RequestUtil.parseParameters(map, data, encoding);
		assertThat(map, hasEntry("lastName", new String[] { "Franks" }));
		assertThat(map, hasEntry("firstName", new String[] { "Michael" }));
		assertThat(map, hasEntry("hobbies", new String[] { "basketball", "game" }));
	}

	/**
	 * 对请求参数中含有的 '' 和 '%' 需要进行特殊的处理。
	 */
	@Test
	public void testParseParametersWithBlank() throws UnsupportedEncodingException {
		String data = "lastNam+e=Franks&firstName=Michae+l";
		String encoding = "ISO-8859-1";

		Map<String, String[]> map = new HashMap<>();
		RequestUtil.parseParameters(map, data, encoding);
		assertThat(map, hasEntry("lastNam e", new String[] { "Franks" }));
		assertThat(map, hasEntry("firstName", new String[] { "Michae l" }));
	}

	private boolean cookieEquals(Cookie expected, Cookie actual) {
		if (expected == null || actual == null) {
			return false;
		}
		if (expected.getName().equals(actual.getName())
				&& expected.getValue().equals(actual.getValue())) {
			return true;
		}
		return false;
	}
}
