/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * This calculator detects direct kicks to goal
 *
 * @author Stefan Schneyer
 */
public class DirectShotDetectionCalc extends ACalculator
{
	private final ETeam attackingTeam;
	
	
	/**
	 * @param attackingTeam the attacking team
	 */
	public DirectShotDetectionCalc(final ETeam attackingTeam)
	{
		this.attackingTeam = attackingTeam;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (!Geometry.getField().isPointInShape(getBall().getPos()))
		{
			return;
		}
		
		ETeamColor attackingTeamColor = baseAiFrame.getTeamColor();
		Goal attackingGoal = Geometry.getGoalTheir();
		if (attackingTeam == ETeam.OPPONENTS)
		{
			attackingTeamColor = attackingTeamColor.opposite();
			attackingGoal = Geometry.getGoalOur();
		}
		
		Optional<IKickEvent> kickEvent = baseAiFrame.getWorldFrame().getKickEvent();
		if (kickEvent.isPresent() && kickEvent.get().getKickingBot().getTeamColor() == attackingTeamColor)
		{
			ILineSegment ballLine = Lines.segmentFromOffset(getBall().getPos(),
					getBall().getVel().scaleToNew(Geometry.getFieldLength()));
			ILineSegment goalLine = attackingGoal.getLineSegment();
			Optional<IVector2> intersection = ballLine.intersectSegment(goalLine);
			if (intersection.isPresent())
			{
				Color color = attackingTeam == ETeam.TIGERS ? Color.GREEN : Color.RED;
				newTacticalField.setDetectedGoalKick(kickEvent.get(), attackingTeam);
				newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_DIRECT_SHOT_DETECTION).add(
						new DrawableLine(Lines.segmentFromPoints(getBall().getPos(), intersection.get()), color));
			}
		}
	}
}
