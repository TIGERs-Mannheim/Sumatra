/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): Gero, AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.EBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IRobotsPanelObserver;


/**
 * A module that is capable of managing and controlling all our BattleMechs! =)
 * 
 * @author Gero
 */
public abstract class ABotManager extends AModule implements IRobotsPanelObserver
{
	private static final Logger		log							= Logger.getLogger(ABotManager.class.getName());
	
	/** */
	public static final String			MODULE_TYPE					= "ABotManager";
	/** */
	public static final String			MODULE_ID					= "botmanager";
	
	
	/** */
	public static final String			KEY_BOTMANAGER_CONFIG	= ABotManager.class.getName() + ".botmanagerConfig";
	/** */
	public static final String			BOTMANAGER_CONFIG_PATH	= "./config/botmanager/";
	/**  */
	public static final String			VALUE_BOTMANAGER_CONFIG	= "botmanager_sumatra.xml";
	
	
	private final Map<BotID, ABot>	botTable						= new ConcurrentSkipListMap<BotID, ABot>(
																						BotID.getComparator());
	/**  */
	public static final String			BLOCKED_BY_SUMATRA		= "Blocked by Sumatra";
	
	private EBotType						botType						= EBotType.UNKNOWN;
	
	
	/**
	 * Be aware of the fact that this method might be called from several threads
	 * 
	 * @param id
	 * @param cmd
	 */
	public void execute(final BotID id, final ACommand cmd)
	{
		final ABot bot = getBotTable().get(id);
		if (bot != null)
		{
			bot.execute(cmd);
		} else
		{
			log.warn("Execute: Invalid botID: " + id);
		}
	}
	
	
	@Override
	public void onHideBotFromAiClicked(final BotID botId, final boolean hide)
	{
		ABot bot = botTable.get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else
		{
			bot.setHideFromAi(hide);
		}
	}
	
	
	@Override
	public void onHideBotFromRcmClicked(final BotID botId, final boolean hide)
	{
		ABot bot = botTable.get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else
		{
			bot.setHideFromRcm(hide);
		}
	}
	
	
	@Override
	public void onDisableBotClicked(final BotID botId, final boolean disable)
	{
		ABot bot = botTable.get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else if (disable)
		{
			bot.setControlledBy(BLOCKED_BY_SUMATRA);
		} else
		{
			bot.setControlledBy("");
		}
	}
	
	
	@Override
	public void onCharge(final BotID botId)
	{
		ABot bot = botTable.get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else
		{
			bot.execute(new TigerKickerChargeAuto(bot.getKickerMaxCap()));
		}
	}
	
	
	@Override
	public void onDischarge(final BotID botId)
	{
		ABot bot = botTable.get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else
		{
			bot.execute(new TigerKickerChargeAuto(0));
		}
	}
	
	
	/**
	 * @param type
	 * @param id
	 * @param name
	 * @return
	 */
	public abstract ABot addBot(EBotType type, BotID id, String name);
	
	
	/**
	 * @param bot
	 */
	public abstract void botConnectionChanged(ABot bot);
	
	
	/**
	 * @param id
	 */
	public abstract void removeBot(BotID id);
	
	
	/**
	 * @param oldId
	 * @param newId
	 */
	public abstract void changeBotId(BotID oldId, BotID newId);
	
	
	/**
	 * @return
	 */
	public abstract Map<BotID, ABot> getAllBots();
	
	
	/**
	 * @param enable
	 */
	public abstract void setUseMulticast(boolean enable);
	
	
	/**
	 * @return
	 */
	public abstract boolean getUseMulticast();
	
	
	/**
	 * @param time
	 */
	public abstract void setUpdateAllSleepTime(long time);
	
	
	/**
	 * @return
	 */
	public abstract long getUpdateAllSleepTime();
	
	
	/**
	 * @param mcastDelegateKey
	 * @return
	 */
	public abstract ITransceiverUDP getMulticastTransceiver(int mcastDelegateKey);
	
	
	/**
	 * @return
	 */
	public abstract Map<EBaseStation, BaseStation> getBaseStations();
	
	
	private final List<IBotManagerObserver>	observers	= new ArrayList<IBotManagerObserver>();
	
	
	/**
	 */
	public abstract void chargeAll();
	
	
	/**
	 * 
	 */
	public abstract void dischargeAll();
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param o
	 */
	public void addObserver(final IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	
	
	protected void notifyBotConnectionChanged(final ABot bot)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver o : observers)
			{
				o.onBotConnectionChanged(bot);
			}
		}
	}
	
	
	protected void notifyBotAdded(final ABot bot)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver o : observers)
			{
				o.onBotAdded(bot);
			}
		}
	}
	
	
	protected void notifyBotRemoved(final ABot bot)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver o : observers)
			{
				o.onBotRemoved(bot);
			}
		}
	}
	
	
	protected void notifyBotIdChanged(final BotID oldId, final BotID newId)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver observer : observers)
			{
				observer.onBotIdChanged(oldId, newId);
			}
		}
	}
	
	
	/**
	 * @return the botTable
	 */
	public Map<BotID, ABot> getBotTable()
	{
		return botTable;
	}
	
	
	/**
	 * @return the botType
	 */
	public final EBotType getBotType()
	{
		return botType;
	}
	
	
	/**
	 * @param botType the botType to set
	 */
	protected final void setBotType(final EBotType botType)
	{
		this.botType = botType;
	}
}
