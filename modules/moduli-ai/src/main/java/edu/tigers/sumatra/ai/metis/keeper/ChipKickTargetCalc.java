/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author UlrikeL <ulrike.leipscher@dlr.de>
 */
public class ChipKickTargetCalc extends ACalculator
{
	@Configurable(comment = "minimal chipKick distance for calculation of pass targets ", defValue = "2000")
	private static int radius = 2000;
	
	@Configurable(comment = "amount of possible pass directions for keeper in chipFastState", defValue = "5")
	private static int nSegments = 5;
	
	@Configurable(comment = "max difference to radius for lastTarget that is still tolerated", defValue = "500")
	private static int maxRadiusOffset = 500;
	
	@Configurable(comment = "positive angle (in deg) which the arc should have where possible targets will be", defValue = "90")
	private static int arcAngle = 90;
	
	@Configurable(comment = "hysteresis for choosing new chipKickTarget", defValue = "1.2")
	private static double hysteresis = 1.2;
	
	private Map<IVector2, Double> targetPoints;
	private List<IDrawableShape> shapes;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		targetPoints = new HashMap<>();
		Map<IVector2, ITrackedBot> fastestBotMap = new HashMap<>();
		
		double segAngle = (double) Math.abs(arcAngle) / nSegments;
		
		IVector2 lastTarget = baseAiFrame.getPrevFrame().getTacticalField().getChipKickTarget();
		if (lastTarget != null && lastTarget.distanceTo(getBall().getPos()) < radius + maxRadiusOffset
				&& lastTarget.distanceTo(getBall().getPos()) > radius - maxRadiusOffset)
		{
			targetPoints.put(lastTarget, -1.0);
		}
		
		// calc of possible targets:
		for (int i = 0; i < nSegments; i++)
		{
			if (isNotPointInSegment(i, segAngle, radius, Math.abs(arcAngle), lastTarget))
			{
				IVector2 possibleTarget = calcPossibleTargetPoint(i, segAngle, Math.abs(arcAngle));
				if (possibleTarget != null)
				{
					targetPoints.put(possibleTarget, -1.0);
				}
			}
		}
		
		BotIDMapConst<ITrackedBot> foes = wFrame.getFoeBots();
		BotIDMapConst<ITrackedBot> tigers = wFrame.getTigerBotsVisible();
		
		// calculation of slack time for each target point:
		targetPoints.keySet().forEach(v -> targetPoints.put(v, calcSlackTime(v, foes, tigers, fastestBotMap)));
		
