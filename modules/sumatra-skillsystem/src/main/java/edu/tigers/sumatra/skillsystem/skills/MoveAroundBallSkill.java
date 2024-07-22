/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Setter;


public class MoveAroundBallSkill extends AMoveToSkill
{
	@Configurable(defValue = "10.0", comment = "Position tolerance for skill state")
	private static double positionTolerance = 10;

	@Configurable(defValue = "0.1", comment = "Orientation tolerance for skill state")
	private static double orientationTolerance = 0.1;

	private final PositionValidator positionValidator = new PositionValidator();

	@Setter
	private double minTimeAtDestForSuccess = 0;

	private long tSuccess = 0;


	@Setter
	private double minMargin = 0;
	@Setter
	private double maxMargin = 0;

	// The ball will be between aimingPosition and the robot
	@Setter
	private IVector2 aimingPosition;


	@Override
	public void doUpdate()
	{
		positionValidator.update(getWorldFrame(), getMoveCon());

		var ballPos = getBall().getPos();
		var virtualDest = positionValidator.movePosInsideFieldWrtBallPos(
				Vector2.fromPoints(aimingPosition, ballPos).add(ballPos)
		);
		var moveDest = AroundBallCalc
				.aroundBall()
				.withBallPos(getBall().getPos())
				.withTBot(getTBot())
				.withDestination(virtualDest)
				.withMaxMargin(minMargin)
				.withMinMargin(maxMargin)
				.build()
				.getAroundBallDest();
		updateDestination(moveDest);

		super.doUpdate();
		updateSkillState();
	}


	private void updateSkillState()
	{
		if (!isInitialized()
				|| (getDestination().distanceTo(getPos()) > positionTolerance)
				|| (AngleMath.diffAbs(getTargetAngle(), getAngle()) > orientationTolerance))
		{
			tSuccess = 0;
			setSkillState(ESkillState.IN_PROGRESS);
			return;
		}

		if (tSuccess == 0)
		{
			tSuccess = getWorldFrame().getTimestamp();
		}

		if ((getWorldFrame().getTimestamp() - tSuccess) / 1e9 >= minTimeAtDestForSuccess)
		{
			setSkillState(ESkillState.SUCCESS);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}
	}


	@Override
	public void updateTargetAngle(final double angle)
	{
		super.updateTargetAngle(angle);
		updateSkillState();
	}


	@Override
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		super.updateLookAtTarget(lookAtTarget);
		updateSkillState();
	}


	@Override
	public void updateLookAtTarget(final ITrackedObject object)
	{
		super.updateLookAtTarget(object);
		updateSkillState();
	}


	@Override
	public void updateLookAtTarget(final IVector2 lookAtTarget)
	{
		super.updateLookAtTarget(lookAtTarget);
		updateSkillState();
	}


	@Override
	public IVector2 getDestination()
	{
		return super.getDestination();
	}


	@Override
	public Double getTargetAngle()
	{
		return super.getTargetAngle();
	}


}
