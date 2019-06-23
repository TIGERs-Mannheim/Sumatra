/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.05.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty;

import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;


/**
 * Penalty play for penalties in game with all bots. It can finish.
 * 
 * @author Malte, GuntherB
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PenaltyUsPlay extends APenaltyUsPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<PassiveDefenderRole>	defendersFront			= new LinkedList<PassiveDefenderRole>();
	private final List<PassiveDefenderRole>	defendersBack			= new LinkedList<PassiveDefenderRole>();
	
	private int											defBackCounter			= 0;
	private static final int						DEFENDERS_ON_BACK		= 2;
	
	private static final int						POSITION_LINES_DIST	= 250;
	
	private static final float						POSITION_LINE_DIST	= 100;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public PenaltyUsPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		// add 2 back defenders
		for (int i = 0; (i < (getNumAssignedRoles() - 2)) && (i < DEFENDERS_ON_BACK); i++)
		{
			final IVector2 position = getNextBackPosition();
			final PassiveDefenderRole defender = new PassiveDefenderRole(position, AIConfig.getGeometry().getGoalTheir()
					.getGoalCenter());
			addDefensiveRole(defender, position);
			defendersBack.add(defender);
		}
		// for the rest of the roles, add front defenders
		for (int i = 0; i < (getNumAssignedRoles() - 2 - DEFENDERS_ON_BACK); i++)
		{
			final IVector2 position = getNextInitPosition(true);
			final PassiveDefenderRole defender = new PassiveDefenderRole(position, AIConfig.getGeometry().getGoalTheir()
					.getGoalCenter());
			addDefensiveRole(defender, position);
			defendersFront.add(defender);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2 getNextBackPosition()
	{
		defBackCounter++;
		if (defBackCounter > 1)
		{
			defBackCounter = -2;
		}
		
		return AIConfig.getGeometry().getPenaltyMarkOur()
				.addNew(new Vector2(POSITION_LINE_DIST, defBackCounter * POSITION_LINES_DIST));
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		super.beforeUpdate(currentFrame);
		for (final PassiveDefenderRole defender : defendersFront)
		{
			if (defender.checkMovementCondition(currentFrame.worldFrame) == EConditionState.BLOCKED)
			{
				final float corrDest = AIConfig.getGeometry().getBotRadius();
				IVector2 newDest = defender.getDestination().addNew(new Vector2(0, corrDest));
				if (Math.abs(newDest.y()) > (AIConfig.getGeometry().getFieldWidth() / 2))
				{
					newDest = getNextInitPosition(true);
				}
				defender.updateDestination(newDest);
			}
			defender.updateLookAtTarget(currentFrame.worldFrame.ball.getPos());
		}
		for (final PassiveDefenderRole defender : defendersBack)
		{
			if (defender.checkMovementCondition(currentFrame.worldFrame) == EConditionState.BLOCKED)
			{
				defender.updateDestination(getNextBackPosition());
			}
			defender.updateLookAtTarget(currentFrame.worldFrame.ball.getPos());
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
