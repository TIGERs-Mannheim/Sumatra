/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 7, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * This play will move two robots in opposite directions, while looking at each other.
 * 
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class MoveTestPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final MoveRole	moveRole1;
	private final MoveRole	moveRole2;
	
	private EState				state	= EState.PREPARE;
	
	private enum EState
	{
		PREPARE,
		DO
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public MoveTestPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		moveRole1 = new MoveRole(EMoveBehavior.NORMAL);
		moveRole2 = new MoveRole(EMoveBehavior.NORMAL);
		addAggressiveRole(moveRole1, new Vector2(-2000, 1500));
		addAggressiveRole(moveRole2, new Vector2(2000, 1900));
		setTimeout(Long.MAX_VALUE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		switch (state)
		{
			case PREPARE:
				if (moveRole1.checkMoveCondition() && moveRole2.checkMoveCondition())
				{
					moveRole1.updateDestination(new Vector2(2000, 1500));
					moveRole2.updateDestination(new Vector2(-2000, 1900));
					state = EState.DO;
				}
				break;
			case DO:
				if (moveRole1.checkMoveCondition() && moveRole2.checkMoveCondition())
				{
					moveRole1.updateDestination(new Vector2(-2000, 1500));
					moveRole2.updateDestination(new Vector2(2000, 1900));
					state = EState.PREPARE;
				}
				break;
		}
		moveRole1.updateLookAtTarget(moveRole2.getPos());
		moveRole2.updateLookAtTarget(moveRole1.getPos());
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
