package com.gmail.tequlia2pop.bendog;

import java.io.IOException;

import com.gmail.tequlia2pop.bendog.connector.http.HttpRequest;
import com.gmail.tequlia2pop.bendog.connector.http.HttpResponse;

/**
 * 静态资源处理器，委托响应对象来处理静态资源。
 * 
 * @author tequlia2pop
 */
public class StaticResourceProcessor {

	public void process(HttpRequest request, HttpResponse response) {
		try {
			response.sendStaticResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
