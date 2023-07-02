/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;

import java.util.Arrays;
import java.util.List;


/**
 * Wrapper class around {@link Geometry} to provide access based on team colors.
 */
public final class NGeometry
{
	private NGeometry()
	{
	}
	
	
	public static Goal getGoal(final ETeamColor color)
	{
		if (color == Geometry.getNegativeHalfTeam())
		{
			return Geometry.getGoalOur();
		}
		return Geometry.getGoalTheir();
	}
	
	
	public static IRectangle getField()
	{
		return Geometry.getField();
	}
	
	
	public static IRectangle getFieldSide(final ETeamColor color)
	{
		if (color == Geometry.getNegativeHalfTeam())
		{
			return Geometry.getFieldHalfOur();
		}
		return Geometry.getFieldHalfTheir();
	}
	
	
	public static IPenaltyArea getPenaltyArea(final ETeamColor color)
	{
		if (color == Geometry.getNegativeHalfTeam())
		{
			return Geometry.getPenaltyAreaOur();
		}
		return Geometry.getPenaltyAreaTheir();
	}
	
	
	public static IVector2 getPenaltyMark(final ETeamColor color)
	{
		if (color == Geometry.getNegativeHalfTeam())
		{
			return Geometry.getPenaltyMarkOur();
		}
		return Geometry.getPenaltyMarkTheir();
	}
	
	
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
		ILineSegment blueGoalLine = NGeometry.getGoal(ETeamColor.BLUE).getGoalLine();
		ILineSegment yellowGoalLine = NGeometry.getGoal(ETeamColor.YELLOW).getGoalLine();
		
		if (blueGoalLine.distanceTo(pos) < yellowGoalLine.distanceTo(pos))
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
		return getField().getCorners().stream().min(new VectorDistanceComparator(pos))
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
		return getPenaltyAreas().stream().anyMatch(penArea -> penArea.withMargin(margin).isPointInShape(pos));
	}
}
