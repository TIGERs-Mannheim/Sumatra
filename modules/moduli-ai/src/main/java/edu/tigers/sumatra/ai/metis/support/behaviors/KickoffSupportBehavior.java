/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class KickoffSupportBehavior extends ASupportBehavior
{
	@Configurable(comment = "Defines if this behavior is enabled", defValue = "true")
	private static boolean enabled = true;
	@Configurable(comment = "How many possible targets should be considered by each bot", defValue = "4")
	private static int nClosestPoints = 4;

	@Configurable(comment = "min distance to consider a possible target", defValue = "0.0")
	private static double minDistanceToPoint = 0.0;

	@Configurable(comment = "Min value for pass score", defValue = "0.25")
	private static double passScoreThreshold = 0.25;

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();

	private final Supplier<List<IVector2>> possibleSupporterKickoffPositions;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		if (possibleSupporterKickoffPositions.get().isEmpty())
		{
			return SupportBehaviorPosition.notAvailable();
		}

		passFactory.update(getWFrame());
		ratedPassFactory.update(getWFrame().getOpponentBots().values(), getWFrame().getOpponentBots().values(), Collections.emptyList());

		return possibleSupporterKickoffPositions.get().stream()
				.sorted(Comparator.comparingDouble(point -> point.distanceToSqr(getWFrame().getBot(botID).getPos())))
				.limit(nClosestPoints)
				.filter(point -> point.distanceTo(getWFrame().getBot(botID).getPos()) >= minDistanceToPoint)
				.findFirst()
				.map(pos -> getSupportBehaviorPosition(botID, pos))
				.orElseGet(SupportBehaviorPosition::notAvailable);
	}


	private SupportBehaviorPosition getSupportBehaviorPosition(BotID botID, IVector2 destination)
	{
		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_KICKOFF)
				.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius() * 1.2), Color.ORANGE));
		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_KICKOFF)
				.add(new DrawableLine(getWFrame().getBot(botID).getPos(), destination, Color.ORANGE));

		var pass = passFactory.straight(getWFrame().getBall().getPos(), destination, BotID.noBot(), botID,
				EBallReceiveMode.DONT_CARE);
		if (pass.isEmpty())
		{
			return SupportBehaviorPosition.notAvailable();
		}
		var rating = ratedPassFactory.rateMaxCombined(pass.get(), EPassRating.PASSABILITY, EPassRating.INTERCEPTION);

		if (rating < passScoreThreshold)
		{
			return SupportBehaviorPosition.notAvailable();
		}
		return SupportBehaviorPosition.fromDestination(destination, rating);
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

}
