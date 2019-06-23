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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standards.penalty.PenaltyUsShooterRole;


/**
 * Play controlling a penalty situation for our team.
 * 
 * @author Malte, GuntherB
 * 
 */
public class PenaltyUsPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= -7157031717592600319L;
	
	private PenaltyUsShooterRole	shooter;
	private PassiveDefenderRole	helper;
	
	/** Indicates whether the penalty is allowed. */
	private boolean					ready					= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PenaltyUsPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PENALTY_US, aiFrame);
		shooter = new PenaltyUsShooterRole();
		helper = new PassiveDefenderRole();
		addAggressiveRole(shooter);
		addDefensiveRole(helper);
		
		helper.setDestTolerance(500);
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		// Change state
		if (currentFrame.refereeMsg != null && currentFrame.refereeMsg.cmd == ERefereeCommand.Ready)
		{
			ready = true;
			setTimeout(8);
		}
		shooter.setReady(ready);
		
		helper.setDest(AIConfig.getGeometry().getCenter());
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// Cancel Penalty Play
		ICircle penCirc = new Circlef(AIConfig.getGeometry().getPenaltyMarkTheir(), 1000);
		if (ready && !penCirc.isPointInShape(currentFrame.worldFrame.ball.pos))
		{
			changeToFailed();
		}
		
	}
	

	@Override
	protected void timedOut()
	{
		shooter.setDesperateShot(true);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
