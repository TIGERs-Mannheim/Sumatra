/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import org.apache.commons.configuration.HierarchicalConfiguration;


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
	void onLoad(HierarchicalConfiguration newConfig);
	
	
	/**
	 * @param freshConfig
	 */
	void onReload(HierarchicalConfiguration freshConfig);
}
