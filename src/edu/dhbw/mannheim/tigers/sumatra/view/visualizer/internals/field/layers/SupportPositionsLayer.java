/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;


/**
 * Visualize some tactical infos from metis
 * 
 * @author JulianT
 */
public class SupportPositionsLayer extends AValuePointLayer
{
	/**
	 */
	public SupportPositionsLayer()
	{
		super(EFieldLayer.SUPPORT_POSITIONS);
	}
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		drawAdvancedPassTargets(g, frame);
	}
	
	
	private void drawAdvancedPassTargets(final Graphics2D g, final IRecordFrame frame)
	{
		Color orange = new Color(222, 222, 222, 255);
		Color pink = new Color(255, 0, 170, 100);
		Color magenta = new Color(255, 120, 0, 120);
		int guiBallRadius = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBallRadius());
		g.setStroke(new BasicStroke(0.4f));
		
		g.setColor(new Color(55, 55, 55, 70));
		for (AdvancedPassTarget target : frame.getTacticalField().getAdvancedPassTargetsRanked())
		{
			
			BotID botId = target.getBotId();
			TrackedTigerBot bot = frame.getWorldFrame().getTigerBotsVisible().get(botId);
			
			IVector2 kickerPos = AiMath.getBotKickerPos(bot);
			IVector2 guiKickerPos = getFieldPanel().transformToGuiCoordinates(kickerPos,
					frame.getWorldFrame().isInverted());
			
			IVector2 guiTarget = getFieldPanel().transformToGuiCoordinates(target, frame.getWorldFrame().isInverted());
			if (!kickerPos.equals(target))
			{
				g.drawLine((int) guiKickerPos.x(), (int) guiKickerPos.y(),
						(int) guiTarget.x(), (int) guiTarget.y());
			}
		}
		
		List<BotID> seenBots = new ArrayList<>();
		int i = 1;
		for (AdvancedPassTarget target : frame.getTacticalField().getAdvancedPassTargetsRanked())
		{
			BotID botId = target.getBotId();
			
			IVector2 guiTarget = getFieldPanel().transformToGuiCoordinates(target, frame.getWorldFrame().isInverted());
			
			if (i == 1)
			{
				seenBots.add(botId);
				g.setColor(orange);
			} else
			{
				if (!seenBots.contains(botId))
				{
					seenBots.add(botId);
					g.setColor(pink);
				}
				else
				{
					g.setColor(magenta);
				}
			}
			
			int guiTargetX = (int) guiTarget.x() - (2 * guiBallRadius);
			int guiTargetY = (int) guiTarget.y() - (2 * guiBallRadius);
			g.fillOval(guiTargetX, guiTargetY, 4 * guiBallRadius, 4 * guiBallRadius);
			
			g.setColor(Color.black);
			// print rank number
			g.setFont(g.getFont().deriveFont(4.5f));
			printSimpleString(g, Integer.toString(i), 4 * guiBallRadius, guiTargetX, guiTargetY + 5);
			// print value
			g.setFont(g.getFont().deriveFont(1.8f));
			printSimpleString(g, (Integer.toString(Math.round(target.getValue() * 1000))), 4 * guiBallRadius, guiTargetX,
					guiTargetY + 7);
			
			i++;
		}
	}
	
	
	private void printSimpleString(final Graphics2D g, final String str, final int width, final int x, final int y)
	{
		int stringWidth = (int) g.getFontMetrics().getStringBounds(str, g).getWidth();
		int start = Math.round((width / 2f) - (stringWidth / 2f));
		g.drawString(str, start + x, y);
	}
}
