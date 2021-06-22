/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


/**
 * Protect the ball against a given opponent
 */
public class ProtectiveGetBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Distance to keep to the ball during protection", defValue = "10.0")
	private static double approachBallDistDefault = 10;

	@Configurable(comment = "Dribbler speed while protecting", defValue = "5000.0")
	private static double protectDribbleSpeedDefault = 5000;

	private final PositionValidator positionValidator = new PositionValidator();
	@Setter
	IVector2 protectionTarget;
	@Setter
	private double approachDistToBall = approachBallDistDefault;
	@Setter
	private double protectDribbleSpeed = protectDribbleSpeedDefault;
	private IVector2 contactPos = null;
	private double contactAngle = 0;

	private TimestampTimer successTimer = new TimestampTimer(0.1);


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		setKickParams(KickParams.disarm().withDribbleSpeed(calcDribbleSpeed()));

		final IVector2 finalDestination = getFinalDestination(protectionTarget);
		IVector2 dest = finalDestination;
		double dist2Ball;

		double finalPositioningAngle = finalDestination.subtractNew(getBallPos())
				.angleToAbs(getTBot().getBotKickerPos().subtractNew(getBallPos())).orElse(0.0);

		var aroundObstacleCalc = new AroundObstacleCalc(protectionTarget, getBallPos(), getTBot());
		if (aroundObstacleCalc.isAroundObstacleNeeded(finalDestination))
		{
			dest = aroundObstacleCalc.getAroundObstacleDest().orElse(dest);
			if (getBallPos().distanceTo(getPos()) < Geometry.getBotRadius() + Geometry.getBallRadius())
			{
				dist2Ball = -50.0;
			} else
			{
				dist2Ball = 0.0;
			}
		} else
		{
			if (finalPositioningAngle < AngleMath.deg2rad(15))
			{
				dist2Ball = -15.0;
			} else
			{
				dist2Ball = approachDistToBall;
			}
		}

		calcSkillState(finalPositioningAngle);

		double targetOrientation;
		if (getTBot().getBallContact().hasContact())
		{
			if (contactPos == null)
			{
				contactPos = getPos();
				contactAngle = getAngle();
			}
			dest = contactPos;
			targetOrientation = contactAngle;
		} else
		{
			contactPos = null;
			dest = aroundBall(dest, dist2Ball);
			targetOrientation = getTargetOrientation(dest);
		}

		if (aroundObstacleCalc.isAroundObstacleNeeded(finalDestination))
		{
			targetOrientation = aroundObstacleCalc.adaptTargetOrientation(targetOrientation);
		}

		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, 0.0);
		dest = positionValidator.movePosInsideField(dest);
		dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);

		updateDestination(dest);
		updateTargetAngle(targetOrientation);
		getMoveCon().setBotsObstacle(
				getBall().getPos().distanceTo(getTBot().getBotKickerPos()) - Geometry.getBotRadius() > 50);

		super.doUpdate();
	}


	private void calcSkillState(double finalPositioningAngle)
	{
		if ((finalPositioningAngle < AngleMath.deg2rad(15) || successTimer.isRunning()) && getTBot().getBallContact()
				.hasContact()
		)
		{
			if (!successTimer.isRunning())
			{
				successTimer.start(getWorldFrame().getTimestamp());
			}
			if (successTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				setSkillState(ESkillState.SUCCESS);
			}
		} else
		{
			successTimer.reset();
		}
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


	private IVector2 getFinalDestination(IVector2 curProtectTarget)
	{
		return LineMath.stepAlongLine(getBallPos(), curProtectTarget, getDistance());
	}


	private double getTargetOrientation(IVector2 dest)
	{
		return getBallPos().subtractNew(dest).getAngle(0);
	}


	private double getDistance()
	{
		return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius();
	}


	private double calcDribbleSpeed()
	{
		if (getPos().distanceTo(getBall().getPos()) < 300)
		{
			return protectDribbleSpeed;
		}
		return 0;
	}


	private IVector2 getBallPos()
	{
		return getBall().getPos();
	}
}

