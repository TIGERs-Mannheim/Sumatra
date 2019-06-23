/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This calculator tries to determine the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotLastTouchedBallCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final double	MIN_DIST			= Geometry.getBotRadius()
																		+ Geometry.getBallRadius() + 10;
	private static final double	EXTENDED_DIST	= MIN_DIST + 25;
	private static final double	ANGLE_EPSILON	= 0.1;
	/** frames to wait, before setting bot. if 60ps: 1sek */
	// private static final int MIN_FRAMES = 0;
	// private int numFrames = 0;
	private BotID						lastBotID		= BotID.get();
																
																
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public BotLastTouchedBallCalc()
	{
	
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		BotID theChosenOne = baseAiFrame.getPrevFrame().getTacticalField().getBotLastTouchedBall();
		IVector2 ball = wFrame.getBall().getPos();
		IVector2 prevBall = baseAiFrame.getPrevFrame().getWorldFrame().getBall().getPos();
		Collection<ITrackedBot> bots = new LinkedList<>();
		bots.addAll(wFrame.tigerBotsVisible.values());
		bots.addAll(wFrame.foeBots.values());
		double smallestDist = Double.MAX_VALUE;
		double smallestAngle = Double.MAX_VALUE;
		boolean foundBotTouchedBall = false;
		for (ITrackedBot bot : bots)
		{
			double dist = GeoMath.distancePP(ball, bot.getPos());
			if ((dist <= MIN_DIST) && (dist < smallestDist))
			{
				smallestDist = dist;
				theChosenOne = bot.getBotId();
				lastBotID = theChosenOne;
				foundBotTouchedBall = true;
				continue;
			}
			// if ball is too fast calculate with position from prev frame
			double preDist = GeoMath.distancePP(prevBall, bot.getPos());
			if ((preDist <= MIN_DIST) && (preDist < smallestDist))
			{
				smallestDist = preDist;
				theChosenOne = bot.getBotId();
				lastBotID = theChosenOne;
				foundBotTouchedBall = true;
				continue;
			}
			// if ball is still too fast check if it was kicked (fast acceleration in kicker direction)
			IVector2 ballVel = wFrame.getBall().getVel();
			if (!ballVel.equals(AVector2.ZERO_VECTOR))
			{
				double ballAngle = GeoMath.angleBetweenXAxisAndLine(AVector2.ZERO_VECTOR, ballVel);
				double botAngle = bot.getAngle();
				double angleDiff = Math.abs(AngleMath.difference(ballAngle, botAngle));
				if ((angleDiff < ANGLE_EPSILON) && (angleDiff < smallestAngle))
				{
					if ((dist < EXTENDED_DIST) || (preDist < EXTENDED_DIST))
					{
						smallestAngle = angleDiff;
						theChosenOne = bot.getBotId();
						lastBotID = theChosenOne;
						foundBotTouchedBall = true;
					}
				}
			}
			
		}
		// if (lastBotID.equals(theChosenOne))
		// {
		// numFrames++;
		// if (numFrames >= MIN_FRAMES)
		// {
		// newTacticalField.setBotLastTouchedBall(theChosenOne);
		// } else
		// {
		// lastBotID = theChosenOne;
		// theChosenOne = baseAiFrame.getPrevFrame().getTacticalField().getBotLastTouchedBall();
		// }
		// } else if (lastBotID.equals(BotID.createBotId()))
		// {
		// lastBotID = theChosenOne;
		// } else
		// {
		// numFrames = 0;
		// }
		
		if (foundBotTouchedBall)
		{
			newTacticalField.setBotLastTouchedBall(theChosenOne);
			newTacticalField.setBotTouchedBall(theChosenOne);
		} else
		{
			newTacticalField.setBotLastTouchedBall(lastBotID);
			newTacticalField.setBotTouchedBall(null);
		}
		
		
		ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(theChosenOne);
		if (bot != null)
		{
			newTacticalField
					.getDrawableShapes()
					.get(EShapesLayer.BALL_POSSESSION)
					.add(new DrawableCircle(new Circle(bot.getPos(), Geometry.getBotRadius() + 10),
							Color.magenta));
		}
	}
}
