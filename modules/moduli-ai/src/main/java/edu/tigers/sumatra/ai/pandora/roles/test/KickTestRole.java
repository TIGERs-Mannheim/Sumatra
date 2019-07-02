/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Simple shooter role for kick skill testing
 */
public class KickTestRole extends ARole
{
	@Configurable(comment = "Allow entering the penalty areas for testing purposes", defValue = "true")
	private static boolean allowPenAreas = true;
	
	private final DynamicPosition passTarget;
	private final EKickerDevice kickerDevice;
	private final double kickSpeed;
	
	
	public KickTestRole(
			final DynamicPosition passTarget,
			final EKickerDevice kickerDevice,
			final double kickSpeed)
	{
		super(ERole.KICK_TEST);
		this.passTarget = passTarget;
		this.kickerDevice = kickerDevice;
		this.kickSpeed = kickSpeed;
		
		final KickState kickState = new KickState();
		
		addTransition(EEvent.KICK, kickState);
		addTransition(EEvent.WAIT, new WaitState());
		
		setInitialState(kickState);
	}
	
	private enum EEvent implements IEvent
	{
		KICK,
		WAIT
	}
	
	
	public void switchToKick()
	{
		triggerEvent(EEvent.KICK);
	}
	
	
	public void switchToWait()
	{
		triggerEvent(EEvent.WAIT);
	}
	
	private class KickState extends AState
	{
		@Override
		public void doEntryActions()
		{
			TouchKickSkill skill = new TouchKickSkill(passTarget, KickParams.of(kickerDevice, kickSpeed));
			setNewSkill(skill);
			
			skill.getMoveCon().setPenaltyAreaAllowedOur(allowPenAreas);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(allowPenAreas);
		}
	}
	
	private class WaitState extends AState
	{
		@Override
		public void doEntryActions()
		{
			setNewSkill(AMoveToSkill.createMoveToSkill());
		}
	}
}
