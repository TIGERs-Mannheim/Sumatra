/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.02.2011
 * Author(s): Malte
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard.penalty.KeeperPenaltyThemRole;


/**
 * Play which is chosen, when opponent team gets a penalty.<br>
 * Requires:<li>1 {@link KeeperPenaltyThemRole}</li> <li>{@link PassiveDefenderRole}s</li> of
 * the shooter!
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenaltyShootoutThemPlay extends APenaltyThemPlay
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
	public PenaltyShootoutThemPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		final float positioningPre = AIConfig.getDefaultBotConfig().getGeneral().getPositioningPreAiming();
		final IVector2 destination = AIConfig.getGeometry().getPenaltyMarkTheir().addNew(new Vector2(-positioningPre, 0));
		ARole nextShooterRole = new PassiveDefenderRole(destination, AIConfig.getGeometry().getGoalTheir()
				.getGoalCenter());
		addAggressiveRole(nextShooterRole, destination);
		
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
