/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;


/**
 * 
 * This interface is used to visualize AI decisions and path planing informations.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public interface IAIObserver
{
	/**
	 * 
	 * This function notifies the actual field raster information to observers.
	 * It can be used to visualize the positioning field raster of AI-Module. Pay attention on
	 * the numbering of sub-fields / sub-rectangles. The raster numbering starts top left form left to right.
	 * See ({@link http://tigers-mannheim.de/trac/wiki/Informatik#Spielfeld}) for field orientation.
	 * Field raster will be loaded at start of AI Module.
	 * 
	 * @param columnSize size/length of one column
	 * @param rowSize size/length of one row
	 * @param columnSizeAnalysing TODO
	 * @param rowSizeAnalysing TODO
	 */
	public void onNewFieldRaster(int columnSize, int rowSize, int columnSizeAnalysing, int rowSizeAnalysing);
	

	/**
	 * 
	 * This function is used to notify the last {@link AIInfoFrame} to visualization observers.
	 * @param lastAIInfoframe
	 */
	public void onNewAIInfoFrame(AIInfoFrame lastAIInfoframe);
	

	public void onNewPath(Path path);
	

}
