/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.wp.data.Geometry;


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
		updateMoveRoles(frame, getRoles(), -1, (int) Geometry.getBotRadius() * 2);
	}
}
