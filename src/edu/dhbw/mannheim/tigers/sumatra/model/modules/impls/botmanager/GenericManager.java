/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.BotFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.BotInitException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.AConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * The one and only generic botmanager.
 * 
 * @author AndreR
 * 
 */
public class GenericManager extends ABotManager implements IWorldPredictorObserver
{
	// --------------------------------------------------------------
	// --- instance-variables ---------------------------------------
	// --------------------------------------------------------------
	// Logger
	private static final Logger		log				= Logger.getLogger(GenericManager.class.getName());
	
	// "UTF-8";
	private static final String		XML_ENCODING	= "ISO-8859-1";
	private final Map<BotID, ABot>	botTable			= new ConcurrentSkipListMap<BotID, ABot>();
	private boolean						moduleRunning	= false;
	private AWorldPredictor				wp					= null;
	
	private MulticastDelegate			mcastDelegate	= null;
	private boolean						useMulticast	= false;
	private BaseStation					baseStation		= null;
	
	private AConfigManager				configMgr		= null;
	private final IConfigClient		configClient	= new ConfigClient();
	
	private final boolean				autoChargeInitially;
	private final int						maxCap;
	
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	/**
	 * Setup properties.
	 * @param subnodeConfiguration Properties for module-configuration
	 */
	public GenericManager(SubnodeConfiguration subnodeConfiguration)
	{
		AConfigManager.registerConfigClient(configClient);
		autoChargeInitially = subnodeConfiguration.getBoolean("autoChargeInitially");
		maxCap = subnodeConfiguration.getInt("maxCap");
	}
	
	
	// --------------------------------------------------------------
	// --- module-method(s) -----------------------------------------
	// --------------------------------------------------------------
	@Override
	public void initModule()
	{
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addObserver(this);
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + AWorldPredictor.MODULE_ID + "'!");
			return;
		}
		
		try
		{
			configMgr = (AConfigManager) SumatraModel.getInstance().getModule(AConfigManager.MODULE_ID);
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + AConfigManager.MODULE_ID + "'!");
			return;
		}
		
		log.debug("Initialized.");
	}
	
	
	@Override
	public void startModule()
	{
		configMgr.reloadConfig(ABotManager.KEY_BOTMANAGER_CONFIG);
		
		mcastDelegate.enable(useMulticast);
		baseStation.connect();
		
		moduleRunning = true;
		
		startBots();
		
		if (autoChargeInitially)
		{
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
					"AutoCharge"));
			executor.schedule(new Runnable()
			{
				
				@Override
				public void run()
				{
					log.info("Auto charging bots");
					for (final ABot bot : getAllBots().values())
					{
						if ((bot.getType() == EBotType.TIGER) || (bot.getType() == EBotType.GRSIM))
						{
							bot.execute(new TigerKickerChargeAuto(maxCap));
						}
					}
				}
			}, 2, TimeUnit.SECONDS);
			executor.shutdown();
		}
		
		log.debug("Started.");
	}
	
	
	@Override
	public void stopModule()
	{
		stopBots();
		
		baseStation.disconnect();
		mcastDelegate.enable(false);
		
		moduleRunning = false;
		
		log.debug("Stopped.");
	}
	
	
	@Override
	public void deinitModule()
	{
		log.debug("Deinitialized.");
	}
	
	
	/**
	 * Be aware of the fact that this method might be called from several threads
	 */
	@Override
	public void execute(BotID id, ACommand cmd)
	{
		final ABot bot = botTable.get(id);
		if (bot != null)
		{
			bot.execute(cmd);
		} else
		{
			log.warn("Execute: Invalid botID: " + id);
		}
	}
	
	
	@Override
	public void botConnectionChanged(ABot bot)
	{
		notifyBotConnectionChanged(bot);
	}
	
	
	@Override
	public ABot addBot(EBotType type, BotID id, String name)
	{
		if (botTable.containsKey(id))
		{
			return null;
		}
		
		final ABot bot = BotFactory.createBot(type, id, name);
		botTable.put(id, bot);
		
		if ((bot.getType() == EBotType.TIGER) || (bot.getType() == EBotType.GRSIM))
		{
			((TigerBot) bot).setMulticastDelegate(mcastDelegate);
		}
		
		if (bot.getType() == EBotType.TIGER_V2)
		{
			((TigerBotV2) bot).setBaseStation(baseStation);
		}
		
		notifyBotAdded(bot);
		
		return bot;
	}
	
	
	@Override
	public void removeBot(BotID id)
	{
		final ABot bot = botTable.remove(id);
		if (bot != null)
		{
			if ((bot.getType() == EBotType.TIGER) || (bot.getType() == EBotType.GRSIM))
			{
				((TigerBot) bot).setMulticastDelegate(null);
			}
			
			if (bot.getType() == EBotType.TIGER_V2)
			{
				((TigerBotV2) bot).setBaseStation(null);
			}
			
			bot.stop();
			notifyBotRemoved(bot);
		}
	}
	
	
	@Override
	public void changeBotId(BotID oldId, BotID newId)
	{
		if (!botTable.containsKey(oldId))
		{
			return;
		}
		
		final ABot bot = botTable.get(oldId);
		
		if (botTable.containsKey(newId))
		{
			bot.internalSetBotId(oldId);
			return;
		}
		
		botTable.remove(oldId);
		
		bot.internalSetBotId(newId);
		
		botTable.put(newId, bot);
		
		notifyBotIdChanged(oldId, newId);
	}
	
	
	@Override
	public Map<BotID, ABot> getAllBots()
	{
		return botTable;
	}
	
	
	@Override
	public void removeAllBots()
	{
		stopBots();
		
		for (final ABot bot : botTable.values())
		{
			notifyBotRemoved(bot);
		}
		
		botTable.clear();
	}
	
	
	private void loadConfig(XMLConfiguration config)
	{
		readConfiguration(config);
		
		if (moduleRunning)
		{
			mcastDelegate.enable(useMulticast);
			baseStation.connect();
			
			startBots();
		}
	}
	
	
	@Override
	public ITransceiverUDP getMulticastTransceiver()
	{
		return mcastDelegate.getTransceiver();
	}
	
	
	@Override
	public void setUseMulticast(boolean enable)
	{
		useMulticast = enable;
		
		mcastDelegate.enable(enable);
	}
	
	
	@Override
	public boolean getUseMulticast()
	{
		return useMulticast;
	}
	
	
	@Override
	public void setUpdateAllSleepTime(long time)
	{
		mcastDelegate.setUpdateAllSleepTime(time);
	}
	
	
	@Override
	public long getUpdateAllSleepTime()
	{
		return mcastDelegate.getUpdateAllSleepTime();
	}
	
	
	@Override
	public BaseStation getBaseStation()
	{
		return baseStation;
	}
	
	
	private boolean readConfiguration(XMLConfiguration config)
	{
		removeAllBots();
		
		final SubnodeConfiguration mcastConfig = config.configurationAt("multicast");
		mcastDelegate = new MulticastDelegate(mcastConfig, this);
		
		useMulticast = config.getBoolean("updateAll.useMulticast", false);
		mcastDelegate.setUpdateAllSleepTime(config.getLong("updateAll.sleep", 20));
		
		if (config.configurationsAt("baseStation").isEmpty())
		{
			baseStation = new BaseStation();
		} else
		{
			baseStation = new BaseStation(config.configurationAt("baseStation"));
		}
		
		final List<?> bots = config.configurationsAt("bots.bot");
		for (final Object name : bots)
		{
			final SubnodeConfiguration botConfig = (SubnodeConfiguration) name;
			
			try
			{
				final ABot bot = BotFactory.createBot(botConfig);
				
				botTable.put(bot.getBotID(), bot);
				notifyBotAdded(bot);
				
			} catch (final BotInitException err)
			{
				log.error("Error while instantiating bot from '" + config.getFileName() + "': " + err.getMessage(), err);
				removeAllBots();
				return false;
			}
		}
		
		return true;
	}
	
	
	private XMLConfiguration saveConfiguration()
	{
		final CombinedConfiguration botmanager = new CombinedConfiguration();
		
		botmanager.addConfiguration(mcastDelegate.getConfig(), "multicast", "multicast");
		
		botmanager.addConfiguration(baseStation.getConfig(), "baseStation", "baseStation");
		
		botmanager.addProperty("updateAll.useMulticast", useMulticast);
		botmanager.addProperty("updateAll.sleep", mcastDelegate.getUpdateAllSleepTime());
		
		final List<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
		
		final List<BotID> sortedBots = new ArrayList<BotID>();
		sortedBots.addAll(botTable.keySet());
		Collections.sort(sortedBots);
		
		for (final BotID i : sortedBots)
		{
			final ABot bot = botTable.get(i);
			nodes.add(bot.getConfiguration().getRootNode().getChild(0));
		}
		
		botmanager.addNodes("bots", nodes);
		
		final XMLConfiguration config = new XMLConfiguration(botmanager);
		config.setRootElementName("botmanager");
		config.setEncoding(XML_ENCODING);
		return config;
	}
	
	
	private void stopBots()
	{
		for (final ABot bot : botTable.values())
		{
			bot.stop();
			
			if ((bot.getType() == EBotType.TIGER) || (bot.getType() == EBotType.GRSIM))
			{
				((TigerBot) bot).setMulticastDelegate(null);
			}
			
			if (bot.getType() == EBotType.TIGER_V2)
			{
				((TigerBotV2) bot).setBaseStation(null);
			}
		}
	}
	
	
	private void startBots()
	{
		for (final ABot bot : botTable.values())
		{
			if ((bot.getType() == EBotType.TIGER) || (bot.getType() == EBotType.GRSIM))
			{
				((TigerBot) bot).setMulticastDelegate(mcastDelegate);
			}
			
			if (bot.getType() == EBotType.TIGER_V2)
			{
				((TigerBotV2) bot).setBaseStation(baseStation);
			}
			
			bot.start();
		}
	}
	
	
	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		mcastDelegate.setOnFieldBots(wf.tigerBotsVisible.keySet());
	}
	
	
	@Override
	public void onVisionSignalLost(WorldFrame emptyWf)
	{
		mcastDelegate.setOnFieldBots(new HashSet<BotID>());
	}
	
	
	private final class ConfigClient extends AConfigClient
	{
		private ConfigClient()
		{
			super("BotManager Config", ABotManager.BOTMANAGER_CONFIG_PATH, ABotManager.KEY_BOTMANAGER_CONFIG,
					ABotManager.VALUE_BOTMANAGER_CONFIG, false);
		}
		
		
		@Override
		public void onLoad(Configuration newConfig)
		{
			loadConfig((XMLConfiguration) newConfig);
		}
		
		
		@Override
		public XMLConfiguration prepareConfigForSaving(XMLConfiguration loadedConfig)
		{
			return saveConfiguration();
		}
	}
}
