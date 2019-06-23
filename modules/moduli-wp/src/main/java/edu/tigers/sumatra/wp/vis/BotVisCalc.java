/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 23, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotVisCalc implements IWpCalc
{
	
	
	@Override
	public void process(final WorldFrameWrapper wfw)
	{
		List<IDrawableShape> shapes = wfw.getShapeMap().get(EWpShapesLayer.BOTS);
		for (ITrackedBot bot : wfw.getSimpleWorldFrame().getBots().values())
		{
			DrawableBotShape shape = new DrawableBotShape(bot.getPos(), bot.getAngle(), Geometry.getBotRadius(),
					bot.getCenter2DribblerDist());
			if (bot.isVisible())
			{
				shape.setColor(bot.getTeamColor() == ETeamColor.YELLOW ? Color.yellow : Color.blue);
			} else
			{
				shape.setColor(bot.getTeamColor() == ETeamColor.YELLOW ? Color.yellow.darker() : Color.cyan.darker());
			}
			shape.setFontColor(bot.getTeamColor() == ETeamColor.YELLOW ? Color.black : Color.white);
			shape.setId(String.valueOf(bot.getBotId().getNumber()));
			shapes.add(shape);
		}
	}
	
}
