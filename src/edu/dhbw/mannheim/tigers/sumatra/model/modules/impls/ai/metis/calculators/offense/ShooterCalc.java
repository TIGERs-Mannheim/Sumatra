/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValueBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.helper.ShooterMemory;


/**
 * ShooterMemory calculates the best target in the goal for the bot that has the ball atm.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ShooterCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private ShooterMemory	mem;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public ShooterCalc()
	{
		mem = new ShooterMemory();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		mem.update(wFrame, wFrame.ball.getPos());
		newTacticalField.setBestDirectShotTarget(mem.getBestPoint());
		newTacticalField.setGoalValuePoints(mem.getGeneratedGoalPoints());
		
		Map<BotID, ValuePoint> goalBotValuePoints = new HashMap<BotID, ValuePoint>();
		
		List<TrackedBot> offensiveBots = new ArrayList<TrackedBot>(6);
		for (TrackedBot bot : wFrame.getTigerBotsAvailable().values())
		{
			// if (bot.getPos().x() < -1000)
			// {
			// continue;
			// }
			offensiveBots.add(bot);
			
			mem.update(wFrame, bot.getPos());
			goalBotValuePoints.put(bot.getId(), mem.getBestPoint());
		}
		newTacticalField.setBestDirectShotTargetBots(goalBotValuePoints);
		
		Map<BotID, List<ValueBot>> shooterReceiverStraightLines = new HashMap<BotID, List<ValueBot>>();
		for (TrackedBot shooter : offensiveBots)
		{
			List<ValueBot> valueTargets = new ArrayList<ValueBot>(6);
			shooterReceiverStraightLines.put(shooter.getId(), valueTargets);
			for (TrackedBot receiver : offensiveBots)
			{
				if (shooter.equals(receiver))
				{
					continue;
				}
				float value = AiMath.getScoreForStraightShot(wFrame, shooter.getPos(), receiver.getPos());
				valueTargets.add(new ValueBot(receiver.getId(), value));
			}
		}
		newTacticalField.setShooterReceiverStraightLines(shooterReceiverStraightLines);
		
		Map<BotID, ValueBot> ballReceiverStraightLine = new HashMap<BotID, ValueBot>();
		for (TrackedBot receiver : offensiveBots)
		{
			float value = AiMath.getScoreForStraightShot(wFrame, wFrame.ball.getPos(), receiver.getPos());
			ballReceiverStraightLine.put(receiver.getId(), new ValueBot(receiver.getId(), value));
		}
		newTacticalField.setBallReceiverStraightLines(ballReceiverStraightLine);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
