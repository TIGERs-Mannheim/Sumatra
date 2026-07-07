package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


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


	public List<IDrawableShape> drawShapes(int index)
	{
		return Stream.of(centerBackDefStrategyData, man2manDefStrategyData)
				.filter(Objects::nonNull)
				.flatMap(e -> e.drawShapes(index))
				.toList();
	}
}
