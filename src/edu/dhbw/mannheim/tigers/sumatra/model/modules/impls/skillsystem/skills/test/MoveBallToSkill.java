/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Move the ball to a destination
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveBallToSkill extends PositionSkill
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log							= Logger.getLogger(MoveBallToSkill.class.getName());
	
	private static final int		TIMEOUT_BALLCONTACT		= 100;
	private static final int		TIME_WAIT_END				= 1000;
	
	private final IVector2			ballTarget;
	private long						timeLostBallContact		= 0;
	private long						timeWaitBeforeComplete	= 0;
	private boolean					switchToWait				= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param ballTarget
	 */
	public MoveBallToSkill(final IVector2 ballTarget)
	{
		super(ESkillName.MOVE_BALL_TO);
		this.ballTarget = ballTarget;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		if (switchToWait)
		{
			switchToWait = false;
			getDevices().dribble(cmds, false);
		}
		if (isMoveComplete())
		{
			complete();
		}
	}
	
	
	private boolean isMoveComplete()
	{
		if (timeWaitBeforeComplete != 0)
		{
			if ((SumatraClock.nanoTime() - timeWaitBeforeComplete) > TimeUnit.MILLISECONDS.toNanos(TIME_WAIT_END))
			{
				return true;
			}
			return false;
		}
		if (!hasBallContact())
		{
			if (timeLostBallContact == 0)
			{
				timeLostBallContact = SumatraClock.currentTimeMillis();
			} else if ((SumatraClock.currentTimeMillis() - timeLostBallContact) > TIMEOUT_BALLCONTACT)
			{
				log.debug("Lost ball contact");
				return true;
			}
		} else
		{
			timeLostBallContact = 0;
		}
		if (isDestinationReached())
		{
			if (timeWaitBeforeComplete == 0)
			{
				timeWaitBeforeComplete = SumatraClock.nanoTime();
				switchToWait = true;
			}
		}
		return false;
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		getMoveCon().setSpeed(EMovingSpeed.SLOW, 1.5f);
		setDestination(GeoMath.stepAlongLine(ballTarget, getPos(), AIConfig.getGeometry().getBotRadius()));
		setOrientation(ballTarget.subtractNew(getPos()).getAngle());
	}
}