		// draw shapes if ball is in own penalty area:
		if (Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos()))
		{
			drawShapes(newTacticalField, baseAiFrame);
		}
		
		IVector2 target = getChipKickTarget(lastTarget, targetPoints, shapes);
		newTacticalField.setChipKickTarget(target);
		newTacticalField.setChipKickTargetBot(fastestBotMap.get(target));
	}
	
	
	private double calcSlackTime(IVector2 target, BotIDMapConst<ITrackedBot> foes, BotIDMapConst<ITrackedBot> tigers,
			Map<IVector2, ITrackedBot> fastestBotMap)
	{
		double minFoeArrivalTime = foes.values().stream()
				.mapToDouble(bot -> TrajectoryGenerator.generatePositionTrajectory(bot, target).getTotalTime())
				.min()
				.orElse(1000000);
		double minTigerArrivalTime = 1000000;
		ITrackedBot fastestTiger = tigers.values().stream()
				.filter(bot -> !bot.getBotId().equals(getAiFrame().getKeeperId()))
				.min(Comparator
						.comparingDouble(bot -> TrajectoryGenerator.generatePositionTrajectory(bot, target).getTotalTime()))
				.orElse(null);
		if (fastestBotMap != null && fastestTiger != null)
		{
			minTigerArrivalTime = TrajectoryGenerator.generatePositionTrajectory(fastestTiger, target).getTotalTime();
			fastestBotMap.put(target, fastestTiger);
		}
		return minFoeArrivalTime - minTigerArrivalTime;
	}
	
	
	private void drawShapes(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.CHIP_KICK_TARGET);
		double segAngle = (double) Math.abs(arcAngle) / nSegments;
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		
		IArc ballArc = Arc.createArc(getBall().getPos(), radius, AngleMath.deg2rad(-1.0 * Math.abs(arcAngle) / 2),
				AngleMath.deg2rad(Math.abs(arcAngle)));
		shapes.add(new DrawableArc(ballArc, wFrame.getTeamColor().getColor()));
		
		for (int i = 1; i < nSegments; i++)
		{
			shapes.add(new DrawableLine(Line.fromPoints(getBall().getPos(),
					getPointOnArcByAngle(AngleMath.deg2rad(i * segAngle - Math.abs(arcAngle) / 2.0), radius)),
					wFrame.getTeamColor().getColor()));
		}
		
		int shift = getWFrame().getTeamColor() == ETeamColor.BLUE ? 50 : -50;
		for (Map.Entry<IVector2, Double> entry : targetPoints.entrySet())
		{
			shapes.add(new DrawableCircle(Circle.createCircle(entry.getKey(), 40), Color.red));
			shapes.add(new DrawableAnnotation(entry.getKey().addNew(Vector2.fromX(shift)),
					String.format("%.5f", entry.getValue())));
		}
	}
	
	
	private IVector2 calcPossibleTargetPoint(int i, double segAngle, int arcAngle)
	{
		for (int trial = 1; trial < 4; trial++)
		{
			// alternative to new Random.nextDouble() = fixed value like 0.5
			double angle = AngleMath.deg2rad(
					-1.0 * arcAngle / 2 + i * segAngle + new Random(getWFrame().getTimestamp()).nextDouble() * segAngle);
			IVector2 possibleTarget = getPointOnArcByAngle(angle, radius);
			if (Geometry.getField().isPointInShape(possibleTarget)
					&& !Geometry.getPenaltyAreaOur().isPointInShape(possibleTarget))
			{
				return possibleTarget;
			}
		}
		return null;
	}
	
	
	/**
	 * @return target position with maximal slack time
	 */
	private IVector2 getChipKickTarget(IVector2 lastTarget, Map<IVector2, Double> targets, List<IDrawableShape> shapes)
	{
		IVector2 target = Vector2.zero();
		double slackTime = 0;
		if (!targets.isEmpty())
		{
			double lastTargetSlackTime = -1;
			if (lastTarget != null && targets.containsKey(lastTarget))
			{
				lastTargetSlackTime = targets.get(lastTarget);
			}
			Map.Entry<IVector2, Double> result = targets.entrySet().stream()
					.filter(v -> v.getValue() > 0)
					.max(Comparator.comparing(Map.Entry::getValue))
					.orElse(null);
			
			if (result != null)
			{
				target = result.getKey();
				slackTime = result.getValue();
			}
			if (lastTargetSlackTime > 0 && slackTime <= hysteresis * lastTargetSlackTime)
			{
				target = lastTarget;
			}
		}
		// mark chosen target with green circle
		if (Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos()))
		{
			IDrawableShape circ = new DrawableCircle(Circle.createCircle(target, 30), Color.GREEN);
			circ.setStrokeWidth(30);
			shapes.add(circ);
		}
		return target;
	}
	
	
	private IVector2 getPointOnArcByAngle(double angle, int radius)
	{
		double newX;
		double newY;
		newY = Math.sin(angle) * radius + getBall().getPos().y();
		newX = Math.cos(angle) * radius + getBall().getPos().x();
		
		return Vector2.fromXY(newX, newY);
	}
	
	
	private boolean isNotPointInSegment(int i, double angle, int radius, int arcAngle, IVector2 lastTarget)
	{
		return !(lastTarget != null && Math.abs(lastTarget.distanceTo(getBall().getPos()) - radius) < 0.000001 &&
				lastTarget.y() > getPointOnArcByAngle(AngleMath.deg2rad(-1.0 * arcAngle / 2 + i * angle), radius).y() &&
				lastTarget.y() < getPointOnArcByAngle(
						AngleMath.deg2rad(-1.0 * arcAngle / 2 + (1 + i) * angle), radius).y());
	}
	
}
