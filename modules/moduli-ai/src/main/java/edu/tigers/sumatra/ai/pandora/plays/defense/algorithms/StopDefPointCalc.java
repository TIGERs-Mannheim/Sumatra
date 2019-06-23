/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms;

import java.util.Map;
import java.util.Map.Entry;

import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * Deal with stop situation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author FelixB <bayer.fel@gmail.com>
 */
public class StopDefPointCalc
{
	
	/**
	 * @param ballPos
	 * @param defenderDistribution
	 */
	public static void modifyDistributionForStop(final IVector2 ballPos,
			final Map<DefenderRole, DefensePoint> defenderDistribution)
	{
		for (Entry<DefenderRole, DefensePoint> entry : defenderDistribution.entrySet())
		{
			DefensePoint defPoint = entry.getValue();
			DefenderRole defRole = entry.getKey();
			
			defPoint.set(modifyPositionForStopSituation(ballPos, defPoint, defPoint.getProtectAgainst()));
			defenderDistribution.put(defRole, defPoint);
		}
	}
	
	
	/**
	 * @param ballPos
	 * @param placementPos
	 * @param defenderDistribution
	 */
	public static void modifyDistributionForBallPlacement(final IVector2 ballPos, final IVector2 placementPos,
			final Map<DefenderRole, DefensePoint> defenderDistribution)
	{
		for (Entry<DefenderRole, DefensePoint> entry : defenderDistribution.entrySet())
		{
			DefensePoint defPoint = entry.getValue();
			DefenderRole defRole = entry.getKey();
			
			defPoint.set(modifyPositionForStopSituation(ballPos, defPoint, defPoint.getProtectAgainst()));
			defPoint.set(modifyPositionForPlacementSituation(placementPos, defPoint, defPoint.getProtectAgainst()));
			
			defenderDistribution.put(defRole, defPoint);
		}
	}
	
	
	/**
	 * @param placementPos
	 * @param oldPosition
	 * @param protectAgainst
	 * @return
	 */
	private static IVector2 modifyPositionForPlacementSituation(final IVector2 placementPos, final IVector2 oldPosition,
			final IVector2 protectAgainst)
	{
		IVector2 newDefPointPosition = new Vector2(oldPosition);
		
		double stopDistance = Geometry.getBotToBallDistanceStop() + (1.5f * Geometry.getBotRadius());
		Circle forbiddenCircle = Circle.getNewCircle(placementPos, stopDistance);
		
		if (forbiddenCircle.isPointInShape(newDefPointPosition))
		{
			newDefPointPosition = GeoMath.stepAlongLine(placementPos, newDefPointPosition, stopDistance);
		}
		
		return newDefPointPosition;
	}
	
	
	/**
	 * @param ballPos
	 * @param oldPosition
	 * @param protectAgainst
	 * @return
	 */
	public static IVector2 modifyPositionForStopSituation(final IVector2 ballPos, final IVector2 oldPosition,
			final IVector2 protectAgainst)
	{
		
		IVector2 newDefPointPosition = new Vector2(oldPosition);
		
		double stopDistance = Geometry.getBotToBallDistanceStop() + (5f * Geometry.getBotRadius());
		Circle forbiddenCircle = Circle.getNewCircle(ballPos, stopDistance);
		double penAreaMargin = Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin();
		PenaltyArea penAreaOur = Geometry.getPenaltyAreaOur();
		
		// if ((newDefPointPosition.x() > ZoneDefenseCalc.getMaxXBotBlockingDefender()))
		// {
		// newDefPointPosition = new Vector2(ZoneDefenseCalc.getMaxXBotBlockingDefender(), newDefPointPosition.y());
		// }
		
		if (forbiddenCircle.isPointInShape(newDefPointPosition))
		{
			newDefPointPosition = GeoMath.stepAlongLine(ballPos, newDefPointPosition, stopDistance);
		}
		
		if (protectAgainst != null)
		{
			
			newDefPointPosition = penAreaOur.nearestPointOutside(newDefPointPosition, protectAgainst, penAreaMargin);
		} else
		{
			
			newDefPointPosition = penAreaOur.nearestPointOutside(newDefPointPosition, penAreaMargin);
		}
		
		return newDefPointPosition;
	}
}
