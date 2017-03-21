package com.gmail.tequlia2pop.bendog.connector.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;

import com.gmail.tequlia2pop.bendog.ServletProcessor;
import com.gmail.tequlia2pop.bendog.StaticResourceProcessor;

/**
 * HTTP 处理器。
 * 它负责创建 HttpRequest 和 HttpResponse 对象；
 * 解析 HTTP 请求的第一行内容和请求头信息，并填充到 HttpRequest 对象；
 * 最后将  HttpRequest 和 HttpResponse 对象传递给 ServletProcessor 或 StaticResourceProcessor 处理。
 * 
 * @author tequlia2pop
 */
public class HttpProcessor {

	/** 与此处理器关联的 HttpConnector **/
	private HttpConnector connector = null;
	/** HTTP 请求对象 **/
	private HttpRequest request;
	/** HTTP 请求行 **/
	private HttpRequestLine requestLine = new HttpRequestLine();
	/** HTTP 响应对象 **/
	private HttpResponse response;

	/** 请求方法 **/
	protected String method = null;
	/** 查询字符串 **/
	protected String queryString = null;

	/**
	 * 用于该包的 StringManager。
	 */
	protected StringManager sm = StringManager
			.getManager("com.gmail.tequlia2pop.bendog.connector.http");

	public HttpProcessor(HttpConnector connector) {
		this.connector = connector;
	}

