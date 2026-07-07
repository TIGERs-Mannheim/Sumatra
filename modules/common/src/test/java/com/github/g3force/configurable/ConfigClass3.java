package com.github.g3force.configurable;


/**
 *
 */
public class ConfigClass3
{
	@Configurable(comment = "Document this field", spezis = { "", "CONF1", "CONF2" }, defValueSpezis = { "1", "2",
			"3" }, defValue = "1", unit = EConfigUnit.NO_UNIT)
	double testSpezi;

	static
	{
		ConfigRegistration.registerClass("default", ConfigClass3.class);
	}


	public ConfigClass3()
	{
		ConfigRegistration.applySpezis(this, "default", "CONF1");
	}
}
