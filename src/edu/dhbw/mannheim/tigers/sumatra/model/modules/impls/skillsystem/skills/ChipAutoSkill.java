/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Chip the ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ChipAutoSkill extends AKickSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2		target;
	private float					kickLength;
	private final float			rollDistance;
	
	private static final long	WAIT_BEFORE_CHIP_NS	= TimeUnit.MILLISECONDS.toNanos(500);
	private long					startTimeNs				= 0;
	private boolean				chipped					= false;
	private float					duration					= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target
	 * @param rollDistance
	 */
	public ChipAutoSkill(IVector2 target, float rollDistance)
	{
		super(ESkillName.CHIP_KICK);
		this.target = target;
		kickLength = 0;
		this.rollDistance = rollDistance;
		setTimeout(TimeUnit.MILLISECONDS.toNanos(5000));
	}
	
	
	/**
	 * @param length
	 * @param rollDistance
	 */
	public ChipAutoSkill(float length, float rollDistance)
	{
		super(ESkillName.CHIP_KICK);
		target = null;
		kickLength = length;
		this.rollDistance = rollDistance;
	}
	
	
	/**
	 * @param duration
	 */
	public ChipAutoSkill(float duration)
	{
		super(ESkillName.CHIP_KICK);
		target = null;
		kickLength = 0;
		rollDistance = 0;
		this.duration = duration;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		if (!chipped && (bot.getBotType() != EBotType.TIGER_V2)
				&& ((System.nanoTime() - startTimeNs) > WAIT_BEFORE_CHIP_NS))
		{
			getDevices().chipRoll(cmds, kickLength);
			calcSpline(bot);
			chipped = true;
		}
	}
	
	
	@Override
	protected List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		super.doCalcEntryActions(bot, cmds);
		if (target != null)
		{
			kickLength = GeoMath.distancePP(bot.getPos(), target) - AIConfig.getGeometry().getBotRadius();
		}
		if (duration != 0)
		{
			getDevices().chipTest(cmds, duration, 0);
			calcSpline(bot);
		} else if (rollDistance > 0)
		{
			getDevices().chipRoll(cmds, kickLength);
			calcSpline(bot);
		} else if (bot.getBotType() == EBotType.TIGER_V2)
		{
			getDevices().chipStop(cmds, kickLength, rollDistance);
			calcSpline(bot);
		} else
		{
			getDevices().dribble(cmds, getDevices().calcChipDribbleSpeed(kickLength, bot.getBotType()));
		}
		
		startTimeNs = System.nanoTime();
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
