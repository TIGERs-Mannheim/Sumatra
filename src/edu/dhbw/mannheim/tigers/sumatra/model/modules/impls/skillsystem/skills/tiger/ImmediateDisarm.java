/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.11.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Mode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;

/**
 * Stops and discharges kicking-devices
 * 
 * @author Gero
 * 
 */
public class ImmediateDisarm extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public ImmediateDisarm()
	{
		super(ESkillName.IMMEDIATE_DISARM, ESkillGroup.KICK);
	}


	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{	
		// Stop and disarm kicking-devices
		cmds.add(new TigerKickerKickV2(Device.STRAIGHT, Mode.DISARM, 0));
		cmds.add(new TigerKickerKickV2(Device.CHIP, Mode.DISARM, 0));
		
		complete();
		
		return cmds;
	}


	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return true;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
