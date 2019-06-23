/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Replace the ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBallReplacer
{
	/**
	 * @param pos
	 */
	void replaceBall(IVector2 pos);
}
