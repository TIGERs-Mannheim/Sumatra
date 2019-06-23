/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2010
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * Use this condition to check that a bot "looks" at a certain point e.g the ball.
 * The target is passed via corresponding update method.
 * 
 * @author DanielW
 * 
 */
public class LookAtCon extends ViewAngleCon
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Vector2	lookAtTarget	= new Vector2(0, 0);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public LookAtCon()
	{
		super(ECondition.LOOK_AT);
	}
	

	public LookAtCon(float tolerance)
	{
		super(ECondition.LOOK_AT, tolerance);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param newTarget the bot should look at
	 */
	public void updateTarget(IVector2 newTarget)
	{
		this.lookAtTarget.set(newTarget);
		resetCache();
	}
	

	@Override
	public boolean doCheckCondition(WorldFrame worldFrame, int botID)
	{
		super.updateTargetViewAngle(AIMath.angleBetweenXAxisAndLine(worldFrame.tigerBots.get(botID).pos, lookAtTarget));
		return super.doCheckCondition(worldFrame, botID);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the lookAtTarget
	 */
	public IVector2 getLookAtTarget()
	{
		return lookAtTarget;
	}
}
