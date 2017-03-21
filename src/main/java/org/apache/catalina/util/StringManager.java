package org.apache.catalina.util;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 一个国际化/本地化辅助类，减少了处理 ResourceBundle 的麻烦，并处理了消息格式化的常见情况（否则需要创建对象数组等）。
 * 
 * <p>基于包可以操作 StringManager。对于一个包，
 * 可以通过 StringManager 的 getManager 方法调用来创建和访问 StringManager 实例。
 * 
 * <p>StringManager 将查找由给定的包名加上"LocalStrings"的后缀命名的 ResourceBundle。
 * 实际上，这意味着本地化信息将包含在位于类路径的包目录中的 LocalStrings.properties 文件中。
 * 
 * <p>有关更多信息，请参阅 java.util.ResourceBundle 的文档。
 * 
 * @author tequlia2pop
 */
public class StringManager {

	// --------------------------------------------------------------
	// STATIC SUPPORT METHODS
	// --------------------------------------------------------------

	private static Hashtable<String, StringManager> managers = new Hashtable<>();

	/**
	 * Get the StringManager for a particular package. If a manager for
	 * a package already exists, it will be reused, else a new
	 * StringManager will be created and returned.
	 *
	 * @param packageName
	 */
	public synchronized static StringManager getManager(String packageName) {
		StringManager mgr = managers.get(packageName);
		if (mgr == null) {
			mgr = new StringManager(packageName);
			managers.put(packageName, mgr);
		}
		return mgr;
	}

	/**
	 * The ResourceBundle for this StringManager.
	 */
	private ResourceBundle bundle;

	/**
	 * Creates a new StringManager for a given package. This is a
	 * private method and all access to it is arbitrated by the
	 * static getManager method call so that only one StringManager
	 * per package will be created.
	 *
	 * @param packageName Name of package to create StringManager for.
	 */
	private StringManager(String packageName) {
		String bundleName = packageName + ".LocalStrings";
		bundle = ResourceBundle.getBundle(bundleName);
	}

	/**
	 * Get a string from the underlying resource bundle.
	 *
	 * @param key
	 */
	public String getString(String key) {
		if (key == null) {
			String msg = "key is null";
			throw new NullPointerException(msg);
		}

		String str = null;
		try {
			str = bundle.getString(key);
		} catch (MissingResourceException mre) {
			str = "Cannot find message associated with key '" + key + "'";
		}
		return str;
	}

	/**
	 * Get a string from the underlying resource bundle and format
	 * it with the given set of arguments.
	 *
	 * @param key
	 * @param args
	 */
	public String getString(String key, Object[] args) {
		String iString = null;
		String value = getString(key);

		// this check for the runtime exception is some pre 1.1.6
		// VM's don't do an automatic toString() on the passed in
		// objects and barf out
		try {
			// ensure the arguments are not null so pre 1.2 VM's don't barf
			Object nonNullArgs[] = args;
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null) {
					if (nonNullArgs == args)
						nonNullArgs = (Object[]) args.clone();
					nonNullArgs[i] = "null";
				}
			}

			iString = MessageFormat.format(value, nonNullArgs);
		} catch (IllegalArgumentException iae) {
			StringBuffer buf = new StringBuffer();
			buf.append(value);
			for (int i = 0; i < args.length; i++) {
				buf.append(" arg[" + i + "]=" + args[i]);
			}
			iString = buf.toString();
		}
		return iString;
	}

	/**
	 * Get a string from the underlying resource bundle and format it
	 * with the given object argument. This argument can of course be
	 * a String object.
	 *
	 * @param key
	 * @param arg
	 */
	public String getString(String key, Object arg) {
		Object[] args = new Object[] { arg };
		return getString(key, args);
	}

	/**
	 * Get a string from the underlying resource bundle and format it
	 * with the given object arguments. These arguments can of course
	 * be String objects.
	 *
	 * @param key
	 * @param arg1
	 * @param arg2
	 */
	public String getString(String key, Object arg1, Object arg2) {
		Object[] args = new Object[] { arg1, arg2 };
		return getString(key, args);
	}

	/**
	 * Get a string from the underlying resource bundle and format it
	 * with the given object arguments. These arguments can of course
	 * be String objects.
	 *
	 * @param key
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public String getString(String key, Object arg1, Object arg2, Object arg3) {
		Object[] args = new Object[] { arg1, arg2, arg3 };
		return getString(key, args);
	}

	/**
	 * Get a string from the underlying resource bundle and format it
	 * with the given object arguments. These arguments can of course
	 * be String objects.
	 *
	 * @param key
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 */
	public String getString(String key, Object arg1, Object arg2, Object arg3, Object arg4) {
		Object[] args = new Object[] { arg1, arg2, arg3, arg4 };
		return getString(key, args);
	}
}
