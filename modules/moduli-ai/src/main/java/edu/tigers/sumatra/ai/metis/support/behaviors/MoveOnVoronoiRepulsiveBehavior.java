/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;


/**
 * This support behavior drive the supporter to a near area where is much space using the voronoi diagram.
 */
@RequiredArgsConstructor
public class MoveOnVoronoiRepulsiveBehavior extends ASupportBehavior
{
	@Configurable(comment = "Defines whether this behavior is active or not", defValue = "false")
	private static boolean enabled = false;

	private final Supplier<List<ICircle>> freeSpots;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		IVector2 pos = getWFrame().getBot(botID).getPos();

		return freeSpots.get().stream()
				.filter(circle -> circle.center().distanceTo(pos) > Geometry.getBotRadius() * 3)
				.filter(circle -> circle.center().x() > pos.x())
				.min(Comparator.comparingDouble(a -> a.center().distanceTo(pos)))
				.map(circle -> SupportBehaviorPosition.fromDestination(circle.center(), 1.0))
				.orElseGet(SupportBehaviorPosition::notAvailable);
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}
}
