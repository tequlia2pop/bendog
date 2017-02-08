package com.gmail.tequlia2pop.bendog;

import java.io.IOException;

/**
 * 静态资源处理器，委托响应对象来处理对静态资源。
 * 
 * @author tequlia2pop
 */
public class StaticResourceProcessor {

	public void process(Request request, Response response) {
		try {
			response.sendStaticResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}