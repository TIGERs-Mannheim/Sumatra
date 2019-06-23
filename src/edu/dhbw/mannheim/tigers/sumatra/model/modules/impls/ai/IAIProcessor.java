/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;


/**
 * This interface encapsulates all parts of the AI that processes {@link AIInfoFrame}s
 * 
 * @author Gero, Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public interface IAIProcessor
{
	/**
	 * @param currentFrame
	 * @param previousFrame
	 * @return The formerly passed {@link AIInfoFrame}, hopefully filled with more interesting information
	 */
	public AIInfoFrame process(AIInfoFrame currentFrame, AIInfoFrame previousFrame);
}
