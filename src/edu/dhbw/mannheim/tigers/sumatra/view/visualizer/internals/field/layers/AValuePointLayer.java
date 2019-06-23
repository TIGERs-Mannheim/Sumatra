/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 2, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * Defines methods for handling value points in a layer
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class AValuePointLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	protected static final int	POINT_SIZE	= 2;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param name
	 */
	public AValuePointLayer(EFieldLayer name)
	{
		super(name, false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	protected void drawValuePoints(Graphics2D g, List<ValuePoint> valuePoints)
	{
		// --- draw goal points ---
		for (int i = valuePoints.size() - 1; i >= 0; i--)
		{
			ValuePoint point = valuePoints.get(i);
			float value = point.getValue();
			if (value > 1)
			{
				value = 1;
			} else if (value < 0)
			{
				value = 0;
			}
			// --- from SSLVision-mm to java2d-coordinates ---
			final IVector2 transPoint = FieldPanel.transformToGuiCoordinates(point);
			final int drawingX = (int) transPoint.x() - (POINT_SIZE / 2);
			final int drawingY = (int) transPoint.y() - (POINT_SIZE / 2);
			g.setColor(new Color((int) (255 * value), 255 - (int) (255 * value), 0));
			g.fillOval(drawingX, drawingY, POINT_SIZE, POINT_SIZE);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
