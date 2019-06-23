/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 7, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.vis;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotAvailableVisCalc extends ACalculator
{
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.BOTS_AVAILABLE);
		for (ITrackedBot bot : baseAiFrame.getWorldFrame().getBots().values())
		{
			if (!bot.isAvailableToAi())
			{
				DrawableCircle arc = new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.red);
				shapes.add(arc);
			}
		}
	}
	
}
