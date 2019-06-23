/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Wrapper class around {@link Geometry} to provide access based on team colors.
 * 
 * @author "Lukas Magel"
 */
public final class NGeometry
{
	private NGeometry()
	{
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static Goal getGoal(final ETeamColor color)
	{
		if (color == Geometry.getNegativeHalfTeam())
		{
			return Geometry.getGoalOur();
		}
		return Geometry.getGoalTheir();
	}
	
	
	/**
	 * @return
	 */
	public static IRectangle getField()
	{
		return Geometry.getField();
	}
	
	
	/**
	 * @return
	 */
	public static double getGoalSize()
	{
		return Geometry.getGoalOur().getWidth();
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static IRectangle getFieldSide(final ETeamColor color)
	{
		if (color == Geometry.getNegativeHalfTeam())
		{
			return Geometry.getFieldHalfOur();
		}
		return Geometry.getFieldHalfTheir();
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
	public static IPenaltyArea getPenaltyArea(final ETeamColor color)
	{
		if (color == Geometry.getNegativeHalfTeam())
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
		if (color == Geometry.getNegativeHalfTeam())
		{
			return Geometry.getPenaltyMarkOur();
		}
		return Geometry.getPenaltyMarkTheir();
	}
	
	
	/**
	 * @return
	 */
	public static List<IPenaltyArea> getPenaltyAreas()
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
		ILine blueGoalLine = NGeometry.getGoal(ETeamColor.BLUE).getLine();
		ILine yellowGoalLine = NGeometry.getGoal(ETeamColor.YELLOW).getLine();
		
		if (LineMath.distancePL(pos, blueGoalLine) < LineMath
				.distancePL(pos, yellowGoalLine))
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
		return getField().getCorners().stream().sorted(new PointDistanceComparator(pos)).findFirst()
				.orElseThrow(IllegalStateException::new);
	}
	
	
	/**
	 * Returns true if the specified position is located inside one of the two goals
	 *
	 * @param pos
	 * @return
	 */
	public static boolean ballInsideGoal(final IVector3 pos)
	{
		return ballInsideGoal(pos, 0, 0);
	}
	
	
	/**
	 * Returns true if the specified position is located inside one of the two goals
	 * The area behind the goal and on each side of the goal up to the margin is considered to be part of the goal.
	 *
	 * @param pos
	 * @param goalLineMarginX [mm] moves goal line in x direction (adds margin to field)
	 * @param goalMarginDepth [mm] margin to add to goal depth
	 * @return
	 */
	public static boolean ballInsideGoal(final IVector3 pos, final double goalLineMarginX, final double goalMarginDepth)
	{
		IRectangle field = getField();
		double absXPos = Math.abs(pos.x());
		double absYPos = Math.abs(pos.y());
		
		boolean xPosCorrect = (absXPos > (field.withMargin(goalLineMarginX).xExtent() / 2))
				&& (absXPos < ((field.xExtent() / 2) + Geometry.getGoalOur().getDepth() + goalMarginDepth));
		boolean yPosCorrect = absYPos < (Geometry.getGoalOur().getWidth() / 2);
		boolean zPosCorrect = pos.z() < Geometry.getGoalHeight();
		return xPosCorrect && yPosCorrect && zPosCorrect;
	}
	
	
	/**
	 * @param pos
	 * @param margin
	 * @return
	 */
	public static boolean posInsidePenaltyArea(final IVector2 pos, final double margin)
	{
		return getPenaltyAreas().stream().anyMatch(penArea -> penArea.isPointInShape(pos, margin));
	}
	
	/**
	 * Sorts points by their distance to a fixed second point
	 *
	 * @author "Lukas Magel"
	 */
	public static class PointDistanceComparator implements Comparator<IVector2>
	{
		
		private final IVector2 pos;
		
		
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
			double distTo1 = VectorMath.distancePP(pos, p1);
			double distTo2 = VectorMath.distancePP(pos, p2);
			
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
		
		private PointDistanceComparator comparator;
		
		
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
}
