/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.Collection;
import java.util.LinkedList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * This calculator tries to determine the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BotLastTouchedBallCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	MIN_DIST		= AIConfig.getGeometry().getBotRadius()
																+ AIConfig.getGeometry().getBallRadius() + 5;
	/** frames to wait, before setting bot. if 60fps: 1sek */
	private static final int	MIN_FRAMES	= 0;
	private int						numFrames	= 0;
	private BotID					lastBotID	= BotID.createBotId();
	
	
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
	public void doCalc(TacticalField newTacticalField, BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		BotID theChosenOne = baseAiFrame.getPrevFrame().getTacticalField().getBotLastTouchedBall();
		IVector2 ball = wFrame.ball.getPos();
		Collection<TrackedBot> bots = new LinkedList<TrackedBot>();
		bots.addAll(wFrame.tigerBotsVisible.values());
		bots.addAll(wFrame.foeBots.values());
		float smallestDist = Float.MAX_VALUE;
		for (TrackedBot bot : bots)
		{
			float dist = GeoMath.distancePP(ball, bot.getPos());
			if ((dist <= MIN_DIST) && (dist < smallestDist))
			{
				smallestDist = dist;
				theChosenOne = bot.getId();
			}
		}
		if (lastBotID.equals(theChosenOne))
		{
			numFrames++;
			if (numFrames >= MIN_FRAMES)
			{
				newTacticalField.setBotLastTouchedBall(theChosenOne);
				return;
			}
		} else
		{
			numFrames = 0;
		}
		lastBotID = theChosenOne;
		newTacticalField.setBotLastTouchedBall(baseAiFrame.getPrevFrame().getTacticalField().getBotLastTouchedBall());
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
