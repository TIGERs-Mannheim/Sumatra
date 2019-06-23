/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.ITree;


/**
 * A path is a way consisting of waypoints and possibly some sort of spline
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPath
{
	/**
	 * get a unique (or at least large random) id for comparison
	 * 
	 * @return
	 */
	long getUniqueId();
	
	
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
	float getTargetOrientation();
	
	
	/**
	 * @return
	 */
	List<IVector2> getPathPoints();
	
	
	/**
	 * Is this a rambo path? Meaning: Is this just a straight line to the destination, because no path could be found?
	 * 
	 * @return
	 */
	boolean isRambo();
	
	
	/**
	 * @return
	 */
	IVector2 getCurrentDestination();
	
	
	/**
	 * @param currentDestinationNodeIdx
	 */
	void setCurrentDestinationNodeIdx(final int currentDestinationNodeIdx);
	
	
	/**
	 * @return
	 */
	int getCurrentDestinationNodeIdx();
	
	
	/**
	 * @param startPos
	 */
	void setStartPos(final IVector2 startPos);
	
	
	/**
	 * @return
	 */
	IVector2 getStartPos();
	
	
	/**
	 * @return
	 */
	List<IVector2> getUnsmoothedPathPoints();
	
	
	/**
	 * @return
	 */
	ITree getTree();
	
}
