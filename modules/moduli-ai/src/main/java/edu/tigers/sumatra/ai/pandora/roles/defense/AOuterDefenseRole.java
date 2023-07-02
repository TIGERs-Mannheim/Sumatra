/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


/**
 * Abstract defense role for those roles that go out (not at the penalty area).
 */
public abstract class AOuterDefenseRole extends ADefenseRole
{
	@Configurable(comment = "Gain factor for threat velocity in ProtectionState, high gain => high overshoot, low gain => defender lags behind", defValue = "0.75")
	private static double mimicThreatVelocityGain = 0.75;
	@Configurable(comment = "[deg] The angle to determine if the CenterBack is between Ball and Goal", defValue = "45.0")
	private static double betweenBallAndGoalAngle = 45.0;

	@Setter
	@Getter
	protected IDefenseThreat threat = null;


	protected AOuterDefenseRole(final ERole type)
	{
		super(type);
	}


	protected abstract Set<BotID> ignoredBots(IVector2 dest);


	protected abstract IVector2 findDest();


	protected ILineSegment protectionLine()
	{
		return threat.getProtectionLine().orElseThrow(IllegalStateException::new);
	}


	protected double protectionTargetAngle()
	{
		return protectionLine().directionVector().getAngle() + AngleMath.DEG_180_IN_RAD;
	}


	protected IVector2 mimicThreatVelocity(IVector2 currentDest)
	{
		IVector2 positioningDirection = threat.getProtectionLine()
				.orElseThrow(IllegalStateException::new)
				.directionVector()
				.getNormalVector()
				.normalizeNew();

		// mimic threat velocity at protection line => no more lag of defenders
		double projectedThreatVelocity = threat.getVel().scalarProduct(positioningDirection);
		double timeToBrake = Math.abs(projectedThreatVelocity) / getBot().getMoveConstraints().getAccMax();
		double brakeDistance = Math.signum(projectedThreatVelocity) * 0.5 * getBot().getMoveConstraints().getAccMax()
				* timeToBrake * timeToBrake * 1000.0;

		return currentDest.addNew(positioningDirection.multiplyNew((brakeDistance * mimicThreatVelocityGain)));
	}


	protected boolean isNotBetweenBallAndGoal()
	{
		IVector2 ball2Bot = getPos().subtractNew(getBall().getPos());
		// ThreatLine is from ThreatSource to ThreatTarget
		double angle = threat.getThreatLine().directionVector().angleToAbs(ball2Bot).orElse(0.0);

		return angle > AngleMath.PI - AngleMath.deg2rad(betweenBallAndGoalAngle);
	}


	protected class DefendState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			skill.setKickParams(calcKickParams());

			final IVector2 destination = moveToValidDest(findDest());
			skill.updateDestination(destination);
			skill.updateTargetAngle(protectionTargetAngle());

			skill.getMoveCon().setBallObstacle(isNotBetweenBallAndGoal());
			skill.getMoveCon().setIgnoredBots(ignoredBots(destination));

			if (threat.getObjectId().isBall())
			{
				skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
			} else
			{
				skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.NORMAL);
			}

			skill.getMoveConstraints().setFastMove(useFastMove());
			skill.getMoveConstraints().setPrimaryDirection(primaryDirection());
		}


		private IVector2 primaryDirection()
		{
			if (!skill.getMoveConstraints().isFastMove() && isNotBetweenBallAndGoal())
			{
				return threat.getThreatLine().directionVector();
			}
			return Vector2f.ZERO_VECTOR;
		}


		private boolean useFastMove()
		{
			return skill.getDestination().distanceTo(getPos()) > 4000;
		}
	}
}
