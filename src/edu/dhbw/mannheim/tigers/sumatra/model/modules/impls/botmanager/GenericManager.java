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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.BotFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.BotInitException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.GrSimBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim.GrSimNetworkCfg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.AConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;


/**
 * The one and only generic botmanager.
 * 
 * @author AndreR
 */
public class GenericManager extends ABotManager implements IWorldPredictorObserver
{
	// --------------------------------------------------------------
	// --- instance-variables ---------------------------------------
	// --------------------------------------------------------------
	// Logger
	private static final Logger					log				= Logger.getLogger(GenericManager.class.getName());
	
	// "UTF-8";
	private static final String					XML_ENCODING	= "ISO-8859-1";
	private final Map<BotID, ABot>				botTable			= new ConcurrentSkipListMap<BotID, ABot>(
																						BotID.getComparator());
	private boolean									moduleRunning	= false;
	private AWorldPredictor							wp					= null;
	
	private Map<Integer, MulticastDelegate>	mcastDelegate	= new HashMap<Integer, MulticastDelegate>();
	private boolean									useMulticast	= false;
	private Map<Integer, BaseStation>			baseStations	= new HashMap<Integer, BaseStation>();
	private Map<Integer, GrSimNetworkCfg>		grSimNetwork	= new HashMap<Integer, GrSimNetworkCfg>();
	
	private static IConfigClient					configClient	= new ConfigClient();
	private static HierarchicalConfiguration	config;
	
