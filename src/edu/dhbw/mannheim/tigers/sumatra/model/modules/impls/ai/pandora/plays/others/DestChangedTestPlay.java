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

import java.awt.Color;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
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
public class DestChangedTestPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final MoveRole	moveRole1;
	
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
	public DestChangedTestPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		moveRole1 = new MoveRole(EMoveBehavior.NORMAL);
		addAggressiveRole(moveRole1, new Vector2(-500, 1500));
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
				IVector2 random = new Vector2(2000, 1300 + (Math.random() * 400));
				moveRole1.updateDestination(random);
				frame.addDebugShape(new DrawableCircle(new Circle(random, 80), Color.white));
				IVector2 lookAt = new Vector2(-200 + (Math.random() * 400), -1500);
				moveRole1.updateLookAtTarget(lookAt);
				frame.addDebugShape(new DrawableCircle(new Circle(lookAt, 20), Color.red));
				
				if (moveRole1.checkMoveCondition())
				{
					state = EState.DO;
				}
				break;
			case DO:
				random = new Vector2(-500, 1300 + (Math.random() * 400));
				moveRole1.updateDestination(random);
				frame.addDebugShape(new DrawableCircle(new Circle(random, 80), Color.white));
				if (moveRole1.checkMoveCondition())
				{
					state = EState.PREPARE;
				}
				break;
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
