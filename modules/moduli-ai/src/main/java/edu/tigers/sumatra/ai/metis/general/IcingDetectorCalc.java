/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculator tries to detect secure icing of opponent:
 * opponent shot in his own half fast to goal line and cannot reach ball himself anymore
 * 
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class IcingDetectorCalc extends ACalculator
{
	@Configurable(comment = "check when icing detection should be used", defValue = "true")
	private static boolean useIcingDetection = false;
	
	@Configurable(comment = "[m/s] Offset for opponent max velocity", defValue = "0")
	private static double maxVelOffset = 0;
	
	@Configurable(comment = "[m/s^2] Offset for opponent max acceleration", defValue = "0")
	private static double maxAccOffset = 0;
	
	private boolean possibleNewIcingEvent = true;
	private IVector2 kickerPos = null;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (!useIcingDetection)
		{
			newTacticalField.setOpponentWillDoIcing(false);
			return;
		}
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.ICING);
		BotID lastTouched = newTacticalField.getBotLastTouchedBall();
		ITrackedBot botLastTouched = getWFrame().getBot(lastTouched);
		if (botLastTouched != null && lastTouched.getTeamColor() != baseAiFrame.getTeamColor())
		{
			if (possibleNewIcingEvent
					&& Geometry.getFieldHalfTheir().isPointInShape(botLastTouched.getPos())
					&& Math.abs(botLastTouched.getPos().x()) > 200)
			{
				kickerPos = botLastTouched.getPos();
				possibleNewIcingEvent = false;
			}
			if (kickerPos != null)
			{
				ILine ballTravelLine = getBall().getTrajectory().getTravelLine();
				Optional<IVector2> possibleIntersection = Geometry.getGoalOur().getLine().intersectionWith(ballTravelLine);
				if (possibleIntersection.isPresent()
						&& ballCrossesGoalLineOutsideGoal(possibleIntersection.get(), ballTravelLine))
				{
					setTacticalFieldAndShapes(newTacticalField, shapes,
							opponentsCanNotReachBall(possibleIntersection.get()), possibleIntersection.get());
					return;
				}
			}
		}
		resetFields();
		newTacticalField.setOpponentWillDoIcing(false);
	}
	
	
	private boolean opponentsCanNotReachBall(final IVector2 goalLineIntersection)
	{
		long possibleIntersects1 = getWFrame().foeBots.values().stream()
				.filter(bot -> getBall().getPos().x() > bot.getPos().x())
				.filter(bot -> LineMath.isPointInFront(getBall().getTrajectory().getTravelLine(), bot.getPos()))
				.count();
		addOffsetsToMoveConstraints();
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
			newTacticalField.setOpponentWillDoIcing(true);
			shapes.add(new DrawableCircle(
					Circle.createCircle(getBall().getPos(), Geometry.getBallRadius() * 2), Color.cyan));
			shapes.add(new DrawableTube(
					Tube.create(getBall().getPos(), intersection, Geometry.getBotRadius()), Color.cyan));
		} else
		{
			newTacticalField.setOpponentWillDoIcing(false);
			resetFields();
		}
	}
	
	
	private boolean ballCrossesGoalLineOutsideGoal(IVector2 intersects, ILine ballTravelLine)
	{
		return ballTravelLine.getEnd().x() < Geometry.getField().minX()
				&& Math.abs(intersects.y()) > Geometry.getGoalOur().getWidth() / 2 + 2 * Geometry.getBotRadius()
				&& Math.abs(intersects.y()) < Geometry.getField().maxY();
	}
	
	
	private void resetFields()
	{
		possibleNewIcingEvent = true;
		kickerPos = null;
	}
	
	
	private void addOffsetsToMoveConstraints()
	{
		for (ITrackedBot bot : getWFrame().foeBots.values())
		{
			MoveConstraints movCon = bot.getMoveConstraints();
			movCon.setAccMax(movCon.getAccMax() + maxAccOffset);
			movCon.setVelMax(movCon.getVelMax() + maxVelOffset);
		}
	}
}
