/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This calculator tries to determine the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Lukas Magel, Nicolai Ommer
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class BotLastTouchedBallCalculator
{
	
	private static final Logger log = Logger.getLogger(BotLastTouchedBallCalculator.class);
	private static final double MIN_DIST = Geometry.getBotRadius() + Geometry.getBallRadius() + 10;
	private static final double EXTENDED_DIST = MIN_DIST + 25;
	private static final double ANGLE_EPSILON = 0.1;
	
	@Configurable(comment = "[degree]")
	private static double angleThresholdDegree = 5.0d;
	@Configurable(comment = "[m/s] Min Gain in velocity that counts as kick", defValue = "0.3")
	private static double velGainThreshold = 0.3d;
	/** in mm */
	@Configurable(comment = "[mm]")
	private static double minSearchRadius = 300;
	@Configurable(comment = "[mm]")
	private static double botRadiusMargin = 30;
	
	static
	{
		ConfigRegistration.registerClass("wp", BotLastTouchedBallCalculator.class);
	}
	
	private SimpleWorldFrame wFrame;
	private SimpleWorldFrame prevWFrame;
	
	private List<ITrackedBot> closeBots = null;
	private ILine reversedBallHeading;
	private double radius;
	
	
	/**
	 * @param frame
	 * @param prevFrame
	 */
	public BotLastTouchedBallCalculator(final SimpleWorldFrame frame, final SimpleWorldFrame prevFrame)
	{
		wFrame = frame;
		prevWFrame = prevFrame;
	}
	
	
	/**
	 * @return
	 */
	public BotID processByVicinity()
	{
		
		BotID theChosenOne = BotID.noBot();
		
		IVector2 ball = wFrame.getBall().getPos();
		IVector2 prevBall = prevWFrame.getBall().getPos();
		Collection<ITrackedBot> bots = new LinkedList<>();
		bots.addAll(wFrame.getBots().values());
		double smallestDist = Double.MAX_VALUE;
		double smallestAngle = Double.MAX_VALUE;
		boolean foundBotTouchedBall = false;
		for (ITrackedBot bot : bots)
		{
			double dist = VectorMath.distancePP(ball, bot.getPos());
			// if ball is too fast calculate with position from prev frame
			double preDist = VectorMath.distancePP(prevBall, bot.getPos());
			double distResult = checkDistanceToBall(dist, preDist, smallestDist);
			if (distResult > -1)
			{
				smallestDist = distResult;
				theChosenOne = bot.getBotId();
				foundBotTouchedBall = true;
				continue;
			}
			
			// if ball is still too fast check if it was kicked (fast acceleration in kicker direction)
			IVector2 ballVel = wFrame.getBall().getVel();
			if (!ballVel.equals(AVector2.ZERO_VECTOR))
			{
				double ballAngle = ballVel.getAngle(0);
				double botAngle = bot.getOrientation();
				double angleDiff = Math.abs(AngleMath.difference(ballAngle, botAngle));
				if (wasKicked(angleDiff, smallestAngle, Math.min(dist, preDist)))
				{
					smallestAngle = angleDiff;
					theChosenOne = bot.getBotId();
					foundBotTouchedBall = true;
				}
			}
			
		}
		
		if (foundBotTouchedBall)
		{
			return theChosenOne;
		}
		return null;
	}
	
	
	private boolean wasKicked(double angleDiff, double smallestAngle, double dist)
	{
		return (angleDiff < Math.min(ANGLE_EPSILON, smallestAngle)) && dist < EXTENDED_DIST;
	}
	
	
	private double checkDistanceToBall(double dist, double preDist, double smallestDist)
	{
		if ((dist <= MIN_DIST) && (dist < smallestDist))
		{
			return dist;
		}
		if ((preDist <= MIN_DIST) && (preDist < smallestDist))
		{
			return preDist;
		}
		return -1;
	}
	
	
	/**
	 * @return botID of bot that last touched ball
	 */
	public BotID processByBallHeading()
	{
		ITrackedBall curBall = wFrame.getBall();
		ITrackedBall prevBall = prevWFrame.getBall();
		
		IVector2 prevHeading = prevBall.getVel();
		IVector2 curHeading = curBall.getVel();
		
		if (curHeading.isZeroVector())
		{
			return null;
		}
		if (ballTouched(curHeading, prevHeading))
		{
			IVector2 ballPos = curBall.getPos();
			reversedBallHeading = Line.fromDirection(ballPos, curBall.getVel().multiplyNew(-1.0d));
			closeBots = getBotsCloseToBall();
			
			Optional<ITrackedBot> optTouchedBot = closeBots.stream()
					.filter(bot -> isBotInFrontOfLine(bot, reversedBallHeading))
					.sorted(Comparator.comparingDouble(bot -> ballPos.distanceTo(bot.getPos())))
					.findFirst();
			
			if (optTouchedBot.isPresent())
			{
				ITrackedBot touchedBot = optTouchedBot.get();
				return touchedBot.getBotId();
			}
		}
		return null;
	}
	
	
	/**
	 * This function tries to determine if the ball heading which is described by {@code line} intersects with the bot
	 * and the intersection is located in the direction of the line.
	 *
	 * @param bot
	 * @param line
	 * @return
	 */
	private boolean isBotInFrontOfLine(final ITrackedBot bot, final ILine line)
	{
		try
		{
			double lineToBotDist = LineMath.distancePL(bot.getPos(), line);
			
			boolean ballOriginatedFromBot = lineToBotDist < (Geometry.getBotRadius() + botRadiusMargin);
			boolean botInFront = line.isPointInFront(bot.getPos());
			return ballOriginatedFromBot && botInFront;
		} catch (RuntimeException e)
		{
			log.debug("Error while calculating the lead point", e);
			return false;
		}
		
	}
	
	
	private List<ITrackedBot> getBotsCloseToBall()
	{
		IBotIDMap<ITrackedBot> bots = wFrame.getBots();
		ITrackedBall ball = wFrame.getBall();
		long timeDeltaNs = wFrame.getTimestamp()
				- prevWFrame.getTimestamp();
		
		/*
		 * Velocity in [m/s] is equal to velocity in [mm/ms]
		 */
		double ballTravelDist = ball.getVel().getLength() * TimeUnit.NANOSECONDS.toMillis(timeDeltaNs);
		radius = Math.max(ballTravelDist, minSearchRadius);
		
		return bots.values().stream()
				.filter(bot -> VectorMath.distancePP(bot.getPos(), ball.getPos()) < radius)
				.collect(Collectors.toList());
	}
	
	
	private boolean ballTouched(final IVector2 curHeading, final IVector2 prevHeading)
	{
		double radAngle = curHeading.angleToAbs(prevHeading).orElse(0.0d);
		
		boolean angleChanged = radAngle > AngleMath.deg2rad(angleThresholdDegree);
		boolean velocityGained = (curHeading.getLength() - prevHeading.getLength()) > velGainThreshold;
		
		return angleChanged || velocityGained;
	}
	
	
	public List<ITrackedBot> getCloseBots()
	{
		return closeBots;
	}
	
	
	public ILine getReversedBallHeading()
	{
		return reversedBallHeading;
	}
	
	
	public double getRadius()
	{
		return radius;
	}
}
