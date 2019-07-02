/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;


/**
 * Calculator tries to detect secure icing of opponent:
 * opponent kick in his own half fast to goal line and cannot reach ball himself anymore
 * 
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class BallLeavingFieldCalc extends ACalculator
{
	@Configurable(comment = "check when ball leaving field detection should be used", defValue = "false")
	private static boolean useBallLeavingFieldDetection = true;
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		
		if (!useBallLeavingFieldDetection)
		{
			tacticalField.setBallLeavingFieldGood(false);
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = tacticalField.getDrawableShapes().get(EAiShapesLayer.AI_BALL_LEAVING_FIELD);
		Optional<BotID> lastTouched = tacticalField.getBotsLastTouchedBall().stream()
				.filter(b -> b.getTeamColor() != baseAiFrame.getTeamColor())
				.filter(b -> getWFrame().getBots().keySet().contains(b))
				.findAny();
		if (lastTouched.isPresent())
		{
			ILineSegment ballTravelLine = getBall().getTrajectory().getTravelLineSegment();
			Optional<IVector2> possibleIntersection = Geometry.getGoalOur().getGoalLine()
					.intersectSegment(ballTravelLine);
			Optional<IVector2> possibleGoal = Geometry.getGoalOur().withMargin(2 * Geometry.getBotRadius())
					.getLineSegment().intersectSegment(ballTravelLine);
			if (possibleIntersection.isPresent() && !possibleGoal.isPresent())
			{
				setTacticalFieldAndShapes(tacticalField, shapes,
						opponentsCanNotReachBall(possibleIntersection.get()), possibleIntersection.get());
				return;
			}
		}
		
		tacticalField.setBallLeavingFieldGood(false);
	}
	
	
	private boolean opponentsCanNotReachBall(final IVector2 goalLineIntersection)
	{
		long possibleIntersects1 = getWFrame().foeBots.values().stream()
				.filter(bot -> getBall().getTrajectory().getTravelLine().isPointInFront(bot.getPos()))
				.count();
		long possibleIntersects2 = getWFrame().foeBots.values().stream()
				.filter(bot -> (TrajectoryGenerator.generatePositionTrajectory(bot,
						goalLineIntersection, bot.getMoveConstraints()).getTotalTime()) < getBall()
								.getTrajectory().getTimeByPos(goalLineIntersection))
				.count();
		return possibleIntersects1 + possibleIntersects2 < 1;
	}
	
	
	private void setTacticalFieldAndShapes(final TacticalField newTacticalField, final List<IDrawableShape> shapes,
			final boolean intersectsNotPossible, final IVector2 intersection)
	{
		if (intersectsNotPossible)
		{
			newTacticalField.setBallLeavingFieldGood(true);
			shapes.add(new DrawableCircle(
					Circle.createCircle(getBall().getPos(), Geometry.getBallRadius() * 2), Color.cyan));
			shapes.add(new DrawableTube(
					Tube.create(getBall().getPos(), intersection, Geometry.getBotRadius()), Color.cyan));
		} else
		{
			newTacticalField.setBallLeavingFieldGood(false);
		}
	}
}
