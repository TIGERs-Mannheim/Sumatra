/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Move the ball to a destination
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class MoveBallToSkill extends AMoveSkill
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
	public MoveBallToSkill(IVector2 ballTarget)
	{
		super(ESkillName.MOVE_BALL_TO);
		this.ballTarget = ballTarget;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		if (switchToWait)
		{
			switchToWait = false;
			getDevices().dribble(cmds, false);
			stopMove(cmds);
		}
	}
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		if (timeWaitBeforeComplete != 0)
		{
			if ((System.nanoTime() - timeWaitBeforeComplete) > TimeUnit.MILLISECONDS.toNanos(TIME_WAIT_END))
			{
				return true;
			}
			return false;
		}
		if (!bot.hasBallContact())
		{
			if (timeLostBallContact == 0)
			{
				timeLostBallContact = System.currentTimeMillis();
			} else if ((System.currentTimeMillis() - timeLostBallContact) > TIMEOUT_BALLCONTACT)
			{
				log.debug("Lost ball contact");
				return true;
			}
		} else
		{
			timeLostBallContact = 0;
		}
		boolean trajCompleted = super.isComplete(bot);
		if (trajCompleted)
		{
			log.debug("Completed due to spline done");
		}
		return waitBeforeComplete(trajCompleted);
	}
	
	
	private boolean waitBeforeComplete(boolean completed)
	{
		if (completed)
		{
			if (timeWaitBeforeComplete == 0)
			{
				timeWaitBeforeComplete = System.nanoTime();
				switchToWait = true;
			}
		}
		return false;
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, true);
		getDevices().disarm(cmds);
		
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(2.0f, 0.5f);
		gen.setReducePathScore(0.0f);
		gen.setRotationTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxRotateVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxRotateAcceleration());
		List<IVector2> nodes = new LinkedList<IVector2>();
		nodes.add(GeoMath.stepAlongLine(ballTarget, bot.getPos(), AIConfig.getGeometry().getBotRadius()));
		
		createSpline(bot, nodes, ballTarget, gen);
		return cmds;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		return cmds;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
