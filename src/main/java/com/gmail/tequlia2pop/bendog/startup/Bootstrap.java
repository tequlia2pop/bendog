package com.gmail.tequlia2pop.bendog.startup;

import com.gmail.tequlia2pop.bendog.connector.http.HttpConnector;

/**
 * 负责启动应用程序。
 * 
 * @author tequlia2pop
 */
public final class Bootstrap {
	
	public static void main(String[] args) {
		HttpConnector connector = new HttpConnector();
		connector.start();
	}
}