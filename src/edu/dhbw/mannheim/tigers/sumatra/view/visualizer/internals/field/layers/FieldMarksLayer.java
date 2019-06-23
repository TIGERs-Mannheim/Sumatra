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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


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
	
	
	private static final int	LINE_MARK_LENGTH	= 50;
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
	protected void paintLayer(Graphics2D g)
	{
		g.setColor(Color.white);
		g.setStroke(new BasicStroke(1));
		g.setFont(new Font("Arial", Font.PLAIN, 10));
		IVector2 globalStart = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		globalStart = globalStart.addNew(new Vector2(0, (AIConfig.getGeometry().getFieldWidth() / 2 / 5) * 3));
		IVector2 start = FieldPanel.transformToGuiCoordinates(globalStart);
		final float numLines = LINE_MARK_COUNT;
		for (int i = 0; i < numLines; i++)
		{
			int step = FieldPanel.scaleXLength((i + 1) * LINE_MARK_STEP);
			float y = start.y() + (step);
			g.drawLine((int) start.x(), (int) y, (int) start.x() + LINE_MARK_LENGTH, (int) y);
			g.drawString(Integer.toString(LINE_MARK_STEP * (i + 1)), (int) start.x() + LINE_MARK_LENGTH + 5, y + 3);
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
