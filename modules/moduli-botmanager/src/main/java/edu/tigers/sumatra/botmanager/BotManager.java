/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.IBaseStationObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.BotSkillFactory;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * New botManager 2015
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotManager extends ABotManager implements IConfigObserver
{
	private static final Logger log = Logger.getLogger(BotManager.class
			.getName());
	private static final String PROP_AUTO_CHARGE = BotManager.class.getName() + ".autoCharge";
	private boolean autoCharge = true;
	
	private final List<IBaseStation> baseStations = new ArrayList<>();
	private final List<BasestationObserver> basestationObservers = new ArrayList<>();
	
	
	/**
	 * Setup properties.
	 * 
	 * @param subnodeConfiguration Properties for module-configuration
	 */
	public BotManager(final SubnodeConfiguration subnodeConfiguration)
	{
		autoCharge = Boolean.valueOf(SumatraModel.getInstance().getUserProperty(
				PROP_AUTO_CHARGE, String.valueOf(true)));
		
		String[] bsImplsArr = subnodeConfiguration.getStringArray("basestation-impl");
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
	public void initModule() throws InitModuleException
	{
		// empty
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
			BasestationObserver bso = new BasestationObserver();
			basestationObservers.add(bso);
			baseStation.addObserver(bso);
			baseStation.connect();
		}
		
		ConfigRegistration.registerConfigurableCallback("botmgr", this);
	}
	
	
	@Override
	public void stopModule()
	{
		for (IBaseStation baseStation : baseStations)
		{
			baseStation.disconnect();
			for (IBaseStationObserver obs : basestationObservers)
			{
				baseStation.removeObserver(obs);
			}
		}
		basestationObservers.clear();
		Collection<ABot> bots = new ArrayList<>(getBotTable().values());
		for (IBot bot : bots)
		{
			removeBot(bot.getBotId());
		}
		ConfigRegistration.unregisterConfigurableCallback("botmgr", this);
	}
	
	
	@Override
	public void chargeAll()
	{
		for (final ABot bot : getAllBots().values())
		{
			bot.getMatchCtrl().setKickerAutocharge(true);
		}
		autoCharge = true;
		SumatraModel.getInstance().setUserProperty(PROP_AUTO_CHARGE,
				String.valueOf(autoCharge));
	}
	
	
	@Override
	public void dischargeAll()
	{
		for (final ABot bot : getAllBots().values())
		{
			bot.getMatchCtrl().setKickerAutocharge(false);
		}
		autoCharge = false;
		SumatraModel.getInstance().setUserProperty(PROP_AUTO_CHARGE,
				String.valueOf(autoCharge));
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
			notifyBotRemoved(bot);
		}
	}
	
	
	@Override
	public void addBasestation(final IBaseStation bs)
	{
		baseStations.add(bs);
	}
	
	
	@Override
	public Map<BotID, ABot> getAllBots()
	{
		return Collections.unmodifiableMap(getBotTable());
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
		for (ABot bot : getBotTable().values())
		{
			bot.afterApply(configClient);
		}
	}
	
	private class BasestationObserver implements IBaseStationObserver
	{
		
		
		@Override
		public void onIncommingBotCommand(final BotID id, final ACommand command)
		{
			for (ABot bot : getAllBots().values())
			{
				if (id.equals(bot.getBotId()))
				{
					bot.onIncommingBotCommand(command);
				}
			}
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
		public void onBotOnline(final ABot bot)
		{
			if (!getBotTable().containsKey(bot.getBotId()))
			{
				getBotTable().put(bot.getBotId(), bot);
				bot.getMatchCtrl().setKickerAutocharge(autoCharge);
				bot.start();
				updateColorOfAllRobotsToMajority(bot);
				notifyBotAdded(bot);
			} else
			{
				log.warn("Bot came online, but we already have it: " + bot, new Exception());
			}
		}
		
		
		private void updateColorOfAllRobotsToMajority(ABot bot)
		{
			
			if (SumatraModel.getInstance().isProductive())
			{
				long numY = getBotTable().values().stream().map(b -> b.getBotId().getTeamColor())
						.filter(tc -> tc.equals(ETeamColor.YELLOW)).count();
				long numB = getBotTable().size() - numY;
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
}

