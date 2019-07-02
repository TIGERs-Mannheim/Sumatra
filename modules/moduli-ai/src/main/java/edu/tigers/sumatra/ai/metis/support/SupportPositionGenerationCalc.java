/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.ai.metis.support.SupportPositionSelectionCalc.getNumberOfOffensivePositions;
import static edu.tigers.sumatra.ai.metis.support.SupportPositionSelectionCalc.getNumberOfPassPositions;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.PassReceiver;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class generates all global Support positions
 */
public class SupportPositionGenerationCalc extends ACalculator
{
	@Configurable(defValue = "30")
	private static int numberOfTriesToFindAllPositions = 30;
	
	@Configurable(defValue = "1500.0", comment = "Distance to our penalty area")
	private static double minDistanceToOurPenaltyArea = 1500;
	
	@Configurable(defValue = "2200.0", comment = "Minimum supporter Distance")
	private static double minSupporterDistance = 2200;
	
	@Configurable(defValue = "1000.0", comment = "Radius of the inner (blocked) arc")
	private static double ignoredArcRadius = 1000;
	
	@Configurable(defValue = "4000.0", comment = "Radius of the outer search arc")
	private static double searchArcRadius = 4000;
	
	@Configurable(defValue = "0.5", comment = "fixed arc angle in own half [pi (rad)]")
	private static double ownHalfAngle = 0.5;
	
	@Configurable(defValue = "1.5", comment = "how much should the angle grow in the other half [pi (rad)]")
	private static double maxAngleGrowth = 1.5;
	
	@Configurable(defValue = "500.0", comment = "distance for full angle (from opponents base line)")
	private static double maxAngleReachedOffset = 500;
	
	@Configurable(defValue = "1500.0", comment = "Safety distance to keep to penalty area")
	private static double safetyDistanceToPenaltyArea = 1500.0;
	
	private final PointChecker pointChecker;
	private List<IDrawableShape> shapes;
	
	private Set<BotID> desiredOffensiveBots = Collections.emptySet();
	private Random rnd;
	
	
	public SupportPositionGenerationCalc()
	{
		pointChecker = new PointChecker()
				.checkBallDistances()
				.checkInsideField()
				.checkNotInPenaltyAreas()
				.checkConfirmWithKickOffRules()
				.checkCustom(point -> !Geometry.getPenaltyAreaOur().isPointInShape(point,
						minDistanceToOurPenaltyArea))
				.checkCustom(p -> p.distanceTo(getWFrame().getFoeBots().values().stream()
						.map(ITrackedBot::getPos)
						.min(Comparator.comparingDouble(p::distanceToSqr))
						.orElse(Geometry.getCenter())) > Geometry.getBotRadius() * 2)
				.checkCustom(p -> desiredOffensiveBots.stream()
						.map(o -> getWFrame().getBot(o).getPos())
						.allMatch(o -> o.distanceTo(p) > getMinSupporterDistance()));
	}
	
	
	@Override
	public boolean isCalculationNecessary(TacticalField tacticalField, BaseAiFrame aiFrame)
	{
		return PassReceiver.isActive();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		pointChecker.setOurPenAreaMargin(safetyDistanceToPenaltyArea);
		if (rnd == null)
		{
			rnd = new Random(getWFrame().getTimestamp());
		}
		shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.SUPPORTER_POSITION_SELECTION);
		desiredOffensiveBots = newTacticalField.getOffensiveStrategy().getDesiredBots();
		newTacticalField.setGlobalSupportPositions(generateSupportPositions());
	}
	
	
	/**
	 * Display the calculated arcs in the visualizer
	 * 
	 * @param mainArc Outer arc
	 * @param blockedArc Inner arc
	 */
	private void renderDebugInfo(IArc mainArc, IArc blockedArc)
	{
		shapes.add(new DrawableArc(mainArc, Color.GREEN));
		shapes.add(new DrawableArc(blockedArc, Color.RED));
	}
	
	
	/**
	 * This method will generate random support positions
	 *
	 * @return a list of available Support Positions
	 */
	private List<SupportPosition> generateSupportPositions()
	{
		double maxAngle = getMaxAngle();
		double maxRadius = getArcRadius();
		double minRadius = getIgnoredArcRadius();
		IVector2 origin = getBall().getPos();
		
		IArc mainArc = Arc.createArc(origin, maxRadius, -maxAngle / 2, maxAngle);
		IArc blockedArc = Arc.createArc(origin, minRadius, -maxAngle / 2, maxAngle);
		renderDebugInfo(mainArc, blockedArc);
		
		long timestamp = getWFrame().getTimestamp();
		List<SupportPosition> allPositions = new LinkedList<>();
		addOldPositions(allPositions, mainArc, blockedArc);
		int numberOfOldPositions = allPositions.size();
		
		for (int i = 0; i < numberOfTriesToFindAllPositions; i++)
		{
			double angle = (rnd.nextDouble() - 0.5) * maxAngle;
			double dist = rnd.nextDouble() * (maxRadius - minRadius) + minRadius;
			
			double x = origin.x() + SumatraMath.cos(angle) * dist;
			double y = origin.y() + SumatraMath.sin(angle) * dist;
			
			IVector2 position = Vector2.fromXY(x, y);
			if (pointChecker.allMatch(getAiFrame(), position)
					&& allPositions.stream().allMatch(p -> p.getPos().distanceTo(position) > Geometry.getBotRadius()))
			{
				allPositions.add(new SupportPosition(position, timestamp));
			}
			if (allPositions.size() >= getNumberOfOffensivePositions() + getNumberOfPassPositions() + numberOfOldPositions)
			{
				break;
			}
		}
		return allPositions;
	}
	
	
	private boolean ballInOurHalf()
	{
		return getBall().getPos().x() < 0;
	}
	
	
	/**
	 * If the ball is in our half, we will use a fixed angle. In the
	 * other half we will use a linear scaling one.
	 * 
	 * @return Maximum angle of the search arc in rad
	 */
	private double getMaxAngle()
	{
		if (ballInOurHalf())
		{
			return ownHalfAngle * Math.PI;
		} else
		{
			double factor = SumatraMath.min(
					Math.abs(getBall().getPos().x()) / (Geometry.getFieldLength() / 2.0 - maxAngleReachedOffset),
					1);
			
			return (ownHalfAngle + maxAngleGrowth * factor) * Math.PI;
		}
	}
	
	
	/**
	 * @return Radius of the Arc
	 */
	private double getArcRadius()
	{
		return searchArcRadius;
	}
	
	
	/**
	 * @return Radius of the inner arc which will be avoided
	 */
	private double getIgnoredArcRadius()
	{
		return ignoredArcRadius;
	}
	
	
	/**
	 * Add previously selected points which are still valid back to the new list
	 * 
	 * @param positions The new position list
	 * @param mainArc The main search arc
	 * @param invalidArc The inner arc where no position is allowed
	 */
	private void addOldPositions(List<SupportPosition> positions, IArc mainArc, IArc invalidArc)
	{
		List<SupportPosition> oldPositions = getAiFrame().getPrevFrame().getTacticalField().getSelectedSupportPositions();
		
		for (SupportPosition pos : oldPositions)
		{
			boolean inSupportArea = mainArc.isPointInShape(pos.getPos()) && !invalidArc.isPointInShape(pos.getPos());
			if (pointChecker.allMatch(getAiFrame(), pos.getPos()) && inSupportArea)
			{
				pos.setCovered(false);
				pos.setShootPosition(false);
				positions.add(pos);
			}
		}
	}
	
	
	public static double getMinSupporterDistance()
	{
		return minSupporterDistance;
	}
}
