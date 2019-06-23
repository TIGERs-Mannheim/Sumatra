/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 26, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.SortedMap;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Buffer positions of bots and ball and show it
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PositionBufferLayer extends AFieldLayer
{
	/**
	  * 
	  */
	public PositionBufferLayer()
	{
		super(EFieldLayer.POSITION_BUFFER);
	}
	
	
	/**
	 * Draws a ball on the field.
	 * 
	 * @param g graphics object
	 */
	private void drawBufferPoint(final Graphics2D g, final IVector2 pos, final int radius, final Color color,
			final boolean invert)
	{
		final IVector2 drawPoint = getFieldPanel().transformToGuiCoordinates(pos, invert);
		
		final int drawingX = (int) (drawPoint.x() - radius);
		final int drawingY = (int) (drawPoint.y() - radius);
		
		g.setColor(color);
		
		g.fillOval(drawingX, drawingY, radius * 2, radius * 2);
	}
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		for (Map.Entry<BotID, SortedMap<Long, IVector2>> entry : frame.getTacticalField().getBotPosBuffer().entrySet())
		{
			for (IVector2 pos : entry.getValue().values())
			{
				drawBufferPoint(g, pos, 2, entry.getKey().getTeamColor() == ETeamColor.YELLOW ? Color.yellow : Color.blue,
						frame.getWorldFrame().isInverted());
			}
		}
		
		for (TrackedBall ball : frame.getTacticalField().getBallBuffer())
		{
			drawBufferPoint(g, ball.getPos(), 1, Color.red, frame.getWorldFrame().isInverted());
		}
	}
}
