package org.apache.catalina.connector.http;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;
import java.util.Stack;
import java.util.Vector;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.UnrecoverableKeyException;
import java.security.KeyManagementException;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Service;
import org.apache.catalina.net.DefaultServerSocketFactory;
import org.apache.catalina.net.ServerSocketFactory;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * HTTP/1.1 连接器的实现。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public final class HttpConnector implements Connector, Lifecycle, Runnable {

	// ----------------------------------------------------- Instance Variables

	/**
	 * The <code>Service</code> we are associated with (if any).
	 */
	private Service service = null;

	/**
	 * 该连接器可以接收的连接数。
	 */
	private int acceptCount = 10;

	/**
	 * 要绑定的IP地址（如果有的话）。如果为<code>null</code>，将绑定服务器上的所有地址。
	 */
	private String address = null;

	/**
	 * 在输入流上创建的输入缓冲区的大小。
	 */
	private int bufferSize = 2048;

	/**
	 * 容器，用于处理此连接器收到的请求。
	 */
	protected Container container = null;

	/**
	 * 已经创建的处理器的集合。
	 */
	private Vector<HttpProcessor> created = new Vector<>();

	/**
	 * 当前已经创建的处理器数量。
	 */
	private int curProcessors = 0;

	/**
	 * The debugging detail level for this component.
	 */
	private int debug = 0;

	/**
	 * The "enable DNS lookups" flag for this Connector.
	 */
	private boolean enableLookups = false;

	/**
	 * 该组件的服务器套接字工厂。
	 */
	private ServerSocketFactory factory = null;

	/**
	 * Descriptive information about this Connector implementation.
	 */
	private static final String info = "org.apache.catalina.connector.http.HttpConnector/1.0";

	/**
	 * 该组件的生命周期事件支持。
	 */
	protected LifecycleSupport lifecycle = new LifecycleSupport(this);

	/**
	 * 初始化时启用的处理器的最小数量。
	 */
	protected int minProcessors = 5;

	/**
	 * 允许的处理器的最大数量，当该值<0时表示无限制。
	 */
	private int maxProcessors = 20;

	/**
	 * 传入的连接上的超时值。
	 * 注意：值为0表示没有超时限制。
	 */
	private int connectionTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT;

	/**
	 * 监听 HTTP 请求的端口号。
	 */
	private int port = 8080;

	/**
	 * 处理器集合。虽然已经创建，但当前并不会用来处理请求。
	 */
	private Stack<HttpProcessor> processors = new Stack<>();

	/**
	 * The server name to which we should pretend requests to this Connector
	 * were directed.  This is useful when operating Tomcat behind a proxy
	 * server, so that redirects get constructed accurately.  If not specified,
	 * the server name included in the <code>Host</code> header is used.
	 */
	private String proxyName = null;

	/**
	 * The server port to which we should pretent requests to this Connector
	 * were directed.  This is useful when operating Tomcat behind a proxy
	 * server, so that redirects get constructed accurately.  If not specified,
	 * the port number specified by the <code>port</code> property is used.
	 */
	private int proxyPort = 0;

	/**
	 * The redirect port for non-SSL to SSL redirects.
	 */
	private int redirectPort = 443;

	/**
	 * 请求 scheme，将为通过此连接器接收的所有请求设置该请求 scheme。
	 */
	private String scheme = "http";

	/**
	 * 安全连接标志，将为通过该连接器接收到的所有请求设置该标志。
	 */
	private boolean secure = false;

	/**
	 * 服务器套接字，我们通过它来监听传入的 TCP 连接。
	 */
	private ServerSocket serverSocket = null;

	/**
	 * The string manager for this package.
	 */
	private StringManager sm = StringManager.getManager(Constants.Package);

	/**
	 * 这个组件是否已初始化？
	 */
	private boolean initialized = false;

	/**
	 * 已经启动该组件了吗？
	 */
	private boolean started = false;

	/**
	 * 关闭后台线程的信号
	 */
	private boolean stopped = false;

	/**
	 * 后台线程。
	 */
	private Thread thread = null;

	/**
	 * 后台线程注册的名称。
	 */
	private String threadName = null;

	/**
	 * 线程同步对象。
	 */
	private Object threadSync = new Object();

	/**
	 * 是否允许块编码？
	 */
	private boolean allowChunking = true;

	/**
	 * Use TCP no delay ?
	 */
	private boolean tcpNoDelay = true;

	// ------------------------------------------------------------- Properties

	/**
	 * Return the <code>Service</code> with which we are associated (if any).
	 */
	public Service getService() {

		return (this.service);

	}

	/**
	 * Set the <code>Service</code> with which we are associated (if any).
	 *
	 * @param service The service that owns this Engine
	 */
	public void setService(Service service) {

		this.service = service;

	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getAcceptCount() {
		return acceptCount;
	}

	public void setAcceptCount(int count) {
		this.acceptCount = count;
	}

	public boolean isChunkingAllowed() {
		return allowChunking;
	}

	/**
	 * Get the allow chunking flag.
	 */
	public boolean getAllowChunking() {

		return isChunkingAllowed();

	}

	public void setAllowChunking(boolean allowChunking) {
		this.allowChunking = allowChunking;
	}

	/**
	 * Return the bind IP address for this Connector.
	 */
	public String getAddress() {

		return (this.address);

	}

	/**
	 * Set the bind IP address for this Connector.
	 *
	 * @param address The bind IP address
	 */
	public void setAddress(String address) {

		this.address = address;

	}

	/**
	 * Is this connector available for processing requests?
	 */
	public boolean isAvailable() {

		return (started);

	}

	public int getBufferSize() {
		return this.bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public Container getContainer() {
		return container;
	}

	@Override
	public void setContainer(Container container) {
		this.container = container;
	}

	/**
	 * Return the current number of processors that have been created.
	 */
	public int getCurProcessors() {
		return curProcessors;
	}

	/**
	 * Return the debugging detail level for this component.
	 */
	public int getDebug() {

		return (debug);

	}

	/**
	 * Set the debugging detail level for this component.
	 *
	 * @param debug The new debugging detail level
	 */
	public void setDebug(int debug) {

		this.debug = debug;

	}

	/**
	 * Return the "enable DNS lookups" flag.
	 */
	public boolean getEnableLookups() {

		return (this.enableLookups);

	}

	/**
	 * Set the "enable DNS lookups" flag.
	 *
	 * @param enableLookups The new "enable DNS lookups" flag value
	 */
	public void setEnableLookups(boolean enableLookups) {

		this.enableLookups = enableLookups;

	}

	/**
	 * 返回此容器使用的服务器套接字工厂。
	 */
	public ServerSocketFactory getFactory() {
		if (this.factory == null) {
			synchronized (this) {
				this.factory = new DefaultServerSocketFactory();
			}
		}
		return this.factory;
	}

	@Override
	public void setFactory(ServerSocketFactory factory) {
		this.factory = factory;
	}

	/**
	 * Return descriptive information about this Connector implementation.
	 */
	public String getInfo() {
		return (info);
	}

	/**
	 * Return the minimum number of processors to start at initialization.
	 */
	public int getMinProcessors() {

		return (minProcessors);

	}

	/**
	 * Set the minimum number of processors to start at initialization.
	 *
	 * @param minProcessors The new minimum processors
	 */
	public void setMinProcessors(int minProcessors) {

		this.minProcessors = minProcessors;

	}

	/**
	 * Return the maximum number of processors allowed, or <0 for unlimited.
	 */
	public int getMaxProcessors() {

		return (maxProcessors);

	}

	/**
	 * Set the maximum number of processors allowed, or <0 for unlimited.
	 *
	 * @param maxProcessors The new maximum processors
	 */
	public void setMaxProcessors(int maxProcessors) {

		this.maxProcessors = maxProcessors;

	}

	/**
	 * 返回监听 HTTP 请求的端口号。
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * 设置监听 HTTP 请求的端口号。
	 *
	 * @param port 新的端口号
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Return the proxy server name for this Connector.
	 */
	public String getProxyName() {

		return (this.proxyName);

	}

	/**
	 * Set the proxy server name for this Connector.
	 *
	 * @param proxyName The new proxy server name
	 */
	public void setProxyName(String proxyName) {

		this.proxyName = proxyName;

	}

	/**
	 * Return the proxy server port for this Connector.
	 */
	public int getProxyPort() {

		return (this.proxyPort);

	}

	/**
	 * Set the proxy server port for this Connector.
	 *
	 * @param proxyPort The new proxy server port
	 */
	public void setProxyPort(int proxyPort) {

		this.proxyPort = proxyPort;

	}

	/**
	 * Return the port number to which a request should be redirected if
	 * it comes in on a non-SSL port and is subject to a security constraint
	 * with a transport guarantee that requires SSL.
	 */
	public int getRedirectPort() {

		return (this.redirectPort);

	}

	/**
	 * Set the redirect port number.
	 *
	 * @param redirectPort The redirect port number (non-SSL to SSL)
	 */
	public void setRedirectPort(int redirectPort) {

		this.redirectPort = redirectPort;

	}

	public String getScheme() {
		return this.scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public boolean getSecure() {
		return this.secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * Return the TCP no delay flag value.
	 */
	public boolean getTcpNoDelay() {

		return (this.tcpNoDelay);

	}

	/**
	 * Set the TCP no delay flag which will be set on the socket after
	 * accepting a connection.
	 *
	 * @param tcpNoDelay The new TCP no delay flag
	 */
	public void setTcpNoDelay(boolean tcpNoDelay) {

		this.tcpNoDelay = tcpNoDelay;

	}

	// --------------------------------------------------------- Public Methods

	@Override
	public Request createRequest() {
		//        if (debug >= 2)
		//            log("createRequest: Creating new request");
		HttpRequestImpl request = new HttpRequestImpl();
		request.setConnector(this);
		return request;

	}

	@Override
	public Response createResponse() {
		//        if (debug >= 2)
		//            log("createResponse: Creating new response");
		HttpResponseImpl response = new HttpResponseImpl();
		response.setConnector(this);
		return response;
	}

	// -------------------------------------------------------- Package Methods

	/**
	 * 回收指定的处理器，以便可以再次使用。
	 *
	 * @param processor 要回收的处理器
	 */
	void recycle(HttpProcessor processor) {
		//        if (debug >= 2)
		//            log("recycle: Recycling processor " + processor);
		processors.push(processor);
	}

	// -------------------------------------------------------- Private Methods

	/**
	 * 如果可能，创建（或分配）并返回一个可用的处理器，以用于处理特定的 HTTP 请求。
	 * 如果已经创建的处理器达到了允许的最大数量，并且都在使用，返回 <code>null</code>。
	 */
	private HttpProcessor createProcessor() {
		synchronized (processors) {
			if (processors.size() > 0) {
				// if (debug >= 2)
				// log("createProcessor: Reusing existing processor");
				return processors.pop();
			}
			if ((maxProcessors > 0) && (curProcessors < maxProcessors)) {
				// if (debug >= 2)
				// log("createProcessor: Creating new processor");
				return newProcessor();
			} else {
				if (maxProcessors < 0) {
					// if (debug >= 2)
					// log("createProcessor: Creating new processor");
					return newProcessor();
				} else {
					// if (debug >= 2)
					// log("createProcessor: Cannot create new processor");
					return null;
				}
			}
		}
	}

	/**
	 * Log a message on the Logger associated with our Container (if any).
	 *
	 * @param message Message to be logged
	 */
	private void log(String message) {
		Logger logger = container.getLogger();
		String localName = threadName;
		if (localName == null)
			localName = "HttpConnector";
		if (logger != null)
			logger.log(localName + " " + message);
		else
			System.out.println(localName + " " + message);

	}

	/**
	 * Log a message on the Logger associated with our Container (if any).
	 *
	 * @param message Message to be logged
	 * @param throwable Associated exception
	 */
	private void log(String message, Throwable throwable) {

		Logger logger = container.getLogger();
		String localName = threadName;
		if (localName == null)
			localName = "HttpConnector";
		if (logger != null)
			logger.log(localName + " " + message, throwable);
		else {
			System.out.println(localName + " " + message);
			throwable.printStackTrace(System.out);
		}

	}

	/**
	 * 创建并返回一个新的处理器，它用于处理 HTTP 请求并返回相应响应。
	 */
	private HttpProcessor newProcessor() {
		//        if (debug >= 2)
		//            log("newProcessor: Creating new processor");
		HttpProcessor processor = new HttpProcessor(this, curProcessors++);
		if (processor instanceof Lifecycle) {
			try {
				((Lifecycle) processor).start();
			} catch (LifecycleException e) {
				log("newProcessor", e);
				return null;
			}
		}
		created.addElement(processor);
		return processor;
	}

	/**
	 * 打开并返回此连接器的服务器套接字。
	 * 如果指定了IP地址，则将在该地址上打开套接字;
	 * 否则将在所有地址上打开套接字。
	 *
	 * @exception IOException                input/output or network error
	 * @exception KeyStoreException          error instantiating the
	 *                                       KeyStore from file (SSL only)
	 * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
	 *                                       by current provider (SSL only)
	 * @exception CertificateException       general certificate error (SSL only)
	 * @exception UnrecoverableKeyException  internal KeyStore problem with
	 *                                       the certificate (SSL only)
	 * @exception KeyManagementException     problem in the key management
	 *                                       layer (SSL only)
	 */
	private ServerSocket open() throws IOException, KeyStoreException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableKeyException, KeyManagementException {
		// 获取此连接器的服务端套接字工厂
		ServerSocketFactory factory = getFactory();

		// 如果没有指定地址，会尝试在所有地址上建立一个连接
		if (address == null) {
			log(sm.getString("httpConnector.allAddresses"));
			try {
				return factory.createSocket(port, acceptCount);
			} catch (BindException be) {
				throw new BindException(be.getMessage() + ":" + port);
			}
		}

		// 在指定的地址上打开服务器套接字
		try {
			InetAddress is = InetAddress.getByName(address);
			log(sm.getString("httpConnector.anAddress", address));
			try {
				return factory.createSocket(port, acceptCount, is);
			} catch (BindException be) {
				throw new BindException(be.getMessage() + ":" + address + ":" + port);
			}
		} catch (Exception e) {
			log(sm.getString("httpConnector.noAddress", address));
			try {
				return factory.createSocket(port, acceptCount);
			} catch (BindException be) {
				throw new BindException(be.getMessage() + ":" + port);
			}
		}
	}

	// ---------------------------------------------- Background Thread Methods

	/**
	 * 后台线程，它负责监听传入的 TCP/IP连接，并将它们移交给适当的处理器。
	 */
	@Override
	public void run() {
		// 循环，直到我们收到一个关闭命令
		while (!stopped) {
			// 接受来自服务器套接字的下一个传入的连接
			Socket socket = null;
			try {
				//                if (debug >= 3)
				//                    log("run: Waiting on serverSocket.accept()");
				socket = serverSocket.accept();
				//                if (debug >= 3)
				//                    log("run: Returned from serverSocket.accept()");
				if (connectionTimeout > 0)
					socket.setSoTimeout(connectionTimeout);
				socket.setTcpNoDelay(tcpNoDelay);
			} catch (AccessControlException ace) {
				log("socket accept security exception", ace);
				continue;
			} catch (IOException e) {
				//                if (debug >= 3)
				//                    log("run: Accept returned IOException", e);
				try {
					// 如果重新打开失败，则退出
					synchronized (threadSync) {
						if (started && !stopped)
							log("accept error: ", e);
						if (!stopped) {
							//                    if (debug >= 3)
							//                        log("run: Closing server socket");
							serverSocket.close();
							//                        if (debug >= 3)
							//                            log("run: Reopening server socket");
							serverSocket = open();
						}
					}
					//                    if (debug >= 3)
					//                        log("run: IOException processing completed");
				} catch (IOException ioe) {
					log("socket reopen, io problem: ", ioe);
					break;
				} catch (KeyStoreException kse) {
					log("socket reopen, keystore problem: ", kse);
					break;
				} catch (NoSuchAlgorithmException nsae) {
					log("socket reopen, keystore algorithm problem: ", nsae);
					break;
				} catch (CertificateException ce) {
					log("socket reopen, certificate problem: ", ce);
					break;
				} catch (UnrecoverableKeyException uke) {
					log("socket reopen, unrecoverable key: ", uke);
					break;
				} catch (KeyManagementException kme) {
					log("socket reopen, key management problem: ", kme);
					break;
				}
				continue;
			}

			// 将此套接字移交给适当的处理器
			HttpProcessor processor = createProcessor();
			if (processor == null) {
				try {
					log(sm.getString("httpConnector.noProcessor"));
					socket.close();
				} catch (IOException e) {
					;
				}
				continue;
			}
			//            if (debug >= 3)
			//                log("run: Assigning socket to processor " + processor);
			processor.assign(socket);

			// 处理器完成后会被自动回收
		}

		// Notify the threadStop() method that we have shut ourselves down
		//        if (debug >= 3)
		//            log("run: Notifying threadStop() that we have shut down");
		synchronized (threadSync) {
			threadSync.notifyAll();
		}
	}

	/**
	 * 启动后台处理线程。
	 */
	private void threadStart() {
		log(sm.getString("httpConnector.starting"));
		thread = new Thread(this, threadName);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Stop the background processing thread.
	 */
	private void threadStop() {

		log(sm.getString("httpConnector.stopping"));

		stopped = true;
		try {
			threadSync.wait(5000);
		} catch (InterruptedException e) {
			;
		}
		thread = null;

	}

	// ------------------------------------------------------ Lifecycle Methods

	/**
	 * Add a lifecycle event listener to this component.
	 *
	 * @param listener The listener to add
	 */
	public void addLifecycleListener(LifecycleListener listener) {

		lifecycle.addLifecycleListener(listener);

	}

	/**
	 * Get the lifecycle listeners associated with this lifecycle. If this
	 * Lifecycle has no listeners registered, a zero-length array is returned.
	 */
	public LifecycleListener[] findLifecycleListeners() {

		return lifecycle.findLifecycleListeners();

	}

	/**
	 * Remove a lifecycle event listener from this component.
	 *
	 * @param listener The listener to add
	 */
	public void removeLifecycleListener(LifecycleListener listener) {

		lifecycle.removeLifecycleListener(listener);

	}

	/**
	 * 初始化此连接器（在此创建 ServerSocket！）
	 */
	@Override
	public void initialize() throws LifecycleException {
		if (initialized)
			throw new LifecycleException(sm.getString("httpConnector.alreadyInitialized"));

		this.initialized = true;
		Exception eRethrow = null;

		// 在指定端口上创建服务器套接字
		try {
			serverSocket = open();
		} catch (IOException ioe) {
			log("httpConnector, io problem: ", ioe);
			eRethrow = ioe;
		} catch (KeyStoreException kse) {
			log("httpConnector, keystore problem: ", kse);
			eRethrow = kse;
		} catch (NoSuchAlgorithmException nsae) {
			log("httpConnector, keystore algorithm problem: ", nsae);
			eRethrow = nsae;
		} catch (CertificateException ce) {
			log("httpConnector, certificate problem: ", ce);
			eRethrow = ce;
		} catch (UnrecoverableKeyException uke) {
			log("httpConnector, unrecoverable key: ", uke);
			eRethrow = uke;
		} catch (KeyManagementException kme) {
			log("httpConnector, key management problem: ", kme);
			eRethrow = kme;
		}

		if (eRethrow != null)
			throw new LifecycleException(threadName + ".open", eRethrow);
	}

	/**
	 * 通过此连接器开始处理请求。
	 *
	 * @exception LifecycleException 如果发生致命的启动错误
	 */
	public void start() throws LifecycleException {
		// 验证和更新当前的状态
		if (started)
			throw new LifecycleException(sm.getString("httpConnector.alreadyStarted"));
		threadName = "HttpConnector[" + port + "]";
		lifecycle.fireLifecycleEvent(START_EVENT, null);
		started = true;

		// 启动后台线程
		threadStart();

		// 创建指定的最小数量的处理器
		while (curProcessors < minProcessors) {
			if ((maxProcessors > 0) && (curProcessors >= maxProcessors))
				break;
			HttpProcessor processor = newProcessor();
			recycle(processor);
		}
	}

	/**
	 * Terminate processing requests via this Connector.
	 *
	 * @exception LifecycleException if a fatal shutdown error occurs
	 */
	public void stop() throws LifecycleException {

		// Validate and update our current state
		if (!started)
			throw new LifecycleException(sm.getString("httpConnector.notStarted"));
		lifecycle.fireLifecycleEvent(STOP_EVENT, null);
		started = false;

		// Gracefully shut down all processors we have created
		for (int i = created.size() - 1; i >= 0; i--) {
			HttpProcessor processor = (HttpProcessor) created.elementAt(i);
			if (processor instanceof Lifecycle) {
				try {
					((Lifecycle) processor).stop();
				} catch (LifecycleException e) {
					log("HttpConnector.stop", e);
				}
			}
		}

		synchronized (threadSync) {
			// Close the server socket we were using
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					;
				}
			}
			// Stop our background thread
			threadStop();
		}
		serverSocket = null;

	}

}
