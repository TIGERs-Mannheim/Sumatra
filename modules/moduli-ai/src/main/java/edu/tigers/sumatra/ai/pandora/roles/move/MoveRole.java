/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * This is a generic move role. It is a wrapper around the {@link MoveToSkill} and provides methods to set
 * the move targets.
 */
public class MoveRole extends ARole
{
	private final MoveToSkill skill;


	/**
	 * Create a simple move role.
	 */
	public MoveRole()
	{
		super(ERole.MOVE);
		setInitialState(new AState());
		skill = MoveToSkill.createMoveToSkill();
		setNewSkill(skill);
	}


	/**
	 * @return the moveCon of the underlying skill
	 */
	public final MovementCon getMoveCon()
	{
		return skill.getMoveCon();
	}


	/**
	 * @return of destination is reached
	 */
	public final boolean isDestinationReached()
	{
		return skill.getDestination() == null
				|| VectorMath.distancePP(getPos(), skill.getDestination()) < 70;
	}


	/**
	 * @param destination to set
	 */
	public void updateDestination(final IVector2 destination)
	{
		skill.updateDestination(destination);
	}


	/**
	 * @param angle [rad]
	 */
	public void updateTargetAngle(final double angle)
	{
		skill.updateTargetAngle(angle);
	}


	/**
	 * Updates the angle the bot should look at.
	 *
	 * @param lookAtTarget to set
	 */
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		skill.updateLookAtTarget(lookAtTarget);
	}


	/**
	 * @param object to set
	 */
	public void updateLookAtTarget(final ITrackedObject object)
	{
		updateLookAtTarget(new DynamicPosition(object));
	}


	/**
	 * Updates the angle the bot should look at.
	 *
	 * @param lookAtTarget to set
	 */
	public void updateLookAtTarget(final IVector2 lookAtTarget)
	{
		updateLookAtTarget(new DynamicPosition(lookAtTarget));
	}


	public final IVector2 getDestination()
	{
		return skill.getDestination();
	}
}
