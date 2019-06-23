/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.GenericManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ManagedConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigAnnotationProcessor;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;


/**
 * This is the implementation of the {@link AConfigManager}-module. It mainly loads, saves and updates configurations of
 * the clients which had registered themselves.
 * 
 * @author Gero
 */
public class ConfigManager extends AConfigManager
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger								log						= Logger.getLogger(ConfigManager.class
																											.getName());
	private static final ConfigManager						INSTANCE					= new ConfigManager();
	
	/** or "ISO-8859-1" */
	private static final String								XML_ENCODING			= "UTF-8";
	
	private final SumatraModel									model						= SumatraModel.getInstance();
	
	private final Map<String, ManagedConfig>				configMap				= new LinkedHashMap<String, ManagedConfig>();
	private final Map<String, List<IConfigObserver>>	waitingObserversMap	= new HashMap<String, List<IConfigObserver>>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create new config Manager.
	 */
	public ConfigManager()
	{
		AConfigManager.registerConfigClient(GenericManager.getBotManagerConfigClient());
		AConfigManager.registerConfigClient(AIConfig.getGeometryClient());
		List<IConfigClient> clients = ConfigRegistration.getConfigClients();
		for (IConfigClient client : clients)
		{
			AConfigManager.registerConfigClient(client);
		}
		
		initModule();
	}
	
	
	/**
	 * @return
	 */
	public static ConfigManager getInstance()
	{
		return INSTANCE;
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
	protected void onNewClient(final IConfigClient client)
	{
	}
	
	
	/**
	 */
	private void initModule()
	{
		for (final IConfigClient client : registered_clients)
		{
			log.trace("Registered config client: " + client.getName());
			// Load config initially
			String fileName = model.getUserProperty(client.getConfigKey());
			if (fileName == null)
			{
				fileName = client.getDefaultValue();
				model.setUserProperty(client.getConfigKey(), fileName);
			}
			final File file = new File(client.getConfigPath() + fileName);
			
			final HierarchicalConfiguration loadedConfig;
			if (!file.exists())
			{
				if (client.isRequired())
				{
					log.warn("Config file " + fileName + " was not found! Using " + client.getDefaultValue());
					fileName = client.getDefaultValue();
					model.setUserProperty(client.getConfigKey(), fileName);
					loadedConfig = doLoadConfig(client, fileName);
				} else
				{
					loadedConfig = null;
				}
			} else
			{
				loadedConfig = doLoadConfig(client, fileName);
			}
			
			final HierarchicalConfiguration config;
			final HierarchicalConfiguration defConfig = client.getDefaultConfig();
			if ((loadedConfig == null) && (defConfig == null))
			{
				log.error("Could not load config for " + fileName + ". No default.");
				continue;
			}
			if (loadedConfig == null)
			{
				config = defConfig;
			} else if (defConfig == null)
			{
				config = loadedConfig;
			} else
			{
				ConfigAnnotationProcessor.merge(defConfig, loadedConfig);
				config = defConfig;
			}
			
			// Initially create it!
			final ManagedConfig mgdConfig = new ManagedConfig(client, config);
			configMap.put(client.getConfigKey(), mgdConfig);
			
			log.trace("ManagedConfig created");
			
			// Any observers already waiting? If yes, remove and add to config
			final List<IConfigObserver> waitingObservers = waitingObserversMap.remove(client.getConfigKey());
			if (waitingObservers != null)
			{
				for (final IConfigObserver observer : waitingObservers)
				{
					mgdConfig.registerObserver(observer);
				}
			}
			
			log.trace("Observers processed");
			
			
			// Notify
			mgdConfig.notifyOnLoad();
			log.trace("Config loaded");
			// IConfigManagerObserver
			notifyConfigAdded(mgdConfig);
			
			log.trace("Config added: " + fileName);
		}
	}
	
	
	@Override
	public void notifyConfigEdited(final String configKey)
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
	public boolean loadConfig(final String configKey, final String newFileName)
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
		log.debug("Loaded config for '" + client.getName() + "': " + newFileName);
		
		// Set new config
		config.setConfig(newXmlConfig);
		// In case the name changed
		model.setUserProperty(configKey, newFileName);
		
		// Notify change
		config.notifyOnLoad();
		notifyConfigReloaded(config);
		
		return true;
	}
	
	
	@Override
	public boolean reloadConfig(final String configKey)
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
		log.debug("Reloaded config for '" + client.getName() + "': " + fileName);
		
		// Set new config
		config.setConfig(newXmlConfig);
		
		// Notify client
		config.notifyOnReload();
		notifyConfigReloaded(config);
		
		return true;
	}
	
	
	private XMLConfiguration doLoadConfig(final IConfigClient client, final String fileName)
	{
		final String filePath = client.getConfigPath() + fileName;
		final XMLConfiguration xmlConfig = new XMLConfiguration();
		try
		{
			xmlConfig.setDelimiterParsingDisabled(true);
			xmlConfig.load(filePath);
			xmlConfig.setFileName(fileName);
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
	public boolean saveConfig(final String configKey, String newFileName)
	{
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("Save: Unable to save config, there is no config for this key: '" + configKey + "'.");
			return false;
		}
		
		if (!newFileName.endsWith(".xml"))
		{
			newFileName += ".xml";
		}
		
		return doSaveConfig(config, newFileName);
	}
	
	
	@Override
	public boolean saveConfig(final String configKey)
	{
		final ManagedConfig config = configMap.get(configKey);
		return doSaveConfig(config);
	}
	
	
	private boolean doSaveConfig(final ManagedConfig config)
	{
		final String fileName = model.getUserProperty(config.getClient().getConfigKey());
		return doSaveConfig(config, fileName);
	}
	
	
	private boolean doSaveConfig(final ManagedConfig config, final String newFileName)
	{
		final IConfigClient client = config.getClient();
		
		// Let the client prepare
		final HierarchicalConfiguration hConfig = client.prepareConfigForSaving(config.getConfig());
		final XMLConfiguration xmlConfig = new XMLConfiguration(hConfig);
		xmlConfig.setDelimiterParsingDisabled(true);
		xmlConfig.setFileName(newFileName);
		
		// Not same object?
		if (hConfig != config.getConfig())
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
			config.setConfig(xmlConfig);
		}
		
		
		final String path = client.getConfigPath() + newFileName;
		FileOutputStream targetFile = null;
		OutputStream prettyOut = null;
		try
		{
			targetFile = new FileOutputStream(path, false);
			
			prettyOut = new PrettyXMLOutputStream(targetFile, XML_ENCODING);
			xmlConfig.save(prettyOut, XML_ENCODING);
			
		} catch (final ConfigurationException err)
		{
			log.error("Unable to save config '" + client.getConfigKey() + "' to '" + path + "'.");
			return false;
		} catch (final FileNotFoundException err)
		{
			log.error("Unable to access the file to save the config to: " + path, err);
		} finally
		{
			try
			{
				if (prettyOut != null)
				{
					prettyOut.close();
				}
				if (targetFile != null)
				{
					targetFile.close();
				}
			} catch (IOException err)
			{
				log.error("Error while saving config: Unable to close streams!", err);
			}
		}
		
		return true;
	}
	
	
	/**
	 * @param configKey
	 */
	public void readConfig(final String configKey)
	{
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("Read: Unable to read config, there is no config for this key: '" + configKey + "'.");
			return;
		}
		IConfigClient client = config.getClient();
		
		config.setConfig(client.getDefaultConfig());
		
		// Notify client
		config.notifyOnReload();
		notifyConfigReloaded(config);
	}
	
	
	// --------------------------------------------------------------------------
	// --- manage configs -------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public List<String> getAvailableConfigs(final String configKey)
	{
		final ManagedConfig config = configMap.get(configKey);
		if (config == null)
		{
			log.error("Get configs: There is no config for the given key: '" + configKey + "'!");
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
		if (list != null)
		{
			return Arrays.asList(list);
		}
		return new ArrayList<>();
	}
	
	
	@Override
	public String getLoadedFileName(final String configKey)
	{
		return model.getUserProperty(configKey);
	}
	
	
	@Override
	public void registerObserverAt(final String configKey, final IConfigObserver newObserver)
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
	
	
	private List<IConfigObserver> getWaitingObservers(final String key)
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
	public boolean unregisterObserverAt(final String configKey, final IConfigObserver oldObserver)
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
		public boolean accept(final File dir, final String name)
		{
			return !name.startsWith(".");
		}
	}
}
