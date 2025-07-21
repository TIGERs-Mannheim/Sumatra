/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.basestation.BotCommand;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.CommandBasedBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.communication.ECommandVerdict;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.observer.EventDistributor;
import edu.tigers.sumatra.observer.EventSubscriber;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Common base for all bot managers working with ACommand.
 * Handles mostly observers and bot online/offline logic.
 */
@Log4j2
public abstract class ACommandBasedBotManager extends BotManager implements ICommandSink
{
	private static final int STAT_ENTRIES = 10;

	private final Queue<BaseStationWifiStats> wifiStats = new LinkedList<>();
	private final Queue<BaseStationEthStats> ethStats = new LinkedList<>();

	private final EventDistributor<BotCommand> onIncomingBotCommand = new EventDistributor<>();
	private final EventDistributor<BotCommand> onOutgoingBotCommand = new EventDistributor<>();
	private final EventDistributor<ACommand> onIncomingCommand = new EventDistributor<>();
	private final EventDistributor<ACommand> onOutgoingCommand = new EventDistributor<>();
	private final EventDistributor<BaseStationWifiStats> onNewBaseStationWifiStats = new EventDistributor<>();
	private final EventDistributor<BaseStationEthStats> onNewBaseStationEthStats = new EventDistributor<>();


	protected abstract ABot createBot(BotID botId, ICommandSink commandSink);


	@Override
	public Map<BotID, ? extends CommandBasedBot> getBots()
	{
		return super.getBots().values().stream()
				.map(CommandBasedBot.class::cast)
				.collect(Collectors.toMap(CommandBasedBot::getBotId, Function.identity()));
	}


	@Override
	public Optional<? extends CommandBasedBot> getBot(BotID botID)
	{
		return super.getBot(botID).map(CommandBasedBot.class::cast);
	}


	/**
	 * Do not use this method to send commands to bots!
	 * Use {@link CommandBasedBot#sendCommand} instead.
	 *
	 * @param cmd
	 */
	@Override
	public void sendCommand(ACommand cmd)
	{
		if (cmd.getType() == ECommand.CMD_BASE_ACOMMAND)
		{
			var baseCmd = (BaseStationACommand) cmd;
			var child = baseCmd.getChild();
			onOutgoingBotCommand.newEvent(new BotCommand(baseCmd.getId(), child));
		}

		onOutgoingCommand.newEvent(cmd);
	}


	protected void processIncommingCommand(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_BASE_ACOMMAND -> processIncomingBaseACommand(cmd);
			case CMD_BASE_PING -> { /* ignore */ }
			case CMD_BASE_WIFI_STATS -> processIncomingBaseWifiStats(cmd);
			case CMD_BASE_ETH_STATS -> processIncomingBaseEthStats(cmd);
			default -> log.warn("Unhandled incoming command. ID: {}", cmd.getType());
		}

		onIncomingCommand.newEvent(cmd);
	}


	private void processIncomingBaseACommand(final ACommand cmd)
	{
		BaseStationACommand baseCmd = (BaseStationACommand) cmd;

		if (baseCmd.getChild() == null)
		{
			log.debug("Invalid BaseStationACommand lost for bot {}", baseCmd.getId());
			return;
		}

		var optBot = getBot(baseCmd.getId());

		if (optBot.isEmpty())
		{
			// Silently fail here. We may receive commands from a robot before it is marked online
			// due to the fact that WIFI_STATS is coming in late.
			return;
		}

		if(optBot.get().processIncomingCommand(baseCmd.getChild()) == ECommandVerdict.PASS)
		{
			onIncomingBotCommand.newEvent(new BotCommand(baseCmd.getId(), baseCmd.getChild()));
		}
	}


	private void processIncomingBaseEthStats(final ACommand cmd)
	{
		BaseStationEthStats incomingStats = (BaseStationEthStats) cmd;

		ethStats.add(incomingStats);

		BaseStationEthStats stats;
		if (ethStats.size() > STAT_ENTRIES)
		{
			stats = new BaseStationEthStats(incomingStats, ethStats.remove());
		} else
		{
			stats = incomingStats;
		}

		onNewBaseStationEthStats.newEvent(stats);
	}


	private void processIncomingBaseWifiStats(final ACommand cmd)
	{
		BaseStationWifiStats incomingStats = (BaseStationWifiStats) cmd;

		wifiStats.add(incomingStats);

		BaseStationWifiStats stats;
		if (wifiStats.size() > STAT_ENTRIES)
		{
			// this gives a nice report over the last second every 100ms :)
			stats = new BaseStationWifiStats(incomingStats, wifiStats.remove());
		} else
		{
			stats = incomingStats;
		}

		Set<BotID> curBots = new HashSet<>();
		for (BaseStationWifiStats.BotStats botStats : stats.getBotStats())
		{
			BotID botId = botStats.getBotId();
			if (botId.isBot())
			{
				curBots.add(botId);
			}
			CommandBasedBot bot = getBots().get(botStats.getBotId());
			if (bot != null)
			{
				bot.setWifiStats(botStats);
			}
		}

		for (BotID botId : getBots().keySet())
		{
			if (!curBots.contains(botId))
			{
				removeBot(botId);
			}
		}

		for (BotID botId : curBots)
		{
			if (!getBots().containsKey(botId))
			{
				addBot(createBot(botId, this));
			}
		}

		onNewBaseStationWifiStats.newEvent(stats);
	}


	protected void resetStats()
	{
		wifiStats.clear();
		ethStats.clear();
	}


	public EventSubscriber<BotCommand> getOnIncomingBotCommand()
	{
		return onIncomingBotCommand;
	}


	public EventSubscriber<BotCommand> getOnOutgoingBotCommand()
	{
		return onOutgoingBotCommand;
	}


	public EventSubscriber<ACommand> getOnIncomingBasestationCommand()
	{
		return onIncomingCommand;
	}


	public EventSubscriber<ACommand> getOnOutgoingBasestationCommand()
	{
		return onOutgoingCommand;
	}


	public EventSubscriber<BaseStationWifiStats> getOnNewBaseStationWifiStats()
	{
		return onNewBaseStationWifiStats;
	}


	public EventSubscriber<BaseStationEthStats> getOnNewBaseStationEthStats()
	{
		return onNewBaseStationEthStats;
	}
}
