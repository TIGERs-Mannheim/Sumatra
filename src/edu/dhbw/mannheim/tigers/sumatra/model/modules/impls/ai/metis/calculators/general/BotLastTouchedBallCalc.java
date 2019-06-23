/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


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
	
	private static final float	MIN_DIST			= AIConfig.getGeometry().getBotRadius()
																	+ AIConfig.getGeometry().getBallRadius() + 10;
	private static final float	EXTENDED_DIST	= MIN_DIST + 25;
	private static final float	ANGLE_EPSILON	= 0.1f;
	/** frames to wait, before setting bot. if 60fps: 1sek */
	// private static final int MIN_FRAMES = 0;
	// private int numFrames = 0;
	private BotID					lastBotID		= BotID.createBotId();
	
	
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
		Collection<TrackedBot> bots = new LinkedList<TrackedBot>();
		bots.addAll(wFrame.tigerBotsVisible.values());
		bots.addAll(wFrame.foeBots.values());
		float smallestDist = Float.MAX_VALUE;
		float smallestAngle = Float.MAX_VALUE;
		boolean foundBotTouchedBall = false;
		for (TrackedBot bot : bots)
		{
			float dist = GeoMath.distancePP(ball, bot.getPos());
			if ((dist <= MIN_DIST) && (dist < smallestDist))
			{
				smallestDist = dist;
				theChosenOne = bot.getId();
				lastBotID = theChosenOne;
				foundBotTouchedBall = true;
				continue;
			}
			// if ball is too fast calculate with position from prev frame
			float preDist = GeoMath.distancePP(prevBall, bot.getPos());
			if ((preDist <= MIN_DIST) && (preDist < smallestDist))
			{
				smallestDist = preDist;
				theChosenOne = bot.getId();
				lastBotID = theChosenOne;
				foundBotTouchedBall = true;
				continue;
			}
			// if ball is still too fast check if it was kicked (fast acceleration in kicker direction)
			IVector2 ballVel = wFrame.getBall().getVel();
			if (!ballVel.equals(AVector2.ZERO_VECTOR))
			{
				float ballAngle = GeoMath.angleBetweenXAxisAndLine(AVector2.ZERO_VECTOR, ballVel);
				float botAngle = bot.getAngle();
				float angleDiff = Math.abs(AngleMath.difference(ballAngle, botAngle));
				if ((angleDiff < ANGLE_EPSILON) && (angleDiff < smallestAngle))
				{
					if ((dist < EXTENDED_DIST) || (preDist < EXTENDED_DIST))
					{
						smallestAngle = angleDiff;
						theChosenOne = bot.getId();
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
		
		
		TrackedTigerBot bot = baseAiFrame.getWorldFrame().getBot(theChosenOne);
		if (bot != null)
		{
			newTacticalField
					.getDrawableShapes()
					.get(EDrawableShapesLayer.BALL_POSSESSION)
					.add(new DrawableCircle(new Circle(bot.getPos(), AIConfig.getGeometry().getBotRadius() + 10),
							Color.magenta));
		}
	}
}
