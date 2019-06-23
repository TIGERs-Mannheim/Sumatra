/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Simple shooter role, basically to test skill
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimpleShooterRole extends ARole
{
	private DynamicPosition	passTarget;
	private EKickerDevice	device;
	
	
	/**
	 * Pass straight to target
	 * 
	 * @param passTarget
	 */
	public SimpleShooterRole(final DynamicPosition passTarget)
	{
		this(passTarget, EKickerDevice.STRAIGHT);
	}
	
	
	/**
	 * @param passTarget
	 * @param device
	 */
	public SimpleShooterRole(final DynamicPosition passTarget, final EKickerDevice device)
	{
		super(ERole.SIMPLE_SHOOTER);
		this.device = device;
		setPassTarget(passTarget);
		setInitialState(new ShootState());
	}
	
	
	private class ShootState implements IState
	{
		private AKickSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new KickChillSkill(passTarget);
			skill.setKickMode(EKickMode.PASS);
			skill.setDevice(device);
			setNewSkill(skill);
		}
	}
	
	
	/**
	 * Set the Target where to pass to.
	 * 
	 * @param passTarget
	 */
	public void setPassTarget(final DynamicPosition passTarget)
	{
		this.passTarget = passTarget;
	}
}
