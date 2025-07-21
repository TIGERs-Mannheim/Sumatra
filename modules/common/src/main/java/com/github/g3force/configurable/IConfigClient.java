/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package com.github.g3force.configurable;

import org.apache.commons.configuration.HierarchicalConfiguration;


/**
 * A config client.
 */
public interface IConfigClient
{
	/**
	 * @return The common name of this {@link IConfigClient}
	 */
	String getName();


	/**
	 * @return The path to the config-file
	 */
	String getPath();


	/**
	 * Get configuration from file
	 */
	HierarchicalConfiguration getFileConfig();


	/**
	 * Get configuration from file
	 */
	HierarchicalConfiguration loadConfig();


	/**
	 * Get configuration from file
	 */
	HierarchicalConfiguration getConfig();


	/**
	 * Read all fields from all registered classes
	 */
	void readClasses();


	/**
	 * Save to file
	 */
	boolean saveCurrentConfig();


	/**
	 * @param observer the observer to add
	 */
	void addObserver(final IConfigObserver observer);


	/**
	 * @param observer the observer to remove
	 */
	void removeObserver(final IConfigObserver observer);
}
