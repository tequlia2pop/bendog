package com.gmail.tequlia2pop.bendog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 该 Servlet 容器能够处理简单的 Servlet 和静态资源。`PrimitiveServlet` 可用于测试 Servlet 容器。
 * 
 * 它会等待 HTTP 请求，为接收到的每个请求创建一个请求和响应对象，
 * 并根据 HTTP 请求的是静态资源或 servlet，将 HTTP 请求分发给对应的 StaticResourceProcessor 实例或 ServletProcessor 实例处理。
 * 
 * @author tequlia2pop
 */
public class HttpServer {

	/** 关闭命令 **/
	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		server.await();
	}

	/** 是否接收到关闭命令 **/
	private boolean shutdown = false;

	public void await() {
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// 循环等待请求
		while (!shutdown) {
			Socket socket = null;
			InputStream input = null;
			OutputStream output = null;
			try {
				// 等待传入的连接请求。只有当接收到连接请求后，accept()方法才会返回
				socket = serverSocket.accept();

				input = socket.getInputStream();
				output = socket.getOutputStream();

				// 创建请求对象，并解析 HTTP 请求的原始数据
				Request request = new Request(input);
				request.parse();

				// 创建响应对象
				Response response = new Response(output);
				response.setRequest(request);

				// 检查这是对 servlet 或静态资源的请求。
				// 对 servlet 的请求以 "/servlet/" 开头。
				if (request.getUri().startsWith("/servlet/")) {
					ServletProcessor processor = new ServletProcessor();
					processor.process(request, response);
				} else {
					StaticResourceProcessor processor = new StaticResourceProcessor();
					processor.process(request, response);
				}

				// 关闭套接字。
				socket.close();

				// 检查 HTTP 请求是否是关闭命令。
				shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
