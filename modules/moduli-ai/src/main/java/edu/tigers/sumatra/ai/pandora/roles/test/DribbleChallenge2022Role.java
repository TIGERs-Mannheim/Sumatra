/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


public class DribbleChallenge2022Role extends ARole
{
	@Configurable(comment = "Connecting arc radius between straight line segments [m]", defValue = "0.2")
	private static double arcRadius = 0.2;

	@Configurable(comment = "Maximum velocity with ball [m/s]", defValue = "2.0")
	private static double velMax = 2.0;

	@Configurable(comment = "Maximum acceleration [m/s^2]", defValue = "1.0")
	private static double accMax = 1.0;

	@Configurable(comment = "Maximum angular velocity [rad/s]", defValue = "2.5")
	private static double velMaxW = 2.5;

	private final List<IVector2> expectedRobotPositions = new ArrayList<>();

	private List<IVector2> actualRobotPositions = new ArrayList<>();

	private List<ILineSegment> gates = new ArrayList<>();

	private List<IVector2> pushPullWaypoints = new ArrayList<>();

	private List<IVector2> fluentWaypoints = new ArrayList<>();

	private int activeWaypointIndex = 0;

	private long tStart = 0;


	public DribbleChallenge2022Role()
	{
		super(ERole.DRIBBLE_CHALLENGE_2022_ROLE);

		expectedRobotPositions.add(Vector2.fromXY(-3000, -2300));
		expectedRobotPositions.add(Vector2.fromXY(-3500, -2750));
		expectedRobotPositions.add(Vector2.fromXY(-4000, -2300));
		expectedRobotPositions.add(Vector2.fromXY(-3500, -2000));
		expectedRobotPositions.add(Vector2.fromXY(-2500, -2300));
		expectedRobotPositions.add(Vector2.fromXY(500, 2500));
		expectedRobotPositions.add(Vector2.fromXY(1000, 2500));

		var waitForRunningState = new WaitForRunningState();
		var preparePushPullState = new PrepareState(() -> pushPullWaypoints.get(0));
		var prepareFluentState = new PrepareState(() -> fluentWaypoints.get(activeWaypointIndex));
		var getBallContactStatePushPull = new RoleState<>(GetBallContactSkill::new);
		var getBallContactStateFluent = new RoleState<>(GetBallContactSkill::new);
		var pushState = new PushPullState(true);
		var pullState = new PushPullState(false);
		var moveArcState = new MoveArcState();
		var moveStraightState = new MoveStraightState();

		setInitialState(waitForRunningState);

		waitForRunningState.addTransition(waitForRunningState::isSetupCompleteAndGameRunning, preparePushPullState);

		preparePushPullState.addTransition(preparePushPullState::isAtTarget, getBallContactStatePushPull);

		getBallContactStatePushPull.addTransition(ESkillState.SUCCESS, pushState);
		getBallContactStatePushPull.addTransition(ESkillState.FAILURE, preparePushPullState);

		pushState.addTransition(ESkillState.SUCCESS, pullState);
		pushState.addTransition(this::isDiamondPhaseDone, moveStraightState);
		pushState.addTransition(this::isBallLost, preparePushPullState);

		pullState.addTransition(ESkillState.SUCCESS, pushState);
		pullState.addTransition(this::isBallLost, preparePushPullState);

		moveStraightState.addTransition(moveStraightState::isComplete, moveArcState);
		moveStraightState.addTransition(this::isBallLost, prepareFluentState);

		moveArcState.addTransition(moveArcState::isPointingAtTarget, moveStraightState);
		moveArcState.addTransition(this::isBallLost, prepareFluentState);

		prepareFluentState.addTransition(prepareFluentState::isAtTarget, getBallContactStateFluent);

		getBallContactStateFluent.addTransition(ESkillState.SUCCESS, moveStraightState);
		getBallContactStateFluent.addTransition(ESkillState.FAILURE, prepareFluentState);
	}


	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();

		for (int i = 0; i < expectedRobotPositions.size(); i++)
		{
			var botShape = new DrawableBotShape(expectedRobotPositions.get(i), AngleMath.deg2rad(90), 95,
					getBot().getCenter2DribblerDist());
			botShape.setBorderColor(Color.GRAY);
			botShape.setFill(false);
			getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(botShape);

			if (actualRobotPositions.size() > i)
			{
				getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(
						new DrawableCircle(actualRobotPositions.get(i), Geometry.getBotRadius() + 50, Color.red));
				getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(
						new DrawableLine(expectedRobotPositions.get(i), actualRobotPositions.get(i), Color.red));
			}
		}

