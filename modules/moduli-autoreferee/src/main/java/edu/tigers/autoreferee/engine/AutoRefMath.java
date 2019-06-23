/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 14, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Collection;
import java.util.stream.Stream;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * Encapsulates static math operations for the autoref rules
 * 
 * @author "Lukas Magel"
 */
public class AutoRefMath
{
	/**  */
	public static double	THROW_IN_DISTANCE						= 100;
	/**  */
	public static double	GOAL_KICK_DISTANCE					= 500;
	/** Minimum distance between the goal line and the kick position during a freekick */
	public static double	DEFENSE_AREA_GOALLINE_DISTANCE	= 600;
	
	/** mm */
	public static double	OFFENSE_FREEKICK_DISTANCE			= 700;
	
	
	/**
	 * Determines the corner kick position that is located closest to the specified position
	 * 
	 * @param pos
	 * @return
	 */
	public static IVector2 getClosestCornerKickPos(final IVector2 pos)
	{
		IVector2 corner = NGeometry.getClosestCorner(pos);
		int ySide = (corner.y() > 0 ? -1 : 1);
		int xSide = (corner.x() > 0 ? -1 : 1);
		return corner.addNew(new Vector2(xSide * THROW_IN_DISTANCE, ySide * THROW_IN_DISTANCE));
	}
	
	
	/**
	 * Determines the corner kick position that is located closest to the specified position
	 * 
	 * @param pos
	 * @return
	 */
	public static IVector2 getClosestGoalKickPos(final IVector2 pos)
	{
		IVector2 corner = NGeometry.getClosestCorner(pos);
		int xSide = (corner.x() > 0 ? -1 : 1);
		int ySide = (corner.y() > 0 ? -1 : 1);
		return corner.addNew(new Vector2(xSide * GOAL_KICK_DISTANCE, ySide * THROW_IN_DISTANCE));
	}
	
	
	/**
	 * This method checks if the specified position is located closer than {@value AutoRefMath#OFFENSE_FREEKICK_DISTANCE}
	 * to the defense area.
	 * If the freekick is to be executed by the attacking team then the ball is positioned at the
	 * closest point that is located {@value AutoRefMath#OFFENSE_FREEKICK_DISTANCE} from the defense area.
	 * If the freekick is to be executed by the defending team then the ball is positioned at one of the two corner
	 * points of the field which are located 600 mm from the goal line and 100 mm from the side line.
	 * 
	 * @param pos
	 * @param kickerColor
	 * @return
	 */
	public static IVector2 getClosestFreekickPos(final IVector2 pos, final ETeamColor kickerColor)
	{
		Rectangle field = NGeometry.getField();
		ETeamColor goalColor = NGeometry.getTeamOfClosestGoalLine(pos);
		IVector2 newKickPos;
		if (goalColor == kickerColor)
		{
			newKickPos = getDefenseKickPos(pos);
		}
		newKickPos = getOffenseKickPos(pos);
		
		/*
		 * Check if the ball is located too close to the touch lines or goal lines
		 */
		int xSide = newKickPos.x() > 0 ? 1 : -1;
		int ySide = newKickPos.y() > 0 ? 1 : -1;
		
		if (Math.abs(newKickPos.x()) > ((field.getxExtend() / 2) - THROW_IN_DISTANCE))
		{
			double newXPos = ((field.getxExtend() / 2) - THROW_IN_DISTANCE) * xSide;
			newKickPos = new Vector2(newXPos, newKickPos.y());
		}
		
		if (Math.abs(newKickPos.y()) > ((field.getyExtend() / 2) - THROW_IN_DISTANCE))
		{
			double newYPos = ((field.getyExtend() / 2) - THROW_IN_DISTANCE) * ySide;
			newKickPos = new Vector2(newKickPos.x(), newYPos);
		}
		
		return newKickPos;
	}
	
	
	private static IVector2 getOffenseKickPos(final IVector2 pos)
	{
		PenaltyArea penArea = NGeometry.getPenaltyArea(NGeometry.getTeamOfClosestGoalLine(pos));
		
		if (penArea.isPointInShape(pos, OFFENSE_FREEKICK_DISTANCE))
		{
			return penArea.nearestPointOutside(pos, OFFENSE_FREEKICK_DISTANCE);
		}
		return pos;
	}
	
	
	private static IVector2 getDefenseKickPos(final IVector2 pos)
	{
		int xSide = pos.x() > 0 ? -1 : 1;
		PenaltyArea penArea = NGeometry.getPenaltyArea(NGeometry.getTeamOfClosestGoalLine(pos));
		if (penArea.isPointInShape(pos, OFFENSE_FREEKICK_DISTANCE))
		{
			return getClosestGoalKickPos(pos)
					.addNew(new Vector2((DEFENSE_AREA_GOALLINE_DISTANCE - GOAL_KICK_DISTANCE) * xSide, 0));
		}
		return pos;
	}
	
	
	/**
	 * Checks if all bots that are located entirely inside the field area are on their own side of the field
	 * 
	 * @param bots
	 * @return true if all bots are in their half of the field
	 */
	public static boolean botsAreOnCorrectSide(final Collection<ITrackedBot> bots)
	{
		Rectangle field = Geometry.getField();
		Stream<ITrackedBot> onFieldBots = bots.stream().filter(
				bot -> {
					return field.isPointInShape(bot.getPos(), Geometry.getBotRadius());
				});
		
		return onFieldBots.allMatch(bot -> {
			Rectangle side = NGeometry.getFieldSide(bot.getTeamColor());
			return side.isPointInShape(bot.getPos());
		});
	}
	
	
	/**
	 * Returns true if all bots have stopped moving
	 * 
	 * @param bots
	 * @return
	 */
	public static boolean botsAreStationary(final Collection<ITrackedBot> bots)
	{
		return bots.stream().allMatch(
				bot -> bot.getVelByTime(0).getLength() < AutoRefConfig.getBotStationarySpeedThreshold());
	}
	
	
	/**
	 * Returns true if the ball has been placed at {@code destPos} with a precision of
	 * {@link AutoRefConfig#getBallPlacementAccuracy()} and is stationary.
	 * 
	 * @param ball
	 * @param destPos
	 * @return
	 */
	public static boolean ballIsPlaced(final TrackedBall ball, final IVector2 destPos)
	{
		return ballIsPlaced(ball, destPos, AutoRefConfig.getBallPlacementAccuracy());
	}
	
	
	/**
	 * @param ball
	 * @param destPos
	 * @param accuracy
	 * @return
	 */
	public static boolean ballIsPlaced(final TrackedBall ball, final IVector2 destPos, final double accuracy)
	{
		double dist = GeoMath.distancePP(ball.getPos(), destPos);
		if ((dist < accuracy) && ballIsStationary(ball))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns {@code true} if the velocity is below the {@link AutoRefConfig#getBallStationarySpeedThreshold()}
	 * threshold.
	 * 
	 * @param ball
	 * @return
	 */
	public static boolean ballIsStationary(final TrackedBall ball)
	{
		return ball.getVel().getLength() < AutoRefConfig.getBallStationarySpeedThreshold();
	}
	
	
	/**
	 * Returns true if all bots are located further than {@link Geometry#getBotToBallDistanceStop()} from the ball
	 * 
	 * @param frame
	 * @return
	 */
	public static boolean botStopDistanceIsCorrect(final SimpleWorldFrame frame)
	{
		Collection<ITrackedBot> bots = frame.getBots().values();
		IVector2 ballPos = frame.getBall().getPos();
		
		return bots.stream().allMatch(
				bot -> GeoMath.distancePP(bot.getPosByTime(0), ballPos) > Geometry.getBotToBallDistanceStop());
	}
	
	
	/**
	 * Calculates the distance between the supplied {@code pos} and the edge of the penalty area.
	 * If the point does not lie inside the penalty area a distance of 0 is returned.
	 * 
	 * @param penArea
	 * @param pos
	 * @return
	 */
	public static double distanceToNearestPointOutside(final PenaltyArea penArea, final IVector2 pos)
	{
		return distanceToNearestPointOutside(penArea, 0, pos);
	}
	
	
	/**
	 * Calculates the distance between the supplied {@code pos} and the edge of the penalty area plus the specified
	 * {@code margin}. If the point does not lie inside the penalty area plus margin a distance of 0 is returned.
	 * 
	 * @param penArea
	 * @param margin
	 * @param pos
	 * @return
	 */
	public static double distanceToNearestPointOutside(final PenaltyArea penArea, final double margin, final IVector2 pos)
	{
		IVector2 nearestPointOutsideMargin = penArea.nearestPointOutside(pos, margin);
		return GeoMath.distancePP(pos, nearestPointOutsideMargin);
	}
}
