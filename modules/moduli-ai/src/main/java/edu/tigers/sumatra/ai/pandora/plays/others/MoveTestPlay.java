/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 7, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.math.Vector2;


/**
 * This play will move two robots in opposite directions, while looking at each other.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveTestPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private MoveRole	moveRole1;
	private MoveRole	moveRole2;
	
	private EState		state	= EState.INIT;
	
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
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (getRoles().size() != 6)
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
				if (moveRole1.isDestinationReached() && moveRole2.isDestinationReached())
				{
					moveRole1.getMoveCon().updateDestination(new Vector2(2000, 1500));
					moveRole2.getMoveCon().updateDestination(new Vector2(-2000, 1900));
					state = EState.DO;
				}
				break;
			case DO:
				if (moveRole1.isDestinationReached() && moveRole2.isDestinationReached())
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
	protected ARole onRemoveRole(final MetisAiFrame frame)
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
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		switch (getRoles().size())
		{
			case 0:
				moveRole1 = new MoveRole(EMoveBehavior.NORMAL);
				return moveRole1;
			case 1:
				moveRole2 = new MoveRole(EMoveBehavior.NORMAL);
				return moveRole2;
			default:
				throw new IllegalStateException();
		}
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
