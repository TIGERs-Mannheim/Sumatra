/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.IPlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;


/**
 * Ai frame for athena data, based on {@link MetisAiFrame}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class AthenaAiFrame extends MetisAiFrame
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IPlayStrategy	playStrategy;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param metisAiFrame
	 * @param playStrategy
	 */
	public AthenaAiFrame(MetisAiFrame metisAiFrame, IPlayStrategy playStrategy)
	{
		super(metisAiFrame, metisAiFrame.getTacticalField());
		this.playStrategy = playStrategy;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Add a drawableShape to the field. Use this, to draw your vectors,
	 * points and other shapes to the field to
	 * visualize your plays actions
	 * 
	 * @see IDrawableShape
	 * 
	 * @param drawableShape
	 */
	public void addDebugShape(IDrawableShape drawableShape)
	{
		playStrategy.getDebugShapes().add(drawableShape);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the playStrategy
	 */
	public IPlayStrategy getPlayStrategy()
	{
		return playStrategy;
	}
}
