/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;


/**
 * Determine a good mid-field position.
 */
@RequiredArgsConstructor
public class MidfieldRepulsiveBehavior extends ASupportBehavior
{
	@Configurable(comment = "Defines if this behavior is enabled", defValue = "true")
	private static boolean enabled = true;

	@Configurable(comment = "How many possible targets should be considered by each bot", defValue = "4")
	private static int nClosestPoints = 3;

	@Configurable(comment = "min distance to consider a possible target", defValue = "600.0")
	private static double minDistanceToPoint = 600.0;

	@Configurable(comment = "Min value for pass score", defValue = "0.25")
	private static double passScoreThreshold = 0.25;

	@Configurable(comment = "Ball decision offset", defValue = "-1000.0")
	private static double ballDecisionOffset = -1000.0;

	@Configurable(comment = "Minimum required offensive positions", defValue = "1")
	private static int minRequiredOffensivePositions = 1;

	@Configurable(comment = "Offense Zone", defValue = "3000.0")
	private static double offenseZone = 3000.0;

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();

	private final Supplier<List<IVector2>> possibleSupporterMidfieldPositions;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		passFactory.update(getWFrame());
		ratedPassFactory.update(getWFrame().getOpponentBots().values());

		if (!isReasonable(getWFrame().getBot(botID)))
		{
			return SupportBehaviorPosition.notAvailable();
		}

		return possibleSupporterMidfieldPositions.get().stream()
				.sorted(Comparator.comparingDouble(point -> point.distanceToSqr(getWFrame().getBot(botID).getPos())))
				.limit(nClosestPoints)
				.filter(point -> point.distanceTo(getWFrame().getBot(botID).getPos()) > minDistanceToPoint)
				.findFirst()
				.map(pos -> getSupportBehaviorPosition(botID, pos))
				.orElseGet(SupportBehaviorPosition::notAvailable);
	}


	private SupportBehaviorPosition getSupportBehaviorPosition(BotID botID, IVector2 destination)
	{
		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
				.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius() * 1.2), Color.ORANGE));
		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
				.add(new DrawableLine(getWFrame().getBot(botID).getPos(), destination, Color.ORANGE));

		var pass = passFactory.straight(getWFrame().getBall().getPos(), destination, BotID.noBot(), botID);
		var rating = ratedPassFactory.rateMaxCombined(pass, EPassRating.PASSABILITY, EPassRating.INTERCEPTION);

		if (rating < passScoreThreshold)
		{
			return SupportBehaviorPosition.notAvailable();
		}
		return SupportBehaviorPosition.fromDestination(destination, rating);
	}


	private boolean isReasonable(ITrackedBot trackedBot)
	{
		boolean ballOnOurHalf = getWFrame().getBall().getPos().x() < ballDecisionOffset;
		boolean isOnOpponentHalf = trackedBot.getPos().x() > 0;
		boolean enoughOffensives = getWFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> bot.getPos().x() >= offenseZone).count() > minRequiredOffensivePositions;

		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
				.add(new DrawableLine(
						Vector2.fromXY(ballDecisionOffset, -0.5 * Geometry.getFieldWidth()),
						Vector2.fromXY(ballDecisionOffset, 0.5 * Geometry.getFieldWidth()), Color.ORANGE));

		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
				.add(new DrawableLine(
						Vector2.fromXY(offenseZone, -0.5 * Geometry.getFieldWidth()),
						Vector2.fromXY(offenseZone, 0.5 * Geometry.getFieldWidth()), Color.RED));

		return enoughOffensives && isOnOpponentHalf && ballOnOurHalf;
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

}
