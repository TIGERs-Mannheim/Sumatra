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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
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
	
	private EState				state	= EState.INIT;
	
	private enum EState
	{
		INIT,
		PREPARE,
		DO
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public MoveTestPlay()
	{
		super(EPlay.MOVE_TEST);
		moveRole1 = new MoveRole(EMoveBehavior.NORMAL);
		moveRole2 = new MoveRole(EMoveBehavior.NORMAL);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void doUpdate(AthenaAiFrame frame)
	{
		if (getRoles().size() != 2)
		{
			return;
		}
		switch (state)
		{
			case INIT:
				moveRole1.getMoveCon().updateDestination(new Vector2(-2000, 1500));
				moveRole2.getMoveCon().updateDestination(new Vector2(2000, 1900));
				state = EState.PREPARE;
				break;
			case PREPARE:
				if (moveRole1.checkMoveCondition() && moveRole2.checkMoveCondition())
				{
					moveRole1.getMoveCon().updateDestination(new Vector2(2000, 1500));
					moveRole2.getMoveCon().updateDestination(new Vector2(-2000, 1900));
					state = EState.DO;
				}
				break;
			case DO:
				if (moveRole1.checkMoveCondition() && moveRole2.checkMoveCondition())
				{
					moveRole1.getMoveCon().updateDestination(new Vector2(-2000, 1500));
					moveRole2.getMoveCon().updateDestination(new Vector2(2000, 1900));
					state = EState.PREPARE;
				}
				break;
		}
		moveRole1.getMoveCon().updateLookAtTarget(moveRole2.getPos());
		moveRole2.getMoveCon().updateLookAtTarget(moveRole1.getPos());
	}
	
	
	@Override
	protected ARole onRemoveRole()
	{
		switch (getRoles().size())
		{
			case 1:
				return moveRole1;
			case 2:
				return moveRole2;
			default:
				throw new IllegalStateException();
		}
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		switch (getRoles().size())
		{
			case 0:
				return moveRole1;
			case 1:
				return moveRole2;
			default:
				throw new IllegalStateException();
		}
	}
	
	
	@Override
	protected void onGameStateChanged(EGameState gameState)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
