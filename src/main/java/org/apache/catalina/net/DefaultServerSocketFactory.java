package org.apache.catalina.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import org.apache.catalina.net.ServerSocketFactory;

/**
 * 默认的服务器套接字工厂，它返回未装载的服务器套接字。
 * 
 * @author tequlia2pop
 */
public final class DefaultServerSocketFactory implements ServerSocketFactory {

	// --------------------------------------------------------- Public Methods

	@Override
	public ServerSocket createSocket(int port)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyManagementException {
		return new ServerSocket(port);
	}

	@Override
	public ServerSocket createSocket(int port, int backlog)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyManagementException {
		return new ServerSocket(port, backlog);
	}

	@Override
	public ServerSocket createSocket(int port, int backlog, InetAddress ifAddress)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyManagementException {
		return new ServerSocket(port, backlog, ifAddress);
	}

}
