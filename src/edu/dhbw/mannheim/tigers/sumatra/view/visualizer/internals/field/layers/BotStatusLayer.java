/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 29, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotStatusLayer extends AFieldLayer
{
	/**
	 * 
	 */
	public BotStatusLayer()
	{
		super(EFieldLayer.BOT_STATUS);
	}
	
	
	@Override
	protected void paintLayerSwf(final Graphics2D g, final SimpleWorldFrame frame)
	{
		g.setStroke(new BasicStroke(2));
		int ringOffsetBat = 0;
		int ringOffsetKicker = 0;
		int rotation = 180;
		for (TrackedTigerBot tbot : frame.getBots().values())
		{
			ABot bot = tbot.getBot();
			if (bot == null)
			{
				return;
			}
			float relBat = bot.getBatteryRelative();
			float relKicker = bot.getKickerLevel() / bot.getKickerLevelMax();
			IVector2 guiPos = getFieldPanel().transformToGuiCoordinates(tbot.getPos());
			float radius = AIConfig.getGeometry().getBotRadius();
			int width = getFieldPanel().scaleXLength(radius * 2) + 1;
			int height = getFieldPanel().scaleYLength(radius * 2) + 1;
			int x = ((int) guiPos.x() - (width / 2));
			int y = ((int) guiPos.y() - (height / 2));
			
			g.setColor(Color.red);
			g.drawArc(x + ringOffsetBat, y + ringOffsetBat, width - (2 * ringOffsetBat), height - (2 * ringOffsetBat),
					0, 360);
			
			int startAngle = 90;
			int arcAngle = (int) (relBat * rotation);
			g.setColor(Color.green);
			g.drawArc(x + ringOffsetBat, y + ringOffsetBat, width - (2 * ringOffsetBat), height - (2 * ringOffsetBat),
					startAngle, -arcAngle);
			
			startAngle = -90;
			arcAngle = (int) (relKicker * rotation);
			g.setColor(Color.cyan);
			g.drawArc(x + ringOffsetKicker, y + ringOffsetKicker, width - (2 * ringOffsetKicker), height
					- (2 * ringOffsetKicker), startAngle, -arcAngle);
		}
	}
}
