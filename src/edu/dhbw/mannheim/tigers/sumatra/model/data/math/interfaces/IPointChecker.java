/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 7, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.interfaces;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Use this interface to let someone define some code to check a given code.
 * This interface can be passed to someone one created points and checks if they are ok.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IPointChecker
{
	/**
	 * Check if the given point is ok
	 * 
	 * @param point
	 * @return
	 */
	boolean checkPoint(IVector2 point);
}
