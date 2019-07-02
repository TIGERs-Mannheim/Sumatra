/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;


/**
 * A MoveTo implementation that uses the {@link MoveToState} with trajectory path planning
 */
public class MoveToTrajSkill extends AMoveToSkill
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MoveToTrajSkill.class.getName());
	
	
	/**
	 * Default constructor. Please use {@link AMoveToSkill#createMoveToSkill()} to instantiate your skill
	 */
	MoveToTrajSkill()
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
			final double kickSpeed = KickParams.limitKickSpeed(getMoveCon().getKickSpeed());
			kickerDribblerOutput.setKick(kickSpeed, EKickerDevice.CHIP, EKickerMode.ARM);
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
