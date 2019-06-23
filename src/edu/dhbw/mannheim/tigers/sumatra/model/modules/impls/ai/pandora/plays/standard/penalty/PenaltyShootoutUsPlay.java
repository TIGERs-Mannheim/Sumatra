/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.05.2011
 * Author(s): DanielAl
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Penalty play for the shotout at the end. Only two bots are alowed to be on the field. Play should not finish and no
 * GAME plays should be selected.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class PenaltyShootoutUsPlay extends APenaltyUsPlay
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
	public PenaltyShootoutUsPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		IVector2 distance = new Vector2f(AIConfig.getGeometry().getBotRadius() + BOT_DIST, 0);
		IVector2 posStart = new Vector2f(AIConfig.getGeometry().getMaintenancePosition()
				.subtractNew(distance.multiplyNew(numAssignedRoles / 2f)));
		
		if (aiFrame.worldFrame.getTigerBotsAvailable().size() > 2)
		{
			for (int i = 0; i < (aiFrame.worldFrame.getTigerBotsAvailable().size() - 2); i++)
			{
				// add a role that looks down to opponent goal line
				final Vector2 initPos = distance.multiplyNew(i + 1);
				initPos.add(posStart);
				ARole role = new MoveRole(EMoveBehavior.NORMAL);
				role.updateTargetAngle(0f);
				addAggressiveRole(role, initPos);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void finish()
	{
		// do not finish this play
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
