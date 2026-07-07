package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * MoveTo skill that moves to a destination using path planning.
 */
public final class MoveToSkill extends AMoveToSkill
{
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
	public void updateDestination(final IVector2 destination)
	{
		super.updateDestination(destination);
	}


	@Override
	public void updateTargetAngle(final Double angle)
	{
		super.updateTargetAngle(angle);
	}


	@Override
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		super.updateLookAtTarget(lookAtTarget);
	}


	@Override
	public void updateLookAtTarget(final ITrackedObject object)
	{
		super.updateLookAtTarget(object);
	}


	@Override
	public void updateLookAtTarget(final IVector2 lookAtTarget)
	{
		super.updateLookAtTarget(lookAtTarget);
	}


	@Override
	public void updateTargetTime(final Double targetTime)
	{
		super.updateTargetTime(targetTime);
	}


	@Override
	public void updateUseComeToAStopIfFaster(boolean useComeToAStopIfFaster)
	{
		super.updateUseComeToAStopIfFaster(useComeToAStopIfFaster);
	}


	public void setVelMax(final Double maxVel)
	{
		if (maxVel != null)
		{
			setMoveConstraints(getMoveConstraints().withVelMax(maxVel));
		}
	}


	public void setAccMax(final Double maxAcc)
	{
		if (maxAcc != null)
		{
			setMoveConstraints(getMoveConstraints().withAccMax(maxAcc));
		}
	}


	public void setVelMaxW(final Double maxVelW)
	{
		if (maxVelW != null)
		{
			setMoveConstraints(getMoveConstraints().withVelMaxW(maxVelW));
		}
	}


	public void setAccMaxW(final Double maxAccW)
	{
		if (maxAccW != null)
		{
			setMoveConstraints(getMoveConstraints().withAccMaxW(maxAccW));
		}
	}


	@Override
	public void setKickParams(final KickParams kickParams)
	{
		super.setKickParams(kickParams);
	}
}
