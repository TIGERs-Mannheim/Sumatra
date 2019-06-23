/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.skillsystem.ESkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToTrajSkill extends AMoveToSkill
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MoveToTrajSkill.class.getName());
	
	
	/**
	 * Default
	 */
	public MoveToTrajSkill()
	{
		super(ESkill.MOVE_TO_TRAJ);
		setInitialState(new MoveToState(this));
	}
	
	
	@Override
	protected final void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		
		if (getMoveCon().isArmChip())
		{
			kickerDribblerOutput.setKick(getMoveCon().getKickSpeed(), EKickerDevice.CHIP, EKickerMode.ARM);
			if (getBall().getPos().distanceTo(getPos()) < 1000)
			{
				kickerDribblerOutput.setDribblerSpeed(getMoveCon().getDribblerSpeed());
			}
		} else
		{
			kickerDribblerOutput.setKick(0, EKickerDevice.CHIP, EKickerMode.DISARM);
			kickerDribblerOutput.setDribblerSpeed(0);
		}
	}
}
