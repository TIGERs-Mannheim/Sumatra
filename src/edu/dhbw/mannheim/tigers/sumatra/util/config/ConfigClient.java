/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 25, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.config;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.AConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;


/**
 * Generic config client for Configurables
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ConfigClient extends AConfigClient
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private final ConfigAnnotationProcessor	cap	= new ConfigAnnotationProcessor();
	private final String								name;
	private final Set<Class<?>>					classes;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param name
	 */
	public ConfigClient(final String name)
	{
		this(name, new LinkedHashSet<Class<?>>());
	}
	
	
	/**
	 * Construct with initial classes
	 * 
	 * @param name
	 * @param classes
	 */
	public ConfigClient(final String name, final Set<Class<?>> classes)
	{
		super(name, AAgent.AI_CONFIG_PATH, AIConfig.class.getName() + "." + name, name + ".xml", true);
		this.name = name;
		this.classes = classes;
	}
	
	
	/**
	 * Construct with automatic class search from enum
	 * 
	 * @param name
	 * @param enumClazz
	 */
	public ConfigClient(final String name, final Class<? extends Enum<? extends IInstanceableEnum>> enumClazz)
	{
		this(name, getAllClasses(enumClazz));
	}
	
	
	/**
	 * Construct with automatic class search from enum and add additional classes
	 * 
	 * @param name
	 * @param enumClazz
	 * @param classes
	 */
	public ConfigClient(final String name, final Class<? extends Enum<? extends IInstanceableEnum>> enumClazz,
			final Set<Class<?>> classes)
	{
		this(name, mergeClasses(getAllClasses(enumClazz), classes));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private static Set<Class<?>> mergeClasses(final Set<Class<?>> classes1, final Set<Class<?>> classes2)
	{
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		classes.addAll(classes1);
		classes.addAll(classes2);
		return classes;
	}
	
	
	private static Set<Class<?>> getAllClasses(final Class<? extends Enum<? extends IInstanceableEnum>> enumClazz)
	{
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		Enum<? extends IInstanceableEnum> values[] = enumClazz.getEnumConstants();
		IInstanceableEnum valInstancable[] = (IInstanceableEnum[]) values;
		for (IInstanceableEnum en : valInstancable)
		{
			Class<?> clazz = en.getInstanceableClass().getImpl();
			if (clazz == null)
			{
				continue;
			}
			classes.add(clazz);
			while ((clazz.getSuperclass() != null) && !clazz.getSuperclass().equals(Object.class))
			{
				clazz = clazz.getSuperclass();
				classes.add(clazz);
			}
		}
		return classes;
	}
	
	
	@Override
	public void onLoad(final HierarchicalConfiguration newConfig)
	{
		cap.loadConfiguration(newConfig);
		cap.applyAll();
	}
	
	
	@Override
	public HierarchicalConfiguration getDefaultConfig()
	{
		return cap.getDefaultConfig(classes, name);
	}
	
	
	/**
	 * Apply all config values with given spezi. If spezi=="", apply default values.
	 * 
	 * @param obj The instance where all fields should be set.
	 * @param spezi
	 */
	public void applyConfigToObject(final Object obj, final String spezi)
	{
		cap.apply(obj, spezi);
	}
	
	
	/**
	 * Apply spezi
	 * 
	 * @param spezi
	 */
	public void applySpezi(final String spezi)
	{
		cap.apply(spezi);
	}
	
	
	/**
	 * Add a configurable class
	 * 
	 * @param clazz
	 */
	public void putClass(final Class<?> clazz)
	{
		classes.add(clazz);
	}
	
	
	@Override
	public boolean isRequired()
	{
		return false;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
