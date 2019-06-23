/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): bernhard
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.Moduli;


/**
 * The model of the application.
 * It contains the low-level-methods and data.
 * You can use the methods in a Presenter for combining
 * to a business - logic.
 * In Sumatra the Model is entirely outsourced in moduli.
 * That means, that all low-level-methods are within separated modules.
 * The model make use of a Singleton - pattern,
 * so you can access the Model
 * with a simple SumatraModel.getInstance() .
 * 
 * @author bernhard
 */
public final class SumatraModel extends Moduli
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger - Start
	private static final Logger	log								= Logger.getLogger(SumatraModel.class.getName());
	// Logger - End
	
	// --- version ---
	private static final String	VERSION							= "4.0";
	
	// --- singleton ---
	private static SumatraModel	instance							= new SumatraModel();
	
	// --- user settings ---
	/** */
	public static final String		DEFAULT_USER_SETTINGS_FILE	= "all.default";
	/** */
	public static final String		USER_SETTINGS_PATH			= "./config/user/";
	/** */
	public static final String		DEFAULT_USER_SETTINGS_PATH	= USER_SETTINGS_PATH + DEFAULT_USER_SETTINGS_FILE;
	
	/** These {@link Properties} contain the information necessary for the application to run properly (e.g., file paths) */
	private Properties				userSettings					= new Properties();
	
	// --- moduli config ---
	private static final String	KEY_MODULI_CONFIG				= SumatraModel.class.getName() + ".moduliConfig";
	/** */
	public static final String		MODULI_CONFIG_PATH			= "./config/moduli/";
	/**  */
	public static final String		MODULI_CONFIG_FILE_DEFAULT	= "moduli_sumatra.xml";
	
	// Application Properties
	private static final String	CONFIG_SETTINGS_PATH			= "./config/user/";
	
	
	private boolean					productive						= true;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor from model.
	 * Initializes data which is kept by the model.
	 */
	private SumatraModel()
	{
		loadApplicationProperties();
		String prod = System.getProperty("productive");
		if (prod != null)
		{
			productive = Boolean.valueOf(prod);
		}
	}
	
	
	/**
	 * getInstance() - Singleton-pattern.
	 * 
	 * @return the "one-and-only" instance of CSModel
	 */
	public static SumatraModel getInstance()
	{
		return instance;
	}
	
	
	/**
	 * Load application properties in two steps
	 */
	private void loadApplicationProperties()
	{
		// ### Load application properties in two steps:
		// 1.: Load default properties (fix path!)
		final Properties defaultUserSettings = new Properties();
		final String defFilename = SumatraModel.DEFAULT_USER_SETTINGS_PATH;
		
		FileInputStream inDefaultSettings = null;
		try
		{
			inDefaultSettings = new FileInputStream(defFilename);
			defaultUserSettings.load(inDefaultSettings);
		} catch (final FileNotFoundException err)
		{
			log.error("Default user config not found at: " + defFilename + "! Trying to continue...");
		} catch (final IOException err)
		{
			log.error("Default user config could not be read from: " + defFilename + "! Trying to continue...");
		} finally
		{
			try
			{
				if (inDefaultSettings != null)
				{
					inDefaultSettings.close();
				}
			} catch (IOException e)
			{
				log.error("Default user config could not be read from: " + defFilename + "! Error on close. ");
			}
		}
		
		
		// 2.: Load user properties (and create from default if it not exists!)
		// Get config file name, which consists of pcName + userName
		// Load properties
		// ...with default
		userSettings = new Properties(defaultUserSettings);
		final File uf = getUserPropertiesFile();
		
		FileInputStream inUserSettings = null;
		try
		{
			if (uf.exists())
			{
				inUserSettings = new FileInputStream(uf);
				userSettings.load(inUserSettings);
			} else
			{
				if (uf.createNewFile())
				{
					log.debug("new user file created");
				}
				// Make defaults to standard
				userSettings.putAll(defaultUserSettings);
			}
		} catch (final IOException err)
		{
			log.warn("Config: " + uf.getPath() + " cannot be read, using default configs!");
		} finally
		{
			try
			{
				if (inUserSettings != null)
				{
					inUserSettings.close();
				}
			} catch (IOException e)
			{
				log.warn("Config: " + uf.getPath() + " cannot be read, using default configs!");
			}
		}
	}
	
	
	/**
	 * @return The {@link File} which represents the user-properties file
	 */
	public File getUserPropertiesFile()
	{
		final String userName = System.getProperty("user.name");
		final String filename = CONFIG_SETTINGS_PATH + userName + ".props";
		
		return new File(filename);
	}
	
	
	// --------------------------------------------------------------------------
	// --- moduli config --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the currentModuliConfig
	 */
	public String getCurrentModuliConfig()
	{
		return userSettings.getProperty(SumatraModel.KEY_MODULI_CONFIG);
	}
	
	
	/**
	 * @param currentModuliConfig the currentModuliConfig to set
	 */
	public void setCurrentModuliConfig(final String currentModuliConfig)
	{
		userSettings.setProperty(SumatraModel.KEY_MODULI_CONFIG, currentModuliConfig);
	}
	
	
	// --------------------------------------------------------------------------
	// --- app properties -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param userSettings the appProperties to set
	 */
	public void setUserSettings(final Properties userSettings)
	{
		this.userSettings = userSettings;
	}
	
	
	/**
	 * @return The applications {@link Properties}
	 */
	public Properties getUserSettings()
	{
		return userSettings;
	}
	
	
	/**
	 * Calls {@link Properties#setProperty(String, String)}
	 * 
	 * @param key
	 * @param value
	 * @return The value which was associated with the given key before
	 */
	public String setUserProperty(final String key, final String value)
	{
		Object obj = null;
		if (value == null)
		{
			obj = userSettings.remove(key);
		} else
		{
			obj = userSettings.setProperty(key, value);
		}
		
		if ((obj == null))
		{
			// no previous item
			return null;
		}
		if (!(obj instanceof String))
		{
			log.warn("Object '" + obj + "' (which has been associated to '" + key + "') is no String!");
			return null;
		}
		
		return (String) obj;
	}
	
	
	/**
	 * Calls {@link Properties#getProperty(String)}
	 * 
	 * @param key
	 * @return The String associated with the given key
	 */
	public String getUserProperty(final String key)
	{
		return userSettings.getProperty(key);
	}
	
	
	/**
	 * Calls {@link Properties#getProperty(String)}
	 * 
	 * @param key
	 * @param def
	 * @return The String associated with the given key
	 */
	public String getUserProperty(final String key, final String def)
	{
		String val = userSettings.getProperty(key);
		if (val == null)
		{
			return def;
		}
		return val;
	}
	
	
	/**
	 * Sumatra version
	 * 
	 * @return
	 */
	public static String getVersion()
	{
		return VERSION;
	}
	
	
	/**
	 * @return the productive
	 */
	public final boolean isProductive()
	{
		return productive;
	}
}
