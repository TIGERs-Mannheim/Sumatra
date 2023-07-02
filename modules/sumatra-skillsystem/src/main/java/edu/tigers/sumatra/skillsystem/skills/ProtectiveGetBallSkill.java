/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;

import java.awt.Color;


/**
 * Protect the ball against a given opponent
 */
public class ProtectiveGetBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Distance to keep to the ball during protection", defValue = "70.0")
	private static double approachBallDistDefault = 70;

	@Configurable(comment = "angle in deg", defValue = "12.0")
	private static double finalOrientationThreshold = 12.0;

	@Configurable(defValue = "1.2")
	private static double ballContactAccMax = 1.2;

	@Configurable(defValue = "5.0")
	private static double ballContactAccMaxW = 5.0;

	@Configurable(defValue = "5.0")
	private static double velMaxW = 5.0;

	private final PositionValidator positionValidator = new PositionValidator();
	@Setter
	IVector2 protectionTarget;
	@Setter
	private double approachDistToBall = approachBallDistDefault;
	@Setter
	private boolean strongDribblerContactNeeded = false;
	private IVector2 contactPos = null;
	private double contactAngle = 0;

	private TimestampTimer successTimer = new TimestampTimer(0.2);


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		setKickParams(KickParams.disarm().withDribblerMode(calcDribbleMode()));
		getMoveCon().setBotsObstacle(
				getBall().getPos().distanceTo(getTBot().getBotKickerPos()) - Geometry.getBotRadius() > 50);

		final IVector2 finalDestination = getFinalDestination(protectionTarget);
		IVector2 dest = finalDestination;
		double dist2Ball;

		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL).add(new DrawableCircle(Circle.createCircle(finalDestination, 30),
				Color.BLACK).setFill(true));

		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL).add(new DrawableCircle(Circle.createCircle(protectionTarget, 30),
				Color.RED).setFill(true));

		double finalPositioningAngle = AngleMath.diffAbs(getBallPos().subtractNew(protectionTarget).getAngle(),
				getTBot().getOrientation());

		var aroundObstacleCalc = new AroundObstacleCalc(protectionTarget, getBallPos(), getTBot());
		if (aroundObstacleCalc.isAroundObstacleNeeded(finalDestination))
		{
			dist2Ball = 150.0;
		} else
		{
			if (finalPositioningAngle < AngleMath.deg2rad(finalOrientationThreshold))
			{
				dist2Ball = -20.0;
			} else
			{
				dist2Ball = approachDistToBall;
			}
		}

		calcSkillState();

		double targetOrientation;
		if (getTBot().getBallContact().hadContact(0.3))
		{
			getMoveConstraints().setAccMax(ballContactAccMax);
			getMoveConstraints().setAccMaxW(ballContactAccMaxW);
			if (contactPos == null)
			{
				contactPos = getPos();
				contactAngle = getAngle();
			}
			dest = contactPos;
			targetOrientation = contactAngle;
		} else
		{
			getMoveConstraints().resetLimits(getBot().getBotParams().getMovementLimits());
			contactPos = null;
			dest = aroundBall(dest, dist2Ball);
			targetOrientation = getTargetOrientation(dest);
		}
		getMoveConstraints().setVelMaxW(velMaxW);

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

		super.doUpdate();
	}


	private void calcSkillState()
	{
		if (getTBot().getRobotInfo().isBarrierInterrupted())
		{
			if (!successTimer.isRunning())
			{
				successTimer.start(getWorldFrame().getTimestamp());
			}
			if (successTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				if (strongDribblerContactNeeded
						&& getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
				{
					setSkillState(ESkillState.SUCCESS);
				} else if (!strongDribblerContactNeeded)
				{
					setSkillState(ESkillState.SUCCESS);
				} else {
					setSkillState(ESkillState.IN_PROGRESS);
				}
			} else
			{
				setSkillState(ESkillState.IN_PROGRESS);
			}
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
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
				.withMaxMargin(150)
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


	private EDribblerMode calcDribbleMode()
	{
		if (getPos().distanceTo(getBall().getPos()) < 300)
		{
			if (strongDribblerContactNeeded)
			{
				return EDribblerMode.HIGH_POWER;
			} else
			{
				return EDribblerMode.DEFAULT;
			}
		}
		return EDribblerMode.OFF;
	}


	private IVector2 getBallPos()
	{
		return getBall().getPos();
	}
}

