/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballplacement;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@RequiredArgsConstructor
public class BallPlacementBotChooser
{
	@Configurable(defValue = "1000", comment = "Max distance for single bot ball placement [mm]")
	private static double maxSingleBotBallPlacementDistance = 1000;

	static
	{
		ConfigRegistration.registerClass("metis", BallPlacementBotChooser.class);
	}

	private final ITrackedBall ball;
	private final IVector2 ballTargetPos;
	private final Collection<ITrackedBot> availableBots;
	private final BotID currentBallPlacementBotPrimary;
	private final BotID currentBallPlacementBotAssistant;


	public Optional<BotID> choosePrimary()
	{
		return availableBots
				.stream()
				.min(Comparator.comparing(this::distanceToTrajectory))
				.map(ITrackedBot::getBotId);
	}


	private double distanceToTrajectory(ITrackedBot bot)
	{
		double bonus = (Objects.equals(bot.getBotId(), currentBallPlacementBotPrimary)) ? 500 : 0;
		if (ball.getVel().getLength2() > 1
				// Give bonus to bots that are in front of the ball or close to the kicker
				&& (ball.getTrajectory().getTravelLine().isPointInFront(bot.getPos())
				|| ball.getPos().distanceTo(bot.getBotKickerPos()) < 50)
		)
		{
			bonus += 10000;
		}
		return ball.getTrajectory().distanceTo(bot.getPos()) - bonus;
	}


	public List<BotID> getOrderedAssistants(BotID primaryBot)
	{
		double ballToTargetDistance = ball.getTrajectory().distanceTo(ballTargetPos);
		if (ballToTargetDistance < maxSingleBotBallPlacementDistance)
		{
			return List.of();
		}

		return availableBots
				.stream()
				.filter(bot -> !bot.getBotId().equals(primaryBot))
				// Sort by distance to target position
				.sorted(Comparator.comparing(r -> ballTargetPos.distanceTo(r.getPos())
						- ((Objects.equals(r.getBotId(), currentBallPlacementBotAssistant)) ? 500 : 0)))
				// Check if the distance is reasonable to choose the receiving bot
				.filter(bot -> bot.getPos().distanceTo(ballTargetPos) < ballToTargetDistance * 2)
				.map(ITrackedBot::getBotId)
				.toList();
	}
}
