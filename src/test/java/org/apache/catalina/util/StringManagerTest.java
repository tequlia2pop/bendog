package org.apache.catalina.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

public class StringManagerTest {

	private static final String PACKAGE_NAME = "com.gmail.tequlia2pop.bendog.connector.http";

	private StringManager manager;

	@Before
	public void setUp() {
		manager = StringManager.getManager(PACKAGE_NAME);
	}

	@Test
	public void testGetManager() {
		assertSame(manager, StringManager.getManager(PACKAGE_NAME));
	}

	@Test(expected = java.util.MissingResourceException.class)
	public void testGetManagerNotDefined() {
		StringManager.getManager("someText");
	}

	@Test
	public void testGetString() {
		assertEquals("HTTP connector has already been initialized",
				manager.getString("httpConnector.alreadyInitialized"));
	}
}
