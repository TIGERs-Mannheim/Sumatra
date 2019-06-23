/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.Collections;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.CircleObstacle;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Protect the ball against a given opponent
 */
public class ProtectBallSkill extends AMoveSkill
{
	@Configurable(comment = "Distance to keep to the ball during protection")
	private static double finalDist2Ball = 10;
	
	private final PositionValidator positionValidator = new PositionValidator();
	private final DynamicPosition protectionTarget;
	
	
	/**
	 * @param protectionTarget
	 */
	public ProtectBallSkill(DynamicPosition protectionTarget)
	{
		super(ESkill.PROTECT_BALL);
		this.protectionTarget = protectionTarget;
		this.protectionTarget.setUseKickerPos(false);
		this.protectionTarget.setLookahead(0.1);
		IState protectBallState = new ProtectBallState();
		setInitialState(protectBallState);
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		if (protectionTarget.distanceTo(getBall().getPos()) < Geometry.getBotRadius())
		{
			kickerDribblerOutput.setKick(1.5, EKickerDevice.STRAIGHT, EKickerMode.ARM);
			getMoveCon().setDribblerSpeed(0);
		} else
		{
			kickerDribblerOutput.setKick(0, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
			getMoveCon().setDribblerSpeed(getTBot().hasBallContact() ? 10000 : 0);
		}
	}
	
	private class ProtectBallState extends MoveToState
	{
		protected ProtectBallState()
		{
			super(ProtectBallSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
			getMoveCon().setBotsObstacle(false);
		}
		
		
		@Override
		public void doUpdate()
		{
			protectionTarget.update(getWorldFrame());
			
			final IVector2 finalDestination = getFinalDestination(protectionTarget);
			IVector2 dest = finalDestination;
			double dist2Ball;
			
			AroundObstacleCalc aroundObstacleCalc = new AroundObstacleCalc(protectionTarget, getBallPos(), getTBot());
			if (aroundObstacleCalc.isAroundObstacleNeeded(finalDestination))
			{
				dest = aroundObstacleCalc.getAroundObstacleDest().orElse(dest);
				dist2Ball = -50;
			} else
			{
				dist2Ball = finalDist2Ball;
			}
			
			dest = aroundBall(dest, dist2Ball);
			double targetOrientation = getTargetOrientation(dest);
			if (aroundObstacleCalc.isAroundObstacleNeeded(finalDestination))
			{
				targetOrientation = aroundObstacleCalc.adaptTargetOrientation(targetOrientation);
			}
			
			positionValidator.update(getWorldFrame(), getMoveCon(), getTBot());
			dest = positionValidator.movePosInsideField(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			
			getMoveCon().updateDestination(dest);
			getMoveCon().updateTargetAngle(targetOrientation);
			getMoveCon().setCustomObstacles(Collections
					.singletonList(new CircleObstacle(Circle.createCircle(protectionTarget, Geometry.getBotRadius() * 2))));
			
			super.doUpdate();
		}
		
		
		private IVector2 aroundBall(final IVector2 destination, final double dist2Ball)
		{
			return AroundBallCalc
					.aroundBall()
					.withBallPos(getBall().getTrajectory().getPosByTime(0.05).getXYVector())
					.withTBot(getTBot())
					.withDestination(destination)
					.withMaxMargin(50)
					.withMinMargin(dist2Ball)
					.build()
					.getAroundBallDest();
		}
		
		
		private IVector2 getFinalDestination(DynamicPosition curProtectTarget)
		{
			return LineMath.stepAlongLine(getBallPos(), curProtectTarget, getDistance());
		}
		
		
		private double getDistance()
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + finalDist2Ball;
		}
		
		
		private IVector2 getBallPos()
		{
			return getBall().getPos();
		}
		
		
		private double getTargetOrientation(IVector2 dest)
		{
			return getBallPos().subtractNew(dest).getAngle(0);
		}
	}
}
