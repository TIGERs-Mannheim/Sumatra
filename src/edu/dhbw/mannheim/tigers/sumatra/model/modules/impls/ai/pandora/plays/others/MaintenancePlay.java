/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * This play moves all bots to the maintenance position.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class MaintenancePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	BOT_DIST	= 120;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public MaintenancePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		IVector2 distance = new Vector2f(AIConfig.getGeometry().getBotRadius() + BOT_DIST, 0);
		
		IVector2 posStart = new Vector2f(AIConfig.getGeometry().getMaintenancePosition()
				.subtractNew(distance.multiplyNew(numAssignedRoles / 2f)));
		
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			// add a role that looks down to opponent goal line
			final Vector2 initPos = distance.multiplyNew(i + 1);
			initPos.add(posStart);
			ARole role = new MoveRole(EMoveBehavior.NORMAL);
			role.updateTargetAngle(0f);
			addAggressiveRole(role, initPos);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		for (ARole role : getRoles())
		{
			if (!role.checkAllConditions(currentFrame.worldFrame))
			{
				return;
			}
		}
		
		// all conditions of all roles are true, but do not finish, cause the bots should stay at there position
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		// nothing todo
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
