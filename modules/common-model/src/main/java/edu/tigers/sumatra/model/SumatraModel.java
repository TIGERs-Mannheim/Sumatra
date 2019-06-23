/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): bernhard
 * *********************************************************
 */
package edu.tigers.sumatra.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.tigers.moduli.Moduli;


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
	private static final Logger			log								= Logger.getLogger(SumatraModel.class.getName());
																						// Logger - End
																						
	// --- version ---
	private static final String			VERSION							= "5.0";
																						
	// --- singleton ---
	private static final SumatraModel	INSTANCE							= new SumatraModel();
																						
	/**
	 * These {@link Properties} contain the information necessary for the application to run properly (e.g., file paths)
	 */
	private Properties						userSettings					= new Properties();
																						
	// --- moduli config ---
	private static final String			KEY_MODULI_CONFIG				= SumatraModel.class.getName() + ".moduliConfig";
	/** */
	public static final String				MODULI_CONFIG_PATH			= "./config/moduli/";
	/**  */
	public static final String				MODULI_CONFIG_FILE_DEFAULT	= "moduli_sumatra.xml";
																						
	// Application Properties
	private static final String			CONFIG_SETTINGS_PATH			= "./config/";
																						
																						
	private boolean							productive						= false;
																						
																						
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
		return INSTANCE;
	}
	
	
	/**
	 * Load application properties in two steps
	 */
	private void loadApplicationProperties()
	{
		// Load user properties
		// Get config file name, which consists of pcName + userName
		userSettings = new Properties();
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
	
	
	@Override
	public void loadModulesSafe(final String filename)
	{
		super.loadModulesSafe(MODULI_CONFIG_PATH + filename);
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
		return userSettings.getProperty(SumatraModel.KEY_MODULI_CONFIG, MODULI_CONFIG_FILE_DEFAULT);
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
	
	
	/**
	 * 
	 */
	public void saveUserProperties()
	{
		final File uf = getUserPropertiesFile();
		
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(uf);
			userSettings.store(out, null);
		} catch (final IOException err)
		{
			log.warn("Could not write to " + uf.getPath() + ", configuration is not saved");
		}
		
		if (out != null)
		{
			try
			{
				out.close();
				log.trace("Saved configuration to: " + uf.getPath());
			} catch (IOException e)
			{
				log.warn("Could not close " + uf.getPath() + ", configuration is not saved");
			}
		}
	}
	
	
	/**
	 * Switch the logger of
	 */
	public static void noLogging()
	{
		Logger.getRootLogger().setLevel(Level.OFF);
	}
	
	
	/**
	 * @param lvl
	 */
	public static void changeLogLevel(final Level lvl)
	{
		Appender appender = Logger.getRootLogger().getAppender("console");
		if ((appender != null) && (appender instanceof ConsoleAppender))
		{
			((ConsoleAppender) appender).setThreshold(lvl);
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
