/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 11, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author "Lukas Magel"
 */
public class BallHeadingCalc extends ACalculator
{
	private static double	ANGLE_THRESHOLD_DEGREE	= 10;
	/** in mm */
	private static double	MIN_SEARCH_RADIUS			= 300;
	
	private static double	BOT_RADIUS_MARGIN			= 30;
	
	/**
	 * @author lukas
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
	 * @author lukas
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
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.BALL_POSSESSION);
		TrackedBall curBall = baseAiFrame.getWorldFrame().getBall();
		TrackedBall prevBall = baseAiFrame.getPrevFrame().getWorldFrame().getBall();
		
		
		IVector2 prevHeading = prevBall.getVel();
		IVector2 curHeading = curBall.getVel();
		
		double radAngle = GeoMath.angleBetweenVectorAndVector(prevHeading, curHeading);
		if (radAngle > Math.toRadians(ANGLE_THRESHOLD_DEGREE))
		{
			IVector2 ballPos = curBall.getPos();
			ILine ballHeadingLine = new Line(ballPos, curBall.getVel().multiplyNew(-1.0d));
			List<ITrackedBot> closeBots = getCloseBots(baseAiFrame, ballPos);
			
			
			Optional<ITrackedBot> optTouchedBot = closeBots.stream()
					.filter(bot -> {
						IVector2 leadPoint = GeoMath.leadPointOnLine(bot.getPos(), ballHeadingLine);
						double lineToBotDist = GeoMath.distancePP(bot.getPos(), leadPoint);
						if ((lineToBotDist < (Geometry.getBotRadius() + BOT_RADIUS_MARGIN)) &&
								ballHeadingLine.isPointInFront(bot.getPos()))
						{
							return true;
						}
						return false;
					})
					.sorted(new BotDistanceComparator(ballPos))
					.findFirst();
			
			if (optTouchedBot.isPresent())
			{
				ITrackedBot touchedBot = optTouchedBot.get();
				shapes.add(new DrawableCircle(touchedBot.getPos(), Geometry.getBotRadius() * 2, Color.ORANGE));
			}
			
			shapes.add(new DrawableCircle(curBall.getPos(), Geometry.getBallRadius() * 3.0, Color.WHITE));
		}
	}
	
	
	private List<ITrackedBot> getCloseBots(final BaseAiFrame baseAiFrame, final IVector2 pos)
	{
		IBotIDMap<ITrackedBot> bots = baseAiFrame.getWorldFrame().getBots();
		TrackedBall ball = baseAiFrame.getWorldFrame().getBall();
		long timeDelta_ns = baseAiFrame.getWorldFrame().getTimestamp()
				- baseAiFrame.getPrevFrame().getWorldFrame().getTimestamp();
		
		/*
		 * Velocity in [m/s] is equal to velocity in [mm/ms]
		 */
		double ballTravelDist = ball.getVel().getLength() * TimeUnit.NANOSECONDS.toMillis(timeDelta_ns);
		double radius = Math.max(ballTravelDist, MIN_SEARCH_RADIUS);
		
		return bots.values().stream()
				.filter(bot -> GeoMath.distancePP(bot.getPos(), pos) < radius)
				.collect(Collectors.toList());
	}
}
