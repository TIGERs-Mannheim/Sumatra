/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Chip fast without dribbler. Skill is based on KickSkill and supports turning around ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipSkill extends KickSkill
{
	/**
	 * @param target
	 */
	public ChipSkill(final DynamicPosition target)
	{
		this(ESkillName.CHIP_FAST, target);
	}
	
	
	/**
	 * @param target
	 * @param moveMode
	 */
	public ChipSkill(final DynamicPosition target, final EMoveMode moveMode)
	{
		this(ESkillName.CHIP_FAST, target, moveMode);
	}
	
	
	/**
	 * @param skillName
	 * @param target
	 */
	protected ChipSkill(final ESkillName skillName, final DynamicPosition target)
	{
		this(skillName, target, EMoveMode.NORMAL);
	}
	
	
	/**
	 * @param skillName
	 * @param target
	 */
	protected ChipSkill(final ESkillName skillName, final DynamicPosition target, final EMoveMode moveMode)
	{
		super(skillName, target, EKickMode.PASS, moveMode);
		setDevice(EKickerDevice.CHIP);
	}
	
}
