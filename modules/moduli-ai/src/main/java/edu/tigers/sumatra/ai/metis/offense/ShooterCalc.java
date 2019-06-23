/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


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
	
	private final ShooterMemory					mem;
	private final Map<BotID, ShooterMemory>	botMemories;
	private final IColorPicker						cp	= ColorPickerFactory.greenRedGradient();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public ShooterCalc()
	{
		mem = new ShooterMemory();
		botMemories = new HashMap<>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		
		// Evaluate indirect goal shot targets
		Map<BotID, ValuePoint> bestShootingTargetsForTigerBots = new HashMap<>();
		for (Entry<BotID, ITrackedBot> bot : wFrame.getTigerBotsAvailable())
		{
			if (!botMemories.containsKey(bot.getKey()))
			{
				botMemories.put(bot.getKey(), new ShooterMemory());
			}
			botMemories.get(bot.getKey()).update(wFrame, baseAiFrame, bot.getValue().getPos());
			bestShootingTargetsForTigerBots.put(bot.getKey(), botMemories.get(bot.getKey()).getBestPoint());
		}
		newTacticalField.setBestDirectShotTargetsForTigerBots(bestShootingTargetsForTigerBots);
		
		// Evaluate direct goal shot targets (using ShooterMemory)
		mem.update(wFrame, baseAiFrame, wFrame.getBall().getPos());
		ValuePoint bestDirectShotTarget = mem.getBestPoint();
		newTacticalField.setBestDirectShotTarget(bestDirectShotTarget);
		List<ValuePoint> goalValuePoints = mem.getGeneratedGoalPoints();
		newTacticalField.getGoalValuePoints().addAll(goalValuePoints);
		
		for (ValuePoint vp : goalValuePoints)
		{
			Color color = cp.getColor(vp.getValue());
			newTacticalField.getDrawableShapes().get(EShapesLayer.GOAL_POINTS).add(new DrawablePoint(vp, color));
		}
		newTacticalField.getDrawableShapes().get(EShapesLayer.GOAL_POINTS)
				.add(new DrawablePoint(bestDirectShotTarget, Color.blue));
	}
}
