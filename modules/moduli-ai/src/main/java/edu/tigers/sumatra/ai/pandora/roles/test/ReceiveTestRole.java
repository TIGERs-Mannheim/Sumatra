/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Test ReceiveSkill by kicking the ball and catching it afterwards.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReceiveTestRole extends ARole
{
	private final double		passEndVel;
	private final double		passDist;
	
	private DynamicPosition	target;
	
	private enum EEvent implements IEvent
	{
		RECEIVED,
		KICKED
	}
	
	
	/**
	 * @param passEndVel
	 * @param passDist
	 */
	public ReceiveTestRole(final double passEndVel, final double passDist)
	{
		super(ERole.RECEIVE_TEST);
		this.passEndVel = passEndVel;
		this.passDist = passDist;
		
		IState receiveState = new ReceiveState();
		IState kickState = new KickState();
		setInitialState(kickState);
		addTransition(EEvent.RECEIVED, kickState);
		addTransition(EEvent.KICKED, receiveState);
	}
	
	
	private class ReceiveState implements IState
	{
		@Override
		public void doEntryActions()
		{
			ReceiverSkill skill = new ReceiverSkill();
			skill.setReceiveDestination(target);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() < 0.1)
			{
				triggerEvent(EEvent.RECEIVED);
			}
		}
	}
	
	private class KickState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			target = new DynamicPosition(findPassTarget());
			AKickSkill skill = new KickNormalSkill(target);
			double minPassSpeed = 0;
			double kickSpeed = SkillUtil.passKickSpeed(getBall().getStraightConsultant(),
					getBall().getPos().distanceTo(target),
					passEndVel, minPassSpeed);
			skill.setKickSpeed(kickSpeed);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() > 0.3)
			{
				triggerEvent(EEvent.KICKED);
			}
		}
		
		
		private IVector2 findPassTarget()
		{
			IVector2 farthest = AiMath.getFarthestPointOnFieldBorder(getBall().getPos());
			return LineMath.stepAlongLine(getBall().getPos(), farthest, passDist);
		}
	}
}
