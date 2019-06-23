/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.EBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStationObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.BotSkillFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationEthStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationWifiStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;
import edu.dhbw.mannheim.tigers.sumatra.util.config.EConfigurableCat;


/**
 * New botManager 2015
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotManagerV2 extends ABotManager
{
	private static final Logger									log							= Logger.getLogger(BotManagerV2.class
																													.getName());
	private final ConfigChangedListener							configChangedListener	= new ConfigChangedListener();
	
	private boolean													autoCharge					= true;
	
	private final Map<EBaseStation, BaseStation>				baseStations				= new EnumMap<>(EBaseStation.class);
	private final Map<EBaseStation, BasestationObserver>	basestationObservers		= new EnumMap<>(EBaseStation.class);
	
	
	/**
	 * Setup properties.
	 * 
	 * @param subnodeConfiguration Properties for module-configuration
	 */
	public BotManagerV2(final SubnodeConfiguration subnodeConfiguration)
	{
		autoCharge = Boolean.valueOf(SumatraModel.getInstance().getUserProperty(
				BotManagerV2.class.getName() + ".autoCharge", String.valueOf(false)));
		setBotType(EBotType.TIGER_V3);
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void startModule()
	{
		BotSkillFactory.getInstance().loadSkills();
		CommandFactory.getInstance().loadCommands();
		
		// ConfigRegistration.applySpezis(EBotType.GRSIM.name());
		ConfigRegistration.applySpezis(EBotType.TIGER_V3.name());
		
		for (EBaseStation ebs : EBaseStation.values())
		{
			BaseStation bs = new BaseStation();
			ConfigRegistration.applySpezis(bs, EConfigurableCat.BOTMGR, "");
			ConfigRegistration.applySpezis(bs, EConfigurableCat.BOTMGR, ebs.name());
			baseStations.put(ebs, bs);
			BasestationObserver bso = new BasestationObserver(bs);
			basestationObservers.put(ebs, bso);
			bs.addObserver(bso);
			bs.connect();
		}
		
		ConfigRegistration.registerConfigurableCallback(EConfigurableCat.BOTMGR, configChangedListener);
		
		
		GlobalShortcuts.register(EShortcut.CHARGE_ALL_BOTS, new Runnable()
		{
			@Override
			public void run()
			{
				chargeAll();
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
	public void stopModule()
	{
		ConfigRegistration.unregisterConfigurableCallback(EConfigurableCat.BOTMGR, configChangedListener);
		GlobalShortcuts.unregisterAll(EShortcut.CHARGE_ALL_BOTS);
		GlobalShortcuts.unregisterAll(EShortcut.DISCHARGE_ALL_BOTS);
		for (Map.Entry<EBaseStation, BaseStation> entry : baseStations.entrySet())
		{
			BaseStation baseStation = entry.getValue();
			baseStation.disconnect();
			baseStation.removeObserver(basestationObservers.remove(entry.getKey()));
		}
		if (!basestationObservers.isEmpty())
		{
			log.warn("BS Observers left?!");
			basestationObservers.clear();
		}
		getBotTable().clear();
	}
	
	
	@Override
	public void chargeAll()
	{
		for (final ABot bot : getAllBots().values())
		{
			bot.execute(new TigerKickerChargeAuto(bot.getKickerMaxCap()));
		}
		autoCharge = true;
		SumatraModel.getInstance().setUserProperty(BotManagerV2.class.getName() + ".autoCharge",
				String.valueOf(autoCharge));
	}
	
	
	@Override
	public void dischargeAll()
	{
		for (final ABot bot : getAllBots().values())
		{
			bot.execute(new TigerKickerChargeAuto(0));
		}
		autoCharge = false;
		SumatraModel.getInstance().setUserProperty(BotManagerV2.class.getName() + ".autoCharge",
				String.valueOf(autoCharge));
	}
	
	
	@Override
	@Deprecated
	public ABot addBot(final EBotType type, final BotID id, final String name)
	{
		throw new IllegalAccessError("this method is obsolete");
	}
	
	
	private ABot addBot(final EBotType type, final BotID id, final BaseStation baseStation)
	{
		assert !getBotTable().containsKey(id) : "Tried to add a bot, but one with id " + id + " already exists";
		
		ABot bot = new TigerBotV3(id, baseStation);
		getBotTable().put(id, bot);
		bot.setDefaultKickerMaxCap();
		bot.start();
		if (autoCharge)
		{
			bot.execute(new TigerKickerChargeAuto(bot.getKickerMaxCap()));
		}
		
		notifyBotAdded(bot);
		return bot;
	}
	
	
	@Override
	public void botConnectionChanged(final ABot bot)
	{
		// obsolete
	}
	
	
	@Override
	public void removeBot(final BotID id)
	{
		ABot bot = getBotTable().remove(id);
		if (bot == null)
		{
			log.warn("Tried to remove a non-existing bot with id " + id);
		} else
		{
			bot.stop();
			notifyBotRemoved(bot);
		}
	}
	
	
	@Override
	public Map<BotID, ABot> getAllBots()
	{
		return Collections.unmodifiableMap(getBotTable());
	}
	
	
	@Override
	public Map<EBaseStation, BaseStation> getBaseStations()
	{
		return baseStations;
	}
	
	
	private class BasestationObserver implements IBaseStationObserver
	{
		private final BaseStation	baseStation;
		
		
		/**
		 * @param baseStation
		 */
		public BasestationObserver(final BaseStation baseStation)
		{
			this.baseStation = baseStation;
		}
		
		
		@Override
		public void onIncommingBotCommand(final BotID id, final ACommand command)
		{
			for (ABot bot : getAllBots().values())
			{
				bot.onIncommingBotCommand(id, command);
			}
		}
		
		
		@Override
		public void onIncommingBaseStationCommand(final ACommand command)
		{
		}
		
		
		@Override
		public void onNewBaseStationStats(final BaseStationStats stats)
		{
		}
		
		
		@Override
		public void onNewBaseStationWifiStats(final BaseStationWifiStats stats)
		{
		}
		
		
		@Override
		public void onNewBaseStationEthStats(final BaseStationEthStats stats)
		{
		}
		
		
		@Override
		public void onNetworkStateChanged(final ENetworkState netState)
		{
		}
		
		
		@Override
		public void onNewPingDelay(final float delay)
		{
		}
		
		
		@Override
		public void onBotOffline(final BotID id)
		{
			ABot bot = getBotTable().get(id);
			if (bot != null)
			{
				bot.stop();
				removeBot(id);
			}
		}
		
		
		@Override
		public void onBotOnline(final BotID id)
		{
			if (!getBotTable().containsKey(id))
			{
				addBot(EBotType.TIGER_V3, id, baseStation);
			} else
			{
				log.warn("Bot came online, but we already have it?!");
			}
		}
	}
	
	private class ConfigChangedListener implements IConfigObserver
	{
		@Override
		public void onLoad(final HierarchicalConfiguration newConfig)
		{
		}
		
		
		@Override
		public void onReload(final HierarchicalConfiguration freshConfig)
		{
			for (Map.Entry<EBaseStation, BaseStation> entry : baseStations.entrySet())
			{
				BaseStation bs = entry.getValue();
				EBaseStation ebs = entry.getKey();
				bs.disconnect();
				ConfigRegistration.applySpezis(bs, EConfigurableCat.BOTMGR, "");
				ConfigRegistration.applySpezis(bs, EConfigurableCat.BOTMGR, ebs.name());
				bs.connect();
			}
			
			for (ABot bot : getAllBots().values())
			{
				bot.setDefaultKickerMaxCap();
			}
		}
	}
	
	
	@Override
	@Deprecated
	public void changeBotId(final BotID oldId, final BotID newId)
	{
		throw new IllegalAccessError("this method is obsolete");
	}
	
	
	@Override
	@Deprecated
	public void setUseMulticast(final boolean enable)
	{
		throw new IllegalAccessError("this method is obsolete");
	}
	
	
	@Override
	@Deprecated
	public boolean getUseMulticast()
	{
		throw new IllegalAccessError("this method is obsolete");
	}
	
	
	@Override
	@Deprecated
	public void setUpdateAllSleepTime(final long time)
	{
		throw new IllegalAccessError("this method is obsolete");
	}
	
	
	@Override
	@Deprecated
	public long getUpdateAllSleepTime()
	{
		throw new IllegalAccessError("this method is obsolete");
	}
	
	
	@Override
	@Deprecated
	public ITransceiverUDP getMulticastTransceiver(final int mcastDelegateKey)
	{
		throw new IllegalAccessError("this method is obsolete");
	}
}
