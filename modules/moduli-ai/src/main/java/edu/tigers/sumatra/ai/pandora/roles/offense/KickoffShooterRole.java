/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
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
	
	private boolean normalStartCalled = false;
	private EMoveState state = EMoveState.LEFT;
	private int numberOfTurns = 0;
	
	private IVector2 moveDestination = null;
	
	
	/**
	 * Default
	 */
	public KickoffShooterRole()
	{
		super(ERole.KICKOFF_SHOOTER);
		numberOfTurns = 0;
		
		moveDestination = Geometry.getCenter().subtractNew(Vector2.fromXY(300, 0));
		
		IState state1 = new PrepareState();
		setInitialState(state1);
		addTransition(state1, EEvent.READY, new MoveState());
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		MOVE
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
	
	private class PrepareState implements IState
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
			if ((getAiFrame().getRefereeMsg() != null)
					&& (getAiFrame().getRefereeMsg().getCommand() == Command.NORMAL_START))
			{
				normalStartCalled = true;
			}
			
			if (normalStartCalled && (VectorMath.distancePP(getPos(), skill.getMoveCon().getDestination()) < 100)
					&& (getBot().getVel().getLength2() < 0.2))
			{
				triggerEvent(EEvent.READY);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			// Empty because there is for now no need for this
		}
		
		
		@Override
		public String getIdentifier()
		{
			return EStateId.PREPARE.name();
		}
	}
	
	private class MoveState implements IState
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
				
				AKickSkill kickSkill;
				
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
		
		
		@Override
		public void doExitActions()
		{
			// Empty because there is for now no need for this
		}
		
		
		@Override
		public String getIdentifier()
		{
			return EStateId.MOVE.name();
		}
		
		
		private IVector2 findKickOffTarget()
		{
			ValuePoint target = getAiFrame().getTacticalField().getBestDirectShotTarget();
			if (target.getValue() < minimumScoreForDirectKick)
			{
				return null;
			}
			return target;
		}
		
		private KickNormalSkill prepareDirectKick(final IVector2 target)
		{
			EKickMode mode = EKickMode.MAX;
			EKickerDevice device = EKickerDevice.STRAIGHT;
			
			KickNormalSkill kickSkill = new KickNormalSkill(new DynamicPosition(target));
			kickSkill.setKickMode(mode);
			kickSkill.setDevice(device);
			
			return kickSkill;
		}
		
		
		private AKickSkill prepareIndirectKick()
		{
			IVector2 target = getAiFrame().getTacticalField().getKickoffStrategy().getBestShotTarget();
			
			if (target == null)
			{
				target = Geometry.getGoalTheir().getCenter();
			}
			
			EKickMode mode = getKickMode();
			
			AKickSkill kickSkill = new KickChillSkill(new DynamicPosition(target));
			kickSkill.setKickMode(mode);
			
			return kickSkill;
		}
		
		
		private EKickMode getKickMode()
		{
			return EKickMode.PASS;
		}
		
	}
	
}
