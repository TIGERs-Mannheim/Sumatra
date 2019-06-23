/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.throwin;

import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.PushAroundObstacleSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author MarkG
 */
public class SecondaryPlacementRole extends APlacementRole
{
	/**
	 * Default
	 */
	public SecondaryPlacementRole()
	{
		super(ERole.SECONDARY_AUTOMATED_THROW_IN);
		setInitialState(new PrepareState());
		addTransition(EEvent.RECEIVE, new ReceiveState());
		addTransition(EEvent.PUSH, new PushState());
		addTransition(EEvent.PREPARE, new PrepareState());
		addTransition(EEvent.CLEAR, new ClearState());
	}
	
	
	private enum EEvent implements IEvent
	{
		PREPARE,
		RECEIVE,
		PUSH,
		CLEAR
	}
	
	private class ClearState implements IState
	{
		private AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			prepareMoveCon(skill.getMoveCon());
			skill.getMoveCon().getMoveConstraints().setAccMax(1);
			skill.getMoveCon().getMoveConstraints().setVelMax(1);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 dest = LineMath.stepAlongLine(getBall().getPos(), getPos(),
					OffensiveConstants.getAutomatedThrowInPushDistance());
			dest = Geometry.getField().nearestPointInside(dest, Geometry.getBotRadius());
			if (isInCorner())
			{
				dest = dest.addNew(Vector2.fromX(-1000 * Math.signum(getPos().x())));
			}
			skill.getMoveCon().updateDestination(dest);
			
			if (getBall().getVel().getLength2() < 0.1 && !isBallAtTarget() && !isBallTooCloseToFieldBorder())
			{
				triggerEvent(EEvent.PREPARE);
			}
		}
		
		
		private boolean isInCorner()
		{
			return Math.abs(getPos().x()) > Geometry.getField().maxX()
					&& Math.abs(getPos().y()) > Geometry.getField().maxY();
		}
	}
	
	private class PrepareState implements IState
	{
		private AMoveSkill skill = AMoveToSkill.createMoveToSkill();
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			prepareMoveCon(skill.getMoveCon());
			skill.getMoveCon().updateDestination(getPlacementPos());
			skill.getMoveCon().updateLookAtTarget(getBall());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() < 0.1 && isBallAtTarget())
			{
				triggerEvent(EEvent.CLEAR);
			} else if (!isBallTooCloseToFieldBorder() && isInsidePushRadius()
					&& getPos().distanceTo(getBall().getPos()) < 2000)
			{
				triggerEvent(EEvent.PUSH);
			} else if (VectorMath.distancePP(getPlacementPos(), getPos()) < 100)
			{
				triggerEvent(EEvent.RECEIVE);
			}
		}
	}
	
	
	private class ReceiveState implements IState
	{
		private ReceiverSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new ReceiverSkill();
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			prepareMoveCon(skill.getMoveCon());
			IVector2 destination = Geometry.getField().nearestPointInside(getPlacementPos(), -Geometry.getBotRadius());
			skill.setReceiveDestination(destination);
			
			if (getBall().getVel().getLength2() < 0.1)
			{
				if (isBallAtTarget())
				{
					triggerEvent(EEvent.CLEAR);
				} else if (!isBallTooCloseToFieldBorder() && isInsidePushRadius()
						&& getPos().distanceTo(getBall().getPos()) < 2000)
				{
					triggerEvent(EEvent.PUSH);
				}
			}
		}
	}
	
	
	private class PushState implements IState
	{
		private long tDestReached = 0;
		
		
		@Override
		public void doEntryActions()
		{
			DynamicPosition obstacle = new DynamicPosition(Vector2.fromXY(99999, 99999));
			DynamicPosition target = new DynamicPosition(getPlacementPos());
			PushAroundObstacleSkill skill = new PushAroundObstacleSkill(obstacle, target);
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (isBallTooCloseToFieldBorder() && Geometry.getField().isPointInShape(getPlacementPos(), -300))
			{
				triggerEvent(EEvent.CLEAR);
			}
			if (isBallAtTarget())
			{
				if (tDestReached == 0)
				{
					tDestReached = getWFrame().getTimestamp();
				}
				if ((getWFrame().getTimestamp() - tDestReached) / 1e9 > 0.5)
				{
					triggerEvent(EEvent.CLEAR);
				}
			} else
			{
				tDestReached = 0;
			}
		}
	}
}
