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

import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard.penalty.KeeperPenaltyThemRole;


/**
 * Play which is chosen, when opponent team gets a penalty.<br>
 * Requires:<li>1 {@link KeeperPenaltyThemRole}</li> <li>{@link PassiveDefenderRole}s</li> of
 * the shooter!
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenaltyThemPlay extends APenaltyThemPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<PassiveDefenderRole>	defenders	= new LinkedList<PassiveDefenderRole>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public PenaltyThemPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		for (int i = 0; i < (getNumAssignedRoles() - 1); i++)
		{
			final IVector2 position = getNextInitPosition(false);
			final PassiveDefenderRole defender = new PassiveDefenderRole(position, AIConfig.getGeometry().getGoalTheir()
					.getGoalCenter());
			addDefensiveRole(defender, position);
			defenders.add(defender);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		super.beforeUpdate(currentFrame);
		
		for (final PassiveDefenderRole defender : defenders)
		{
			if (defender.checkMovementCondition(currentFrame.worldFrame) == EConditionState.BLOCKED)
			{
				defender.updateDestination(getNextInitPosition(false));
			}
			defender.updateLookAtTarget(currentFrame.worldFrame.ball.getPos());
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
