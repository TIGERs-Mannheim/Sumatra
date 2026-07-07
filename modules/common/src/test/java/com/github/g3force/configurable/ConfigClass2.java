package com.github.g3force.configurable;


/**
 *
 */
public class ConfigClass2
{
	@Configurable(comment = "Document this field", spezis = { "", "CONF1", "CONF2" }, defValueSpezis = { "1", "2",
			"3" }, defValue = "1", unit = EConfigUnit.NO_UNIT)
	static double testSpezi;


	static
	{
		ConfigRegistration.registerClass("default", ConfigClass2.class);
	}

}
