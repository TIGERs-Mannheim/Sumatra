/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;


/**
 * Chip fast without dribbler. Skill is based on KickSkill and supports turning around ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipFastSkill extends KickSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target
	 */
	public ChipFastSkill(final DynamicPosition target)
	{
		super(ESkillName.CHIP_FAST, target, EKickMode.PASS);
	}
	
	
	/**
	 * @param skillName
	 * @param target
	 */
	protected ChipFastSkill(final ESkillName skillName, final DynamicPosition target)
	{
		super(skillName, target, EKickMode.PASS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void generateKickCmd(final List<ACommand> cmds)
	{
		if (getBot().getBotFeatures().get(EFeature.CHIP_KICKER) == EFeatureState.WORKING)
		{
			if (getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.KAPUT)
			{
				getDevices().chip(cmds, getChipParams(), EKickerMode.FORCE);
			} else
			{
				getDevices().chip(cmds, getChipParams(), EKickerMode.ARM);
			}
		}
	}
	
	
	protected ChipParams getChipParams()
	{
		float kickLength = getReceiver().subtractNew(getWorldFrame().getBall().getPos()).getLength2();
		return TigerDevices.calcChipFastParams(kickLength);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
