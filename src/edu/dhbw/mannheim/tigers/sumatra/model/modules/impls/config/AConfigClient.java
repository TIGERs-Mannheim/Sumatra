/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;


/**
 * Base implementation for {@link IConfigClient}
 * 
 * @author Gero
 */
public abstract class AConfigClient implements IConfigClient
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String						name;
	private final String						configPath;
	private final String						configKey;
	private final String						defaultValue;
	private final boolean					editable;
	private final List<IConfigObserver>	observers	= new CopyOnWriteArrayList<IConfigObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param name
	 * @param configPath
	 * @param configKey
	 * @param defaultValue
	 * @param editable
	 */
	public AConfigClient(final String name, final String configPath, final String configKey, final String defaultValue,
			final boolean editable)
	{
		super();
		this.name = name;
		this.configPath = configPath;
		this.configKey = configKey;
		this.defaultValue = defaultValue;
		this.editable = editable;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onReload(final HierarchicalConfiguration freshConfig)
	{
		onLoad(freshConfig);
		
		for (IConfigObserver observer : observers)
		{
			observer.onReload(freshConfig);
		}
	}
	
	
	@Override
	public HierarchicalConfiguration prepareConfigForSaving(final HierarchicalConfiguration loadedConfig)
	{
		return loadedConfig;
	}
	
	
	@Override
	public HierarchicalConfiguration getDefaultConfig()
	{
		return null;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IConfigObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IConfigObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 */
	@Override
	public void clearObservers()
	{
		observers.clear();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public final String getName()
	{
		return name;
	}
	
	
	@Override
	public final String getConfigPath()
	{
		return configPath;
	}
	
	
	@Override
	public final String getConfigKey()
	{
		return configKey;
	}
	
	
	@Override
	public final String getDefaultValue()
	{
		return defaultValue;
	}
	
	
	@Override
	public final boolean isEditable()
	{
		return editable;
	}
}
