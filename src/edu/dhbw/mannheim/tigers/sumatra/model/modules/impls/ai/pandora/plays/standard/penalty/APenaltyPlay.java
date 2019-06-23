/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 13, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;


/**
 * This is the base class for a penalty play
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class APenaltyPlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	ROW_STEP			= 200;
	private int						initPosCounter	= 0;
	// will be increased, if bots have to go behind first row (no space)
	private int						secondRow		= 0;
	private static final int[]	POSITIONS		= { -3, 3, -6, 6, -9, 9, -2, 2, -5, 5, -8, 8, -1, 1, -4, 4, -7, 7 };
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public APenaltyPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected IVector2 getNextInitPosition(boolean ourPenalty)
	{
		final float halfFieldWidth = AIConfig.getGeometry().getFieldWidth() / 2;
		final float factor = halfFieldWidth / 10;
		IVector2 penaltyLine;
		
		initPosCounter++;
		if (initPosCounter > POSITIONS.length)
		{
			secondRow -= ROW_STEP;
			initPosCounter = 1;
		}
		final IVector2 changeVec = new Vector2(secondRow, factor * POSITIONS[initPosCounter - 1]);
		if (ourPenalty)
		{
			penaltyLine = AIConfig.getGeometry().getPenaltyLineTheir();
			return penaltyLine.addNew(changeVec);
		}
		penaltyLine = AIConfig.getGeometry().getPenaltyLineOur();
		return penaltyLine.subtractNew(changeVec);
	}
	
	
	/**
	 * call this method to finish the play. It can be overwritten to disable the finish.
	 */
	protected void finish()
	{
		changeToFinished();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
