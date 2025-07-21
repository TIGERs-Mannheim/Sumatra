/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 14, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.configurable;


/**
 *
 */
public class ConfigClass4
{
	@Configurable(comment = "Document this field")
	static boolean testBoolFalse = false;

	@Configurable(comment = "Document this field")
	static boolean testBoolTrue = true;

	@Configurable(comment = "Document this field")
	static ETest testEnum = ETest.ONE;

	@Configurable(comment = "Document this field", defValue = "TWO")
	static ETest testEnumDefValue = ETest.ONE;

	@Configurable(comment = "Document this field")
	static double testDouble = 1;

	@Configurable(comment = "Document this field", defValue = "2")
	static double testDoubleWithDefault = 2;

	@Configurable(comment = "Document this field", defValue = "6")
	static double testDefaultDifferent = 5;

	@Configurable(comment = "Document this field")
	static double testStoredDifferent = 5;

	static
	{
		ConfigRegistration.registerClass("read", ConfigClass4.class);
	}

	enum ETest
	{
		ONE,
		TWO
	}
}
