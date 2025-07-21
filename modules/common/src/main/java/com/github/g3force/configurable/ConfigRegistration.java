/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 25, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.configurable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Central registration for all configs
 */
public class ConfigRegistration
{
	private static final Logger log = LogManager.getLogger(ConfigRegistration.class.getName());
	private final Map<String, ConfigClient> configs = new LinkedHashMap<>();

	private static String defPath = "config/";

	private static List<IConfigClientsObserver> observers = new CopyOnWriteArrayList<>();

	private static final ConfigRegistration INSTANCE = new ConfigRegistration();


	private ConfigRegistration()
	{
		try
		{
			Files.createDirectories(Paths.get(defPath));
		} catch (IOException e)
		{
			log.error("Could not create default config dir: {}", defPath, e);
		}
	}


	/**
	 * Update the default config path (default: config/)
	 * <br>
	 * Note: This must be called before config clients are added
	 *
	 * @param path a new default path
	 */
	public static void setDefPath(String path)
	{
		defPath = path;
	}


	public static synchronized void addObserver(final IConfigClientsObserver observer)
	{
		observers.add(observer);
	}


	public static synchronized void removeObserver(final IConfigClientsObserver observer)
	{
		observers.remove(observer);
	}


	private static synchronized void registerConfigClient(final ConfigClient cc)
	{
		INSTANCE.configs.put(cc.getName(), cc);
		for (IConfigClientsObserver o : observers)
		{
			o.onNewConfigClient(cc.getName());
		}
	}


	/**
	 * @param key the category
	 * @return the config client of given category
	 */
	private ConfigClient getConfigClient(final String key)
	{
		ConfigClient cc = configs.get(key);
		if (cc == null)
		{
			cc = new ConfigClient(defPath, key);
			registerConfigClient(cc);
			cc.getCap().loadConfiguration(cc.getFileConfig());
			cc.applyConfig();
		}
		return cc;
	}


	public static synchronized void registerClass(final String key, final Class<?>... classes)
	{
		ConfigClient cc = INSTANCE.getConfigClient(key);
		for (Class<?> c : classes)
		{
			cc.putClass(c);
		}
	}


	public static synchronized boolean save(final String key)
	{
		ConfigClient cc = INSTANCE.getConfigClient(key);
		return cc.saveCurrentConfig();
	}


	/**
	 * Register a callback to a config category to get informed by changes
	 *
	 * @param cat      the category
	 * @param callback the callback
	 */
	public static synchronized void registerConfigurableCallback(final String cat, final IConfigObserver callback)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.addObserver(callback);
	}


	/**
	 * Unregister previously registered callbacks
	 *
	 * @param cat      the category
	 * @param callback the callback
	 */
	public static synchronized void unregisterConfigurableCallback(final String cat, final IConfigObserver callback)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.removeObserver(callback);
	}


	/**
	 * Apply the spezi to the object in category
	 *
	 * @param obj   the object
	 * @param cat   the category
	 * @param spezi the specialization
	 */
	public static synchronized void applySpezis(final Object obj, final String cat, final String spezi)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.getCap().applySpezi(obj, spezi);
	}


	/**
	 * Apply the spezi to all classes
	 *
	 * @param cat
	 * @param spezi
	 */
	public static synchronized void applySpezi(final String cat, final String spezi)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.getCap().applySpezi(spezi);
	}


	/**
	 * Apply the spezi to all classes and all categories
	 *
	 * @param spezi
	 */
	public static synchronized void applyGlobalSpezi(final String spezi)
	{
		for (ConfigClient cc : INSTANCE.configs.values())
		{
			cc.getCap().applySpezi(spezi);
		}
	}


	/**
	 * Apply all and notify observers
	 *
	 * @param cat
	 */
	public static synchronized void applyConfig(final String cat)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.applyConfig();
	}


	/**
	 * Override a fields value
	 *
	 * @param obj       the object that the change should applied to
	 * @param cat       the category of this config
	 * @param fieldName the name of the field to override
	 * @param value     the value to apply
	 */
	public static synchronized void overrideConfig(final Object obj, String cat, String fieldName, String value)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.getCap().overrideField(obj, fieldName, value);
	}


	/**
	 * Override a fields value
	 *
	 * @param clazz     the class that the change should applied to
	 * @param cat       the category of this config
	 * @param fieldName the name of the field to override
	 * @param value     the value to apply
	 */
	public static synchronized void overrideConfig(final Class<?> clazz, String cat, String fieldName, String value)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.getCap().overrideField(clazz, fieldName, value);
	}


	public static synchronized HierarchicalConfiguration getConfig(final String cat)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		return cc.getConfig();
	}


	public static synchronized HierarchicalConfiguration loadConfig(final String cat)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		return cc.loadConfig();
	}


	/**
	 * Read all fields from all classes and store values internally.
	 *
	 * @param cat
	 */
	public static synchronized void readClasses(final String cat)
	{
		ConfigClient cc = INSTANCE.getConfigClient(cat);
		cc.readClasses();
	}


	public static synchronized List<String> getConfigClients()
	{
		return new ArrayList<>(INSTANCE.configs.keySet());
	}
}
