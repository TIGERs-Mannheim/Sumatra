package com.github.g3force.configurable;


/**
 *
 */
public class ConfigClass1
{

	@Configurable(comment = "Document this field", defValue = "false", unit = EConfigUnit.BOOLEAN)
	static boolean testBool;

	@Configurable(comment = "Document this field", defValue = "1", unit = EConfigUnit.NO_UNIT)
	static double testDouble;

	@Configurable(comment = "Document this field", defValue = "ONE", unit = EConfigUnit.NO_UNIT)
	static ETest testEnum;

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
