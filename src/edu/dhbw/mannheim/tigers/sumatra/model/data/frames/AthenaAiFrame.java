/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.IPlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * Ai frame for athena data, based on {@link MetisAiFrame}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AthenaAiFrame extends MetisAiFrame
{
	private final IPlayStrategy	playStrategy;
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategy
	 */
	public AthenaAiFrame(final MetisAiFrame metisAiFrame, final IPlayStrategy playStrategy)
	{
		super(metisAiFrame, metisAiFrame.getTacticalField());
		this.playStrategy = playStrategy;
	}
	
	
	/**
	 * Add a drawableShape to the field. Use this, to draw your vectors,
	 * points and other shapes to the field to
	 * visualize your plays actions
	 * 
	 * @see IDrawableShape
	 * @param drawableShape
	 */
	public void addDebugShape(final IDrawableShape drawableShape)
	{
		getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.UNSORTED).add(drawableShape);
	}
	
	
	/**
	 * @return the playStrategy
	 */
	@Override
	public IPlayStrategy getPlayStrategy()
	{
		return playStrategy;
	}
}
