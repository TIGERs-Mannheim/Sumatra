package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Abstract defense role for those roles that go out (not at the penalty area).
 */
public abstract class AOuterDefenseRole extends ADefenseRole
{
	@Configurable(comment = "Gain factor for threat velocity in ProtectionState, high gain => high overshoot, low gain => defender lags behind", defValue = "0.75", unit = EConfigUnit.FACTOR)
	private static double mimicThreatVelocityGain = 0.75;
	@Configurable(comment = "The angle to determine if the CenterBack is between Ball and Goal", defValue = "45.0", unit = EConfigUnit.ANGLE_DEG)
	private static double betweenBallAndGoalAngle = 45.0;

	@Setter
	@Getter
	protected IDefenseThreat threat = null;
	@Getter(AccessLevel.PROTECTED)
	private Destination defendDestination;


	protected AOuterDefenseRole(final ERole type)
	{
		super(type);
	}


	@Override
	protected void beforeUpdate()
	{
		defendDestination = findDest();
	}


	protected abstract Set<BotID> ignoredBots();


	protected abstract Destination findDest();


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


	protected List<IObstacle> offensePassObstacles()
	{
		return getAiFrame().getTacticalField().getPrimaryPassObstacles().entrySet().stream()
				.filter(entry -> avoidKickFromAction(entry.getKey()))
				.map(Map.Entry::getValue)
				.flatMap(List::stream)
				.toList();
	}


	private boolean avoidKickFromAction(EOffensiveActionType type)
	{
		return switch (type)
		{
			case REDIRECT_KICK, PASS -> true;
			default -> false;
		};
	}


	protected boolean isNotBetweenBallAndGoal()
	{
		IVector2 ball2Bot = getPos().subtractNew(getBall().getPos());
		// ThreatLine is from ThreatSource to ThreatTarget
		double angle = threat.getThreatLine().directionVector().angleToAbs(ball2Bot).orElse(0.0);

		return angle > AngleMath.PI - AngleMath.deg2rad(betweenBallAndGoalAngle);
	}


	protected Optional<EObstacleAvoidanceMode> forceObstacleAvoidanceMode()
	{
		return Optional.empty();
	}


	protected class DefendState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			skill.setKickParams(calcKickParams());

			skill.updateDestination(defendDestination.validPos());
			skill.updateTargetAngle(protectionTargetAngle());
			skill.updateTargetTime(defendDestination.getTime());
			skill.updateUseComeToAStopIfFaster(true);

			getShapes(EAiShapesLayer.DEFENSE_OUTER_DEFENDER).addAll(List.of(
					new DrawableBotShape(
							defendDestination.validPos(), protectionTargetAngle(), Geometry.getBotRadius(),
							Geometry.getOpponentCenter2DribblerDist()
					).setColor(Color.GREEN),
					DrawableArrow.fromPositions(defendDestination.getPos(), defendDestination.validPos())
							.setColor(Color.BLUE),
					new DrawableAnnotation(
							defendDestination.validPos(),
							String.format("%.3f", defendDestination.getTime()),
							true
					)
			));

			skill.setMoveCon(skill.getMoveCon().withIgnoredBots(ignoredBots()));
			if (isNotBetweenBallAndGoal())
			{
				skill.setMoveCon(skill.getMoveCon().withCustomObstacles(offensePassObstacles()));
			} else
			{
				skill.setMoveCon(skill.getMoveCon().withCustomObstacles(List.of()));
			}
			skill.setMoveCon(skill.getMoveCon().withBallObstacle(false));

			if (threat.getObjectId().isBall())
			{
				skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE));
			} else
			{
				skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.NORMAL));
			}
			forceObstacleAvoidanceMode().ifPresent(
					mode -> skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(mode)));
		}
	}
}
