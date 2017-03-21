package org.apache.catalina.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 适配器类，它将 Java2 集合类对象<code>Iterator</code>包装为一个<code>Enumeration</code>，
 * 以便返回 Enumeration 的现有 API 可以轻松地在新集合之上运行。
 * 这里提供了构造函数以便创建这样的包装器。
 * 
 * @author tequlia2pop
 */
public final class Enumerator<T> implements Enumeration<T> {

	// ----------------------------------------------------- Instance Variables

	/**
	 * The <code>Iterator</code> over which the <code>Enumeration</code>
	 * represented by this class actually operates.
	 */
	private Iterator<T> iterator = null;

	// ----------------------------------------------------------- Constructors

	/**
	 * Return an Enumeration over the values returned by the
	 * specified Iterator.
	 *
	 * @param iterator Iterator to be wrapped
	 */
	public Enumerator(Iterator<T> iterator) {
		super();
		this.iterator = iterator;
	}

	/**
	 * Return an Enumeration over the values of the specified Collection.
	 *
	 * @param collection Collection whose values should be enumerated
	 */
	public Enumerator(Collection<T> collection) {
		this(collection.iterator());
	}

	/**
	 * Return an Enumeration over the values of the specified Map.
	 *
	 * @param map Map whose values should be enumerated
	 */
	public Enumerator(Map<?, T> map) {
		this(map.values().iterator());
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Tests if this enumeration contains more elements.
	 *
	 * @return <code>true</code> if and only if this enumeration object
	 *  contains at least one more element to provide, <code>false</code>
	 *  otherwise
	 */
	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	/**
	 * Returns the next element of this enumeration if this enumeration
	 * has at least one more element to provide.
	 *
	 * @return the next element of this enumeration
	 *
	 * @exception NoSuchElementException if no more elements exist
	 */
	@Override
	public T nextElement() throws NoSuchElementException {
		return iterator.next();
	}
}