		gates.forEach(g -> getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableLine(g, Color.cyan)));

		pushPullWaypoints.forEach(
				o -> getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableCircle(o, 20.0, Color.black)));

		fluentWaypoints.forEach(
				o -> getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableCircle(o, 20.0, Color.pink)));

		if (tStart > 0)
		{
			getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE)
					.add(new DrawableAnnotation(getPos().addNew(Vector2.fromXY(0, 200)),
							String.format("Runtime: %.2f", (getWFrame().getTimestamp() - tStart) * 1e-9), true));
		}
	}


	private class WaitForRunningState extends RoleState<IdleSkill>
	{
		private boolean isObstacleAssignmentComplete = false;


		public WaitForRunningState()
		{
			super(IdleSkill::new);
		}


		@Override
		public void onUpdate()
		{
			// map opponent robots to expected positions to figure out actual positions
			actualRobotPositions.clear();

			var opponents = new ArrayList<>(getWFrame().getOpponentBots().values());

			for (var expectedLocation : expectedRobotPositions)
			{
				Optional<ITrackedBot> closestBot = opponents.stream()
						.min(Comparator.comparingDouble(bot -> bot.getPos().distanceTo(expectedLocation)));

				if (closestBot.isPresent())
				{
					actualRobotPositions.add(closestBot.get().getPos());
					opponents.remove(closestBot.get());
				} else
				{
					// not enough robots on the field to match all expected locations
					isObstacleAssignmentComplete = false;
					return;
				}
			}

			isObstacleAssignmentComplete = true;

			// compute gates from actual positions
			gates.clear();

			gates.add(Lines.segmentFromPoints(actualRobotPositions.get(0), actualRobotPositions.get(1))); // Gate A
			gates.add(Lines.segmentFromPoints(actualRobotPositions.get(1), actualRobotPositions.get(2))); // Gate B
			gates.add(Lines.segmentFromPoints(actualRobotPositions.get(2), actualRobotPositions.get(3))); // Gate C
			gates.add(Lines.segmentFromPoints(actualRobotPositions.get(3), actualRobotPositions.get(0))); // Gate D
			gates.add(Lines.segmentFromPoints(actualRobotPositions.get(0), actualRobotPositions.get(4))); // Gate E
			gates.add(Lines.segmentFromPoints(actualRobotPositions.get(5), actualRobotPositions.get(6))); // Gate F

			// compute first set of waypoints: gates A, B, and C are passed twice by push/pull motion
			pushPullWaypoints.clear();

			for (int i = 0; i < 3; i++)
			{
				var gate = gates.get(i);
				var passpoint = gate.getPathStart().addNew(gate.directionVector().multiplyNew(0.5));

				var rotation = AngleMath.DEG_090_IN_RAD;

				var inner = passpoint.addNew(gate.directionVector().turnNew(-rotation).scaleTo(350.0));
				var outer = passpoint.addNew(gate.directionVector().turnNew(rotation).scaleTo(200.0));

				if (i != 0)
					pushPullWaypoints.add(inner);

				pushPullWaypoints.add(outer);

				if (i == 2)
					pushPullWaypoints.add(inner);
			}

			// compute waypoints for fluent dribbling
			fluentWaypoints.clear();
			IVector2 betweenRobot0And4 = actualRobotPositions.get(0).addNew(actualRobotPositions.get(4)).multiply(0.5);
			IVector2 betweenRobot5And6 = actualRobotPositions.get(5).addNew(actualRobotPositions.get(6)).multiply(0.5);

			fluentWaypoints.add(actualRobotPositions.get(0).addNew(Vector2.fromXY(-100, 300))); // -3100, -2000
			fluentWaypoints.add(betweenRobot0And4.addNew(Vector2.fromXY(-50, 300))); // -2800, -2000
			fluentWaypoints.add(betweenRobot0And4.addNew(Vector2.fromXY(0, -300))); // -2750, -2600
			fluentWaypoints.add(betweenRobot0And4.addNew(Vector2.fromXY(500, -300))); // -2250, -2600
			fluentWaypoints.add(betweenRobot5And6.addNew(Vector2.fromXY(-500, 300))); // 250, 2800
			fluentWaypoints.add(betweenRobot5And6.addNew(Vector2.fromXY(0, 300))); // 750, 2800
			fluentWaypoints.add(betweenRobot5And6.addNew(Vector2.fromXY(0, -1200))); // 750, 1300

			if (getAiFrame().getGameState().isGameRunning())
			{
				tStart = getWFrame().getTimestamp();
			}
		}


		public boolean isSetupCompleteAndGameRunning()
		{
			return isObstacleAssignmentComplete && getAiFrame().getGameState().isGameRunning();
		}
	}

	@RequiredArgsConstructor
	private class PrepareState extends MoveState
	{
		private final Supplier<IVector2> nextWaypointSupplier;


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
		}


		@Override
		protected void onUpdate()
		{
			IVector2 nextWaypoint = nextWaypointSupplier.get();

			skill.updateDestination(
					getBall().getPos().addNew(Vector2.fromPoints(getBall().getPos(), nextWaypoint).scaleTo(-200.0)));

			skill.updateLookAtTarget(getBall());

			if (skill.getDestination().distanceTo(getPos()) < 1000.0)
			{
				skill.setVelMax(velMax);
			}
		}


		public boolean isAtTarget()
		{
			return skill.getSkillState() == ESkillState.SUCCESS && getBall().getVel().getLength() < 0.1;
		}
	}

	@RequiredArgsConstructor
	private class PushPullState extends MoveState
	{
		private final boolean isPush;

		private IVector2 currentPlacementPos;


		@Override
		protected void onInit()
		{
			skill.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);

			if (!pushPullWaypoints.isEmpty())
			{
				currentPlacementPos = pushPullWaypoints.get(0);
				updateTargetPose();
			}
		}


		@Override
		protected void onUpdate()
		{
			skill.setVelMax(velMax);
			skill.setAccMax(accMax);
			skill.getMoveConstraints().setVelMaxW(velMaxW);

			if (currentPlacementPos != null && currentPlacementPos.distanceTo(getBall().getPos()) > 300)
			{
				updateTargetPose();
			}
		}


		@Override
		protected void onExit()
		{
			if (!pushPullWaypoints.isEmpty() && skill.getSkillState() == ESkillState.SUCCESS)
				pushPullWaypoints.remove(0);
		}


		private void updateTargetPose()
		{
			double ball2Target = currentPlacementPos.subtractNew(getBall().getPos()).getAngle();
			skill.updateDestination(currentPlacementPos);
			skill.updateTargetAngle(ball2Target + (isPush ? 0 : AngleMath.DEG_180_IN_RAD));
		}
	}

	private class MoveStraightState extends MoveState
	{
		private IVector2 target;
		private double entryDistance;

		private double dist2Target;


		@Override
		public void onInit()
		{
			dist2Target = Double.MAX_VALUE;

			IVector2 activeWp = fluentWaypoints.get(activeWaypointIndex);
			IVector2 curPos = getPos();
			double targetAngle = Vector2.fromPoints(curPos, activeWp).getAngle();

			if (activeWaypointIndex == fluentWaypoints.size() - 1)
			{
				target = activeWp;
				entryDistance = 50.0;
			} else
			{
				// During arc state we assume an async BB traj and this computation computes a target point and switch
				// distance to this point. When switching at exactly the computed instance, the async BB traj will
				// result in an arc movement smoothly reaching the path to the next WP

				IVector2 nextWp = fluentWaypoints.get(activeWaypointIndex + 1);
				Vector2 nextToCurWp = Vector2.fromPoints(nextWp, activeWp);
				Vector2 curToPrevWp = Vector2.fromPoints(activeWp, curPos);

				double alpha = nextToCurWp.angleToAbs(curToPrevWp).orElse(0.0);

				double distToPriDir = SumatraMath.sin(alpha) * arcRadius;
				double orthoVel = Math.sqrt(2.0 * distToPriDir * accMax * 2.0 / 3.0);
				double totalVel = orthoVel / SumatraMath.sin(alpha);

				double arcVelMax = Math.sqrt(arcRadius * accMax);

				if (totalVel > arcVelMax)
				{
					totalVel = arcVelMax;
				}

				double stopDist = 0.5 * totalVel * totalVel / accMax * 1000.0;

				IVector2 entry = LineMath.stepAlongLine(activeWp, curPos, arcRadius * 1000.0);
				target = LineMath.stepAlongLine(entry, activeWp, stopDist);

				entryDistance = stopDist;
			}

			skill.updateTargetAngle(targetAngle);
			skill.updateDestination(target);
			skill.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
		}


		@Override
		public void onUpdate()
		{
			getAiFrame().getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableCircle(target, 5.0));

			if (skill.getDestination().distanceTo(getPos()) < 1000.0)
			{
				skill.setVelMax(velMax);
			}

			skill.setAccMax(accMax);
			skill.getMoveConstraints().setVelMaxW(velMaxW);

			dist2Target = getBot().getFilteredState().map(State::getPos).orElse(getPos()).distanceTo(target);

			if (dist2Target < entryDistance && activeWaypointIndex == fluentWaypoints.size() - 1)
			{
				setCompleted();
			}
		}


		public boolean isComplete()
		{
			return dist2Target < entryDistance;
		}
	}

	private class MoveArcState extends MoveState
	{
		private IVector2 outVector;


		@Override
		public void onInit()
		{
			IVector2 activeWp = fluentWaypoints.get(activeWaypointIndex);
			IVector2 nextWp = fluentWaypoints.get(activeWaypointIndex + 1);

			outVector = Vector2.fromPoints(activeWp, nextWp);

			skill.updateLookAtTarget(nextWp);
			skill.updateDestination(nextWp);
			skill.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveConstraints().setPrimaryDirection(outVector);
		}


		@Override
		public void onUpdate()
		{
			skill.setVelMax(velMax);
			skill.setAccMax(accMax);
			skill.getMoveConstraints().setVelMaxW(velMaxW);
		}


		public boolean isPointingAtTarget()
		{
			double orientDiff = getBot().getFilteredState().map(State::getVel2).orElse(getBot().getVel())
					.angleToAbs(outVector).orElse(1.0);
			if (orientDiff < 0.1)
			{
				activeWaypointIndex++;
				return true;
			}

			return false;
		}
	}


	private boolean isBallLost()
	{
		return !getBot().getBallContact().hadContact(0.25)
				&& !getBot().getBallContact().isBallContactFromVision();
	}


	private boolean isDiamondPhaseDone()
	{
		return pushPullWaypoints.isEmpty();
	}
}
