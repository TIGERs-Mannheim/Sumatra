/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 13, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipParams;


/**
 * Test skill for chipping with given duration and dribble speed
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipTestSkill extends ChipSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final ChipParams	chipParams;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param duration
	 * @param dribbleSpeed
	 */
	public ChipTestSkill(final int duration, final int dribbleSpeed)
	{
		super(ESkillName.CHIP_DURATION, AVector2.ZERO_VECTOR, 0);
		chipParams = new ChipParams(duration, dribbleSpeed);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		setTarget(new Vector2(-getWorldFrame().getBall().getPos().x(), getWorldFrame().getBall().getPos().y()));
		super.calcEntryActions(cmds);
		return cmds;
	}
	
	
	@Override
	protected ChipParams getChipParams()
	{
		return chipParams;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
