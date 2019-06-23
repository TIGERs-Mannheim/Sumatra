/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Graphics2D;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;


/**
 * This layer will draw any DEBUG shapes to the field
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ShapeLayer extends AFieldLayer
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Map<Date, IDrawableShape>	BAD_DEBUG_SHAPES	= new LinkedHashMap<Date, IDrawableShape>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public ShapeLayer()
	{
		super(EFieldLayer.SHAPES, false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		final List<IDrawableShape> debugShapes = frame.getPlayStrategy().getDebugShapes();
		for (IDrawableShape shape : debugShapes)
		{
			shape.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
		}
		synchronized (BAD_DEBUG_SHAPES)
		{
			for (Map.Entry<Date, IDrawableShape> entry : BAD_DEBUG_SHAPES.entrySet())
			{
				if (entry.getKey().after(frame.getWorldFrame().getSystemTime()))
				{
					break;
				}
				Date date = new Date(frame.getWorldFrame().getSystemTime().getTime() - 200);
				if (!entry.getKey().before(date))
				{
					entry.getValue().paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
				}
			}
			while (BAD_DEBUG_SHAPES.size() > 5000)
			{
				Date d = BAD_DEBUG_SHAPES.keySet().iterator().next();
				BAD_DEBUG_SHAPES.remove(d);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param shape
	 */
	public static void addDebugShape(final IDrawableShape shape)
	{
		synchronized (BAD_DEBUG_SHAPES)
		{
			BAD_DEBUG_SHAPES.put(new Date(), shape);
		}
	}
}
