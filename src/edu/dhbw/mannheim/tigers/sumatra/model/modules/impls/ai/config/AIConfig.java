/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.exceptions.LoadConfigException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.AConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;


/**
 * This holds some static variables to parameterize the AI
 * hard choices - null == usual procedures in classes
 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte
 */
public final class AIConfig
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log				= Logger.getLogger(AIConfig.class.getName());
	
	/** max number of bots that are in one team */
	public static final int						MAX_NUM_BOTS	= 13;
	
	// Observers
	private final List<IAIConfigObserver>	observers		= new LinkedList<IAIConfigObserver>();
	
	// AIConfig
	private final IConfigClient				aiClient			= new AIConfigClient();
	private Errt									errt				= null;
	private OptimizationConfig					optimization	= null;
	private FieldRasterConfig					fieldRaster		= null;
	private MetisCalculators					calculators		= null;
	private Tactics								tactics			= null;
	private Roles									roles				= null;
	private Plays									plays				= null;
	
	// Geometry
	private final IConfigClient				geomClient		= new GeometryConfigClient();
	private volatile Geometry					geometry;
	
	// BotConfig
	private IConfigClient						botClient		= new BotConfigClient();
	private BotConfig								defaultBotConfig;
	private Map<EBotType, BotConfig>			botConfig		= new HashMap<EBotType, BotConfig>();
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class AIConfigHolder
	{
		private static final AIConfig	CONFIG	= new AIConfig();
	}
	
	
	private AIConfig()
	{
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static AIConfig getInstance()
	{
		return AIConfigHolder.CONFIG;
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigClients -------------------------------------------------------
	// --------------------------------------------------------------------------
	private final class AIConfigClient extends AConfigClient
	{
		private AIConfigClient()
		{
			super("AI Config", AAgent.AI_CONFIG_PATH, AAgent.KEY_AI_CONFIG, AAgent.VALUE_AI_CONFIG, true);
		}
		
		
		@Override
		public void onLoad(Configuration newConfig)
		{
			errt = new Errt(newConfig);
			optimization = new OptimizationConfig(newConfig);
			try
			{
				fieldRaster = new FieldRasterConfig(newConfig);
			} catch (final LoadConfigException err)
			{
				log.error("Error while parsing FieldRaster-Config: ", err);
			}
			calculators = new MetisCalculators(newConfig);
			tactics = new Tactics(newConfig);
			roles = new Roles(newConfig);
			plays = new Plays(newConfig);
			
			notifyNewFieldRaster();
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IConfigClient getAiClient()
	{
		return aiClient;
	}
	
	
	private final class GeometryConfigClient extends AConfigClient
	{
		private GeometryConfigClient()
		{
			super("Geometry", AAgent.GEOMETRY_CONFIG_PATH, AAgent.KEY_GEOMETRY_CONFIG, AAgent.VALUE_GEOMETRY_CONFIG, true);
		}
		
		
		@Override
		public void onLoad(Configuration config)
		{
			geometry = new Geometry(config);
		}
	}
	
	private final class BotConfigClient extends AConfigClient
	{
		private BotConfigClient()
		{
			super("Bot Config", AAgent.BOT_CONFIG_PATH, AAgent.KEY_BOT_CONFIG, AAgent.VALUE_BOT_CONFIG, true);
		}
		
		
		@Override
		public void onLoad(Configuration newConfig)
		{
			Iterator<String> it = newConfig.getKeys();
			Set<String> keys = new HashSet<String>();
			while (it.hasNext())
			{
				keys.add((String) Array.get(it.next().split("\\."), 0));
			}
			keys.remove("default");
			defaultBotConfig = new BotConfig(newConfig.subset("default"));
			botConfig.put(EBotType.UNKNOWN, defaultBotConfig);
			for (String key : keys)
			{
				Configuration conf = newConfig.subset(key);
				try
				{
					EBotType botType = EBotType.valueOf(key.toUpperCase(Locale.ENGLISH));
					botConfig.put(botType, new BotConfig(conf, defaultBotConfig));
				} catch (IllegalArgumentException e)
				{
					log.error("Could not load bot specific parameters from " + getConfigPath() + getDefaultValue()
							+ " for bot type " + key);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IConfigClient getGeomClient()
	{
		return geomClient;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IConfigClient getBotClient()
	{
		return botClient;
	}
	
	
	// --------------------------------------------------------------------------
	// --- observable -----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param newObserver
	 */
	public void addObserver(IAIConfigObserver newObserver)
	{
		synchronized (observers)
		{
			observers.add(newObserver);
			
			newObserver.onNewFieldRaster(fieldRaster);
		}
	}
	
	
	/**
	 * 
	 * @param oldObserver
	 */
	public void removeObserver(IAIConfigObserver oldObserver)
	{
		synchronized (observers)
		{
			observers.remove(oldObserver);
		}
	}
	
	
	/**
	 * 
	 * This function is used to visualize the positioning field raster in sumatra field view.
	 * Thus field raster will only be loaded once at startup this method is private and will
	 * be called with AI-Module start.
	 * 
	 */
	private void notifyNewFieldRaster()
	{
		synchronized (observers)
		{
			for (final IAIConfigObserver o : observers)
			{
				o.onNewFieldRaster(fieldRaster);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- accessors ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the errt configuration
	 */
	public static Errt getErrt()
	{
		return AIConfig.getInstance().errt;
	}
	
	
	/**
	 * Do not call this method in {@link ARole#update(AIInfoFrame)} or similar
	 * frequently called methods. Rather store this value locally (but not static!!)
	 * 
	 * @param botType
	 * @return the botConfig configuration
	 */
	public static BotConfig getBotConfig(EBotType botType)
	{
		BotConfig botConfig = AIConfig.getInstance().botConfig.get(botType);
		if (botConfig == null)
		{
			log.warn("You requested a botConfig for a botType (" + botType + ") that has no specialized config.");
			log.warn("Maybe you called this before the botType is known. (botType will be unknown then)");
			log.warn(
					"The default config will be loaded, but you should make sure you grap the configs later in your code!",
					new Exception());
			return AIConfig.getInstance().defaultBotConfig;
		}
		return botConfig;
	}
	
	
	/**
	 * Use this only, if you do not care about bot specific config params.
	 * 
	 * @return the defaultBotConfig
	 */
	public static BotConfig getDefaultBotConfig()
	{
		return AIConfig.getInstance().defaultBotConfig;
	}
	
	
	/**
	 * @return geometry values
	 */
	public static Geometry getGeometry()
	{
		if (AIConfig.getInstance().geometry == null)
		{
			throw new IllegalStateException("geometry is null!");
		}
		return AIConfig.getInstance().geometry;
	}
	
	
	/**
	 * @return the field raster configuration
	 */
	public static FieldRasterConfig getFieldRaster()
	{
		return AIConfig.getInstance().fieldRaster;
	}
	
	
	/**
	 * @return the role configuration
	 */
	public static Roles getRoles()
	{
		return AIConfig.getInstance().roles;
	}
	
	
	/**
	 * @return the role configuration
	 */
	public static Plays getPlays()
	{
		return AIConfig.getInstance().plays;
	}
	
	
	/**
	 * @return the calculators configuration
	 */
	public static MetisCalculators getMetisCalculators()
	{
		return AIConfig.getInstance().calculators;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static Tactics getTactics()
	{
		return AIConfig.getInstance().tactics;
	}
	
	
	/**
	 * Do not call this method in {@link ARole#update(AIInfoFrame)} or similar
	 * frequently called methods. Rather store this value locally (but not static!!)
	 * 
	 * @param botType
	 * @return the general
	 */
	public static General getGeneral(EBotType botType)
	{
		return AIConfig.getBotConfig(botType).getGeneral();
	}
	
	
	/**
	 * Do not call this method in {@link ARole#update(AIInfoFrame)} or similar
	 * frequently called methods. Rather store this value locally (but not static!!)
	 * 
	 * @param botType
	 * @return the tolerances
	 */
	public static Tolerances getTolerances(EBotType botType)
	{
		return AIConfig.getBotConfig(botType).getTolerances();
	}
	
	
	/**
	 * Do not call this method in {@link ARole#update(AIInfoFrame)} or similar
	 * frequently called methods. Rather store this value locally (but not static!!)
	 * 
	 * @param botType
	 * @return the skills
	 */
	public static Skills getSkills(EBotType botType)
	{
		return AIConfig.getBotConfig(botType).getSkills();
	}
	
	
	/**
	 * @return the optimization
	 */
	public static OptimizationConfig getOptimization()
	{
		return AIConfig.getInstance().optimization;
	}
	
	
}
