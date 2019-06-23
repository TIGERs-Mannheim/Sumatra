/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 4, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Display role names for each bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FieldMarksLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private static final int	LINE_MARK_LENGTH	= 500;
	private static final int	LINE_MARK_STEP		= 200;
	private static final int	LINE_MARK_COUNT	= 32;
	
	
	/**
	 */
	public FieldMarksLayer()
	{
		super(EFieldLayer.FIELD_MARKS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayerSwf(Graphics2D g, SimpleWorldFrame frame)
	{
		g.setColor(Color.white);
		g.setStroke(new BasicStroke(1));
		g.setFont(new Font("Arial", Font.PLAIN, 8));
		Vector2 globalStart = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalCenter());
		globalStart = globalStart.addNew(new Vector2(0, (AIConfig.getGeometry().getFieldWidth() / 2 / 5) * 3));
		Vector2 globalEnd = globalStart.addNew(new Vector2(0, LINE_MARK_LENGTH));
		for (int i = 0; i < LINE_MARK_COUNT; i++)
		{
			globalStart.setX(globalStart.x() + LINE_MARK_STEP);
			globalEnd.setX(globalEnd.x() + LINE_MARK_STEP);
			
			IVector2 start = getFieldPanel().transformToGuiCoordinates(globalStart);
			IVector2 end = getFieldPanel().transformToGuiCoordinates(globalEnd);
			IVector2 textPos = getFieldPanel().transformToGuiCoordinates(globalEnd.addNew(new Vector2(0, 100)));
			
			g.drawLine((int) start.x(), (int) start.y(), (int) end.x(), (int) end.y());
			g.drawString(Integer.toString(LINE_MARK_STEP * (i + 1)), (int) textPos.x() - 10, textPos.y() + 3);
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
