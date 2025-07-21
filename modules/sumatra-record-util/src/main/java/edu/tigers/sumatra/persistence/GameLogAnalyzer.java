/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogReader;
import edu.tigers.sumatra.gamelog.filters.MessageTypeFilter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.util.BotTimings;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Log4j2
@NoArgsConstructor
@SuppressWarnings("java:S106") // allow System.out.println
public class GameLogAnalyzer
{
	private final BotTimings botTimings = new BotTimings();

	public static void main(String[] args)
	{
		SumatraModel.changeLogLevel(Level.INFO);
		String path = "data/gamelog/2025-05-05_11-02-50-ELIMINATION_PHASE-NORMAL_FIRST_HALF-KIKS-vs-TIGERs_Mannheim.log";
		new GameLogAnalyzer().run(path);
	}


	private void run(String path)
	{
		GameLogReader reader = new GameLogReader();
		reader.addFilter(new MessageTypeFilter(
				EnumSet.of(EMessageType.TIGERS_BASE_STATION_CMD_RECEIVED,
						EMessageType.TIGERS_BASE_STATION_CMD_SENT)));
		reader.loadFileBlocking(path);

		System.out.println("Messages: " + reader.getMessages().size());

		Map<BotID, List<Long>> receivedTimestamps = new HashMap<>();
		Map<BotID, List<Long>> sentTimestamps = new HashMap<>();
		Map<EMessageType, Map<ECommand, AtomicInteger>> commandCounts = new EnumMap<>(EMessageType.class);
		for (var message : reader.getMessages())
		{
			ACommand cmd = CommandFactory.getInstance().decode(message.getData());
			if (cmd == null)
			{
				System.out.println("Unknown command: " + message.getType());
				continue;
			}
			commandCounts.computeIfAbsent(message.getType(), k -> new EnumMap<>(ECommand.class))
					.computeIfAbsent(cmd.getType(), k -> new AtomicInteger()).incrementAndGet();
			if (cmd.getType() == ECommand.CMD_BASE_ACOMMAND)
			{
				BaseStationACommand baseCmd = (BaseStationACommand) cmd;
				if (message.getType() == EMessageType.TIGERS_BASE_STATION_CMD_RECEIVED)
				{
					receivedTimestamps.computeIfAbsent(baseCmd.getId(), e -> new ArrayList<>())
							.add(message.getTimestampNs());
				} else if (message.getType() == EMessageType.TIGERS_BASE_STATION_CMD_SENT)
				{
					sentTimestamps.computeIfAbsent(baseCmd.getId(), e -> new ArrayList<>()).add(message.getTimestampNs());
				}
			}
		}

		System.out.println("Received\n");
		botTimings.stats(receivedTimestamps);
		System.out.println("\n\nSent\n");
		botTimings.stats(sentTimestamps);

		System.out.println("\n\nCommands\n");
		for (var entry : commandCounts.entrySet())
		{
			System.out.printf("%s %s %n", entry.getKey(), entry.getValue());
		}

		botTimings.export("gamelogBotFeedbackReceive", receivedTimestamps);
		botTimings.export("gamelogBotFeedbackSent", sentTimestamps);
	}
}
