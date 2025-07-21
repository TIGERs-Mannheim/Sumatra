/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.GameLogBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogPlayer;
import edu.tigers.sumatra.gamelog.GameLogPlayerObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Log4j2
public class GameLogBotManager extends ACommandBasedBotManager implements GameLogPlayerObserver
{
	@Override
	public void startModule()
	{
		super.startModule();
		SumatraModel.getInstance().getModule(GameLogPlayer.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(GameLogPlayer.class).removeObserver(this);
		super.stopModule();
	}


	@Override
	public void onNewGameLogMessage(GameLogMessage message, int index)
	{
		if (message.getType() == EMessageType.TIGERS_BASE_STATION_CMD_RECEIVED)
		{
			ACommand cmd = CommandFactory.getInstance().decode(message.getData());
			if (cmd == null)
			{
				log.warn("Failed to decode received command from game log at index: {}", index);
				return;
			}

			processIncommingCommand(cmd);
		} else if (message.getType() == EMessageType.TIGERS_BASE_STATION_CMD_SENT)
		{
			ACommand cmd = CommandFactory.getInstance().decode(message.getData());
			if (cmd == null)
			{
				log.warn("Failed to decode sent command from game log at index: {}", index);
				return;
			}

			if (cmd.getType() == ECommand.CMD_BASE_ACOMMAND)
			{
				BaseStationACommand baseCmd = (BaseStationACommand) cmd;

				if (baseCmd.getChild() == null)
				{
					log.debug("Invalid BaseStationACommand in gamelog for bot {}", baseCmd.getId());
					return;
				}

				getBot(baseCmd.getId()).ifPresent(b -> b.processOutgoingCommand(baseCmd.getChild()));
			} else
			{
				sendCommand(cmd);
			}
		}
	}


	@Override
	public void onGameLogTimeJump()
	{
		getBots().keySet().forEach(this::removeBot);
		resetStats();
	}


	@Override
	protected ABot createBot(BotID botId, ICommandSink commandSink)
	{
		return new GameLogBot(botId, commandSink);
	}


	@Override
	public Map<BotID, GameLogBot> getBots()
	{
		return super.getBots().values().stream()
				.map(GameLogBot.class::cast)
				.collect(Collectors.toMap(GameLogBot::getBotId, Function.identity()));
	}


	@Override
	public Optional<GameLogBot> getBot(BotID botID)
	{
		return super.getBot(botID).map(GameLogBot.class::cast);
	}
}
