/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.processors;

import edu.tigers.sumatra.ai.PersistenceAiFrame;
import edu.tigers.sumatra.ai.data.EBotInformation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.persistence.util.BotTimings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("java:S106") // allow System.out.println
public class BotFeedbackAnalyzer implements IPersistenceDbAnalyzer<PersistenceAiFrame>
{
	private final Map<BotID, List<Long>> receivedTimestamps = new HashMap<>();


	@Override
	public void process(PersistenceAiFrame frame)
	{
		var feedback = getLastFeedback(frame);

		feedback.forEach((botID, feedbackTime) ->
				receivedTimestamps.computeIfAbsent(botID, k -> new ArrayList<>()).add(feedbackTime)
		);
	}


	private Map<BotID, Long> getLastFeedback(PersistenceAiFrame frame)
	{
		Map<BotID, Long> map = new HashMap<>();
		for (ETeamColor color : ETeamColor.values())
		{
			var visFrame = frame.getVisFrame(color);
			if (visFrame == null)
			{
				continue;
			}
			visFrame.getAiInfos().forEach(((botID, botAiInformation) ->
			{
				var feedback = botAiInformation.getMap().get(EBotInformation.LAST_FEEDBACK);
				if (feedback != null)
				{
					long value = Long.parseLong(feedback);
					if (value > 0)
					{
						map.put(botID, value);
					}
				}
			}));
		}
		return map;
	}


	@Override
	public void save(Path outputFolder)
	{
		BotTimings botTimings = new BotTimings();

		botTimings.stats(receivedTimestamps);
		botTimings.export("recordingBotFeedback", receivedTimestamps);
	}
}
