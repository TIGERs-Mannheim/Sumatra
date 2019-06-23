/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * This is a dribble skill for using the dribble device of the TigerRobot.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class DribbleBall extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * This is a constant for rpm of the dribble device.
	 */
	private final int	REF_RPM	= AIConfig.getSkills().getRefRPM();
	
	private int			rpm;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This skill switches the dribbling device on or off.
	 * @param status set status of dribbling device
	 */
	public DribbleBall(boolean status)
	{
		super(ESkillName.DRIBBLE_BALL, ESkillGroup.DRIBBLE);
		
		if (status)
		{
			this.rpm = REF_RPM;
		} else
		{
			this.rpm = 0;
		}
	}
	

	/**
	 * This skill activates the dribbling device with a specified rpm value.
	 * @param rpm of the dribbling device
	 */
	public DribbleBall(int rpm)
	{
		super(ESkillName.DRIBBLE_BALL, ESkillGroup.DRIBBLE);
		
		this.rpm = rpm;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		// check if bot is available
		if (getBot() == null)
		{
			return cmds;
		}
		
		cmds.add(new TigerDribble(rpm));
		// do never complete: dribble until another command comes in
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		DribbleBall dribble = (DribbleBall) newSkill;
		return this.rpm == dribble.rpm;
	}
}