	private boolean									autoCharge;
	private int											maxCap;
	
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	/**
	 * Setup properties.
	 * 
	 * @param subnodeConfiguration Properties for module-configuration
	 */
	public GenericManager(final SubnodeConfiguration subnodeConfiguration)
	{
		autoCharge = subnodeConfiguration.getBoolean("autoChargeInitially");
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
	}
	
	
	@Override
	public void startModule()
	{
		CommandFactory.getInstance().loadCommands();
		
		if (config == null)
		{
			ConfigManager.getInstance().reloadConfig(KEY_BOTMANAGER_CONFIG);
		}
		
		loadConfig(config);
		
		if (!botTable.isEmpty())
		{
			AIConfig.setBotSpezi(botTable.values().iterator().next().getType().name());
		}
		
		for (MulticastDelegate md : mcastDelegate.values())
		{
			md.enable(useMulticast);
		}
		for (BaseStation bs : baseStations.values())
		{
			bs.connect();
		}
		
		moduleRunning = true;
		
		startBots();
		
		if (autoCharge)
		{
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
					"AutoCharge"));
			executor.schedule(new Runnable()
			{
				
				@Override
				public void run()
				{
					chargeAll(maxCap);
				}
			}, 2, TimeUnit.SECONDS);
			executor.shutdown();
		}
		
		GlobalShortcuts.register(EShortcut.CHARGE_ALL_BOTS, new Runnable()
		{
			
			@Override
			public void run()
			{
				chargeAll(maxCap);
			}
		});
		
		GlobalShortcuts.register(EShortcut.DISCHARGE_ALL_BOTS, new Runnable()
		{
			
			@Override
			public void run()
			{
				dischargeAll();
			}
		});
	}
	
	
	@Override
	public void chargeAll(final int chg)
	{
		log.info("Auto charging bots");
		maxCap = chg;
		for (final ABot bot : getAllBots().values())
		{
			bot.execute(new TigerKickerChargeAuto(maxCap));
		}
		autoCharge = true;
	}
	
	
	@Override
	public void dischargeAll()
	{
		log.info("Discharging bots");
		for (final ABot bot : getAllBots().values())
		{
			bot.execute(new TigerKickerChargeAuto(0));
		}
		autoCharge = false;
	}
	
	
	@Override
	public void stopModule()
	{
		ConfigManager.getInstance().saveConfig(KEY_BOTMANAGER_CONFIG);
		removeAllBots();
		for (BaseStation bs : baseStations.values())
		{
			bs.disconnect();
		}
		for (MulticastDelegate md : mcastDelegate.values())
		{
			md.enable(false);
		}
		
		baseStations.clear();
		mcastDelegate.clear();
		
		moduleRunning = false;
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	/**
	 * Be aware of the fact that this method might be called from several threads
	 */
	@Override
	public void execute(final BotID id, final ACommand cmd)
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
	public void botConnectionChanged(final ABot bot)
	{
		notifyBotConnectionChanged(bot);
		
		if ((bot.getNetworkState() == ENetworkState.ONLINE) && autoCharge)
		{
			bot.setKickerMaxCap(maxCap);
			bot.execute(new TigerKickerChargeAuto(maxCap));
		}
	}
	
	
	@Override
	public ABot addBot(final EBotType type, final BotID id, final String name)
	{
		if (botTable.containsKey(id))
		{
			return null;
		}
		
		final ABot bot = BotFactory.createBot(type, id, name);
		botTable.put(id, bot);
		
		if ((bot.getType() == EBotType.TIGER))
		{
			((TigerBot) bot).setMulticastDelegate(mcastDelegate.get(bot.getMcastDelegateKey()));
		}
		
		if (bot.getType() == EBotType.TIGER_V2)
		{
			((TigerBotV2) bot).setBaseStation(baseStations.get(bot.getBaseStationKey()));
		}
		
		notifyBotAdded(bot);
		
		return bot;
	}
	
	
	@Override
	public void removeBot(final BotID id)
	{
		final ABot bot = botTable.remove(id);
		if (bot != null)
		{
			if ((bot.getType() == EBotType.TIGER))
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
	public void changeBotId(final BotID oldId, final BotID newId)
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
	
	
	private void removeAllBots()
	{
		stopBots();
		
		for (final ABot bot : botTable.values())
		{
			notifyBotRemoved(bot);
		}
		
		botTable.clear();
	}
	
	
	private void loadConfig(final HierarchicalConfiguration config)
	{
		readConfiguration(config);
		
		if (moduleRunning)
		{
			for (MulticastDelegate md : mcastDelegate.values())
			{
				md.enable(useMulticast);
			}
			for (BaseStation bs : baseStations.values())
			{
				bs.connect();
			}
			
			startBots();
		}
	}
	
	
	@Override
	public ITransceiverUDP getMulticastTransceiver(final int mcastDelegateKey)
	{
		MulticastDelegate md = mcastDelegate.get(mcastDelegateKey);
		if (md != null)
		{
			return md.getTransceiver();
		}
		return null;
	}
	
	
	@Override
	public void setUseMulticast(final boolean enable)
	{
		useMulticast = enable;
		for (MulticastDelegate md : mcastDelegate.values())
		{
			md.enable(enable);
		}
	}
	
	
	@Override
	public boolean getUseMulticast()
	{
		return useMulticast;
	}
	
	
	@Override
	public void setUpdateAllSleepTime(final long time)
	{
		for (MulticastDelegate md : mcastDelegate.values())
		{
			md.setUpdateAllSleepTime(time);
		}
	}
	
	
	@Override
	public long getUpdateAllSleepTime()
	{
		return mcastDelegate.get(0) != null ? mcastDelegate.get(0).getUpdateAllSleepTime() : -1;
	}
	
	
	@Override
	public Map<Integer, BaseStation> getBaseStations()
	{
		return baseStations;
	}
	
	
	private boolean readConfiguration(final HierarchicalConfiguration config)
	{
		removeAllBots();
		
		final List<?> mcastConfigs = config.configurationsAt("multicast");
		for (final Object name : mcastConfigs)
		{
			final SubnodeConfiguration mcastConfig = (SubnodeConfiguration) name;
			int key = mcastConfig.getInt("[@id]");
			MulticastDelegate md = new MulticastDelegate(mcastConfig, this);
			mcastDelegate.put(key, md);
		}
		
		useMulticast = config.getBoolean("updateAll.useMulticast", false);
		
		final List<?> baseStationConfigs = config.configurationsAt("baseStation");
		for (final Object name : baseStationConfigs)
		{
			final SubnodeConfiguration baseStationConfig = (SubnodeConfiguration) name;
			int key = baseStationConfig.getInt("[@id]");
			baseStations.put(key, new BaseStation(baseStationConfig));
		}
		
		final List<?> grSimConfigs = config.configurationsAt("grSimNetwork");
		for (final Object name : grSimConfigs)
		{
			final SubnodeConfiguration grSimConfig = (SubnodeConfiguration) name;
			int key = grSimConfig.getInt("[@id]");
			grSimNetwork.put(key, new GrSimNetworkCfg(grSimConfig));
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
				log.error("Error while instantiating bot", err);
				removeAllBots();
				return false;
			}
		}
		
		return true;
	}
	
	
	private XMLConfiguration saveConfiguration()
	{
		final CombinedConfiguration botmanager = new CombinedConfiguration();
		
		{
			// save multicast config
			final List<ConfigurationNode> mcNodes = new ArrayList<ConfigurationNode>();
			for (MulticastDelegate md : mcastDelegate.values())
			{
				mcNodes.add(md.getConfig().getRootNode());
			}
			botmanager.addNodes("", mcNodes);
		}
		
		{
			// save base station config
			final List<ConfigurationNode> bsNodes = new ArrayList<ConfigurationNode>();
			for (BaseStation baseStation : baseStations.values())
			{
				bsNodes.add(baseStation.getConfig().getRootNode());
			}
			botmanager.addNodes("", bsNodes);
		}
		
		botmanager.addProperty("updateAll.useMulticast", useMulticast);
		
		{
			// save grSim config
			final List<ConfigurationNode> grsnNodes = new ArrayList<ConfigurationNode>();
			for (GrSimNetworkCfg grsn : grSimNetwork.values())
			{
				grsnNodes.add(grsn.getConfig().getRootNode());
			}
			botmanager.addNodes("", grsnNodes);
		}
		
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
		
		final XMLConfiguration xmlconfig = new XMLConfiguration(botmanager);
		xmlconfig.setRootElementName("botmanager");
		xmlconfig.setEncoding(XML_ENCODING);
		return xmlconfig;
	}
	
	
	private void stopBots()
	{
		for (final ABot bot : botTable.values())
		{
			bot.stop();
			
			if ((bot.getType() == EBotType.TIGER))
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
			if ((bot.getType() == EBotType.TIGER))
			{
				((TigerBot) bot).setMulticastDelegate(mcastDelegate.get(bot.getMcastDelegateKey()));
			}
			
			if (bot.getType() == EBotType.TIGER_V2)
			{
				((TigerBotV2) bot).setBaseStation(baseStations.get(bot.getBaseStationKey()));
			}
			
			if ((bot.getType() == EBotType.GRSIM))
			{
				((GrSimBot) bot).setGrSimCfg(grSimNetwork.get(bot.getMcastDelegateKey()));
			}
			
			bot.start();
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final SimpleWorldFrame wf)
	{
		for (Map.Entry<Integer, MulticastDelegate> entry : mcastDelegate.entrySet())
		{
			int key = entry.getKey();
			MulticastDelegate md = entry.getValue();
			Set<BotID> botIds = new HashSet<BotID>();
			for (TrackedTigerBot bot : wf.getBots().values())
			{
				if ((bot.getBot() != null) && (bot.getBot().getMcastDelegateKey() == key))
				{
					botIds.add(bot.getId());
				}
			}
			md.setOnFieldBots(botIds);
		}
	}
	
	
	@Override
	public void onManualBotAdded(final BotID botId)
	{
		ABot bot = botTable.get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else
		{
			bot.setManualControl(true);
		}
	}
	
	
	@Override
	public void onManualBotRemoved(final BotID botId)
	{
		ABot bot = botTable.get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else
		{
			bot.setManualControl(false);
		}
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		for (MulticastDelegate md : mcastDelegate.values())
		{
			md.setOnFieldBots(new HashSet<BotID>());
		}
	}
	
	
	private static final class ConfigClient extends AConfigClient
	{
		private ConfigClient()
		{
			super("BotManager Config", ABotManager.BOTMANAGER_CONFIG_PATH, ABotManager.KEY_BOTMANAGER_CONFIG,
					ABotManager.VALUE_BOTMANAGER_CONFIG, false);
		}
		
		
		@Override
		public void onLoad(final HierarchicalConfiguration newConfig)
		{
			config = newConfig;
		}
		
		
		@Override
		public HierarchicalConfiguration prepareConfigForSaving(final HierarchicalConfiguration loadedConfig)
		{
			GenericManager manager;
			try
			{
				manager = (GenericManager) SumatraModel.getInstance().getModule(MODULE_ID);
				XMLConfiguration xmlConfig = manager.saveConfiguration();
				config = null;
				return xmlConfig;
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not find module for BotManager");
			}
			return new XMLConfiguration();
		}
	}
	
	
	/**
	 * @return
	 */
	public static IConfigClient getBotManagerConfigClient()
	{
		return configClient;
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final CamDetectionFrame frame)
	{
	}
}
