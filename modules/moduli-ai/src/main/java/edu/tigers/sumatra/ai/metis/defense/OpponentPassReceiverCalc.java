/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath.ReceiveData;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Determines the opponent's pass receiver. <br>
 * Calculations are based on planar curves. For the ball a planar curve is directly calculated from the trajectory.
 * A chipped ball's planar curve starts at the first touchdown location.<br>
 * The robot uses a planar curve segment that assumes it wants to stop as quickly as possible. Hence, its velocity is
 * used for a small lookahead.
 */
public class OpponentPassReceiverCalc extends ACalculator
{
	@Configurable(comment = "Bots with a minimum curve to (ball) curve distance below this are consired to be pass receivers. [mm]", defValue = "300.0")
	private static double validReceiveDistance = 300.0;

	@Configurable(comment = "Minimum ball speed to start looking for a pass receiver. [m/s]", defValue = "1.0")
	private static double minBallSpeed = 1.0;

	@Configurable(comment = "Bots further away than this from the ball travel line are not considered as pass receivers. [mm]", defValue = "2000.0")
	private static double maxBotToBallTravelLineDistance = 2000.0;

	@Getter
	private ITrackedBot opponentPassReceiver;


	@Override
	public boolean isCalculationNecessary()
	{
		return getBall().getVel().getLength2() >= minBallSpeed;
	}


	@Override
	protected void reset()
	{
		opponentPassReceiver = null;
	}


	@Override
	public void doCalc()
	{
		List<ReceiveData> ratings = getRatings();

		Optional<ReceiveData> bestReceiver = ratings.stream()
				.filter(this::isDistanceValid)
				.min(Comparator.comparingDouble(ReceiveData::getDistToBall));

		opponentPassReceiver = bestReceiver.map(ReceiveData::getBot).orElse(null);

		// drawing
		for (ReceiveData data : ratings)
		{
			DrawableAnnotation distAnno = new DrawableAnnotation(data.getBot().getPos(),
					String.format("%.1f", data.getDistToBallCurve()),
					Vector2.fromXY(0, -150));
			distAnno.withCenterHorizontally(true);
			getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(distAnno);
		}

		if (bestReceiver.isPresent())
		{
			AnimatedCrosshair cross = AnimatedCrosshair
					.aCrosshairWithContinuousRotation(Circle.createCircle(bestReceiver.get().getBot().getPos(), 120), 2.0f,
							Color.CYAN);
			getShapes(EAiShapesLayer.DEFENSE_PASS_RECEIVER).add(cross);
		}
	}


	private List<ReceiveData> getRatings()
	{
		IBallTrajectory ballTrajectory = getBall().getTrajectory();

		List<ReceiveData> ratings;
		if (ballTrajectory.getTouchdownLocations().isEmpty())
		{
			ratings = DefenseMath.calcReceiveRatingsNonRestricted(
					ballTrajectory,
					getWFrame().getOpponentBots().values(),
					maxBotToBallTravelLineDistance);
		} else
		{
			// restrict start of curve
			ratings = DefenseMath.calcReceiveRatingsForRestrictedStart(
					ballTrajectory,
					getWFrame().getOpponentBots().values(),
					maxBotToBallTravelLineDistance);
		}
		return ratings;
	}


	private boolean isDistanceValid(ReceiveData data)
	{
		// Per meter distance to the ball increase allowed distance to ball curve by 0.1 meter
		var allowedDistance = validReceiveDistance + data.getDistToBall() / 10;
		return data.getDistToBallCurve() < allowedDistance;
	}
}
