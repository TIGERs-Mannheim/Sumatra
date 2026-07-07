package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class DesiredOffenseBotsCalc extends ACalculator
{
	// bots that actually do something with the ball
	private final Supplier<List<BotID>> ballHandlingBots;
	// bots that do other things with the opponents (block, interfere, etc.)
	private final Supplier<Map<BotID, IVector2>> supportiveAttackers;
	// bots that are about to be passed to
	private final Supplier<List<BotID>> passReceivers;

	@Getter
	private List<BotID> desiredOffenseBots;

	@Getter
	private int numOffenseBots;


	@Override
	protected void doCalc()
	{
		desiredOffenseBots = Stream.of(
						ballHandlingBots,
						() -> supportiveAttackers.get().keySet().stream().toList(),
						passReceivers
				)
				.map(Supplier::get)
				.flatMap(Collection::stream)
				.distinct()
				.toList();
		numOffenseBots = desiredOffenseBots.size();
	}
}
