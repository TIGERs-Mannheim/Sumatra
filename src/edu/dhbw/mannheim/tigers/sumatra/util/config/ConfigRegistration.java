/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 25, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;


/**
 * Central registration for all configs
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ConfigRegistration
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger								log		= Logger.getLogger(ConfigRegistration.class.getName());
	private final Map<EConfigurableCat, ConfigClient>	configs	= new LinkedHashMap<EConfigurableCat, ConfigClient>();
	private static final ConfigRegistration				INSTANCE	= new ConfigRegistration();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	private ConfigRegistration()
	{
		for (EConfigurableCat config : EConfigurableCat.values())
		{
			final ConfigClient cc;
			if (config.getEnumClazz() == null)
			{
				cc = new ConfigClient(config.name().toLowerCase(Locale.ENGLISH), config.getClasses());
			} else
			{
				cc = new ConfigClient(config.name().toLowerCase(Locale.ENGLISH), config.getEnumClazz(), config.getClasses());
			}
			configs.put(config, cc);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Register a callback to a config category to get informed by changes
	 * 
	 * @param cat
	 * @param callback
	 */
	public static void registerConfigurableCallback(final EConfigurableCat cat, final IConfigObserver callback)
	{
		ConfigClient cc = INSTANCE.configs.get(cat);
		if (cc == null)
		{
			log.error("Categorie " + cat + " not found");
			return;
		}
		cc.addObserver(callback);
	}
	
	
	/**
	 * Unregister previously registered callbacks
	 * 
	 * @param cat
	 * @param callback
	 */
	public static void unregisterConfigurableCallback(final EConfigurableCat cat, final IConfigObserver callback)
	{
		ConfigClient cc = INSTANCE.configs.get(cat);
		if (cc == null)
		{
			log.error("Categorie " + cat + " not found");
			return;
		}
		cc.removeObserver(callback);
	}
	
	
	/**
	 * Apply the spezi to the object in category
	 * 
	 * @param obj
	 * @param cat
	 * @param spezi
	 */
	public static void applySpezis(final Object obj, final EConfigurableCat cat, final String spezi)
	{
		ConfigClient cc = INSTANCE.configs.get(cat);
		if (cc == null)
		{
			log.error("Categorie " + cat + " not found");
			return;
		}
		// first apply default
		cc.applyConfigToObject(obj, "");
		// then the spezi
		cc.applyConfigToObject(obj, spezi);
	}
	
	
	/**
	 * Apply the spezi to all classes
	 * 
	 * @param cat
	 * @param spezi
	 */
	public static void applySpezis(final EConfigurableCat cat, final String spezi)
	{
		ConfigClient cc = INSTANCE.configs.get(cat);
		if (cc == null)
		{
			log.error("Categorie " + cat + " not found");
			return;
		}
		cc.applySpezi(spezi);
	}
	
	
	/**
	 * Apply the spezi to all classes and all categories
	 * 
	 * @param spezi
	 */
	public static void applySpezis(final String spezi)
	{
		for (ConfigClient cc : INSTANCE.configs.values())
		{
			cc.applySpezi(spezi);
		}
	}
	
	
	/**
	 * @return
	 */
	public static List<IConfigClient> getConfigClients()
	{
		return new ArrayList<IConfigClient>(INSTANCE.configs.values());
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
