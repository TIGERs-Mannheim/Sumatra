/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.Optional;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.BallInterceptor;


/**
 * Temporary test skill
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallInterceptorTestSkill extends AMoveSkill
{
	/**
	 * Default constructor
	 */
	public BallInterceptorTestSkill()
	{
		super(ESkill.BALL_INTERCEPTOR_TEST);
		setInitialState(new MyState());
	}
	
	private class MyState extends MoveToState
	{
		protected MyState()
		{
			super(BallInterceptorTestSkill.this);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getVel().getLength2() > 0.1)
			{
				IVector2 dest;
				double orientation = getBall().getVel().getAngle(0) + AngleMath.PI;
				IVector2 bot2Ball = getPos().subtractNew(getBall().getPos());
				double angle = bot2Ball.angleToAbs(getBall().getVel()).orElse(0.0);
				if (angle > AngleMath.PI_HALF)
				{
					getMoveCon().setBallObstacle(true);
					
					IVector2 ballPos = getBall().getTrajectory().getPosByVel(0);
					
					dest = BotShape.getCenterFromKickerPos(ballPos, orientation, getTBot().getCenter2DribblerDist());
				} else
				{
					getMoveCon().setBallObstacle(false);
					
					Optional<Double> t = BallInterceptor.aBallInterceptor()
							.withBallTrajectory(getBall().getTrajectory())
							.withMoveConstraints(getMoveCon().getMoveConstraints())
							.withTrackedBot(getTBot())
							.build()
							.optimalTimeIfReasonable();
					
					IVector2 ballPos;
					if (t.isPresent())
					{
						ballPos = getBall().getTrajectory().getPosByTime(t.get());
					} else
					{
						ballPos = getBall().getTrajectory().getTravelLine().leadPointOf(getPos());
					}
					
					dest = BotShape.getCenterFromKickerPos(ballPos, orientation, getTBot().getCenter2DribblerDist());
				}
				
				
				getMoveCon().updateDestination(dest);
				getMoveCon().updateTargetAngle(orientation);
			}
			super.doUpdate();
		}
	}
}
