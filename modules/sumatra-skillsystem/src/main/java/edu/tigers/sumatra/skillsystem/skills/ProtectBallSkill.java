/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import lombok.Setter;


/**
 * Protect the ball against a given opponent
 */
public class ProtectBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Distance to keep to the ball during protection", defValue = "10.0")
	private static double finalDist2BallDefault = 10;

	private final PositionValidator positionValidator = new PositionValidator();

	@Setter
	private DynamicPosition protectionTarget;
	@Setter
	private double finalDist2Ball = finalDist2BallDefault;
	@Setter
	private double marginToTheirPenArea = 0;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		protectionTarget = protectionTarget.update(getWorldFrame());

		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.OFF));

		final IVector2 finalDestination = getFinalDestination(protectionTarget);
		IVector2 dest = finalDestination;
		double dist2Ball;

		AroundObstacleCalc aroundObstacleCalc = new AroundObstacleCalc(protectionTarget.getPos(), getBallPos(),
				getTBot());
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
			dist2Ball = finalDist2Ball;
		}

		dest = aroundBall(dest, dist2Ball);
		double targetOrientation = getTargetOrientation(dest);
		if (aroundObstacleCalc.isAroundObstacleNeeded(finalDestination))
		{
			targetOrientation = aroundObstacleCalc.adaptTargetOrientation(targetOrientation);
		}

		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
		dest = positionValidator.movePosInsideField(dest);
		dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);

		updateDestination(dest);
		updateTargetAngle(targetOrientation);
		getMoveCon().setBotsObstacle(
				getBall().getPos().distanceTo(getTBot().getBotKickerPos()) - Geometry.getBotRadius() > 50);

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
		return LineMath.stepAlongLine(getBallPos(), curProtectTarget.getPos(), getDistance());
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
