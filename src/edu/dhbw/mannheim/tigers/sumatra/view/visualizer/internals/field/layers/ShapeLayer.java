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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * This layer will draw any DEBUG shapes to the field
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ShapeLayer extends AFieldLayer
{
	private final Map<EDrawableShapesLayer, Boolean>	visibilityMap	= new HashMap<>();
	
	
	/**
	 */
	public ShapeLayer()
	{
		super(EFieldLayer.SHAPES, true);
		for (EDrawableShapesLayer l : EDrawableShapesLayer.values())
		{
			visibilityMap.put(l, false);
		}
	}
	
	
	/**
	 * @param dsLayer
	 * @param visible
	 */
	public void setVisible(final EDrawableShapesLayer dsLayer, final boolean visible)
	{
		visibilityMap.put(dsLayer, visible);
	}
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		for (EDrawableShapesLayer l : EDrawableShapesLayer.values())
		{
			boolean visible = visibilityMap.get(l);
			if (visible && frame.getTacticalField().getDrawableShapes().containsKey(l))
			{
				// copy to avoid sync
				List<IDrawableShape> shapes = new ArrayList<IDrawableShape>(frame.getTacticalField().getDrawableShapes()
						.get(l));
				
				for (IDrawableShape shape : shapes)
				{
					shape.setDrawDebug(isDebugInformationVisible());
					shape.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
				}
			}
		}
	}
	
	
	@Override
	protected boolean isForceVisible()
	{
		return true;
	}
}
