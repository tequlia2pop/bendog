package org.apache.catalina.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.UnrecoverableKeyException;
import java.security.KeyManagementException;

/**
 * 该接口描述了用于创建服务器套接字（连接器所需要的）的工厂类的公共特性。
 * 通过连接器的<code>setFactory()</code>方法可以指定要使用套接字工厂的哪一个具体实现。
 * 
 * @author tequlia2pop
 */
public interface ServerSocketFactory {

	// --------------------------------------------------------- Public Methods

	/**
	 * 返回使用主机上所有网络接口的服务器套接字，并绑定到指定的端口。
	 * 套接字配置了这个工厂提供的套接字选项（如接受超时）。
	 *
	 * @param port 侦听的端口
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
	public ServerSocket createSocket(int port)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyManagementException;

	/**
	 * 返回使用主机上所有网络接口的服务器套接字，绑定到指定的端口，并将连接队列的大小设置为指定的值。
	 * 套接字配置了这个工厂提供的套接字选项（如接受超时）。
	 *
	 * @param port 侦听的端口
	 * @param backlog 连接队列的大小
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
	public ServerSocket createSocket(int port, int backlog)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyManagementException;

	/**
	 * 返回仅使用本地主机上指定的网络接口的服务器套接字，绑定到指定的端口，并将连接队列的大小设置为指定的值。
	 * 套接字配置了这个工厂提供的套接字选项（如接受超时）。
	 *
	 * @param port 侦听的端口
	 * @param backlog 连接队列的大小
	 * @param ifAddress 要使用的网络接口地址
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
	public ServerSocket createSocket(int port, int backlog, InetAddress ifAddress)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyManagementException;

}
