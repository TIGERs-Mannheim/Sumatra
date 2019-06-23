/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculate the ball threat.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DefenseBallThreatCalc extends ACalculator
{
	@Configurable(comment = "Lookahead for ball position", defValue = "0.1")
	private static double ballLookahead = 0.1;
	
	@Configurable(comment = "Use ball direction instead of position if faster than this", defValue = "0.5")
	private static double checkBallDirectionVelThreshold = 0.5;
	
	@Configurable(comment = "Left/Right extension of goal line to use for shot-at-goal intersection", defValue = "200")
	private static double goalWidthMargin = 200;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final List<IDrawableShape> defenseShapes = newTacticalField.getDrawableShapes()
				.get(EAiShapesLayer.DEFENSE_BALL_THREAT);
		
		DefenseBallThreat ballThreat;
		
		if (newTacticalField.getOpponentPassReceiver().isPresent())
		{
			ITrackedBot passReceiver = newTacticalField.getOpponentPassReceiver().get();
			
			IVector2 source = Geometry.getField().nearestPointInside(
					passReceiver.getBotKickerPosByTime(ballLookahead), passReceiver.getBotKickerPos());
			
			ballThreat = new DefenseBallThreat(source,
					DefenseMath.getBisectionGoal(source),
					passReceiver.getVel(), passReceiver);
		} else if (getBall().getVel().getLength2() > checkBallDirectionVelThreshold)
		{
			ILine travelLine = getBall().getTrajectory().getTravelLine();
			Optional<IVector2> goalCrossing = Geometry.getGoalOur().withMargin(0, goalWidthMargin).getLine()
					.intersectionOfSegments(travelLine);
			
			if (goalCrossing.isPresent())
			{
				ballThreat = new DefenseBallThreat(getBall().getPos(),
						goalCrossing.get(),
						getBall().getVel(),
						null);
			} else
			{
				IVector2 source = Geometry.getField().nearestPointInside(
						getBall().getTrajectory().getPosByTime(ballLookahead).getXYVector(), getBall().getPos());
				
				ballThreat = new DefenseBallThreat(source,
						DefenseMath.getBisectionGoal(source),
						getBall().getVel(),
						null);
			}
		} else
		{
			IVector2 source = getBall().getTrajectory().getPosByTime(ballLookahead).getXYVector();
			
			ballThreat = new DefenseBallThreat(source,
					DefenseMath.getBisectionGoal(source),
					getBall().getVel(),
					null);
		}
		
		DrawableLine threatLine = new DrawableLine(ballThreat.getThreatLine(), Color.RED);
		defenseShapes.add(threatLine);
		
		newTacticalField.setDefenseBallThreat(ballThreat);
	}
}
