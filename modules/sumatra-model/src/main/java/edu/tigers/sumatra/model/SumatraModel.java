/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.model;

import edu.tigers.moduli.Moduli;
import edu.tigers.moduli.exceptions.DependencyException;
import edu.tigers.moduli.exceptions.LoadModulesException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


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
 */
@Log4j2
public final class SumatraModel extends Moduli
{
	// --- version ---
	private static final String VERSION = getVersionNameFromManifest();

	// --- singleton ---
	private static final SumatraModel INSTANCE = new SumatraModel();

	/**
	 * These {@link Properties} contain the information necessary for the application to run properly (e.g., file paths)
	 */
	private Properties userSettings = new Properties();

	// --- moduli config ---
	private static final String KEY_MODULI_CONFIG = SumatraModel.class.getName() + ".moduliConfig";
	public static final String MODULI_CONFIG_PATH = "./config/moduli/";
	public static final String MODULI_CONFIG_FILE_DEFAULT = "sim.xml";

	// Application Properties
	private static final String CONFIG_SETTINGS_PATH = "./config/";

	@Getter
	@Setter
	private boolean tournamentMode;

	private boolean simulation = false;
	private String environment = "";
	@Getter
	private String geometry = "";


	/**
	 * Constructor from model.
	 * Initializes data which is kept by the model.
	 */
	private SumatraModel()
	{
		loadApplicationProperties();
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


	@SuppressWarnings("java:S1181") // catching Throwables intentionally here
	public void startUp(final String moduliConfig)
	{
		try
		{
			if (getModulesState().get() == ModulesState.ACTIVE)
			{
				stopModules();
			}
			SumatraModel.getInstance().setCurrentModuliConfig(moduliConfig);
			loadModulesOfConfig(getCurrentModuliConfig());
			startModules();
		} catch (Exception e)
		{
			log.error("Could not start Sumatra.", e);
		}
	}


	/**
	 * Load application properties in two steps
	 */
	private void loadApplicationProperties()
	{
		userSettings = new Properties();
		final File uf = getUserPropertiesFile();
		if (!uf.exists())
		{
			return;
		}
		try (FileInputStream inUserSettings = new FileInputStream(uf))
		{
			userSettings.load(inUserSettings);
		} catch (final IOException err)
		{
			log.warn("Config: {} could not be read, using default configs!", uf.getPath(), err);
		}
	}


	/**
	 * Load modules of the given config file. The config path is appended by this method and must not be prepended.
	 *
	 * @param configFileName the config file name
	 * @throws DependencyException
	 * @throws LoadModulesException
	 */
	@SuppressWarnings("squid:S1160") // throwing two exceptions, because this is only a proxy method
	public void loadModulesOfConfig(final String configFileName) throws DependencyException, LoadModulesException
	{
		try
		{
			super.loadModules(MODULI_CONFIG_PATH + configFileName);
		} catch (LoadModulesException e)
		{
			log.error("Could not load moduli config {}. Trying default one.", configFileName, e);
			setCurrentModuliConfig(MODULI_CONFIG_FILE_DEFAULT);
			super.loadModules(MODULI_CONFIG_PATH + MODULI_CONFIG_FILE_DEFAULT);
		}
		updateInternalStateFromGlobalConfig();
	}


	/**
	 * Load modules of the given config file. The config path is appended by this method and must not be prepended.
	 * Catch exceptions and continue normally
	 *
	 * @param configFileName the config file name
	 */
	public void loadModulesOfConfigSafe(final String configFileName)
	{
		super.loadModulesSafe(MODULI_CONFIG_PATH + configFileName);
		updateInternalStateFromGlobalConfig();
	}


	private void updateInternalStateFromGlobalConfig()
	{
		simulation = getGlobalConfiguration().getBoolean("simulation", false);
		environment = getGlobalConfiguration().getString("environment");
		geometry = getGlobalConfiguration().getString("geometry", "DIV_A");
	}


	/**
	 * @return The {@link File} which represents the user-properties file
	 */
	private File getUserPropertiesFile()
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
	public String setUserProperty(final String key, final Object value)
	{
		Object obj;
		if (value == null)
		{
			obj = userSettings.remove(key);
		} else
		{
			obj = userSettings.setProperty(key, String.valueOf(value));
		}

		if (obj == null)
		{
			// no previous item
			return null;
		}
		if (!(obj instanceof String))
		{
			log.warn("Object '{}' (which has been associated to '{}') is no String!", obj, key);
			return null;
		}

		return (String) obj;
	}


	public String setUserProperty(Class<?> type, String key, Object value)
	{
		return setUserProperty(type.getCanonicalName() + "." + key, value);
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
	 * @param type
	 * @param key
	 * @return The String associated with the given key
	 */
	public String getUserProperty(Class<?> type, String key)
	{
		return userSettings.getProperty(type.getCanonicalName() + "." + key);
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
	 * Calls {@link Properties#getProperty(String)}
	 *
	 * @param type
	 * @param key
	 * @param def
	 * @return The String associated with the given key
	 */
	public String getUserProperty(Class<?> type, String key, String def)
	{
		return getUserProperty(type.getCanonicalName() + "." + key, def);
	}


	/**
	 * Calls {@link Properties#getProperty(String)} and parses value to boolean
	 *
	 * @param key
	 * @param def
	 * @return The boolean associated with the given key
	 */
	public boolean getUserProperty(String key, boolean def)
	{
		String val = getUserProperty(key);
		if (val == null)
		{
			return def;
		}
		return Boolean.parseBoolean(val);
	}


	/**
	 * Calls {@link Properties#getProperty(String)} and parses value to boolean
	 *
	 * @param type
	 * @param key
	 * @param def
	 * @return The boolean associated with the given key
	 */
	public boolean getUserProperty(Class<?> type, String key, boolean def)
	{
		String val = getUserProperty(type, key);
		if (val == null)
		{
			return def;
		}
		return Boolean.parseBoolean(val);
	}


	/**
	 * Calls {@link Properties#getProperty(String)} and parses value to double
	 *
	 * @param key
	 * @param def
	 * @return The double associated with the given key
	 */
	public double getUserProperty(String key, double def)
	{
		String val = getUserProperty(key);
		if (val == null)
		{
			return def;
		}
		return Double.parseDouble(val);
	}


	/**
	 * Calls {@link Properties#getProperty(String)} and parses value to double
	 *
	 * @param type
	 * @param key
	 * @param def
	 * @return The double associated with the given key
	 */
	public double getUserProperty(Class<?> type, String key, double def)
	{
		String val = getUserProperty(type, key);
		if (val == null)
		{
			return def;
		}
		return Double.parseDouble(val);
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
	 * save properties
	 */
	public void saveUserProperties()
	{
		if (userSettings.isEmpty())
		{
			return;
		}
		final File uf = getUserPropertiesFile();
		if (!uf.exists())
		{
			try
			{
				if (uf.createNewFile())
				{
					log.debug("new user file created");
				}
			} catch (IOException e)
			{
				log.error("Could not create properties file: {}", uf.getAbsolutePath(), e);
			}
		}

		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(uf);
			userSettings.store(out, null);
		} catch (final IOException err)
		{
			log.warn("Could not write to {}, configuration is not saved", uf.getPath(), err);
		}

		if (out != null)
		{
			try
			{
				out.close();
				log.trace("Saved configuration to: {}", uf.getPath());
			} catch (IOException e)
			{
				log.warn("Could not close {}, configuration is not saved", uf.getPath(), e);
			}
		}
	}


	/**
	 * @param lvl
	 */
	public static void changeLogLevel(final Level lvl)
	{
		Configurator.setRootLevel(lvl);
	}


	/**
	 * @return the current environment (Robocup, Lab, etc) as defined by moduli config
	 */
	public String getEnvironment()
	{
		return environment;
	}


	public boolean isSimulation()
	{
		return simulation;
	}


	private static String getVersionNameFromManifest()
	{
		InputStream manifestStream = SumatraModel.class.getClassLoader()
				.getResourceAsStream("edu/tigers/sumatra/model/metadata.properties");
		if (manifestStream != null)
		{
			try
			{
				final Properties properties = new Properties();
				properties.load(manifestStream);
				return properties.getProperty("version", "unknown version");
			} catch (IOException e)
			{
				log.error("Could not read manifest", e);
			}

		}
		return "";
	}
}
