/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.ai.metis.support.PassTargetGenerationCalc.getSafetyDistanceToPenaltyArea;
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

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.roles.support.PointChecker;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class generates all global Support positions
 */
public class SupportPositionGenerationCalc extends ACalculator
{
	
	@Configurable(defValue = "6000.0")
	private static double maxSupportPositionInFrontBall = 6000;
	
	@Configurable(defValue = "-1500.0")
	private static double minSupportPositionBehindBall = -1500;
	
	@Configurable(defValue = "30")
	private static int numberOfTriesToFindAllPositions = 30;
	
	@Configurable(defValue = "1500.0", comment = "Distance to our penalty area")
	private static double minDistanceToOurPenaltyArea = 1500;
	
	@Configurable(defValue = "2200.0")
	private static double minSupporterDistance = 2200;
	
	private PointChecker pointChecker = new PointChecker();
	private List<IDrawableShape> shapes;
	
	private Set<BotID> desiredOffensiveBots = Collections.emptySet();
	private Random rnd;
	
	/**
	 * default
	 */
	public SupportPositionGenerationCalc()
	{
		pointChecker.useRuleEnforcement();
		pointChecker.addFunction(point -> !Geometry.getPenaltyAreaOur().isPointInShape(point,
				minDistanceToOurPenaltyArea));
		pointChecker.addFunction(p -> p.distanceTo(getBall().getPos()) > getMinSupporterDistance());
		pointChecker.addFunction(p -> p.distanceTo(getWFrame().getFoeBots().values().stream()
				.map(ITrackedBot::getPos)
				.min(Comparator.comparingDouble(p::distanceToSqr))
				.orElse(Geometry.getCenter())) > Geometry.getBotRadius() * 2);
		pointChecker.addFunction(p -> desiredOffensiveBots.stream()
				.map(o -> getWFrame().getBot(o).getPos())
				.allMatch(o -> o.distanceTo(p) > getMinSupporterDistance()));
		
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		pointChecker.setOurPenAreaMargin(getSafetyDistanceToPenaltyArea());
		if (rnd == null)
		{
			rnd = new Random(getWFrame().getTimestamp());
		}
		shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.SUPPORTER_POSITION_SELECTION);
		desiredOffensiveBots = newTacticalField.getOffensiveStrategy().getDesiredBots();
		newTacticalField.setGlobalSupportPositions(generateSupportPositions());
	}
	
	
	private List<SupportPosition> generateSupportPositions()
	{
		double areaCoveredBySupporter = maxSupportPositionInFrontBall + minSupportPositionBehindBall;
		double xOffset = calcOffset();
		long timestamp = getWFrame().getTimestamp();
		List<SupportPosition> allPositions = new LinkedList<>();
		addOldPositions(allPositions, xOffset, areaCoveredBySupporter);
		int numberOfOldPositions = allPositions.size();
		for (int i = 0; i < numberOfTriesToFindAllPositions; i++)
		{
			double x = (rnd.nextDouble()) * areaCoveredBySupporter + xOffset;
			double y = (rnd.nextDouble() - 0.5) * Geometry.getFieldWidth();
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
	
	
	private double calcOffset()
	{
		IVector2 ballPos = getBall().getPos();
		double xOffset = ballPos.x() - minSupportPositionBehindBall;
		xOffset = Math.max(xOffset, Geometry.getGoalOur().getCenter().x());
		xOffset = Math.min(xOffset,
				Geometry.getFieldLength() / 2. - minSupportPositionBehindBall - maxSupportPositionInFrontBall);
		// Drawing
		ILine backLine = Line.fromDirection(Vector2.fromXY(xOffset, -Geometry.getFieldWidth() / 2),
				Vector2.Y_AXIS.scaleToNew(Geometry.getFieldWidth()));
		ILine frontLine = Line.fromDirection(
				Vector2.fromXY(xOffset + minSupportPositionBehindBall + maxSupportPositionInFrontBall,
						-Geometry.getFieldWidth() / 2),
				Vector2.Y_AXIS.scaleToNew(Geometry.getFieldWidth()));
		shapes.add(new DrawableLine(backLine, Color.RED));
		shapes.add(new DrawableLine(frontLine, Color.RED));
		return xOffset;
	}
	
	
	private void addOldPositions(List<SupportPosition> positions, double xOffset, double areaCoveredBySupporter)
	{
		List<SupportPosition> oldPositions = getAiFrame().getPrevFrame().getTacticalField().getSelectedSupportPositions();
		
		for (SupportPosition pos : oldPositions)
		{
			double relativeXZero = pos.getPos().x() - xOffset;
			boolean inSupportArea = relativeXZero > 0 && relativeXZero < areaCoveredBySupporter;
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
