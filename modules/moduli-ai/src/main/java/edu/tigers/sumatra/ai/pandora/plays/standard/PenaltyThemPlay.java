/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;


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
	protected ARole onAddRole()
	{
		return new MoveRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		updateMoveRoles(frame, -1, (int) Geometry.getBotRadius() * 2);
	}
}
