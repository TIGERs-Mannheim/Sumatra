/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.redirect.ARedirectConsultant;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class RedirectBallSkill extends AMoveSkill
{
	@Configurable(comment = "Distance bot pos to ball [mm] to fix the target orientation of the bot.")
	private static double distThresholdToFixOrientation = 200;
	
	@Configurable(defValue = "1000.0")
	private static double maxDistanceToReceivingPosition = 1000.0;
	
	private final IVector2 receivingPosition;
	private final DynamicPosition target;
	private final KickParams kickParams;
	
	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.4, 0.6);
	private final BallStabilizer ballStabilizer = new BallStabilizer();
	private final PositionValidator positionValidator = new PositionValidator();
	
	private ARedirectConsultant consultant;
	
	private final TimestampTimer redirectDoneTimer = new TimestampTimer(0.1);
	
	
	/**
	 * UI constructor
	 * 
	 * @param receivingPosition
	 * @param target
	 * @param device
	 * @param kickSpeed
	 */
	@SuppressWarnings("unused") // used by UI
	public RedirectBallSkill(
			final IVector2 receivingPosition,
			final DynamicPosition target,
			final EKickerDevice device,
			final double kickSpeed)
	{
		this(receivingPosition, target, KickParams.of(device, kickSpeed));
	}
	
	
	public RedirectBallSkill(final IVector2 receivingPosition,
			final DynamicPosition target,
			final KickParams kickParams)
	{
		super(ESkill.REDIRECT_BALL);
		this.receivingPosition = receivingPosition;
		this.target = target;
		this.kickParams = kickParams;
		
		// initially, the ball is moving
		ballSpeedHysteresis.update(RuleConstraints.getMaxBallSpeed());
		
		setInitialState(new RedirectState());
	}
	
	
	public boolean ballCanBeRedirected()
	{
		return !isInitialized() || (ballIsMoving() && receivingPositionIsReachable() && ballIsMovingTowardsMe());
	}
	
	
	private boolean receivingPositionIsReachable()
	{
		return getBall().getTrajectory().getTravelLineRolling()
				.distanceTo(receivingPosition) < maxDistanceToReceivingPosition;
	}
	
	
	private boolean ballIsMovingTowardsMe()
	{
		
		boolean directionTowardsMe = getPos().subtractNew(ballStabilizer.getBallPos()).angleToAbs(getBall().getVel())
				.map(a -> a < AngleMath.PI_HALF).orElse(false);
		
		if (directionTowardsMe)
		{
			redirectDoneTimer.reset();
		} else
		{
			redirectDoneTimer.update(getWorldFrame().getTimestamp());
		}
		return !redirectDoneTimer.isTimeUp(getWorldFrame().getTimestamp());
	}
	
	
	private boolean ballIsMoving()
	{
		return ballSpeedHysteresis.isUpper();
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		
		kickerDribblerOutput.setKick(calcKickSpeed(), EKickerDevice.STRAIGHT, EKickerMode.ARM);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		ballSpeedHysteresis.update(getBall().getVel().getLength2());
		ballStabilizer.update(getBall(), getTBot());
		positionValidator.update(getWorldFrame(), getMoveCon(), getTBot());
		updateConsultant();
	}
	
	
	private void updateConsultant()
	{
		IVector2 ballVelAtCollision = getBall().getTrajectory()
				.getVelByTime(getBall().getTrajectory().getTimeByPos(getTBot().getBotKickerPos())).getXYVector();
		if (ballVelAtCollision.isZeroVector() || getBall().getVel().isZeroVector())
		{
			ballVelAtCollision = getPos().subtractNew(getBall().getPos()).scaleTo(3.0);
		}
		
		IVector2 desiredBallDir = target.subtractNew(getTBot().getBotKickerPos());
		IVector2 desiredBallVel = desiredBallDir.scaleToNew(kickParams.getKickSpeed());
		consultant = RedirectConsultantFactory.createDefault(ballVelAtCollision, desiredBallVel);
	}
	
	
	private double calcKickSpeed()
	{
		double kickSpeed;
		if (consultant != null)
		{
			kickSpeed = consultant.getKickSpeed();
		} else
		{
			// Just for redirect preparation
			kickSpeed = 0;
		}
		
		return KickParams.limitKickSpeed(kickSpeed);
	}
	
	private class RedirectState extends MoveToState
	{
		private IVector2 currentRedirectPosition;
		private double currentTargetAngle;
		
		
		private RedirectState()
		{
			super(RedirectBallSkill.this);
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
			if (ballIsMoving())
			{
				moveToNearestPointOnBallLine();
			} else
			{
				moveToReceivingPosition();
			}
			
			writeTargetPoseToMoveCon();
			
			drawShapes();
			
			super.doUpdate();
		}
		
		
		private void moveToReceivingPosition()
		{
			currentRedirectPosition = receivingPosition;
		}
		
		
		private void moveToNearestPointOnBallLine()
		{
			IVector2 dest = getBall().getTrajectory().getTravelLineRolling().closestPointOnLine(receivingPosition);
			boolean ballMovingTowardsBot = getBall().getTrajectory().getTravelLine().isPointInFront(receivingPosition);
			if (currentRedirectPosition == null
					|| (ballMovingTowardsBot && dest.distanceTo(receivingPosition) < maxDistanceToReceivingPosition))
			{
				dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
				dest = positionValidator.movePosInFrontOfOpponent(dest);
				dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
				currentRedirectPosition = dest;
			}
		}
		
		
		private void drawShapes()
		{
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawablePoint(ballStabilizer.getBallPos(), Color.green));
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawableAnnotation(getPos(), ballIsMoving() ? "ballMoving" : "ballNotMoving",
							Vector2.fromY(100)));
			
			
			// bot to target
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawableLine(Line.fromPoints(getTBot().getBotKickerPos(), target),
							getBotId().getTeamColor().getColor()));
			
			// current angle
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawableLine(Line.fromDirection(getPos(), Vector2.fromAngle(getAngle()).scaleTo(200)),
							Color.black));
			
			// desired angle
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawableLine(
							Line.fromDirection(getPos(), Vector2.fromAngle(currentTargetAngle).scaleTo(200)),
							Color.magenta));
			
			double bot2Target = Line.fromPoints(getTBot().getBotKickerPos(), target).getAngle().orElse(0.0);
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawableAnnotation(getPos(),
							String.format("angle diff: %.3f", currentTargetAngle - bot2Target))
									.withOffset(Vector2.fromY(150)));
		}
		
		
		private void writeTargetPoseToMoveCon()
		{
			currentTargetAngle = calcTargetAngle(currentRedirectPosition);
			getMoveCon().updateTargetAngle(currentTargetAngle);
			
			IVector2 dest = BotShape.getCenterFromKickerPos(currentRedirectPosition, currentTargetAngle,
					getTBot().getCenter2DribblerDist() + Geometry.getBallRadius());
			getMoveCon().updateDestination(dest);
			
			getMoveCon().getMoveConstraints().setPrimaryDirection(getBall().getVel());
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
			
			return consultant.getTargetAngle();
		}
	}
}
