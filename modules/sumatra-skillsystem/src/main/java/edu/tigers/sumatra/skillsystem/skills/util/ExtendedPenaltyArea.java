/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawablePath;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * ExtendedPenaltyArea
 *
 * @author Jonas, Stefan
 */
public class ExtendedPenaltyArea
{
	private final IPenaltyArea penaltyArea;
	
	private final double distanceBetweenBots = Geometry.getBotRadius();
	
	private final double slope = Math.tan(Math.PI / 6);
	
	private List<InterferingBot> interferingBots = new ArrayList<>();
	
	private double extendedConstMargin;

	/**
	 * Constructor
	 *
	 * @param newRadius
	 */
	public ExtendedPenaltyArea(final double newRadius)
	{
		extendedConstMargin = newRadius - Geometry.getPenaltyAreaOur().getRadius();
		penaltyArea = Geometry.getPenaltyAreaOur().withMargin(extendedConstMargin);
	}
	
	
	/**
	 * Calculate a Point on ExtendedPenaltyArea, that intersects with a Line through goal and given point
	 *
	 * @param point defines a Line through this point and the own goal
	 * @return the position on the extended PenArea, that intersects with a Line through goal and given point
	 */
	public IVector2 lineIntersectionsBallGoalLine(final IVector2 point)
	{
		Line ballGoalLine = Line.fromPoints(Geometry.getGoalOur().getCenter(), point);
		List<IVector2> lineIntersectionPoints = penaltyArea.lineIntersections(ballGoalLine);
		if (!lineIntersectionPoints.isEmpty())
		{
			IVector2 direction = ballGoalLine.directionVector().normalizeNew();
			double length = penaltyArea.lengthToPointOnPenArea(lineIntersectionPoints.get(0));
			double offset = getOffset(length);
			return lineIntersectionPoints.get(0).addNew(direction.multiplyNew(offset));
		}
		return null;
	}
	
	
	/**
	 * Calculates a specific Offset for every Point on the PenArea
	 *
	 * @param length in [0,circumference] defines Point on PenArea
	 * @return the Offset for the given length
	 */
	public double getOffset(final double length)
	{
		double offset = 0;
		
		for (InterferingBot bot : interferingBots)
		{
			if (Math.abs(bot.getPeakValue()) > (slope * Math.abs(bot.positionLength - length)))
			{
				double x = length - bot.positionLength;
				if (Math.abs(bot.getOffset(x)) > Math.abs(offset))
				{
					offset = bot.getOffset(x);
				}
			}
		}
		return offset;
	}
	
	
	/**
	 * Searches for interfering Bots and add them to a List (interferingBots)
	 *
	 * @param worldFrame
	 * @param drawableShapes
	 */
	public void updatePenArea(final WorldFrame worldFrame, final ShapeMap drawableShapes)
	{
		// detect all interfering Bots
		interferingBots.clear();
		for (ITrackedBot trackedBot : worldFrame.getFoeBots().values())
		{
			IVector2 botPosition = trackedBot.getPos();
			detectInterferingBot(botPosition);
		}
		
		solvePathProblems();
		
		// draw ExtendedPenArea
		List<IVector2> list = new ArrayList<>();
		for (double d = 15; d < (penaltyArea.getLength()); d += 100.0)
		{
			list.add(stepAlongPenaltyAreaWithOffset(d));
		}
		
		DrawablePath db = new DrawablePath(list, Color.blue);
		drawableShapes.get(ESkillShapesLayer.PENALTY_AREA_DEFENSE).add(db);
		
		// draw Maximum on left and right of Ball
		IVector2 maxNegative = getNextMaximum(penaltyArea.lengthToPointOnPenArea(worldFrame.getBall().getPos()), -1);
		IVector2 maxPositiv = getNextMaximum(penaltyArea.lengthToPointOnPenArea(worldFrame.getBall().getPos()), 1);
		
		if (maxNegative != null)
		{
			drawableShapes.get(ESkillShapesLayer.PENALTY_AREA_DEFENSE).add(new DrawablePoint(maxNegative, Color.red));
		}
		if (maxPositiv != null)
		{
			drawableShapes.get(ESkillShapesLayer.PENALTY_AREA_DEFENSE).add(new DrawablePoint(maxPositiv, Color.darkGray));
		}
	}
	
	
	/**
	 * decides if the Bot at <botPosition> is an interfering bot
	 */
	private void detectInterferingBot(final IVector2 botPosition)
	{
		double botDistanceToPenArea = botPosition.subtractNew(penaltyArea.stepAlongPenArea(botPosition, 0)).getLength();
		if (penaltyArea.isPointInShape(botPosition, 0.0))
		{
			botDistanceToPenArea *= -1;
		}
		double botDistanceToExPenAreaWithoutOffset = botDistanceToPenArea;
		
		double detectionOffsetLimit = (Geometry.getBotRadius() * 2) + distanceBetweenBots;
		
		InterferingBot interferingBot = new InterferingBot(penaltyArea.lengthToPointOnPenArea(botPosition),
				detectionOffsetLimit, botDistanceToExPenAreaWithoutOffset);
		
		// If bot is in ExtendedPenaltyArea Shape (without Offset)
		if (botDistanceToExPenAreaWithoutOffset < 0)
		{
			if (interferingBot.getPeakValue() > 0)
			{
				interferingBots.add(interferingBot);
			}
		}
		// Bot is close to ExtendedPenaltyArea
		else if (botDistanceToExPenAreaWithoutOffset < detectionOffsetLimit)
		{
			interferingBot.scale = -1; // negative Offset
			if(extendedConstMargin + interferingBot.getPeakValue() - Geometry.getBotRadius() < 0)	//negative Offset Peak Value is in Penalty Area
				interferingBot.scale = 1;
			interferingBots.add(interferingBot);
		}
	}
	
	
	/**
	 * Solve merging overlapping negative and positive Offsets
	 */
	private void solvePathProblems()
	{
		interferingBots.sort((o1, o2) -> o1.positionLength > o2.positionLength ? 1 : -1);
		final double sqrt3 = Math.sqrt(3);
		for (int i = 0; i < (interferingBots.size() - 1); i++)
		{
			InterferingBot bot1 = interferingBots.get(i);
			InterferingBot bot2 = interferingBots.get(i + 1);
			boolean botsHaveDifferentOffsets = (bot1.scale + bot2.scale) == 0;
			boolean offsetRangeOverlapping = (bot1.positionLength
					+ (sqrt3 * Math.abs(bot1.getPeakValue()))) > (bot2.positionLength
							- (sqrt3 * Math.abs(bot2.getPeakValue())));
			if (!(botsHaveDifferentOffsets && offsetRangeOverlapping))
			{
				continue; // Overlapping negative and positive Path
			}
			
			// set both Offsets positive
			bot1.scale = 1;
			bot2.scale = 1;
			
			// Correct previous Offsets
			for (int j = i; 0 < j; j--)
			{
				bot1 = interferingBots.get(j - 1);
				bot2 = interferingBots.get(j);
				
				botsHaveDifferentOffsets = (bot1.scale + bot2.scale) == 0;
				offsetRangeOverlapping = (bot1.positionLength
						+ (sqrt3 * Math.abs(bot1.getPeakValue()))) > (bot2.positionLength
								- (sqrt3 * Math.abs(bot2.getPeakValue())));
				if (botsHaveDifferentOffsets && offsetRangeOverlapping)
				{
					bot1.scale = 1;
					bot2.scale = 1;
				} else
				{
					break;
				}
			}
		}
	}
	
	
	/**
	 * Class represents an interfering Bot on the extended Penalty Area
	 */
	private class InterferingBot
	{
		
