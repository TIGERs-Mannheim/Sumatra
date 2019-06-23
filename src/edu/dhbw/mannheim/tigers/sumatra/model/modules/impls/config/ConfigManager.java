/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ManagedConfig;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.StartModuleException;


/**
 * This is the implementation of the {@link AConfigManager}-module. It mainly loads, saves and updates configurations of
 * the clients which had registered themselves.
 * 
 * @author Gero
 * 
 */
public class ConfigManager extends AConfigManager
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger								log						= Logger.getLogger(ConfigManager.class
																											.getName());
	
	/** or "ISO-8859-1" */
	private static final String								XML_ENCODING			= "UTF-8";
	
	private final SumatraModel									model						= SumatraModel.getInstance();
	
	private final Map<String, ManagedConfig>				configMap				= new HashMap<String, ManagedConfig>();
	private final Map<String, List<IConfigObserver>>	waitingObserversMap	= new HashMap<String, List<IConfigObserver>>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create new config Manager. This is usually rather a workaround for testing
	 */
	public ConfigManager()
	{
		this(null);
	}
	
	
	/**
	 * @param subNode
	 */
	public ConfigManager(SubnodeConfiguration subNode)
	{
		super();
		
		for (final IConfigClient client : REGISTERED_CLIENTS)
		{
			// Load config initially
			final String fileName = model.getUserProperty(client.getConfigKey());
			final XMLConfiguration xmlConfig = doLoadConfig(client, fileName);
			if (xmlConfig == null)
			{
				// doLoadConfig already error'd
				return;
			}
			
			// Initially create it!
			final ManagedConfig config = new ManagedConfig(client, xmlConfig);
			configMap.put(client.getConfigKey(), config);
			
			// Any observers already waiting? If yes, remove and add to config
			final List<IConfigObserver> waitingObservers = waitingObserversMap.remove(client.getConfigKey());
			if (waitingObservers != null)
			{
				for (final IConfigObserver observer : waitingObservers)
				{
					config.registerObserver(observer);
				}
			}
			
			// Notify
			config.notifyOnLoad();
			// IConfigManagerObserver
			notifyConfigAdded(config);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Collection<ManagedConfig> getLoadedConfigs()
	{
		return configMap.values();
	}
	
	
	@Override
	protected void onStartup()
	{
	}
	
	
	@Override
	protected void onNewClient(IConfigClient client)
	{
	}
	
	
	@Override
	protected void onStop()
	{
		configMap.clear();
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		/** nothing to do here */
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		/** nothing to do here */
	}
	
	
	@Override
	public void stopModule()
	{
		/** nothing to do here */
	}
	
	
	@Override
	public void deinitModule()
	{
		for (final ManagedConfig config : configMap.values())
		{
			// Save
			doSaveConfig(config);
		}
	}
	
	
	@Override
	public void notifyConfigEdited(String configKey)
	{
		ManagedConfig changedConfig = configMap.get(configKey);
		if (changedConfig == null)
		{
			log.error("Could not get config");
			try
			{
				Thread.sleep(500);
			} catch (InterruptedException err)
			{
			}
			changedConfig = configMap.get(configKey);
			if (changedConfig == null)
			{
				return;
			}
			log.info("Config got after second try");
		}
		final IConfigClient client = changedConfig.getClient();
		
		changedConfig.notifyOnReload();
		
		if (!client.isEditable())
		{
			log.error("Something went wrong: This config should not be editable!!" + client.getConfigKey());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- load -----------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean loadConfig(String configKey, String newFileName)
	{
		// Save old
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("There is no config for key '" + configKey + "'; thus it can not be loaded from '" + newFileName
					+ "'.");
			return false;
		}
		
		// Load new
		final IConfigClient client = config.getClient();
		final XMLConfiguration newXmlConfig = doLoadConfig(client, newFileName);
		if (newXmlConfig == null)
		{
			log.error("Could not load config from '" + newFileName + "'.");
			return false;
		}
		
		// Actually save old
		// Ignore the fact that the old config has not been stored
		doSaveConfig(config);
		
		
		// Set new config
		config.setXmlConfig(newXmlConfig);
		// In case the name changed
		model.setUserProperty(configKey, newFileName);
		
		// Notify change
		config.notifyOnLoad();
		notifyConfigReloaded(config);
		
		return true;
	}
	
	
	@Override
	public boolean reloadConfig(String configKey)
	{
		// Reload from same file
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("There is no config for key: '" + configKey + "'; thus it can not be reloaded.");
			return false;
		}
		
		final IConfigClient client = config.getClient();
		
		final String fileName = model.getUserProperty(client.getConfigKey());
		final XMLConfiguration newXmlConfig = doLoadConfig(client, fileName);
		if (newXmlConfig == null)
		{
			log.error("Unable to reload from '" + fileName + "'.");
			return false;
		}
		
		// Set new config
		config.setXmlConfig(newXmlConfig);
		
		// Notify client
		config.notifyOnReload();
		notifyConfigReloaded(config);
		
		return true;
	}
	
	
	private XMLConfiguration doLoadConfig(IConfigClient client, String fileName)
	{
		final String filePath = client.getConfigPath() + fileName;
		final XMLConfiguration xmlConfig = new XMLConfiguration();
		try
		{
			xmlConfig.setDelimiterParsingDisabled(true);
			xmlConfig.load(filePath);
			xmlConfig.setFileName(fileName);
			
			log.debug("Loaded config for '" + client.getName() + "': " + fileName);
		} catch (final ConfigurationException err)
		{
			log.error("Unable to load '" + client.getConfigKey() + "' from '" + filePath + "':", err);
			return null;
		}
		
		return xmlConfig;
	}
	
	
	// --------------------------------------------------------------------------
	// --- save -----------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean saveConfig(String configKey, String newFileName)
	{
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("Unable to save config, there is no config for this key: '" + configKey + "'.");
			return false;
		}
		
		if (!newFileName.endsWith(".xml"))
		{
			newFileName += ".xml";
		}
		
		return doSaveConfig(config, newFileName);
	}
	
	
	@Override
	public boolean saveConfig(String configKey)
	{
		final ManagedConfig config = configMap.get(configKey);
		return doSaveConfig(config);
	}
	
	
	private boolean doSaveConfig(ManagedConfig config)
	{
		final String fileName = model.getUserProperty(config.getClient().getConfigKey());
		return doSaveConfig(config, fileName);
	}
	
	
	private boolean doSaveConfig(ManagedConfig config, String newFileName)
	{
		final IConfigClient client = config.getClient();
		
		// Let the client prepare
		final XMLConfiguration xmlConfig = client.prepareConfigForSaving(config.getXmlConfig());
		xmlConfig.setFileName(newFileName);
		
		// Not same object?
		if (xmlConfig != config.getXmlConfig())
		{
			// Basically allowed. But problematic...
			// :
			if (client.isEditable())
			{
				log.warn("The IConfigClient '"
						+ client.getConfigKey()
						+ "' manipulated its config before save, but is also editable. This may lead to incosistencies! Read doc.");
			}
			
			// Update ManagedConfig
			config.setXmlConfig(xmlConfig);
		}
		
		
		final String path = client.getConfigPath() + newFileName;
		try
		{
			final FileOutputStream targetFile = new FileOutputStream(path, false);
			
			final OutputStream prettyOut = new PrettyXMLOutputStream(targetFile, XML_ENCODING);
			xmlConfig.save(prettyOut, XML_ENCODING);
			
			prettyOut.close();
			targetFile.close();
		} catch (final ConfigurationException err)
		{
			log.error("Unable to save config '" + client.getConfigKey() + "' to '" + path + "'.");
			return false;
		} catch (final FileNotFoundException err)
		{
			log.error("Unable to access the file to save the config to: " + path, err);
		} catch (final IOException err)
		{
			log.error("Error while saving config: Unable to close streams!", err);
		}
		
		return true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- manage configs -------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public List<String> getAvailableConfigs(String configKey)
	{
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("There is no config for the given key: '" + configKey + "'!");
			return Collections.emptyList();
		}
		
		// Gather all files in folder that match certain properties
		final String path = config.getClient().getConfigPath();
		final File configFolder = new File(path);
		if (!configFolder.isDirectory())
		{
			log.error("The path '" + path + "' specified for '" + configKey + "' actually is not a path!");
			return Collections.emptyList();
		}
		
		// Filter
		final String[] list = configFolder.list(new ConfigNameFilter());
		
		return Arrays.asList(list);
	}
	
	
	@Override
	public String getLoadedFileName(String configKey)
	{
		return model.getUserProperty(configKey);
	}
	
	
	@Override
	public void registerObserverAt(String configKey, IConfigObserver newObserver)
	{
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			final List<IConfigObserver> waitingObservers = getWaitingObservers(configKey);
			waitingObservers.add(newObserver);
		} else
		{
			config.registerObserver(newObserver);
		}
	}
	
	
	private List<IConfigObserver> getWaitingObservers(String key)
	{
		List<IConfigObserver> result = waitingObserversMap.get(key);
		if (result == null)
		{
			result = new LinkedList<IConfigObserver>();
			waitingObserversMap.put(key, result);
		}
		return result;
	}
	
	
	@Override
	public boolean unregisterObserverAt(String configKey, IConfigObserver oldObserver)
	{
		final List<IConfigObserver> observers = waitingObserversMap.get(configKey);
		if (observers != null)
		{
			observers.remove(oldObserver);
		}
		
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("There is no config for the given key: '" + configKey + "'!");
			return false;
		}
		
		config.unregisterObserver(oldObserver);
		return true;
	}
	
	
	private static class ConfigNameFilter implements FilenameFilter
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return !name.startsWith(".");
		}
	}
}
