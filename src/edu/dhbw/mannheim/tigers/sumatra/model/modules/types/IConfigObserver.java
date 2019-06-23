/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import org.apache.commons.configuration.Configuration;


/**
 * Used to observe a {@link ManagedConfig} in the {@link AConfigManager} (mainly by {@link IConfigClient} itself).
 * 
 * @author Gero
 */
public interface IConfigObserver
{
	/**
	 * @param newConfig
	 */
	void onLoad(Configuration newConfig);
	
	
	/**
	 * @param freshConfig
	 */
	void onReload(Configuration freshConfig);
}
