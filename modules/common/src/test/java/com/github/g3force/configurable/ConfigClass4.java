package com.github.g3force.configurable;


/**
 *
 */
public class ConfigClass4
{
	@Configurable(comment = "Document this field", defValue = "false", unit = EConfigUnit.BOOLEAN)
	static boolean testBoolFalse = false;

	@Configurable(comment = "Document this field", defValue = "true", unit = EConfigUnit.BOOLEAN)
	static boolean testBoolTrue = true;

	@Configurable(comment = "Document this field", defValue = "ONE", unit = EConfigUnit.NO_UNIT)
	static ETest testEnum = ETest.ONE;

	@Configurable(comment = "Document this field", defValue = "2", unit = EConfigUnit.NO_UNIT)
	static double testDoubleWithDefault = 2;

	@Configurable(comment = "Document this field", defValue = "6", unit = EConfigUnit.NO_UNIT)
	static double testDefaultDifferent = 5;

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
