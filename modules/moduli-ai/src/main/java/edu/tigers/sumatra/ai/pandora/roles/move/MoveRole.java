/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Setter;


/**
 * This is a generic move role. It is a wrapper around the {@link MoveToSkill} and provides methods to set
 * the move targets.
 */
public class MoveRole extends ARole
{
	private final MoveToSkill skill;
	@Setter
	private DestinationAdjuster destinationAdjuster = (aiFrame, destination, botID) -> destination;


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


	public final void setKickParams(KickParams kickParams)
	{
		skill.setKickParams(kickParams);
	}


	/**
	 * @return of destination is reached
	 */
	public final boolean isDestinationReached()
	{
		return skill.getDestination() == null
				|| VectorMath.distancePP(getPos(), skill.getDestination()) < 70;
	}


	public final boolean isSkillStateSuccess()
	{
		return skill.getSkillState() == ESkillState.SUCCESS;
	}


	/**
	 * @param destination to set
	 */
	public void updateDestination(final IVector2 destination)
	{
		activateMotors();
		skill.updateDestination(destinationAdjuster.adjustDestination(getAiFrame(), destination, getBotID()));
	}


	/**
	 * @param angle [rad]
	 */
	public void updateTargetAngle(final double angle)
	{
		activateMotors();
		skill.updateTargetAngle(angle);
	}


	/**
	 * Updates the angle the bot should look at.
	 *
	 * @param lookAtTarget to set
	 */
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		activateMotors();
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


	public void setVelMax(final double maxVel)
	{
		skill.setVelMax(maxVel);
	}


	public void setAccMax(final double maxAcc)
	{
		skill.setAccMax(maxAcc);
	}

	public void setVelMaxW(final double maxVelW)
	{
		skill.setVelMaxW(maxVelW);
	}


	public void setAccMaxW(final double maxAccW)
	{
		skill.setAccMaxW(maxAccW);
	}


	public void disableMotors()
	{
		if (getCurrentSkill().getClass() != IdleSkill.class)
		{
			setNewSkill(new IdleSkill());
		}
	}


	private void activateMotors()
	{
		if (getCurrentSkill() != skill)
		{
			setNewSkill(skill);
		}
	}
}
