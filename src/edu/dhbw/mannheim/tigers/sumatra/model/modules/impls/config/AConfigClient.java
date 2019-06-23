/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;


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
	private final String		name;
	private final String		configPath;
	private final String		configKey;
	private final String		defaultValue;
	private final boolean	editable;
	
	
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
	public AConfigClient(String name, String configPath, String configKey, String defaultValue, boolean editable)
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
	public void onReload(Configuration freshConfig)
	{
		onLoad(freshConfig);
	}
	
	
	@Override
	public XMLConfiguration prepareConfigForSaving(XMLConfiguration loadedConfig)
	{
		return loadedConfig;
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
