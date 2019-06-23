/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * Wrapper class around {@link Geometry} to provide access based on team colors.
 * 
 * @author "Lukas Magel"
 */
public class NGeometry
{
	/**
	 * Sorts points by their distance to a fixed second point
	 * 
	 * @author "Lukas Magel"
	 */
	public static class PointDistanceComparator implements Comparator<IVector2>
	{
		
		private final IVector2	pos;
		
		
		/**
		 * @param pos
		 */
		public PointDistanceComparator(final IVector2 pos)
		{
			this.pos = pos;
		}
		
		
		@Override
		public int compare(final IVector2 p1, final IVector2 p2)
		{
			double distTo1 = GeoMath.distancePP(pos, p1);
			double distTo2 = GeoMath.distancePP(pos, p2);
			
			if (distTo1 < distTo2)
			{
				return -1;
			} else if (distTo1 > distTo2)
			{
				return 1;
			}
			return 0;
		}
		
	}
	
	/**
	 * @author "Lukas Magel"
	 */
	public static class BotDistanceComparator implements Comparator<ITrackedBot>
	{
		
		private PointDistanceComparator	comparator;
		
		
		/**
		 * @param pos
		 */
		public BotDistanceComparator(final IVector2 pos)
		{
			comparator = new PointDistanceComparator(pos);
		}
		
		
		@Override
		public int compare(final ITrackedBot o1, final ITrackedBot o2)
		{
			return comparator.compare(o1.getPos(), o2.getPos());
		}
		
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static Line getGoalLine(final ETeamColor color)
	{
		if (color == TeamConfig.getLeftTeam())
		{
			return Geometry.getGoalLineOur();
		}
		return Geometry.getGoalLineTheir();
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static Goal getGoal(final ETeamColor color)
	{
		if (color == TeamConfig.getLeftTeam())
		{
			return Geometry.getGoalOur();
		}
		return Geometry.getGoalTheir();
	}
	
	
	/**
	 * @return
	 */
	public static Rectangle getField()
	{
		return Geometry.getField();
	}
	
	
	/**
	 * @return
	 */
	public static double getGoalSize()
	{
		return Geometry.getGoalSize();
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static Rectangle getFieldSide(final ETeamColor color)
	{
		if (color == TeamConfig.getLeftTeam())
		{
			return Geometry.getHalfOur();
		}
		return Geometry.getHalfTheir();
	}
	
	
	/**
	 * @return
	 */
	public static IVector2 getCenter()
	{
		return Geometry.getCenter();
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static PenaltyArea getPenaltyArea(final ETeamColor color)
	{
		if (color == TeamConfig.getLeftTeam())
		{
			return Geometry.getPenaltyAreaOur();
		}
		return Geometry.getPenaltyAreaTheir();
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static IVector2 getPenaltyMark(final ETeamColor color)
	{
		if (color == TeamConfig.getLeftTeam())
		{
			return Geometry.getPenaltyMarkOur();
		}
		return Geometry.getPenaltyMarkTheir();
	}
	
	
	/**
	 * The no-go area for bots other than the kicker during a penalty kick
	 * 
	 * @param color
	 * @return
	 */
	public static Rectangle getPenaltyKickArea(final ETeamColor color)
	{
		if (color == TeamConfig.getLeftTeam())
		{
			return Geometry.getPenaltyKickAreaOur();
		}
		return Geometry.getPenaltyKickAreaTheir();
	}
	
	
	/**
	 * @return
	 */
	public static List<PenaltyArea> getPenaltyAreas()
	{
		return Arrays.asList(Geometry.getPenaltyAreaOur(), Geometry.getPenaltyAreaTheir());
	}
	
	
	/**
	 * Determines the color of the team whose goal line is located closest to the ball
	 * 
	 * @param pos
	 * @return
	 */
	public static ETeamColor getTeamOfClosestGoalLine(final IVector2 pos)
	{
		Line goalLineBlue = getGoalLine(ETeamColor.BLUE);
		Line goalLineYellow = getGoalLine(ETeamColor.YELLOW);
		
		if (GeoMath.distancePL(pos, goalLineBlue) < GeoMath
				.distancePL(pos, goalLineYellow))
		{
			return ETeamColor.BLUE;
		}
		return ETeamColor.YELLOW;
	}
	
	
	/**
	 * Returns the corner of the field which is located closest to the specified point
	 * The result is not deterministic if the supplied position is spaced equally apart to more than one corner
	 * 
	 * @param pos
	 * @return
	 */
	public static IVector2 getClosestCorner(final IVector2 pos)
	{
		return getField().getCorners().stream().sorted(new PointDistanceComparator(pos)).findFirst().get();
	}
	
	
	/**
	 * Returns true if the specified position is located inside one of the two goals
	 * 
	 * @param pos
	 * @return
	 */
	public static boolean ballInsideGoal(final IVector2 pos)
	{
		return ballInsideGoal(pos, 0);
	}
	
	
	/**
	 * Returns true if the specified position is located inside one of the two goals
	 * The area behind the goal (abs(x) <= GoalDepth + goalDepthMargin) up to the margin is considered to be part of the
	 * goal
	 * 
	 * @param pos
	 * @param goalDepthMargin
	 * @return
	 */
	public static boolean ballInsideGoal(final IVector2 pos, final double goalDepthMargin)
	{
		Rectangle field = getField();
		double absXPos = Math.abs(pos.x());
		double absYPos = Math.abs(pos.y());
		
		boolean xPosCorrect = (absXPos > (field.getxExtend() / 2))
				&& (absXPos < ((field.getxExtend() / 2) + Geometry.getGoalDepth() + goalDepthMargin));
		boolean yPosCorrect = absYPos < (Geometry.getGoalSize() / 2);
		return xPosCorrect && yPosCorrect;
	}
	
	
	/**
	 * @param pos
	 * @return
	 */
	public static boolean posInsidePenaltyArea(final IVector2 pos)
	{
		return getPenaltyAreas().stream().anyMatch(penArea -> penArea.isPointInShape(pos));
	}
}
