package com.gmail.tequlia2pop.bendog;

import java.io.File;

/**
 * 常量类。
 * 
 * @author tequlia2pop
 */
public class Constants {
	
	/** 
	 *  WEB_ROOT 是我们的 HTML 和其他文件所在的目录。
	 *  对于此程序包，WEB_ROOT 是工作目录下的 "\src\main\webapp" 目录。
	 *  工作目录是调用 java 命令的文件系统位置。
	 */
	/*public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator
			+ "webroot";*/
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "src"
			+ File.separator + "main" + File.separator + "webapp";
}