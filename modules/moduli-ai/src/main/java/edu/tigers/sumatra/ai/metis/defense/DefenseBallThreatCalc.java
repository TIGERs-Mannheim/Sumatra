/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


/**
 * Calculate the ball threat.
 * The threat line starts either at the the opponent's ball receiver or at the ball position
 * It ends in the center of the goal
 */
@RequiredArgsConstructor
public class DefenseBallThreatCalc extends ADefenseThreatCalc
{
	@Configurable(comment = "Lookahead for ball position", defValue = "0.1")
	private static double ballLookahead = 0.1;

	@Configurable(comment = "Use ball direction instead of position if faster than this", defValue = "0.5")
	private static double checkBallDirectionVelThreshold = 0.5;

	@Configurable(comment = "Left/Right extension of goal line to use for shot-at-goal intersection", defValue = "200")
	private static double goalMargin = 200;

	private final Supplier<IKickEvent> detectedGoalKickOpponents;
	private final Supplier<ITrackedBot> opponentPassReceiver;

	@Getter
	private DefenseBallThreat defenseBallThreat;


	@Override
	public void doCalc()
	{
		IVector2 threatSource = threatSource();
		IVector2 threatTarget = threatTarget(threatSource);
		ILineSegment threatLine = Lines.segmentFromPoints(threatSource, threatTarget);
		var protectionLine = centerBackProtectionLine(threatLine, minDistanceToThreat());

		defenseBallThreat = new DefenseBallThreat(
				getBall().getVel(),
				threatLine,
				protectionLine.orElse(null),
				opponentPassReceiver.get());
		drawThreat(defenseBallThreat);
	}


	private IVector2 threatTarget(final IVector2 threatSource)
	{
		final IVector2 threatTarget = Geometry.getGoalOur().bisection(threatSource);
		if (getBall().getVel().getLength2() > checkBallDirectionVelThreshold)
		{
			IHalfLine travelLine = getBall().getTrajectory().getTravelLine();
			return Geometry.getGoalOur().withMargin(0, goalMargin).getLineSegment()
					.intersectHalfLine(travelLine).orElse(threatTarget);
		}
		return threatTarget;
	}


	private IVector2 threatSource()
	{
		IVector2 predictedBallPos = getBall().getTrajectory().getPosByTime(ballLookahead).getXYVector();
		IVector2 threatSource = Geometry.getField().nearestPointInside(predictedBallPos, getBall().getPos());

		if (detectedGoalKickOpponents.get() != null)
		{
			// For DirectShots, always protectBall directly
			return threatSource;
		}
		if (opponentPassReceiver.get() != null)
		{
			// Prefer protecting opponentPassReceiver than protecting the ball
			return Geometry.getField().nearestPointInside(
					opponentPassReceiver.get().getBotKickerPosByTime(ballLookahead),
					opponentPassReceiver.get().getBotKickerPos());
		}
		// Protect the ball
		return threatSource;
	}


	private double minDistanceToThreat()
	{
		if (getAiFrame().getGameState().isDistanceToBallRequired())
		{
			return RuleConstraints.getStopRadius() + Geometry.getBotRadius() + Geometry.getBallRadius();
		}
		return Geometry.getBotRadius() * 2;
	}
}
