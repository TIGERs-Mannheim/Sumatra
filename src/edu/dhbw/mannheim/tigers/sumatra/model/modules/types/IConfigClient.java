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
import org.apache.commons.configuration.XMLConfiguration;


/**
 * With implementing and registering this interface at an {@link AConfigManager}-instance, the config described by this
 * interface is:
 * <ul>
 * <li>loaded at startup of moduli (when status changes to
 * {@link edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState#RESOLVED})</li>
 * <li>editable via the config editor view</li>
 * </ul>
 * By implementing the methods of {@link IConfigObserver} correctly, this config will be exchangeable at runtime without
 * further work.
 * 
 * @author Gero
 */
public interface IConfigClient extends IConfigObserver
{
	/**
	 * @return The common name of this {@link IConfigClient}
	 */
	String getName();
	
	
	/**
	 * @return The path to the config-files
	 */
	String getConfigPath();
	
	
	/**
	 * @return The unique key which is used to store the user-settings of Sumatra
	 */
	String getConfigKey();
	
	
	/**
	 * @return the defaultValue
	 */
	String getDefaultValue();
	
	
	/**
	 * @return Whether this config should be editable in Sumatra
	 */
	boolean isEditable();
	
	
	/**
	 * This method is called everytime this config is going to be saved and provides the client a possibility to save its
	 * own values (used for more static configs, like BotManager!)<br/>
	 * To stay consistent, the config should not be {@link #isEditable()} in any other way!<br/>
	 * If you return another instance of {@link XMLConfiguration} then the given one, be sure not to be
	 * {@link #isEditable()}!!!
	 * 
	 * @param loadedConfig
	 * @return The modified XMLConfiguration
	 */
	HierarchicalConfiguration prepareConfigForSaving(HierarchicalConfiguration loadedConfig);
	
	
	/**
	 * Get default configuration that should be merged with existing config.
	 * 
	 * @return
	 */
	HierarchicalConfiguration getDefaultConfig();
	
	
	/**
	 * 
	 */
	void clearObservers();
	
	
	/**
	 * Is the config file required? Must it exist?
	 * 
	 * @return
	 */
	boolean isRequired();
}
