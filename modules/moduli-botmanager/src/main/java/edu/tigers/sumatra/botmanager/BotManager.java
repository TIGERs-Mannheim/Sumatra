/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.IBaseStationObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.BotSkillFactory;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationCameraViewport;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.BotParamsProvider;
import edu.tigers.sumatra.botparams.NoBotParamsProvider;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IVisionFilterObserver;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.util.IRobotInfoProvider;


/**
 * New botManager 2015
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotManager extends ABotManager implements IConfigObserver, IVisionFilterObserver
{
	private static final Logger log = Logger.getLogger(BotManager.class.getName());
	private static final String PROP_AUTO_CHARGE = BotManager.class.getName() + ".autoCharge";
	private boolean autoCharge = true;
	
	private final Map<BotID, ABot> botTable = new ConcurrentSkipListMap<>(BotID.getComparator());
	private final List<IBaseStation> baseStations = new ArrayList<>();
	private final List<BaseStationObserver> baseStationObservers = new ArrayList<>();
	
	
	@Override
	public void initModule()
	{
		autoCharge = Boolean.valueOf(SumatraModel.getInstance().getUserProperty(
				PROP_AUTO_CHARGE, String.valueOf(true)));
		
		String[] bsImplsArr = getSubnodeConfiguration().getStringArray("basestation-impl");
		for (String impl : bsImplsArr)
		{
			try
			{
				Class<?> clazz = Class.forName(impl);
				Object bsObj = clazz.newInstance();
				addBasestation((IBaseStation) bsObj);
			} catch (ClassNotFoundException e)
			{
				log.error("Could not find basestation class: " + impl, e);
			} catch (InstantiationException | IllegalAccessException e)
			{
				log.error("Could not create basestation: " + impl, e);
			} catch (ClassCastException e)
			{
				log.error("Invalid basestation class: " + impl, e);
			}
		}
	}
	
	
	@Override
	public void deinitModule()
	{
		// empty
	}
	
	
	@Override
	public void startModule()
	{
		BotSkillFactory.getInstance().loadSkills();
		CommandFactory.getInstance().loadCommands();
		for (IBaseStation baseStation : baseStations)
		{
			BaseStationObserver bso = new BaseStationObserver();
			baseStationObservers.add(bso);
			baseStation.addObserver(bso);
			baseStation.connect();
		}
		
		try
		{
			AVisionFilter visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
			visionFilter.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.debug("No VisionFilter found. Viewports won't be forwared.", e);
		}
		
		IRobotInfoProvider robotInfoProvider = new RobotInfoProvider(this, getBotParamsProvider());
		try
		{
			SumatraModel.getInstance().getModule(AWorldPredictor.class).setRobotInfoProvider(robotInfoProvider);
		} catch (ModuleNotFoundException e)
		{
			log.warn("Could not find WP module. Can not set robot info provider", e);
		}
		
		ConfigRegistration.registerConfigurableCallback("botmgr", this);
	}
	
	
	private BotParamsProvider getBotParamsProvider()
	{
		if (SumatraModel.getInstance().isModuleLoaded(BotParamsManager.class))
		{
			return SumatraModel.getInstance().getModule(BotParamsManager.class);
		}
		return new NoBotParamsProvider();
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AVisionFilter visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
			visionFilter.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.debug("No VisionFilter found. Viewports won't be forwared.", e);
		}
		
		for (IBaseStation baseStation : baseStations)
		{
			baseStation.disconnect();
			for (IBaseStationObserver obs : baseStationObservers)
			{
				baseStation.removeObserver(obs);
			}
		}
		baseStationObservers.clear();
		Collection<ABot> bots = new ArrayList<>(botTable.values());
		for (IBot bot : bots)
		{
			removeBot(bot.getBotId());
		}
		ConfigRegistration.unregisterConfigurableCallback("botmgr", this);
	}
	
	
	@Override
	public void chargeAll()
	{
		for (final ABot bot : botTable.values())
		{
			bot.getMatchCtrl().setKickerAutocharge(true);
		}
		setAutoCharge(true);
	}
	
	
	@Override
	public void dischargeAll()
	{
		for (final ABot bot : botTable.values())
		{
			bot.getMatchCtrl().setKickerAutocharge(false);
		}
		setAutoCharge(false);
	}
	
	
	private void setAutoCharge(final boolean autoCharge)
	{
		this.autoCharge = autoCharge;
		SumatraModel.getInstance().setUserProperty(PROP_AUTO_CHARGE,
				String.valueOf(autoCharge));
	}
	
	
	@Override
	public void removeBot(final BotID id)
	{
		ABot bot = botTable.remove(id);
		if (bot == null)
		{
			log.warn("Tried to remove a non-existing bot with id " + id);
		} else
		{
			notifyBotRemoved(bot);
		}
	}
	
	
	@Override
	public void addBasestation(final IBaseStation bs)
	{
		baseStations.add(bs);
	}
	
	
	@Override
	public Map<BotID, ABot> getBots()
	{
		return Collections.unmodifiableMap(botTable);
	}
	
	
	@Override
	public final List<IBaseStation> getBaseStations()
	{
		return baseStations;
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		for (IBaseStation bs : baseStations)
		{
			bs.afterApply(configClient);
		}
		for (ABot bot : botTable.values())
		{
			bot.afterApply(configClient);
		}
	}
	
	
	@Override
	public Optional<ABot> getBot(final BotID botID)
	{
		return Optional.ofNullable(getBots().get(botID));
	}
	
	private class BaseStationObserver implements IBaseStationObserver
	{
		@Override
		public void onIncomingBotCommand(final BotID id, final ACommand command)
		{
			ABot bot = botTable.get(id);
			if (bot != null)
			{
				bot.onIncomingBotCommand(command);
				notifyIncomingBotCommand(botTable.get(id), command);
			}
		}
		
		
		@Override
		public void onBotOffline(final BotID id)
		{
			ABot bot = botTable.get(id);
			log.debug("Bot is offline: " + bot);
			if (bot != null)
			{
				bot.stop();
				removeBot(id);
			}
		}
		
		
		@Override
		public void onBotOnline(final ABot bot)
		{
			log.debug("Bot is online: " + bot);
			if (!botTable.containsKey(bot.getBotId()))
			{
				botTable.put(bot.getBotId(), bot);
				bot.getMatchCtrl().setKickerAutocharge(autoCharge);
				bot.start();
				updateColorOfAllRobotsToMajority(bot);
				notifyBotAdded(bot);
			} else
			{
				log.warn("Bot came online, but we already have it: " + bot, new Exception());
			}
		}
		
		
		private void updateColorOfAllRobotsToMajority(final ABot bot)
		{
			if (SumatraModel.getInstance().isProductive())
			{
				long numY = botTable.values().stream().map(b -> b.getBotId().getTeamColor())
						.filter(tc -> tc.equals(ETeamColor.YELLOW)).count();
				long numB = botTable.size() - numY;
				String command = null;
				if (numY > numB)
				{
					if (bot.getBotId().getTeamColor().equals(ETeamColor.BLUE))
					{
						command = "color y";
					}
				} else
				{
					if (bot.getBotId().getTeamColor().equals(ETeamColor.YELLOW))
					{
						command = "color b";
					}
				}
				if (command != null)
				{
					TigerSystemConsoleCommand cmd = new TigerSystemConsoleCommand(ConsoleCommandTarget.MAIN, command);
					bot.execute(cmd);
				}
			}
		}
	}
	
	
	@Override
	public void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		// not used
	}
	
	
	@Override
	public void onViewportUpdated(final int cameraId, final IRectangle viewport)
	{
		BaseStationCameraViewport cmd = new BaseStationCameraViewport(cameraId, viewport);
		baseStations.forEach(b -> b.enqueueCommand(cmd));
	}
}

