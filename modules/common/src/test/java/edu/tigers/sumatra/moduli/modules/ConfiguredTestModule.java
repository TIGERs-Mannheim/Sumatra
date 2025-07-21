/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.moduli.modules;

import edu.tigers.sumatra.moduli.AModule;


public class ConfiguredTestModule extends AModule
{
	private String configProperty;


	@Override
	public void initModule()
	{
		configProperty = getSubnodeConfiguration().getString("testProperty");
	}


	public String getConfigProperty()
	{
		return configProperty;
	}
}
