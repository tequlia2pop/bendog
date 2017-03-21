package com.gmail.tequlia2pop.bendog.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * HTTP 连接器。
 * 它负责创建一个服务器套接字，并等待传入的 HTTP 请求。
 * 
 * HttpConnector 类实现了 java.lang.Runnable 接口，这样它可以专用于自己的线程。
 * 
 * @author tequlia2pop
 */
public class HttpConnector implements Runnable {

	boolean stopped;
	private String scheme = "http";

	/**
	 * 返回请求协议，如 HTTP 协议。
	 * 
	 * @return
	 */
	public String getScheme() {
		return scheme;
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();//
			System.exit(1);
		}
		
		// 等待 HTTP 请求，为每个请求创建一个 HttpProcessor 实例，并调用其 process() 方法。
		while (!stopped) {
			// 从服务器套接字等待下一个传入的连接。
			Socket socket = null;
			try {
				// 只有当接收到连接请求后，accept()方法才会返回。
				socket = serverSocket.accept();
			} catch (Exception e) {
				continue;
			}
			// Hand this socket off to an HttpProcessor
			HttpProcessor processor = new HttpProcessor(this);
			processor.process(socket);
		}
	}

	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}
}