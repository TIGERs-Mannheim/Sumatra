/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ATouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Simple kickoff shooter role, intended for the kickoff shot
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class KickoffShooterRole extends ARole
{
	@Configurable(comment = "The minimum score of a direct shot to do it", defValue = "0.2")
	private static double minimumScoreForDirectKick = 0.2;
	
	@Configurable(defValue = "3.0")
	private static double passEndVel = 3.0;
	
	private boolean normalStartCalled = false;
	private EMoveState state = EMoveState.LEFT;
	private int numberOfTurns;
	
	private IVector2 moveDestination;
	
	
	public KickoffShooterRole()
	{
		super(ERole.KICKOFF_SHOOTER);
		numberOfTurns = 0;
		
		moveDestination = Geometry.getCenter().subtractNew(Vector2.fromXY(300, 0));
		
		IState state1 = new PrepareState();
		setInitialState(state1);
		addTransition(state1, EEvent.READY, new MoveState());
	}
	
	private enum EEvent implements IEvent
	{
		READY
	}
	
	private enum EMoveState
	{
		LEFT,
		RIGHT,
		MIDDLE
	}
	
	private class PrepareState extends AState
	{
		
		AMoveToSkill skill = null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateTargetAngle(0);
			skill.getMoveCon().updateDestination(Vector2.fromXY(-300, 0));
			skill.getMoveCon().setIgnoreGameStateObstacles(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getAiFrame().getRefereeMsg().getCommand() == Command.NORMAL_START)
			{
				normalStartCalled = true;
			}
			
			if (normalStartCalled && (VectorMath.distancePP(getPos(), skill.getMoveCon().getDestination()) < 100)
					&& (getBot().getVel().getLength2() < 0.2))
			{
				triggerEvent(EEvent.READY);
			}
		}
	}
	
	private class MoveState extends AState
	{
		
		private AMoveToSkill skill = null;
		private int counter = 0;
		private boolean kickSkillSet = false;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (VectorMath.distancePP(getPos(), moveDestination) < 50)
			{
				if (state == EMoveState.RIGHT)
				{
					state = EMoveState.LEFT;
				} else
				{
					state = EMoveState.RIGHT;
				}
				counter++;
			}
			
			counter = 1;
			if ((counter > numberOfTurns) && !kickSkillSet)
			{
				kickSkillSet = true;
				final IVector2 shotTarget = findKickOffTarget();
				
				ATouchKickSkill kickSkill;
				
				if (shotTarget != null)
				{
					kickSkill = prepareDirectKick(shotTarget);
				} else
				{
					kickSkill = prepareIndirectKick();
				}
				
				setNewSkill(kickSkill);
				
			}
			skill.getMoveCon().updateDestination(moveDestination);
		}
		
		
		private IVector2 findKickOffTarget()
		{
			return getAiFrame().getTacticalField().getBestGoalKickTarget()
					.filter(s -> s.getScore() >= minimumScoreForDirectKick)
					.map(IRatedTarget::getTarget)
					.map(DynamicPosition::getPos)
					.orElse(null);
		}
		
		
		private ATouchKickSkill prepareDirectKick(final IVector2 target)
		{
			return new TouchKickSkill(new DynamicPosition(target), KickParams.maxStraight());
		}
		
		
		private ATouchKickSkill prepareIndirectKick()
		{
			IVector2 target = getAiFrame().getTacticalField().getKickoffStrategy().getBestShotTarget();
			
			if (target == null)
			{
				target = Geometry.getGoalTheir().getCenter();
			}
			
			double distance = target.distanceTo(getBall().getPos());
			double kickSpeed = getBall().getStraightConsultant().getInitVelForDist(distance, passEndVel);
			return new SingleTouchKickSkill(new DynamicPosition(target), KickParams.straight(kickSpeed));
		}
	}
	
}
