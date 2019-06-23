/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;


public class ApproachBallLineSkill extends AMoveSkill
{
	public ApproachBallLineSkill()
	{
		super(ESkill.APPROACH_BALL_LINE);
		
		setInitialState(new DefaultState());
	}
	
	
	private class DefaultState extends MoveToState
	{
		private final PositionValidator positionValidator = new PositionValidator();
		
		
		private DefaultState()
		{
			super(ApproachBallLineSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			getMoveCon().setBallObstacle(false);
			super.doEntryActions();
		}
		
		
		@Override
		public void doUpdate()
		{
			positionValidator.update(getWorldFrame(), getMoveCon(), getTBot());
			if (getBall().getVel().getLength2() > 0.1)
			{
				updatePrimaryDirection();
				
				IVector2 dest = findDestination();
				dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
				dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
				getMoveCon().updateDestination(dest);
				
				IVector2 bot2Ball = getBall().getPos().subtractNew(getPos());
				if (!bot2Ball.isZeroVector())
				{
					getMoveCon().updateTargetAngle(bot2Ball.getAngle());
				}
			}
			super.doUpdate();
		}
		
		
		private void updatePrimaryDirection()
		{
			if (ballAndRobotMoveInTheSameDirection()
					|| getBall().getTrajectory().getTravelLineRolling().distanceTo(getPos()) < 300)
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(getBall().getVel());
			} else
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(Vector2.zero());
			}
		}
		
		
		private Boolean ballAndRobotMoveInTheSameDirection()
		{
			return getBall().getVel().angleToAbs(getVel()).map(a -> a < AngleMath.PI_HALF).orElse(true);
		}
		
		
		private IVector2 findDestination()
		{
			return getBall().getTrajectory().getTravelLineRolling().closestPointOnLine(getPos());
		}
	}
}
