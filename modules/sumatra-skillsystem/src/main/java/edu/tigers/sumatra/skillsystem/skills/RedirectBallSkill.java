/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.redirect.IRedirectConsultant;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.Color;


@NoArgsConstructor
public class RedirectBallSkill extends ABallArrivalSkill
{
	private final TimestampTimer redirectDoneTimer = new TimestampTimer(0.1);
	private final IRedirectConsultant redirectConsultant = RedirectConsultantFactory.createDefault();

	@Setter
	private IVector2 target;
	@Setter
	private KickParams desiredKickParams = KickParams.disarm();


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


	@Override
	public void doUpdate()
	{
		var desiredKickSpeed = redirectConsultant.getKickSpeed(
				getBall(),
				getTBot().getBotKickerPos(Geometry.getBallRadius()),
				target,
				desiredKickParams.getKickSpeed()
		);

		var desiredTargetAngle = redirectConsultant.getTargetAngle(
				getBall(),
				getTBot().getBotKickerPos(Geometry.getBallRadius()),
				target,
				desiredKickParams.getKickSpeed()
		);

		if (isRoughlyTargeted(desiredTargetAngle))
		{
			setKickParams(KickParams.of(desiredKickParams.getDevice(), desiredKickSpeed));
		} else
		{
			setKickParams(KickParams.disarm());
		}
		setDesiredTargetAngle(desiredTargetAngle);

		var bot2Target = Lines.lineFromPoints(getTBot().getPos(), target).getAngle().orElse(0.0);
		var turnDiff = AngleMath.diffAbs(desiredTargetAngle, getAngle());
		var offsetDiff = AngleMath.diffAbs(desiredTargetAngle, bot2Target);
		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawableAnnotation(getPos(),
						String.format("turnDiff: %.3f, offsetDiff: %.3f", turnDiff, offsetDiff))
						.withOffset(Vector2.fromY(150)));

		// bot to target
		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawableLine(getTBot().getBotKickerPos(Geometry.getBallRadius()), target,
						getBotId().getTeamColor().getColor()));

		// current angle
		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawableLine(Lines.segmentFromOffset(getPos(), Vector2.fromAngle(getAngle()).scaleTo(200)),
						Color.black));

		super.doUpdate();
		setSkillState(calcSkillState());
	}


	private boolean isRoughlyTargeted(double desiredTargetAngle)
	{
		return AngleMath.diffAbs(desiredTargetAngle, getTBot().getOrientation()) < AngleMath.deg2rad(20);
	}


	private ESkillState calcSkillState()
	{
		if (!ballIsMoving())
		{
			return ESkillState.FAILURE;
		}

		if (!getBall().getTrajectory().getTravelLine().isPointInFront(getPos()))
		{
			return ESkillState.SUCCESS;
		}

		return ESkillState.IN_PROGRESS;
	}


	/**
	 * Setter for instanceables. Setting desiredKickParams directly should be preferred.
	 *
	 * @param kickerDevice
	 */
	public void setKickerDevice(EKickerDevice kickerDevice)
	{
		this.desiredKickParams = KickParams.of(kickerDevice, desiredKickParams.getKickSpeed())
				.withDribblerMode(desiredKickParams.getDribblerMode());
	}


	/**
	 * Setter for instanceables. Setting desiredKickParams directly should be preferred.
	 *
	 * @param kickSpeed
	 */
	public void setKickSpeed(double kickSpeed)
	{
		this.desiredKickParams = KickParams.of(desiredKickParams.getDevice(), kickSpeed)
				.withDribblerMode(desiredKickParams.getDribblerMode());
	}
}
