/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.KickBallWatcher;


/**
 * Use custom duration for kicking
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickTestSkill extends KickSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final int					duration;
	
	
	private final KickBallWatcher	watcher;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target
	 * @param duration
	 */
	public KickTestSkill(final IVector2 target, final int duration)
	{
		super(ESkillName.KICK_TEST, new DynamicPosition(target), EKickMode.PASS);
		this.duration = duration;
		watcher = new KickBallWatcher(duration, Math.abs(target.x()) - 100);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected int calcDuration(final float length)
	{
		return duration;
	}
	
	
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		super.calcEntryActions(cmds);
		watcher.start();
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
