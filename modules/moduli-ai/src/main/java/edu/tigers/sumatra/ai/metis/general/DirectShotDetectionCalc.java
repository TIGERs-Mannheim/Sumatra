/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.KickedBall;
import lombok.Getter;

import java.awt.Color;
import java.util.Optional;


/**
 * This calculator detects direct kicks to goal
 */
public class DirectShotDetectionCalc extends ACalculator
{
	@Getter
	private KickedBall detectedGoalKickTigers;
	@Getter
	private KickedBall detectedGoalKickOpponents;


	@Override
	protected boolean isCalculationNecessary()
	{
		return Geometry.getField().isPointInShape(getBall().getPos());
	}


	@Override
	protected void reset()
	{
		detectedGoalKickTigers = null;
		detectedGoalKickOpponents = null;
	}


	@Override
	public void doCalc()
	{
		detectedGoalKickTigers = detectGoalKick(ETeam.TIGERS);
		detectedGoalKickOpponents = detectGoalKick(ETeam.OPPONENTS);
	}


	private KickedBall detectGoalKick(ETeam attackingTeam)
	{
		ETeamColor attackingTeamColor = getAiFrame().getTeamColor();
		Goal attackedGoal = Geometry.getGoalTheir();
		if (attackingTeam == ETeam.OPPONENTS)
		{
			attackingTeamColor = attackingTeamColor.opposite();
			attackedGoal = Geometry.getGoalOur();
		}

		Optional<KickedBall> kickEvent = getWFrame().getKickedBall();
		if (kickEvent.isPresent() && kickEvent.get().getKickingBot().getTeamColor() == attackingTeamColor)
		{
			ILineSegment ballLine = Lines.segmentFromOffset(getBall().getPos(),
					getBall().getVel().scaleToNew(Geometry.getFieldLength()));
			ILineSegment goalLine = attackedGoal.getLineSegment();
			Optional<IVector2> intersection = ballLine.intersect(goalLine).asOptional();
			if (intersection.isPresent())
			{
				Color color = attackingTeam == ETeam.TIGERS ? Color.GREEN : Color.RED;
				getShapes(EAiShapesLayer.AI_DIRECT_SHOT_DETECTION).add(
						new DrawableLine(getBall().getPos(), intersection.get(), color));
				return kickEvent.get();
			}
		}
		return null;
	}
}
