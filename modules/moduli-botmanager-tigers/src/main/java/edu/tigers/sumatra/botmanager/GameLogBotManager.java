/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.bots.BasicTigerBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogPlayer;
import edu.tigers.sumatra.gamelog.GameLogPlayerObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static edu.tigers.sumatra.botmanager.commands.ECommand.CMD_BASE_ACOMMAND;
import static edu.tigers.sumatra.botmanager.commands.ECommand.CMD_BASE_WIFI_STATS;
import static edu.tigers.sumatra.botmanager.commands.ECommand.CMD_SYSTEM_MATCH_FEEDBACK;


@Log4j2
public class GameLogBotManager extends ABotManager implements GameLogPlayerObserver
{
	private Set<BotID> lastBots = new HashSet<>();


	@Override
	public void initModule()
	{
		super.initModule();

		CommandFactory.getInstance().loadCommands();
	}


	@Override
	public void startModule()
	{
		super.startModule();

		SumatraModel.getInstance().getModule(GameLogPlayer.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		revokeOnlineActions();
		SumatraModel.getInstance().getModule(GameLogPlayer.class).removeObserver(this);
		super.stopModule();
	}


	@Override
	public void onNewGameLogMessage(GameLogMessage message, int index)
	{
		if (message.getType() != EMessageType.TIGERS_BASE_STATION_CMD_RECEIVED)
			return;

		ACommand cmd = CommandFactory.getInstance().decode(message.getData());
		if (cmd == null)
			return;

		if (cmd.getType() == CMD_BASE_ACOMMAND)
			handleIncomingBaseStationACommand(cmd);
		else if (cmd.getType() == CMD_BASE_WIFI_STATS)
			handleIncomingBaseStationWifiStats(cmd);
	}


	@Override
	public void onGameLogTimeJump()
	{
		revokeOnlineActions();
	}


	private void revokeOnlineActions()
	{
		for (BotID botId : lastBots)
		{
			onBotOffline(botId);
		}
		lastBots.clear();
	}


	private void handleIncomingBaseStationWifiStats(final ACommand cmd)
	{
		BaseStationWifiStats stats = (BaseStationWifiStats) cmd;

		Set<BotID> curBots = new HashSet<>();
		for (BaseStationWifiStats.BotStats botStats : stats.getBotStats())
		{
			BotID botId = botStats.getBotId();
			if (botId.isBot())
			{
				curBots.add(botId);
			}
		}
		for (BotID botId : lastBots)
		{
			if (!curBots.contains(botId))
			{
				onBotOffline(botId);
			}
		}
		for (BotID botId : curBots)
		{
			if (!lastBots.contains(botId))
			{
				BasicTigerBot bot = new BasicTigerBot(botId, getBaseStation());
				onBotOnline(bot);
			}
		}

		lastBots = curBots;

		for (BaseStationWifiStats.BotStats botStats : stats.getBotStats())
		{
			getTigerBot(botStats.getBotId()).ifPresent(bot -> bot.setStats(botStats));
		}
	}


	private void handleIncomingBaseStationACommand(final ACommand cmd)
	{
		BaseStationACommand baseCmd = (BaseStationACommand) cmd;

		if (baseCmd.getChild() == null)
		{
			log.info("Invalid BaseStationACommand lost");
			return;
		}

		if (baseCmd.getChild().getType() == ECommand.CMD_SYSTEM_CONSOLE_PRINT)
		{
			final TigerSystemConsolePrint print = (TigerSystemConsolePrint) baseCmd.getChild();
			log.info("Console({}): {}", baseCmd.getId().getNumberWithColorOffset(),
					print.getText().replaceAll("[\n\r]$", ""));
		}

		getTigerBot(baseCmd.getId()).ifPresent(bot -> processCommand(baseCmd.getChild(), bot));
	}


	private Optional<BasicTigerBot> getTigerBot(final BotID botID)
	{
		return getBot(botID).map(BasicTigerBot.class::cast);
	}


	private void processCommand(final ACommand command, final BasicTigerBot bot)
	{
		if (command.getType() == CMD_SYSTEM_MATCH_FEEDBACK)
		{
			// update bot params as feature may have changed, which include the type of robot
			bot.setBotParams(botParamsManager.get(bot.getBotParamLabel()));
		}
		bot.onIncomingBotCommand(command);
	}
}
