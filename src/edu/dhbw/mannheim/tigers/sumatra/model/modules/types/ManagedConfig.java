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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
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
	private XMLConfiguration				xmlConfig;
	
	private final List<IConfigObserver>	observers	= new LinkedList<IConfigObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor ----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param client
	 * @param config
	 */
	public ManagedConfig(IConfigClient client, XMLConfiguration config)
	{
		this.client = client;
		xmlConfig = config;
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
			client.onLoad(xmlConfig);
			
			for (final IConfigObserver observer : observers)
			{
				observer.onLoad(xmlConfig);
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
		client.onReload(xmlConfig);
		
		for (final IConfigObserver observer : observers)
		{
			observer.onReload(xmlConfig);
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
	public XMLConfiguration getXmlConfig()
	{
		return xmlConfig;
	}
	
	
	/**
	 * @param xmlConfig
	 */
	public void setXmlConfig(XMLConfiguration xmlConfig)
	{
		this.xmlConfig = xmlConfig;
	}
}
