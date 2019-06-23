/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;


public class ReceiveBallSkill extends AMoveSkill
{
	@Configurable(comment = "Distance bot pos to ball [mm] to fix the target orientation of the bot.")
	private static double distThresholdToFixOrientation = 200;
	
	@Configurable(comment = "Dribble speed during receive", defValue = "3000.0")
	private static double dribbleSpeed = 3000;
	
	@Configurable(defValue = "1000.0")
	private static double maxDistanceToReceivingPosition = 1000.0;
	
	private final IVector2 receivingPosition;
	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.3, 0.6);
	private final BallStabilizer ballStabilizer = new BallStabilizer();
	private final PositionValidator positionValidator = new PositionValidator();
	
	
	public ReceiveBallSkill(final IVector2 receivingPosition)
	{
		super(ESkill.RECEIVE_BALL);
		this.receivingPosition = receivingPosition;
		
		// initially, the ball is moving
		ballSpeedHysteresis.update(RuleConstraints.getMaxBallSpeed());
		
		setInitialState(new ReceiveState());
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		if (getTBot().hasBallContact())
		{
			kickerDribblerOutput.setDribblerSpeed(0);
		} else
		{
			kickerDribblerOutput.setDribblerSpeed(dribbleSpeed);
		}
	}
	
	
	public boolean ballCanBeReceived()
	{
		return !isInitialized() || (ballIsMoving() && receivingPositionIsReachable() && ballIsMovingTowardsMe());
	}
	
	
	public boolean ballHasBeenReceived()
	{
		return isInitialized() && !ballIsMoving() && ballIsNearKicker();
	}
	
	
	private boolean receivingPositionIsReachable()
	{
		return getBall().getTrajectory().getTravelLineRolling()
				.distanceTo(receivingPosition) < maxDistanceToReceivingPosition;
	}
	
	
	private boolean ballIsMoving()
	{
		return ballSpeedHysteresis.isUpper() && !getTBot().hasBallContact();
	}
	
	
	private boolean ballIsNearKicker()
	{
		return ballStabilizer.getBallPos().distanceTo(getTBot().getBotKickerPos()) < 100;
	}
	
	
	private boolean ballIsMovingTowardsMe()
	{
		return getPos().subtractNew(ballStabilizer.getBallPos()).angleToAbs(getBall().getVel())
				.map(a -> a < AngleMath.PI_HALF).orElse(false);
	}
	
	private class ReceiveState extends MoveToState
	{
		private IVector2 currentReceivingPosition;
		private double currentTargetAngle;
		
		
		private ReceiveState()
		{
			super(ReceiveBallSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
			
			// init target orientation
			currentTargetAngle = getBall().getPos().subtractNew(getPos()).getAngle();
			moveToReceivingPosition();
		}
		
		
		@Override
		public void doUpdate()
		{
			updateState();
			
			if (ballIsMoving())
			{
				moveToNearestPointOnBallLine();
			} else if (!ballIsNearKicker())
			{
				moveToReceivingPosition();
			}
			
			writeTargetPoseToMoveCon();
			
			drawShapes();
			
			super.doUpdate();
		}
		
		
		private void moveToReceivingPosition()
		{
			currentReceivingPosition = receivingPosition;
		}
		
		
		private void moveToNearestPointOnBallLine()
		{
			if (receivingPositionIsReachable())
			{
				IVector2 dest = getBall().getTrajectory().getTravelLine().leadPointOf(receivingPosition);
				dest = positionValidator.movePosInFrontOfOpponent(dest);
				dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
				dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
				currentReceivingPosition = dest;
			}
		}
		
		
		private void updateState()
		{
			ballSpeedHysteresis.update(getBall().getVel().getLength2());
			ballStabilizer.update(getBall(), getTBot());
			positionValidator.update(getWorldFrame(), getMoveCon(), getTBot());
		}
		
		
		private void drawShapes()
		{
			getShapes().get(ESkillShapesLayer.RECEIVE_BALL_SKILL)
					.add(new DrawablePoint(ballStabilizer.getBallPos(), Color.green));
			getShapes().get(ESkillShapesLayer.RECEIVE_BALL_SKILL)
					.add(new DrawableAnnotation(getPos(), ballIsMoving() ? "ballMoving" : "ballNotMoving",
							Vector2.fromX(100)));
		}
		
		
		private void writeTargetPoseToMoveCon()
		{
			currentTargetAngle = calcTargetAngle(currentReceivingPosition);
			IVector2 dest = BotShape.getCenterFromKickerPos(currentReceivingPosition, currentTargetAngle,
					getTBot().getCenter2DribblerDist());
			
			getMoveCon().updateDestination(dest);
			getMoveCon().updateTargetAngle(currentTargetAngle);
			if (getBall().getVel().getLength2() > 0.2)
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(getBall().getVel());
			} else
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(Vector2f.ZERO_VECTOR);
			}
		}
		
		
		private double calcTargetAngle(final IVector2 kickerPos)
		{
			IVector2 ballPos = ballStabilizer.getBallPos();
			double distBallBot = ballPos.distanceTo(kickerPos);
			if (distBallBot < distThresholdToFixOrientation)
			{
				// just keep last position -> this is probably most safe to not push ball away again
				return currentTargetAngle;
			}
			
			IVector2 kickerToBall = ballPos.subtractNew(kickerPos);
			if (kickerToBall.getLength2() > 50)
			{
				return kickerToBall.getAngle();
			}
			return ballPos.subtractNew(getPos()).getAngle(0);
		}
	}
}
