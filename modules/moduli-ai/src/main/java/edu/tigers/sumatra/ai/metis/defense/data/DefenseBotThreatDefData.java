/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Optional;


public record DefenseBotThreatDefData(ITrackedBot bot,
                                      DefenseBotThreatDefStrategyData centerBackDefStrategyData,
                                      DefenseBotThreatDefStrategyData man2manDefStrategyData,
                                      double threatRating)
{

	public Optional<DefenseBotThreatDefStrategyData> getDefStrategyData(EDefenseBotThreatDefStrategy strategy)
	{
		return Optional.ofNullable(switch (strategy)
				{
					case CENTER_BACK -> centerBackDefStrategyData;
					case MAN_2_MAN_MARKER -> man2manDefStrategyData;
				});
	}


	public boolean canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy strategy)
	{
		return getDefStrategyData(strategy).map(DefenseBotThreatDefStrategyData::isComplete).orElse(false);
	}


	public BotID getBotId()
	{
		return bot.getBotId();
	}
}
