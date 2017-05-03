package com.gmail.tequlia2pop.bendog.core;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.io.File;
import java.io.IOException;
import javax.naming.directory.DirContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Mapper;
import org.apache.catalina.Realm;
import org.apache.catalina.Request;
import org.apache.catalina.Response;

/**
 * SimpleContainer 实现了 org.apache.catalina.Container 接口，这样它就可以与默认连接器进行关联。
 * 
 * 相比上一版的应用程序，这里已经移除了连接器模块，以及对 ServletProcessor 类和 StaticResourceProcessor 类的使用，
 * 所以现在不能请求静态页面了。
 * 
 * @author tequlia2pop
 */
public class SimpleContainer implements Container {

	/** 
	 *  WEB_ROOT 是我们的 HTML 和其他文件所在的目录。
	 *  对于此程序包，WEB_ROOT 是工作目录下的 "\src\main\webapp" 目录。
	 *  工作目录是调用 java 命令的文件系统位置。
	 */
	/*public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator
			+ "webroot";*/
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "src"
			+ File.separator + "main" + File.separator + "webapp";

	public SimpleContainer() {
	}

	public String getInfo() {
		return null;
	}

	public Loader getLoader() {
		return null;
	}

	public void setLoader(Loader loader) {
	}

	public Logger getLogger() {
		return null;
	}

	public void setLogger(Logger logger) {
	}

	public Manager getManager() {
		return null;
	}

	public void setManager(Manager manager) {
	}

	public Cluster getCluster() {
		return null;
	}

	public void setCluster(Cluster cluster) {
	}

	public String getName() {
		return null;
	}

	public void setName(String name) {
	}

	public Container getParent() {
		return null;
	}

	public void setParent(Container container) {
	}

	public ClassLoader getParentClassLoader() {
		return null;
	}

	public void setParentClassLoader(ClassLoader parent) {
	}

	public Realm getRealm() {
		return null;
	}

	public void setRealm(Realm realm) {
	}

	public DirContext getResources() {
		return null;
	}

	public void setResources(DirContext resources) {
	}

	public void addChild(Container child) {
	}

	public void addContainerListener(ContainerListener listener) {
	}

	public void addMapper(Mapper mapper) {
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	public Container findChild(String name) {
		return null;
	}

	public Container[] findChildren() {
		return null;
	}

	public ContainerListener[] findContainerListeners() {
		return null;
	}

	public Mapper findMapper(String protocol) {
		return null;
	}

	public Mapper[] findMappers() {
		return null;
	}

	/*
	 * 默认连接器会调用该方法。
	 * 
	 * 这里会创建一个类载入器，载入相关 servlet 类，并调用该 Servlet 类的 service() 方法。
	 * 
	 * @see org.apache.catalina.Container#invoke(org.apache.catalina.Request, org.apache.catalina.Response)
	 */
	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		String servletName = ((HttpServletRequest) request).getRequestURI();
		servletName = servletName.substring(servletName.lastIndexOf("/") + 1);
		URLClassLoader loader = null;
		try {
			URL[] urls = new URL[1];
			URLStreamHandler streamHandler = null;// 显式声明参数类型，以明确调用哪一个构造函数
			File classPath = new File(WEB_ROOT);
			String repository = (new URL("file", null,
					classPath.getCanonicalPath() + File.separator)).toString();
			urls[0] = new URL(null, repository, streamHandler);
			loader = new URLClassLoader(urls);
		} catch (IOException e) {
			System.out.println(e.toString());
		}

		Class<?> myClass = null;
		try {
			myClass = loader.loadClass(servletName);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}

		Servlet servlet = null;
		try {
			servlet = (Servlet) myClass.newInstance();
			servlet.service((HttpServletRequest) request, (HttpServletResponse) response);
		} catch (Exception e) {
			System.out.println(e.toString());
		} catch (Throwable e) {
			System.out.println(e.toString());
		}

	}

	public Container map(Request request, boolean update) {
		return null;
	}

	public void removeChild(Container child) {
	}

	public void removeContainerListener(ContainerListener listener) {
	}

	public void removeMapper(Mapper mapper) {
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

}