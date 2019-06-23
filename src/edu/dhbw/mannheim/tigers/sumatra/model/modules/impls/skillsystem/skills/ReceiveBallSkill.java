/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 2, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Receive a ball that was passed to the bot. Uses the dribbler and completes if
 * it has ball contact or the ball moves away
 * You have to use the movecondition like in {@link MoveToSkill} to set destination.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ReceiveBallSkill extends MoveToSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log				= Logger.getLogger(ReceiveBallSkill.class.getName());
	
	private static final float		TOLERANCE		= 20;
	private float						distanceToBall	= Float.MAX_VALUE;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param moveCon
	 */
	public ReceiveBallSkill(MovementCon moveCon)
	{
		super(ESkillName.RECEIVE_BALL, moveCon);
		moveCon.setBallObstacle(false);
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, true);
		return super.doCalcEntryActions(bot, cmds);
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		return super.doCalcExitActions(bot, cmds);
	}
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		float dist = GeoMath.distancePP(bot.getPos(), getWorldFrame().ball.getPos());
		if ((distanceToBall + TOLERANCE) < dist)
		{
			log.trace("Completed due to new distance is bigger than last. Ball is moving away...");
			return true;
		}
		if (bot.hasBallContact())
		{
			log.trace("Completed due to ball contact");
			return true;
		}
		if (getWorldFrame().ball.getVel().isZeroVector())
		{
			log.trace("Completed due to lying ball.");
			return true;
		}
		distanceToBall = dist;
		return false;
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
