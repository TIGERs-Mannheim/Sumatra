/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * MoveTo skill that moves to a destination using path planning.
 */
public final class MoveToSkill extends AMoveToSkill
{
	@Configurable(defValue = "5.0", comment = "Position tolerance for skill state")
	private static double positionTolerance = 5;

	@Configurable(defValue = "0.1", comment = "Orientation tolerance for skill state")
	private static double orientationTolerance = 0.1;


	/**
	 * Create a new moveTo skill. For historical reasons, this is a static constructor.
	 * The default constructor can also be used instead.
	 *
	 * @return new instance
	 */
	public static MoveToSkill createMoveToSkill()
	{
		return new MoveToSkill();
	}


	@Override
	public void doUpdate()
	{
		super.doUpdate();
		updateSkillState();
	}


	private void updateSkillState()
	{
		if (isInitialized()
				&& getDestination().distanceTo(getPos()) < positionTolerance
				&& AngleMath.diffAbs(getTargetAngle(), getAngle()) < orientationTolerance)
		{
			setSkillState(ESkillState.SUCCESS);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}
	}


	@Override
	public void updateDestination(final IVector2 destination)
	{
		super.updateDestination(destination);
		updateSkillState();
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


	public void setVelMax(final Double maxVel)
	{
		getMoveConstraints().setVelMax(maxVel);
	}


	public void setAccMax(final Double maxAcc)
	{
		getMoveConstraints().setAccMax(maxAcc);
	}


	@Override
	public void setKickParams(final KickParams kickParams)
	{
		// allow setting kick params from outside directly
		super.setKickParams(kickParams);
	}
}
