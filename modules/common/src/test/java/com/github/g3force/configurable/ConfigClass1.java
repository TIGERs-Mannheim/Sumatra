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
public class ConfigClass1
{

	@Configurable(comment = "Document this field", defValue = "false")
	static boolean testBool;

	@Configurable(comment = "Document this field", defValue = "1")
	static double testDouble;

	@Configurable(comment = "Document this field", defValue = "ONE")
	static ETest testEnum;

	@Configurable(comment = "Document this field")
	static double testNoDefault = 5;

	@Configurable(comment = "Document this field", defValue = "6")
	static double testDefaultDifferent = 5;

	@Configurable(comment = "Document this field")
	static double testStoredDifferent = 5;

	static
	{
		ConfigRegistration.registerClass("default", ConfigClass1.class);
	}

	enum ETest
	{
		ONE,
		TWO
	}
}
