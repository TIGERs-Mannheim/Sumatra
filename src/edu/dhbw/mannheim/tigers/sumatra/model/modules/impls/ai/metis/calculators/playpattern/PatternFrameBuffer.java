/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.04.2012
 * Author(s): Paul
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Buffer container for pattern frames
 * 
 * @author Paul
 * 
 */
public class PatternFrameBuffer
{
	private static final Logger		log				= Logger.getLogger(PatternFrameBuffer.class.getName());
	// 125 Frames * 16ms/Frame = 2s
	private static final int			MAX_FRAMES		= 125;
	// m/s
	private static final int			MIN_BALLSPEED	= 1;
	// 2s
	private static final int			FRAMESSTOP		= 125;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final List<AIInfoFrame>	buffer;
	private final List<Pattern>		foundPatterns;
	private int								framestop;
	
	private AIInfoFrame					frame;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PatternFrameBuffer()
	{
		buffer = new ArrayList<AIInfoFrame>();
		foundPatterns = new ArrayList<Pattern>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 */
	public void addFrame(AIInfoFrame aiFrame)
	{
		frame = aiFrame;
		
		for (final Pattern pattern : foundPatterns)
		{
			pattern.compare(aiFrame);
		}
		
		if (framestop-- <= 0)
		{
			if (isShotAtGoal() && (getPasser() != null))
			{
				log.info("Pattern!");
				final IVector2 passerPos = getPasser().getPos();
				final IVector2 recieverPos = getReciever().getPos();
				final Pattern foundPatttern = new Pattern(passerPos, recieverPos, getPasser().getAngle(), getReciever()
						.getAngle());
				
				for (final Pattern pattern : foundPatterns)
				{
					if (pattern.isSameAs(foundPatttern))
					{
						return;
					}
				}
				
				foundPatttern.initializeIndex();
				foundPatterns.add(foundPatttern);
				framestop = FRAMESSTOP;
			}
			
			if (buffer.size() > MAX_FRAMES)
			{
				buffer.remove(0);
			}
			buffer.add(aiFrame);
			
			if (framestop == Integer.MIN_VALUE)
			{
				framestop = 0;
			}
		}
	}
	
	
	private boolean isShotAtGoal()
	{
		final TrackedBall ball = frame.worldFrame.ball;
		
		if (ball.getVel().getLength2() > MIN_BALLSPEED)
		{
			Vector2 intersectPoint;
			// for testing change here
			final Goal ourGoal = AIConfig.getGeometry().getGoalOur();
			try
			{
				// for testing change here
				intersectPoint = GeoMath.intersectionPoint(ball.getPos(), ball.getVel(), ourGoal.getGoalPostLeft(),
						AIConfig.getGeometry().getGoalLineOur().directionVector());
			} catch (final MathException err)
			{
				// lines parallel
				return false;
			}
			
			// for testing change here
			// ball.vel.x() < 0 --> ball rolling towards our goal
			if ((ball.getVel().x() < 0) && (Math.abs(intersectPoint.y) < ourGoal.getSize()))
			{
				return true;
			}
		}
		
		return false;
		
	}
	
	
	private TrackedBot getPasser()
	{
		final TrackedBot reciever = getReciever();
		
		if (reciever != null)
		{
			for (int i = buffer.size() - 1; i >= 0; i--)
			{
				if (buffer.get(i).worldFrame.ball.getVel().equals(AVector2.ZERO_VECTOR))
				{
					// Nicolai: we can not calc the angle of the ball. This will receive an IllegalArgumentException below
					continue;
				}
				// for testing change here
				for (final TrackedBot foeBot : buffer.get(i).worldFrame.foeBots.values())
				{
					if ((GeoMath.distancePP(buffer.get(i).worldFrame.ball.getPos(), foeBot.getPos()) < (AIConfig
							.getGeometry().getBotRadius() * 2))
							&& (foeBot.getId().getNumber() != reciever.getId().getNumber()))
					{
						final float ballBotAngle = Math.abs(foeBot.getAngle()
								- buffer.get(i).worldFrame.ball.getVel().getAngle());
						if (ballBotAngle < AngleMath.deg2rad(5))
						{
							return foeBot;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	
	private TrackedBot getReciever()
	{
		double minDist = Double.POSITIVE_INFINITY;
		TrackedBot closestBot = null;
		final TrackedBall ball = frame.worldFrame.ball;
		
		// for testing change here
		for (final TrackedBot foeBot : frame.worldFrame.foeBots.values())
		{
			final double ballBotDist = GeoMath.distancePP(ball.getPos(), foeBot.getPos());
			if (ballBotDist < minDist)
			{
				closestBot = foeBot;
				minDist = ballBotDist;
			}
		}
		
		if (closestBot == null)
		{
			return null;
		}
		
		final float ballBotAngle = Math.abs(closestBot.getAngle() - ball.getVel().getAngle());
		if (ballBotAngle < AngleMath.deg2rad(5))
		{
			return closestBot;
		}
		return null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the foundPatterns
	 */
	public List<Pattern> getFoundPatterns()
	{
		return foundPatterns;
	}
	
	
}
