/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.kick;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;


/**
 * Simple shooter role for kick skill testing
 */
public class KickTestRole extends ARole
{
	@Configurable(comment = "The kick skill to use", defValue = "TOUCH")
	private static EKickSkill kickSkill = EKickSkill.TOUCH;

	private final IVector2 passTarget;
	private final EKickerDevice kickerDevice;
	private final double kickSpeed;


	public KickTestRole(
			final IVector2 passTarget,
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

	private enum EKickSkill
	{
		TOUCH,
		SINGLE_TOUCH,
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
			var skill = getSkill();
			setNewSkill(skill);

			skill.getMoveCon().physicalObstaclesOnly();
		}


		private AMoveToSkill getSkill()
		{
			if (kickSkill == EKickSkill.TOUCH)
			{
				return new TouchKickSkill(passTarget, KickParams.of(kickerDevice, kickSpeed));
			} else if (kickSkill == EKickSkill.SINGLE_TOUCH)
			{
				return new SingleTouchKickSkill(passTarget, KickParams.of(kickerDevice, kickSpeed));
			}
			throw new IllegalArgumentException("Unknown kick skill type: " + kickSkill);
		}
	}

	private class WaitState extends AState
	{
		@Override
		public void doEntryActions()
		{
			MoveToSkill skill = MoveToSkill.createMoveToSkill();
			skill.getMoveCon().physicalObstaclesOnly();
			setNewSkill(skill);
		}
	}
}