	/**
	 * 该处理方法完成4个操作：
	 * 1. 创建 HttpRequest 对象；
	 * 2. 创建 HttpResponse 对象；
	 * 3. 解析 HTTP 请求的第一行内容和请求头信息，填充 HttpRequest 对象；
	 * 4. 将  HttpRequest 和 HttpResponse 对象传递给 ServletProcessor 或 StaticResourceProcessor 的 process() 方法。
	 * 
	 * @param socket
	 */
	public void process(Socket socket) {
		try (SocketInputStream input = new SocketInputStream(socket.getInputStream(), 2048);
				OutputStream output = socket.getOutputStream();) {
			// 创建一个 HttpRequest 对象
			request = new HttpRequest(input);

			// 创建一个 HttpResponse 对象
			response = new HttpResponse(output);
			response.setRequest(request);

			//
			response.setHeader("Server", "Bendog Servlet Container");

			// 解析 HTTP 请求的第一行内容和请求头信息，填充 HttpRequest 对象
			parseRequest(input, output);
			parseHeaders(input);

			// 根据请求的 URI 模式来判断这是对 servlet 或静态资源的请求。
			// 对 servlet 的请求以 "/servlet/" 开头。
			if (request.getRequestURI().startsWith("/servlet/")) {
				ServletProcessor processor = new ServletProcessor();
				processor.process(request, response);
			} else {
				StaticResourceProcessor processor = new StaticResourceProcessor();
				processor.process(request, response);
			}

			// 关闭套接字
			socket.close();

			// no shutdown for this application
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 此方法是 org.apache.catalina.connector.http.HttpProcessor 中类似方法的简化版本。
	 * 但是，此方法仅解析一些“简单”的请求头，例如 "cookie"、"content-length" 和 "content-type"，并忽略其他请求头。
	 * 
	 * @param input 连接到套接字的输入流
	 *
	 * @exception IOException 如果发生输入/输出错误
	 * @exception ServletException 如果发生解析错误
	 */
	private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
		// 循环地从 SocketInputStream 中读取请求头信息。
		while (true) {
			HttpHeader header = new HttpHeader();

			// 读取下一个请求头
			input.readHeader(header);

			// 检查是否已经从输入流中读取了所有的请求头信息
			if (header.nameEnd == 0) {
				if (header.valueEnd == 0) {
					return;
				} else {
					throw new ServletException(sm.getString("httpProcessor.parseHeaders.colon"));
				}
			}

			// 获取请求头的名称和值
			String name = new String(header.name, 0, header.nameEnd);
			String value = new String(header.value, 0, header.valueEnd);

			// 将请求头信息添加到 HttpRequest 的 HashMap 请求头中
			request.addHeader(name, value);

			// 某些请求头会包含一些属性设置信息。
			// 这里设置了 "cookie"、"content-length" 和 "content-type" 请求头的属性。
			if (name.equals("cookie")) {
				Cookie cookies[] = RequestUtil.parseCookieHeader(value);
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals("jsessionid")) {
						// Override anything requested in the URL
						if (!request.isRequestedSessionIdFromCookie()) {
							// 只接受第一个 会话标识 cookie
							request.setRequestedSessionId(cookies[i].getValue());
							request.setRequestedSessionCookie(true);
							request.setRequestedSessionURL(false);
						}
					}
					request.addCookie(cookies[i]);
				}
			} else if (name.equals("content-length")) {
				int n = -1;
				try {
					n = Integer.parseInt(value);
				} catch (Exception e) {
					throw new ServletException(
							sm.getString("httpProcessor.parseHeaders.contentLength"));
				}
				request.setContentLength(n);
			} else if (name.equals("content-type")) {
				request.setContentType(value);
			}
		}
	}

	/**
	 * 解析请求行，包括请求方法、请求 URI、请求协议和版本，以及查询字符串和 session ID。
	 * 
	 * 关于请求 URI，有以下注意事项：
	 * （1）请求 URI 后面可以加上可选的查询字符串。当浏览器禁用 Cookie 时，也可以将会话标识（jsessionid）嵌入到查询字符串中。
	 * （2）当请求 URI 是一个绝对路径中的值时，需要删除其协议和主机名部分。
	 * （3）对请求 URI 要进行规范化。
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 * @throws ServletException
	 */
	private void parseRequest(SocketInputStream input, OutputStream output)
			throws IOException, ServletException {
		// 解析传入的请求行
		input.readRequestLine(requestLine);
		String method = new String(requestLine.method, 0, requestLine.methodEnd);
		String uri = null;
		String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

		// 验证传入的请求行
		if (method.length() < 1) {
			throw new ServletException("Missing HTTP request method");
		} else if (requestLine.uriEnd < 1) {
			throw new ServletException("Missing HTTP request URI");
		}

		// 解析请求 URI 中所有的查询字符串
		// 查询字符串与 URI 用一个"?"分隔
		int question = requestLine.indexOf("?");// "?" 在 uri[] 中的位置
		if (question >= 0) {
			request.setQueryString(
					new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1));
			uri = new String(requestLine.uri, 0, question);
		} else {
			request.setQueryString(null);
			uri = new String(requestLine.uri, 0, requestLine.uriEnd);
		}

		// 检查是否为绝对路径的 URI（使用 HTTP 协议）
		// URI 可以指向一个相对路径中的资源，也可以是一个绝对路径中的值，例如：
		// 相对 URI —— /myApp/ModernServlet?userName=tarzan&password=pwd
		// 绝对路径 —— http://www.brainysoftware.com/index.html?name=Tarzan
		if (!uri.startsWith("/")) {
			int pos = uri.indexOf("://");
			// 解析协议和主机名
			if (pos != -1) {
				pos = uri.indexOf('/', pos + 3);
				if (pos == -1) {
					uri = "";
				} else {
					uri = uri.substring(pos);
				}
			}
		}

		// 解析请求 URI 中请求的会话 ID
		// 若存在参数 jsessionid，则表明会话标识符在查询字符串中，而不在 Cookie 中
		String match = ";jsessionid=";
		int semicolon = uri.indexOf(match);
		if (semicolon >= 0) {
			String rest = uri.substring(semicolon + match.length());
			int semicolon2 = rest.indexOf(';');
			if (semicolon2 >= 0) {
				request.setRequestedSessionId(rest.substring(0, semicolon2));
				rest = rest.substring(semicolon2);
			} else {
				request.setRequestedSessionId(rest);
				rest = "";
			}
			request.setRequestedSessionURL(true);
			uri = uri.substring(0, semicolon) + rest;
		} else {
			request.setRequestedSessionId(null);
			request.setRequestedSessionURL(false);
		}

		// 规范化 URI（使用字符串操作）
		String normalizedUri = normalize(uri);

		// 设置相应的请求属性
		request.setMethod(method);
		request.setProtocol(protocol);
		if (normalizedUri != null) {
			request.setRequestURI(normalizedUri);
		} else {
			request.setRequestURI(uri);
		}

		// 如果 normalizedUri 为 null，将会抛出异常。
		if (normalizedUri == null) {
			throw new ServletException("Invalid URI: " + uri + "'");
		}
	}

	/**
	 * 返回一个上下文相对路径，以"/"开头，表示解析得到的".."和"."元素之后的指定路径的规范版本。 
	 * 如果指定的路径试图超出当前上下文的边界（即存在太多的".."路径元素），则返回<code>null</ code>。
	 *  
	 * <p>对非正常的 URL 进行修正。例如，出现"\"的地方会被替换为"/"。
	 * 若 URI 本身是正常的，则返回相同的 URI；否则，返回修正过的 URI。若 URI 无法修正，则会认为它是无效的，将会返回 null。
	 *
	 * @param path 要规范化的路径
	 */
	protected String normalize(String path) {
		if (path == null)
			return null;

		// Create a place for the normalized path
		String normalized = path;

		// 将开头的 "/%7E" 和 "/%7e" 标准化为 "/~"
		if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
			normalized = "/~" + normalized.substring(4);

		// 防止对 '%'、'/'、'.' 和 '\' 进行编码，这些是特殊的保留字符
		if ((normalized.indexOf("%25") >= 0) || (normalized.indexOf("%2F") >= 0)
				|| (normalized.indexOf("%2E") >= 0) || (normalized.indexOf("%5C") >= 0)
				|| (normalized.indexOf("%2f") >= 0) || (normalized.indexOf("%2e") >= 0)
				|| (normalized.indexOf("%5c") >= 0)) {
			return null;
		}

		if (normalized.equals("/."))
			return "/";

		// 规范化斜线，如有必要添加前导斜线
		if (normalized.indexOf('\\') >= 0)
			normalized = normalized.replace('\\', '/');
		if (!normalized.startsWith("/"))
			normalized = "/" + normalized;

		// 对规范化路径中出现的 "//" 进行解析
		while (true) {
			int index = normalized.indexOf("//");
			if (index < 0)
				break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 1);
		}

		// 对规范化路径中出现的 "/./" 进行解析
		while (true) {
			int index = normalized.indexOf("/./");
			if (index < 0)
				break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 2);
		}

		// 对规范化路径中出现的 "/../" 进行解析
		while (true) {
			int index = normalized.indexOf("/../");
			if (index < 0)
				break;
			if (index == 0)
				return null; // 试图跳出上下文
			int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
		}

		// 声明 "/..."（三个或更多个点）的出现是无效的（在某些Windows平台上，这会遍历目录树!!!）
		if (normalized.indexOf("/...") >= 0)
			return null;

		// 返回我们已完成的规范化路径
		return normalized;
	}

}
