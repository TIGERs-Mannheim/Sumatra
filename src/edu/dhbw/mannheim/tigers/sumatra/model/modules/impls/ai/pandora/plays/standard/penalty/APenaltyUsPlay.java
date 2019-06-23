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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role;


/**
 * Abstract play controlling a penalty situation for our team.
 * 
 * @author Malte, GuntherB
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public abstract class APenaltyUsPlay extends APenaltyPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger			log						= Logger.getLogger(APenaltyUsPlay.class.getName());
	
	private final PassiveDefenderRole	preparer;
	private final ShooterV2Role			shooter;
	
	/** Indicates whether the penalty is allowed. */
	private boolean							ready						= false;
	private boolean							waitForResult			= false;
	
	private static final long				TIMEOUT_AFTER_READY	= 8;
	
	
	// private static final long TIMEOUT_PREPARE = 4;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public APenaltyUsPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		// wait until forever for ready signal
		setTimeout(Long.MAX_VALUE);
		
		final float positioningPre = AIConfig.getDefaultBotConfig().getGeneral().getPositioningPreAiming();
		IVector2 destination = AIConfig.getGeometry().getPenaltyMarkTheir().addNew(new Vector2(-positioningPre, 0));
		preparer = new PassiveDefenderRole(destination, AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		addAggressiveRole(preparer, destination);
		shooter = new ShooterV2Role();
		
		// keeper
		if (aiFrame.worldFrame.tigerBotsVisible.size() > 1)
		{
			KeeperSoloRole keeper = new KeeperSoloRole();
			addDefensiveRole(keeper, AIConfig.getGeometry().getGoalOur().getGoalCenter());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		// Change state
		if ((currentFrame.refereeMsg != null) && (currentFrame.refereeMsg.getCommand() == Command.NORMAL_START))
		{
			ready = true;
			setTimeout(TIMEOUT_AFTER_READY);
			switchRoles(preparer, shooter, currentFrame);
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (ready && shooter.isCompleted())
		{
			if (!waitForResult)
			{
				// wait max 2 seconds to wait for the result of the penalty
				setTimeout(2);
				waitForResult = true;
			}
			// have we scored a goal? than we were successful, else we weren't
			// FIXME DanielAl
			if (!currentFrame.worldFrame.ball.isOnCam()
					|| (currentFrame.worldFrame.ball.getPos().x() >= ((AIConfig.getGeometry().getFieldLength() / 2) - 5)))
			{
				finish();
				log.info("Penalty shot succeeded " + currentFrame.worldFrame.ball.getPos().x() + " "
						+ ((AIConfig.getGeometry().getFieldLength() / 2) - 5));
			}
		}
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		finish();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
