/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillShooterCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.EKickDevice;


/**
 * This Bot Skill will shoot on desired target by local positioning
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ShooterBotSkill extends ASkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2		shootTarget;
	private final EKickDevice	device;
	private final int				duration;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param shootTarget
	 * @param device
	 * @param duration
	 */
	public ShooterBotSkill(IVector2 shootTarget, EKickDevice device, int duration)
	{
		super(ESkillName.BOT_SHOOTER);
		this.shootTarget = shootTarget;
		this.device = device;
		this.duration = duration;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public List<ACommand> calcEntryActions(List<ACommand> cmds)
	{
		TigerSkillShooterCommand cmd = new TigerSkillShooterCommand(shootTarget, duration, device.getValue());
		cmds.add(cmd);
		return super.calcEntryActions(cmds);
	}
	
	
	@Override
	public List<ACommand> calcActions(List<ACommand> cmds)
	{
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
