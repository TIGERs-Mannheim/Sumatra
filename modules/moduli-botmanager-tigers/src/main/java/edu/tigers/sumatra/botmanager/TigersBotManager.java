/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import static edu.tigers.sumatra.botmanager.commands.ECommand.CMD_SYSTEM_MATCH_FEEDBACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.basestation.ITigersBaseStationObserver;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.ITigerBotObserver;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationCameraViewport;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IVisionFilterObserver;


/**
 * Tigers Bot manager implementation
 */
public class TigersBotManager extends ABotManager implements IVisionFilterObserver, ITigersBaseStationObserver
{
	private static final String PROP_AUTO_CHARGE = TigersBotManager.class.getName() + ".autoCharge";
	private boolean autoCharge = true;

	private final List<ITigerBotObserver> botObservers = new CopyOnWriteArrayList<>();


	@Override
	public void initModule()
	{
		super.initModule();

		CommandFactory.getInstance().loadCommands();

		autoCharge = Boolean.valueOf(SumatraModel.getInstance().getUserProperty(
				PROP_AUTO_CHARGE, String.valueOf(true)));

	}


	@Override
	public void startModule()
	{
		super.startModule();

		getBaseStation().addTigersBsObserver(this);

		AVisionFilter visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
		visionFilter.addObserver(this);
	}


	@Override
	public void stopModule()
	{
		getBaseStation().removeTigersBsObserver(this);

		super.stopModule();

		AVisionFilter visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
		visionFilter.removeObserver(this);

		Collection<ABot> bots = new ArrayList<>(getBots().values());
		for (IBot bot : bots)
		{
			onBotOffline(bot.getBotId());
		}
	}


	public void chargeAll()
	{
		for (final ABot bot : getBots().values())
		{
			bot.getMatchCtrl().setKickerAutocharge(true);
		}
		setAutoCharge(true);
	}


	public void dischargeAll()
	{
		for (final ABot bot : getBots().values())
		{
			bot.getMatchCtrl().setKickerAutocharge(false);
		}
		setAutoCharge(false);
	}


	public void broadcast(ACommand cmd)
	{
		getTigerBots().values().forEach(b -> b.execute(cmd));
	}


	private void setAutoCharge(final boolean autoCharge)
	{
		this.autoCharge = autoCharge;
		SumatraModel.getInstance().setUserProperty(PROP_AUTO_CHARGE,
				String.valueOf(autoCharge));
	}


	@Override
	public TigersBaseStation getBaseStation()
	{
		return (TigersBaseStation) baseStation;
	}


	public Optional<TigerBot> getTigerBot(final BotID botID)
	{
		return getBot(botID).map(b -> (TigerBot) b);
	}


	public Map<BotID, TigerBot> getTigerBots()
	{
		return getBots().values().stream()
				.map(b -> (TigerBot) b)
				.collect(Collectors.toMap(TigerBot::getBotId, Function.identity()));
	}


	@Override
	public void onIncomingBotCommand(final BotID id, final ACommand command)
	{
		getTigerBot(id).ifPresent(bot -> processCommand(command, bot));
	}


	private void processCommand(final ACommand command, final TigerBot bot)
	{
		if (command.getType() == CMD_SYSTEM_MATCH_FEEDBACK)
		{
			// update bot params as feature may have changed, which include the type of robot
			bot.setBotParams(botParamsManager.get(bot.getBotParamLabel()));
		}
		bot.onIncomingBotCommand(command);
		botObservers.forEach(c -> c.onIncomingBotCommand(bot, command));
	}


	@Override
	public void onBotOnline(final ABot bot)
	{
		super.onBotOnline(bot);
		bot.getMatchCtrl().setKickerAutocharge(autoCharge);
		updateColorOfAllRobotsToMajority((TigerBot) bot);
	}


	private void updateColorOfAllRobotsToMajority(final TigerBot bot)
	{
		if (SumatraModel.getInstance().isProductive())
		{
			long numY = getBots().values().stream().map(b -> b.getBotId().getTeamColor())
					.filter(tc -> tc.equals(ETeamColor.YELLOW)).count();
			long numB = getBots().size() - numY;
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


	@Override
	public void onViewportUpdated(final int cameraId, final IRectangle viewport)
	{
		BaseStationCameraViewport cmd = new BaseStationCameraViewport(cameraId, viewport);
		getBaseStation().enqueueCommand(cmd);
	}


	@Override
	public void onNewBaseStationWifiStats(final BaseStationWifiStats stats)
	{
		for (BaseStationWifiStats.BotStats botStats : stats.getBotStats())
		{
			getTigerBot(botStats.getBotId()).ifPresent(bot -> bot.setStats(botStats));
		}
	}


	public void addBotObserver(final ITigerBotObserver observer)
	{
		botObservers.add(observer);
	}


	public void removeBotObserver(final ITigerBotObserver observer)
	{
		botObservers.remove(observer);
	}
}

