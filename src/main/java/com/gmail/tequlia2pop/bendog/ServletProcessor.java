package com.gmail.tequlia2pop.bendog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import javax.servlet.Servlet;

/**
 * Servlet 处理器。
 * 它负责载入相应的 Servlet 类，调用其 service() 方法，同时传入 ServletRequest 和 ServletResponse 对象。
 * 
 * URI 的格式为：servlet/servletName，其中 servletName 是请求的 Servlet 资源的类名。
 * 
 * 注意：每次请求 Servlet 都会载入相应的 Servlet 类，并且不会调用 Servlet 的 init() 和 destroy() 方法。
 * 
 * @author tequlia2pop
 */
public class ServletProcessor {

	/**
	 * 从 URI 获得 Servlet 类名，创建并使用 java.net.URLClassLoader 来载入 Servlet 类，
	 * 调用其 service() 方法，同时传入 ServletRequest 和 ServletResponse 对象。
	 * 
	 * @param request
	 * @param response
	 */
	public void process(Request request, Response response) {
		String uri = request.getUri();
		String servletName = uri.substring(uri.lastIndexOf("/") + 1);
		
		URLClassLoader loader = null;
		try {
			// 创建 URLClassLoader
			URL[] urls = new URL[1];// 每一个 URL 都指明类加载器要到哪里查找类。若 URL 以 "/" 结尾，则表名它指向一个目录；否则，URL 默认指向一个 JAR 文件
			URLStreamHandler streamHandler = null;// 显式声明参数类型，以明确调用哪一个构造函数
			File classPath = new File(Constants.WEB_ROOT);
			// the forming of repository is taken from the createClassLoader method in
			// org.apache.catalina.startup.ClassLoaderFactory
			String repository = (new URL("file", null,
					classPath.getCanonicalPath() + File.separator)).toString();
			// the code for forming the URL is taken from the addRepository method in
			// org.apache.catalina.loader.StandardClassLoader class.
			urls[0] = new URL(null, repository, streamHandler);
			loader = new URLClassLoader(urls);
		} catch (IOException e) {
			System.out.println(e.toString());//
		}
		
		Class<?> myClass = null;
		try {
			myClass = loader.loadClass(servletName);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());//
		}

		Servlet servlet = null;
		RequestFacade requestFacade = new RequestFacade(request);
	    ResponseFacade responseFacade = new ResponseFacade(response);
		try {
			servlet = (Servlet) myClass.newInstance();
			servlet.service(requestFacade, responseFacade);
		} catch (Exception e) {
			System.out.println(e.toString());//
		} catch (Throwable e) {
			System.out.println(e.toString());//
		}
	}
}