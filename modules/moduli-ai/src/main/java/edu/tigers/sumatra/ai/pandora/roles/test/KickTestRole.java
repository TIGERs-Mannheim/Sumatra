/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.PullBallSkill;
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
	@Configurable(comment = "Allow entering the penalty areas for testing purposes", defValue = "false")
	private static boolean forceAllowPenAreas = false;
	
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
		final PlaceBallState placeBallState = new PlaceBallState();
		
		addTransition(EEvent.KICK, kickState);
		addTransition(EEvent.PLACE_BALL, placeBallState);
		
		setInitialState(kickState);
	}
	
	private enum EEvent implements IEvent
	{
		KICK,
		PLACE_BALL
	}
	
	private class KickState extends AState
	{
		private TouchKickSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new TouchKickSkill(passTarget, KickParams.of(kickerDevice, kickSpeed));
			setNewSkill(skill);
			
			if (forceAllowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaAllowedOur(true);
				skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!Geometry.getField().isPointInShape(getBall().getPos()) && getBall().getVel().getLength2() < 0.1)
			{
				triggerEvent(EEvent.PLACE_BALL);
			}
		}
	}
	
	private class PlaceBallState extends AState
	{
		PullBallSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			IVector2 target = Geometry.getField().withMargin(-100).nearestPointInside(getBall().getPos());
			skill = new PullBallSkill(target);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (Geometry.getField().withMargin(-50).isPointInShape(getBall().getPos()) && skill.hasReleasedBall())
			{
				triggerEvent(EEvent.KICK);
			}
		}
	}
}
