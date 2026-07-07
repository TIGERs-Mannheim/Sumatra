package edu.tigers.sumatra.ai.pandora.roles.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.data.RobotTarget;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.RobotTargetChecker;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Getter;
import lombok.Setter;


/**
 * This is a generic move role. It is a wrapper around the {@link MoveToSkill}
 * and provides methods to set the move targets.
 */
public class MoveRole extends ARole
{
	private final RobotTargetChecker robotTargetChecker = new RobotTargetChecker();

	@Setter
	private DestinationAdjuster destinationAdjuster = (_, destination, _) -> destination;

	private boolean disableMotorsWhenDestReached;
	private boolean motorsEnabled = true;

	private RobotTarget robotTarget = new RobotTarget();
	@Getter
	@Setter
	private MovementCon moveCon = MovementCon.DEFAULT;
	@Setter
	private KickParams kickParams;
	@Setter
	private Double velMax;
	@Setter
	private Double accMax;
	@Setter
	private Double velMaxW;
	@Setter
	private Double accMaxW;


	public MoveRole()
	{
		super(ERole.MOVE);
		var moveState = new DefaultState();
		var idleState = new RoleState<>(IdleSkill::new);
		setInitialState(moveState);
		moveState.addTransition(this::shouldIdle, idleState);
		idleState.addTransition(() -> !shouldIdle(), moveState);
	}


	/**
	 * @return of destination is reached
	 */
	public final boolean isDestinationReached()
	{
		var destination = robotTarget.getDestination(getWFrame(), getBotID());
		return destination == null || VectorMath.distancePP(getPos(), destination) < 70;
	}


	public boolean isTargetReached()
	{
		return robotTargetChecker.isTargetReached(
				getWFrame(),
				getBotID(),
				robotTarget
		);
	}


	public void setMinTimeAtDest(double seconds)
	{
		robotTargetChecker.setMinTimeAtDest(seconds);
	}


	/**
	 * @param destination to set
	 */
	public void updateDestination(final IVector2 destination)
	{
		activateMotors();
		robotTarget = robotTarget.withDestination(
				destinationAdjuster.adjustDestination(getAiFrame(), destination, getBotID()));
	}


	/**
	 * @param angle [rad]
	 */
	public void updateTargetAngle(final double angle)
	{
		activateMotors();
		robotTarget = robotTarget.withTargetAngle(angle);
	}


	/**
	 * Updates the angle the bot should look at.
	 *
	 * @param lookAtTarget to set
	 */
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		activateMotors();
		robotTarget = robotTarget.withLookAtTarget(lookAtTarget);
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
		return robotTarget.getDestination(getWFrame(), getBotID());
	}


	/**
	 * Disable motors until a new target is set.
	 * Note: Even if the robot is manually moved, it will not move back until
	 * the target is set again.
	 */
	public void disableMotors()
	{
		motorsEnabled = false;
	}


	private void activateMotors()
	{
		motorsEnabled = true;
	}


	public void disableMotorsWhenDestReached()
	{
		disableMotorsWhenDestReached = true;
	}


	private boolean shouldIdle()
	{
		return !motorsEnabled || (disableMotorsWhenDestReached && isTargetReached());
	}


	private class DefaultState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			skill.setRobotTarget(robotTarget);
			skill.setMoveCon(moveCon);
			skill.setKickParams(kickParams);
			skill.setVelMax(velMax);
			skill.setAccMax(accMax);
			skill.setVelMaxW(velMaxW);
			skill.setAccMaxW(accMaxW);
			super.onUpdate();
		}
	}
}
