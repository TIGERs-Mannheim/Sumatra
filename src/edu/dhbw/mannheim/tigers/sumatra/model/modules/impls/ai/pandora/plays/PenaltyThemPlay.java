/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Handle bots that would normally be Supporters and Offense
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenaltyThemPlay extends APenaltyPlay
{
	
	/**
	  */
	public PenaltyThemPlay()
	{
		super(EPlay.PENALTY_THEM);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		return new MoveRole(EMoveBehavior.LOOK_AT_BALL);
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		updateMoveRoles(frame, getRoles(), -1, (int) AIConfig.getGeometry().getBotRadius() * 2);
	}
}
