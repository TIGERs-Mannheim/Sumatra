/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickChillTestRole extends ARole
{
	private final double		rotation;
	private KickChillSkill skill;
	
	
	/**
	 * Default
	 * 
	 * @param rotation
	 */
	public KickChillTestRole(final double rotation)
	{
		super(ERole.KICK_CHILL_TEST);
		this.rotation = AngleMath.deg2rad(rotation);
		IState prepare = new PrepareState();
		IState shoot = new ShootState();
		setInitialState(prepare);
		addTransition(EEvent.DONE, prepare);
		addTransition(EEvent.STILL, shoot);
	}
	
	private enum EEvent implements IEvent
	{
		STILL,
		DONE
	}
	
	
	private DynamicPosition getTarget()
	{
		if (getBall().getPos().x() > 0)
		{
			return new DynamicPosition(Geometry.getGoalOur().getCenter());
		}
		return new DynamicPosition(Geometry.getGoalTheir().getCenter());
	}
	
	
	private class PrepareState implements IState
	{
		@Override
		public void doEntryActions()
		{
			if (skill == null)
			{
				skill = new KickChillSkill(getTurnedTarget());
				skill.setKickSpeed(1.0);
				setNewSkill(skill);
			}
			skill.setReceiver(getTurnedTarget());
			skill.setReadyForKick(false);
		}
		
		
		private DynamicPosition getTurnedTarget()
		{
			return new DynamicPosition(getTarget().turnNew(rotation));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() < 0.1 && getBot().getVel().getLength2() < 0.1)
			{
				triggerEvent(EEvent.STILL);
			}
		}
	}
	
	private class ShootState implements IState
	{
		@Override
		public void doEntryActions()
		{
			skill.setReadyForKick(true);
			skill.setReceiver(getTarget());
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() > 0.1
					|| getBall().getPos().distanceTo(getPos()) > 200)
			{
				triggerEvent(EEvent.DONE);
			}
		}
		
		
	}
}
