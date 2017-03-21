package org.apache.catalina.util;

import java.util.HashMap;
import java.util.Map;

/**
 * <strong>HashMap</strong>的扩展实现，它包含了一个<code>locked</code>属性。
 * 此类可以将 Catalina 内部的 parameter map 对象安全地公开给用户类，而无需克隆它们以避免修改。
 * 首次创建时，<code>ParmaeterMap</code>实例未锁定。
 * 
 * 注意，开发人员不可以修改参数值，所以需要使用一个特殊的 HashMap 类。
 * ParameterMap 继承自 java.util.HashMap，其中有一个名为 locked 的布尔变量。
 * 只有当 locked 变量的值为 false 时，才可以对  ParameterMap 中的名/值对进行添加、更新或者删除操作。
 * 否则，会抛出 IllegalStateException 异常。
 * 
 * @author tequlia2pop
 *
 * @param <K>
 * @param <V>
 */
public final class ParameterMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The string manager for this package.
	 */
	private static final StringManager sm = StringManager.getManager("org.apache.catalina.util");

	// ----------------------------------------------------------- Constructors

	/**
	 * Construct a new, empty map with the default initial capacity and
	 * load factor.
	 */
	public ParameterMap() {
		super();
	}

	/**
	 * Construct a new, empty map with the specified initial capacity and
	 * default load factor.
	 *
	 * @param initialCapacity The initial capacity of this map
	 */
	public ParameterMap(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Construct a new, empty map with the specified initial capacity and
	 * load factor.
	 *
	 * @param initialCapacity The initial capacity of this map
	 * @param loadFactor The load factor of this map
	 */
	public ParameterMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Construct a new map with the same mappings as the given map.
	 *
	 * @param map Map whose contents are dupliated in the new map
	 */
	public ParameterMap(Map<K, V> map) {
		super(map);
	}

	// ------------------------------------------------------------- Properties

	/**
	 * 此 parameter map 当前的锁定状态。
	 */
	private boolean locked = false;

	/**
	 * Return the locked state of this parameter map.
	 */
	public boolean isLocked() {
		return (this.locked);
	}

	/**
	 * Set the locked state of this parameter map.
	 *
	 * @param locked The new locked state
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Remove all mappings from this map.
	 *
	 * @exception IllegalStateException if this map is currently locked
	 */
	@Override
	public void clear() {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		super.clear();
	}

	/**
	 * Associate the specified value with the specified key in this map.  If
	 * the map previously contained a mapping for this key, the old value is
	 * replaced.
	 *
	 * @param key Key with which the specified value is to be associated
	 * @param value Value to be associated with the specified key
	 *
	 * @return The previous value associated with the specified key, or
	 *  <code>null</code> if there was no mapping for key
	 *
	 * @exception IllegalStateException if this map is currently locked
	 */
	@Override
	public V put(K key, V value) {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		return super.put(key, value);

	}

	/**
	 * Copy all of the mappings from the specified map to this one.  These
	 * mappings replace any mappings that this map had for any of the keys
	 * currently in the specified Map.
	 *
	 * @param map Mappings to be stored into this map
	 *
	 * @exception IllegalStateException if this map is currently locked
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		super.putAll(map);

	}

	/**
	 * Remove the mapping for this key from the map if present.
	 *
	 * @param key Key whose mapping is to be removed from the map
	 *
	 * @return The previous value associated with the specified key, or
	 *  <code>null</code> if there was no mapping for that key
	 *
	 * @exception IllegalStateException if this map is currently locked
	 */
	@Override
	public V remove(Object key) {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		return super.remove(key);
	}
}
