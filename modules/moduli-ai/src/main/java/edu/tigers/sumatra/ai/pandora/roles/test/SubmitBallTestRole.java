/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SubmitBallTestRole extends ARole
{
	
	private final double submitDist;
	
	
	/**
	 * @param submitDist
	 */
	public SubmitBallTestRole(final double submitDist)
	{
		super(ERole.SUBMIT_BALL_TEST);
		this.submitDist = submitDist;
		
		IState def = new DefaultState();
		IState wait = new WaitState();
		addTransition(EEvent.BALL_STOPPED, def);
		addTransition(EEvent.WAIT, wait);
		setInitialState(def);
	}
	
	private enum EEvent implements IEvent
	{
		WAIT,
		BALL_STOPPED
	}
	
	private class DefaultState implements IState
	{
		DynamicPosition	target;
		KickNormalSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			target = new DynamicPosition(Vector2.zero());
			skill = new KickNormalSkill(target);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() > 0.1)
			{
				if (getBall().getVel().angleToAbs(Vector2.X_AXIS).orElse(0.0) > AngleMath.PI_HALF)
				{
					triggerEvent(EEvent.WAIT);
				} else
				{
					target.update(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
					skill.setKickMode(AKickSkill.EKickMode.MAX);
				}
			} else
			{
				target.update(new DynamicPosition(findTarget()));
				skill.setKickMode(AKickSkill.EKickMode.POINT);
			}
		}
		
		
		private IVector2 findTarget()
		{
			return LineMath.stepAlongLine(getBall().getPos(), Geometry.getPenaltyMarkTheir(), submitDist);
		}
	}
	
	private class WaitState implements IState
	{
		@Override
		public void doEntryActions()
		{
			setNewSkill(new IdleSkill());
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() < 0.1)
			{
				triggerEvent(EEvent.BALL_STOPPED);
			}
		}
	}
}
