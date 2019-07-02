/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.redirect.ARedirectConsultant;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class RedirectBallSkill extends ABallArrivalSkill
{
	@Configurable(defValue = "100.0", comment = "Margin between penaltyarea and bot destination [mm]")
	private static double marginBetweenDestAndPenArea = 100.0;

	private final DynamicPosition target;
	private final KickParams kickParams;

	private final TimestampTimer redirectDoneTimer = new TimestampTimer(0.1);

	private ARedirectConsultant redirectConsultant;


	@SuppressWarnings("unused") // used by UI
	public RedirectBallSkill(
			final DynamicPosition receivingPosition,
			final DynamicPosition target,
			final EKickerDevice device,
			final double kickSpeed)
	{
		this(receivingPosition, target, KickParams.of(device, kickSpeed));
	}


	public RedirectBallSkill(
			final IVector2 receivingPosition,
			final DynamicPosition target,
			final KickParams kickParams)
	{
		this(new DynamicPosition(receivingPosition), target, kickParams);
	}


	private RedirectBallSkill(
			final DynamicPosition receivingPosition,
			final DynamicPosition target,
			final KickParams kickParams)
	{
		super(ESkill.REDIRECT_BALL, receivingPosition);
		this.target = target;
		this.kickParams = kickParams;

		setInitialState(new RedirectState());
	}


	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);

		kickerDribblerOutput.setKick(calcKickSpeed(), kickParams.getDevice(), EKickerMode.ARM);
	}


	public boolean ballCanBeRedirected()
	{
		return !isInitialized() || (ballIsMoving()
				&& receivingPositionIsReachableByBall(receivingPosition.getPos())
				&& ballIsMovingTowardsMe());
	}


	@Override
	protected boolean ballIsMovingTowardsMe()
	{
		boolean directionTowardsMe = super.ballIsMovingTowardsMe();

		if (directionTowardsMe)
		{
			redirectDoneTimer.reset();
		} else
		{
			redirectDoneTimer.update(getWorldFrame().getTimestamp());
		}
		return !redirectDoneTimer.isTimeUp(getWorldFrame().getTimestamp());
	}


	private void updateConsultant()
	{
		IVector2 desiredBallDir = target.getPos().subtractNew(getTBot().getBotKickerPos());
		IVector2 desiredBallVel = desiredBallDir.scaleToNew(kickParams.getKickSpeed());
		redirectConsultant = RedirectConsultantFactory.createDefault(calcBallVelAtCollision(), desiredBallVel);
	}


	private IVector2 calcBallVelAtCollision()
	{
		final double timeByPos = getBall().getTrajectory().getTimeByPos(getTBot().getBotKickerPos());
		final IVector2 ballVelAtCollision = getBall().getTrajectory().getVelByTime(timeByPos).getXYVector();
		if (getBall().getVel().isZeroVector() || ballVelAtCollision.isZeroVector())
		{
			// use a rough guess to have a good default target angle
			return getPos().subtractNew(getBall().getPos()).scaleTo(3.0);
		}
		return ballVelAtCollision;
	}


	private double calcKickSpeed()
	{
		double kickSpeed = redirectConsultant.getKickSpeed();
		return adaptKickSpeed(target.getPos(), kickSpeed);
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		target.update(getWorldFrame());
		updateConsultant();
	}

	private class RedirectState extends ABallArrivalState
	{
		@Override
		protected void drawShapes()
		{
			super.drawShapes();

			// bot to target
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawableLine(Line.fromPoints(getTBot().getBotKickerPos(), target.getPos()),
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

			double bot2Target = Line.fromPoints(getTBot().getBotKickerPos(), target.getPos()).getAngle().orElse(0.0);
			getShapes().get(ESkillShapesLayer.REDIRECT_BALL_SKILL)
					.add(new DrawableAnnotation(getPos(),
							String.format("angle diff: %.3f", currentTargetAngle - bot2Target))
									.withOffset(Vector2.fromY(150)));
		}


		@Override
		protected double calcMyTargetAngle(final IVector2 kickerPos)
		{
			return redirectConsultant.getTargetAngle();
		}


		@Override
		protected double getMarginBetweenDestAndPenArea()
		{
			return marginBetweenDestAndPenArea;
		}
	}
}