		private double positionLength; // Position of interfering Bot at PenaltyArea
		private double minDistance; // minimal Distance between Bot and Offset
		private int scale; // positive (= 1) and negative (= -1) Offset
		private double constantOffset; // additional constant Offset in the range of interfering Bot
		
		
		public InterferingBot(final double positionLength, final double minDistance)
		{
			this.positionLength = positionLength;
			this.minDistance = minDistance;
			scale = 1;
			constantOffset = 0;
		}
		
		
		public InterferingBot(final double positionLength, final double minDistance, final double constantOffset)
		{
			this(positionLength, minDistance);
			this.constantOffset = constantOffset;
		}
		
		
		/**
		 * Calculates the Offset for the specific Bot at given length
		 *
		 * @param inputLength in [0,circumference] defines Point on PenArea
		 * @return Offset at the Position of given length
		 */
		public double getOffset(final double inputLength)
		{
			return getPeakValue() - ((Math.abs(slope * inputLength)) * scale);
		}
		
		
		private double getPeakValue()
		{
			return ((scale * minDistance) / Math.sin(Math.PI / 3)) + constantOffset;
		}
	}
	
	
	/**
	 * @param lengthToBot length on penalty area disregarding offset
	 * @param directionSign 1 for positive / -1 for negative
	 * @return the point where the offset has its maximum
	 *         (return null if there is no Maximum)
	 */
	public IVector2 getNextMaximum(final double lengthToBot, final int directionSign)
	{
		double lengthMaximum = 0;
		double minDistance = Double.MAX_VALUE;
		boolean noMaximumFound = true;
		
		for (InterferingBot bot : interferingBots)
		{
			double delta = lengthToBot - bot.positionLength;
			if (((directionSign * delta) > 0) && (Math.abs(delta) < minDistance))
			{
				noMaximumFound = false;
				minDistance = Math.abs(delta);
				lengthMaximum = bot.positionLength;
			}
		}
		
		if (!noMaximumFound)
		{
			return stepAlongPenaltyAreaWithOffset(lengthMaximum);
		}
		
		return null;
	}
	
	
	/**
	 * @param length the position of a Point on PenaltyArea
	 * @return the point with corresponding Offset that is <length> away when stepping on penalty area line
	 */
	public IVector2 stepAlongPenaltyAreaWithOffset(final double length)
	{
		IPenaltyArea extendedPenaltyArea = penaltyArea.withMargin(getOffset(length));
		IVector2 projectionPenaltyArea = penaltyArea.stepAlongPenArea(length);
		return extendedPenaltyArea.stepAlongPenArea(projectionPenaltyArea, 0);
	}
	
	
	/**
	 * @param startPoint some Point defining a projected Point on PenaltyArea
	 * @param additionalLength length to be stepped from <startPoint>
	 * @return the point with corresponding Offset that is <length> away from <startPoint> when stepping on penalty area
	 *         line
	 */
	public IVector2 stepAlongPenaltyAreaWithOffset(final IVector2 startPoint, final double additionalLength)
	{
		IVector2 projectionPenaltyArea = penaltyArea.stepAlongPenArea(startPoint, additionalLength);
		double length = penaltyArea.lengthToPointOnPenArea(projectionPenaltyArea);
		return pointWithOffset(projectionPenaltyArea, getOffset(length));
	}
	
	
	/**
	 * @param startPoint some Point defining a projected Point on PenaltyArea
	 * @param additionalLength length to be stepped from <startPoint>
	 * @return the point that is <length> away from <startPoint> when stepping on penalty area line
	 */
	public IVector2 stepAlongPenArea(final IVector2 startPoint, final double additionalLength)
	{
		return penaltyArea.stepAlongPenArea(startPoint, additionalLength);
	}
	
	
	/**
	 * @param length to step along penalty area
	 * @return the point that is <length> away from penalty area start point (positive arc)
	 */
	public IVector2 stepAlongPenArea(final double length)
	{
		return penaltyArea.stepAlongPenArea(length);
	}
	
	
	/**
	 * @param basePoint a point on the extended penalty area
	 * @param offset (may be negative)
	 * @return pointWithOffset starting from <basePoint> this point is <offset> away from extendedPenArea
	 */
	public IVector2 pointWithOffset(final IVector2 basePoint, final double offset)
	{
		IPenaltyArea extendedPenaltyArea = penaltyArea.withMargin(offset);
		return extendedPenaltyArea.stepAlongPenArea(basePoint, 0);
	}
	
	
	/**
	 * @param point on Field
	 * @return true if <position> lies in Shape of ExtendedPenaltyArea
	 */
	public boolean isPointInShapeWithOffset(final IVector2 point)
	{
		return penaltyArea.isPointInShape(point, getOffset(penaltyArea.lengthToPointOnPenArea(point)));
	}
	
	
	/**
	 * @param point on Field
	 * @param margin of Shape
	 * @return true if <position> lies in Shape of ExtendedPenaltyArea with <margin>
	 */
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return penaltyArea.isPointInShape(point, getOffset(penaltyArea.lengthToPointOnPenArea(point)) + margin);
	}
	
	
	/**
	 * @param point
	 * @return projected Point of <point>
	 */
	public IVector2 projectPointOnPenaltyAreaLine(final IVector2 point)
	{
		return penaltyArea.projectPointOnPenaltyAreaLine(point);
	}
	
	
	/**
	 * @param point on Field
	 * @return get length of projected <point> on ExtendedPenaltyArea
	 */
	public double lengthToPointOnPenArea(final IVector2 point)
	{
		return penaltyArea.lengthToPointOnPenArea(point);
	}
	
	
	/**
	 * @return total Length of ExtendedPenaltyArea
	 */
	public double getLength()
	{
		return penaltyArea.getLength();
	}
	
}

