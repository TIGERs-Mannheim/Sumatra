/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 4, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.path;

import java.util.List;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPath
{
	
	/**
	 * @return
	 */
	IVector2 getStart();
	
	
	/**
	 * @return
	 */
	IVector2 getEnd();
	
	
	/**
	 * @return
	 */
	List<IVector2> getPathPoints();
	
	
	/**
	 * @return
	 */
	List<IVector2> getUnsmoothedPathPoints();
	
	
	/**
	 * @return
	 */
	double getTargetOrientation();
}
