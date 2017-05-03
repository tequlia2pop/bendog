package com.gmail.tequlia2pop.bendog.startup;

import org.apache.catalina.connector.http.HttpConnector;

import com.gmail.tequlia2pop.bendog.core.SimpleContainer;

/**
 * 用于启动应用程序。
 * 
 * @author tequlia2pop
 *
 */
public final class Bootstrap {
	public static void main(String[] args) {
		HttpConnector connector = new HttpConnector();
		SimpleContainer container = new SimpleContainer();
		connector.setContainer(container);
		try {
			connector.initialize();
			connector.start();

			// 应用程序将阻塞等待，直到我们按下任意键
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}