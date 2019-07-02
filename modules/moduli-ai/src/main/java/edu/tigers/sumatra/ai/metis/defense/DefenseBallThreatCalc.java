/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Calculate the ball threat.
 */
public class DefenseBallThreatCalc extends ADefenseThreatCalc
{
	@Configurable(comment = "Lookahead for ball position", defValue = "0.1")
	private static double ballLookahead = 0.1;

	@Configurable(comment = "Use ball direction instead of position if faster than this", defValue = "0.5")
	private static double checkBallDirectionVelThreshold = 0.5;

	@Configurable(comment = "Left/Right extension of goal line to use for shot-at-goal intersection", defValue = "200")
	private static double goalMargin = 200;


	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IVector2 threatSource = threatSource();
		IVector2 threatTarget = threatTarget(threatSource);
		ILineSegment threatLine = Lines.segmentFromPoints(threatSource, threatTarget);
		ILineSegment protectionLine = centerBackProtectionLine(threatLine, minDistanceToThreat());

		DefenseBallThreat ballThreat = new DefenseBallThreat(
				getBall().getVel(),
				threatLine,
				protectionLine,
				getNewTacticalField().getOpponentPassReceiver().orElse(null));
		newTacticalField.setDefenseBallThreat(ballThreat);
		drawThreat(ballThreat);
	}


	private IVector2 threatTarget(final IVector2 threatSource)
	{
		final IVector2 threatTarget = DefenseMath.getBisectionGoal(threatSource);
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
		return getNewTacticalField().getOpponentPassReceiver()
				.map(passReceiver -> Geometry.getField().nearestPointInside(
						passReceiver.getBotKickerPosByTime(ballLookahead), passReceiver.getBotKickerPos()))
				.orElse(threatSource);
	}


	private double minDistanceToThreat()
	{
		if (getAiFrame().getGamestate().isDistanceToBallRequired())
		{
			return RuleConstraints.getStopRadius() + Geometry.getBotRadius() + Geometry.getBallRadius();
		}
		return Geometry.getBotRadius() * 2;
	}
}
