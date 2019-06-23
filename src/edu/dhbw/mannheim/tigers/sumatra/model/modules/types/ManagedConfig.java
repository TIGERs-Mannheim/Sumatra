/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;


/**
 * Simple Data-Holder for a configuration managed by an {@link AConfigManager}-instance.
 */
public class ManagedConfig
{
	// --------------------------------------------------------------------------
	// --- constants and variables ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log			= Logger.getLogger(ManagedConfig.class.getName());
	
	private IConfigClient					client;
	private HierarchicalConfiguration	config;
	
	private final Set<IConfigObserver>	observers	= new HashSet<IConfigObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor ----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param client
	 * @param config
	 */
	public ManagedConfig(IConfigClient client, HierarchicalConfiguration config)
	{
		this.client = client;
		this.config = config;
	}
	
	
	// --------------------------------------------------------------------------
	// --- observers ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
 */
	public void notifyOnLoad()
	{
		try
		{
			client.onLoad(config);
			
			for (final IConfigObserver observer : observers)
			{
				observer.onLoad(config);
			}
		} catch (final RuntimeException rex)
		{
			log.error("Error while loading new config!", rex);
		}
	}
	
	
	/**
	 */
	public void notifyOnReload()
	{
		client.onReload(config);
		
		for (final IConfigObserver observer : observers)
		{
			observer.onReload(config);
		}
	}
	
	
	/**
	 * @param newObserver
	 */
	public void registerObserver(IConfigObserver newObserver)
	{
		observers.add(newObserver);
	}
	
	
	/**
	 * @param oldObserver
	 */
	public void unregisterObserver(IConfigObserver oldObserver)
	{
		observers.remove(oldObserver);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public IConfigClient getClient()
	{
		return client;
	}
	
	
	/**
	 * @param client
	 */
	public void setClient(IConfigClient client)
	{
		this.client = client;
	}
	
	
	/**
	 * @return
	 */
	public HierarchicalConfiguration getConfig()
	{
		return config;
	}
	
	
	/**
	 * @param config
	 */
	public void setConfig(HierarchicalConfiguration config)
	{
		this.config = config;
	}
}
