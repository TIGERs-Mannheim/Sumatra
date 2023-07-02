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
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;


/**
 * Role for RoboCup 2021 Hardware Challenge 3: Dribbling
 * <a href="https://robocup-ssl.github.io/ssl-hardware-challenge-rules/rules.html#_challenge_3_dribbling">rules</a>
 * Takes the ball and passes through gates of robots.
 * This advanced implementation does a single run through all gates. It uses BB trajectories alternating in sync
 * and async fashion to drive in arcs while still under full position control. The computation is pretty clever :)
 */
public class DribbleChallengeAdvancedRole extends ARole
{
	@Configurable(comment = "Connecting arc radius between straight line segments [m]", defValue = "0.2")
	private static double arcRadius = 0.2;

	@Configurable(comment = "Maximum velocity [m/s]", defValue = "2.0")
	private static double velMax = 2.0;

	@Configurable(comment = "Maximum acceleration [m/s^2]", defValue = "1.0")
	private static double accMax = 1.0;

	@Configurable(comment = "Maximum angular velocity [rad/s]", defValue = "2.5")
	private static double velMaxW = 2.5;

	private List<ILineSegment> gates = new ArrayList<>();
	private List<IVector2> obstacles = new ArrayList<>();
	private List<IVector2> waypoints = new ArrayList<>();
	private List<IVector2> finalWaypoints = new ArrayList<>();
	private long tStart = 0;
	private int activeWaypointIndex = 0;


	public DribbleChallengeAdvancedRole()
	{
		super(ERole.DRIBBLE_CHALLENGE_ADVANCED_ROLE);

		var waitForRunningState = new WaitForRunningState();
		var prepareState = new PrepareState();
		var getBallContactState = new RoleState<>(GetBallContactSkill::new);
		var moveArcState = new MoveArcState();
		var moveStraightState = new MoveStraightState();
		var finalMovesStateA = new FinalMovesState();
		var finalMovesStateB = new FinalMovesState();

		setInitialState(waitForRunningState);

		addTransition(waitForRunningState, EEvent.DONE, prepareState);
		prepareState.addTransition(prepareState::isAtTarget, getBallContactState);
		getBallContactState.addTransition(ESkillState.FAILURE, prepareState);
		getBallContactState.addTransition(ESkillState.SUCCESS, moveStraightState);
		addTransition(moveStraightState, EEvent.DONE, moveArcState);
		addTransition(moveStraightState, EEvent.FAILED, prepareState);
		addTransition(moveArcState, EEvent.DONE, moveStraightState);
		addTransition(moveArcState, EEvent.FAILED, prepareState);
		addTransition(moveStraightState, EEvent.WAYPOINTS_COMPLETE, finalMovesStateA);
		finalMovesStateA.addTransition(ESkillState.SUCCESS, finalMovesStateB);
		finalMovesStateA.addTransition(ESkillState.FAILURE, prepareState);
		finalMovesStateB.addTransition(ESkillState.SUCCESS, finalMovesStateA);
		finalMovesStateB.addTransition(ESkillState.FAILURE, prepareState);
	}


	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();

		gates.forEach(g -> getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableLine(g, Color.cyan)));
		obstacles.forEach(o -> getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE)
				.add(new DrawableCircle(o, Geometry.getBotRadius() + 50, Color.red)));
		waypoints.forEach(
				o -> getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableCircle(o, 20.0, Color.black)));
		finalWaypoints.forEach(
				o -> getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableCircle(o, 20.0, Color.pink)));

		if (tStart > 0)
		{
			getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE)
					.add(new DrawableAnnotation(getPos().addNew(Vector2.fromXY(0, 200)),
							String.format("Runtime: %.2f", (getWFrame().getTimestamp() - tStart) * 1e-9), true));
		}
	}


	private enum EEvent implements IEvent
	{
		DONE,
		WAYPOINTS_COMPLETE,
		FAILED,
	}

	private class WaitForRunningState extends AState
	{
		@Override
		public void doUpdate()
		{
			IVector2 ballPos = getBall().getPos();

			obstacles = getWFrame().getOpponentBots().values().stream()
					.sorted(Comparator.comparingDouble(bot -> bot.getPos().distanceTo(ballPos)))
					.map(ITrackedBot::getPos)
					.toList();

			waypoints.clear();
			finalWaypoints.clear();

			if (obstacles.size() >= 2)
			{
				gates = IntStream.range(0, obstacles.size() - 1)
						.boxed()
						.map(i -> Lines.segmentFromPoints(obstacles.get(i), obstacles.get(i + 1)))
						.toList();

				setWaypointsFromGates();
			}

			if (getAiFrame().getGameState().isGameRunning())
			{
				tStart = getWFrame().getTimestamp();
				triggerEvent(EEvent.DONE);
			}
		}


		private void setWaypointsFromGates()
		{
			for (int i = 0; i < gates.size(); i++)
			{
				var gate = gates.get(i);
				var passpoint = gate.getPathStart().addNew(gate.directionVector().multiplyNew(0.5));

				var rotation = i % 2 == 0 ? AngleMath.DEG_090_IN_RAD : -AngleMath.DEG_090_IN_RAD;

				var first = passpoint.addNew(gate.directionVector().turnNew(-rotation).scaleTo(300.0));
				var second = passpoint.addNew(gate.directionVector().turnNew(rotation).scaleTo(300.0));

				if (i == gates.size() - 1)
				{
					second = passpoint.addNew(gate.directionVector().turnNew(rotation).scaleTo(150.0));
				}

				if (i == 0)
				{
					var entry = gate.getPathStart().addNew(gate.directionVector().turnNew(-rotation).scaleTo(300.0));
					waypoints.add(entry);
				}

				waypoints.add(first);
				waypoints.add(second);

				if (i == gates.size() - 1)
				{
					finalWaypoints.add(passpoint.addNew(gate.directionVector().turnNew(-rotation).scaleTo(150.0)));
					finalWaypoints.add(passpoint.addNew(gate.directionVector().turnNew(rotation).scaleTo(250.0)));
				}
			}
		}
	}


	private class PrepareState extends RoleState<MoveToSkill>
	{
		public PrepareState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
		}


		@Override
		protected void onUpdate()
		{
			IVector2 nextWaypoint = waypoints.get(activeWaypointIndex);

			skill.updateLookAtTarget(getBall());

			skill.updateDestination(
					getBall().getPos().addNew(Vector2.fromPoints(getBall().getPos(), nextWaypoint).scaleTo(-200.0)));
		}


		public boolean isAtTarget()
		{
			return skill.getSkillState() == ESkillState.SUCCESS && getBall().getVel().getLength() < 0.1;
		}
	}

	private class MoveStraightState extends AState
	{
		private MoveToSkill moveTo;
		private IVector2 target;
		private double entryDistance;


		@Override
		public void doEntryActions()
		{
			IVector2 activeWp = waypoints.get(activeWaypointIndex);
			IVector2 curPos = getPos();
			double targetAngle = Vector2.fromPoints(curPos, activeWp).getAngle();

			if (activeWaypointIndex == waypoints.size() - 1)
			{
				target = activeWp;
				entryDistance = 50.0;
			} else
			{
				// During arc state we assume an async BB traj and this computation computes a target point and switch
				// distance to this point. When switching at exactly the computed instance, the async BB traj will
				// result in an arc movement smoothly reaching the path to the next WP

				IVector2 nextWp = waypoints.get(activeWaypointIndex + 1);
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

			moveTo = new MoveToSkill();
			moveTo.updateTargetAngle(targetAngle);
			moveTo.updateDestination(target);
			moveTo.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
			moveTo.getMoveCon().physicalObstaclesOnly();
			moveTo.getMoveCon().setBotsObstacle(false);
			moveTo.getMoveCon().setBallObstacle(false);

			setNewSkill(moveTo);
		}


		@Override
		public void doUpdate()
		{
			getAiFrame().getShapes(EAiShapesLayer.TEST_DRIBBLE_CHALLENGE).add(new DrawableCircle(target, 5.0));

			moveTo.setVelMax(velMax);
			moveTo.setAccMax(accMax);
			moveTo.getMoveConstraints().setVelMaxW(velMaxW);

			if (!getBot().getBallContact().hadContact(0.5)
					&& !getBot().getBallContact().isBallContactFromVision())
			{
				triggerEvent(EEvent.FAILED);
			}

			double dist2Target = getBot().getFilteredState().map(State::getPos).orElse(getPos()).distanceTo(target);
			if (dist2Target < entryDistance)
			{
				if (activeWaypointIndex == waypoints.size() - 1)
				{
					triggerEvent(EEvent.WAYPOINTS_COMPLETE);
				} else
				{
					triggerEvent(EEvent.DONE);
				}
			}
		}
	}

	private class MoveArcState extends AState
	{
		private MoveToSkill moveTo;
		private IVector2 outVector;


		@Override
		public void doEntryActions()
		{
			IVector2 activeWp = waypoints.get(activeWaypointIndex);
			IVector2 nextWp = waypoints.get(activeWaypointIndex + 1);

			outVector = Vector2.fromPoints(activeWp, nextWp);

			moveTo = new MoveToSkill();
			moveTo.updateLookAtTarget(nextWp);
			moveTo.updateDestination(nextWp);
			moveTo.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
			moveTo.getMoveCon().physicalObstaclesOnly();
			moveTo.getMoveCon().setBotsObstacle(false);
			moveTo.getMoveCon().setBallObstacle(false);
			moveTo.getMoveConstraints().setPrimaryDirection(outVector);

			setNewSkill(moveTo);
		}


		@Override
		public void doUpdate()
		{
			moveTo.setVelMax(velMax);
			moveTo.setAccMax(accMax);
			moveTo.getMoveConstraints().setVelMaxW(velMaxW);

			if (!getBot().getBallContact().hadContact(0.5)
					&& !getBot().getBallContact().isBallContactFromVision())
			{
				triggerEvent(EEvent.FAILED);
			}

			double orientDiff = getBot().getFilteredState().map(State::getVel2).orElse(getBot().getVel())
					.angleToAbs(outVector).orElse(1.0);
			if (orientDiff < 0.1)
			{
				activeWaypointIndex++;
				triggerEvent(EEvent.DONE);
			}
		}
	}

	private class FinalMovesState extends RoleState<MoveWithBallSkill>
	{
		private IVector2 currentPlacementPos;


		public FinalMovesState()
		{
			super(MoveWithBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			if (!finalWaypoints.isEmpty())
			{
				currentPlacementPos = finalWaypoints.get(0);
				updateTargetPose();
			}
		}


		@Override
		protected void onUpdate()
		{
			if (currentPlacementPos != null && currentPlacementPos.distanceTo(getBall().getPos()) > 300)
			{
				updateTargetPose();
			}
		}


		@Override
		protected void onExit()
		{
			if (!finalWaypoints.isEmpty())
				finalWaypoints.remove(0);

			if (finalWaypoints.isEmpty())
				setCompleted();
		}


		private void updateTargetPose()
		{
			double dist2Ball = Geometry.getBallRadius() + getBot().getCenter2DribblerDist();
			double ball2Target = currentPlacementPos.subtractNew(getBall().getPos()).getAngle();
			IVector2 offset = AngleMath.diffAbs(getBot().getOrientation(), ball2Target) < AngleMath.DEG_090_IN_RAD
					? Vector2.fromAngleLength(ball2Target + AngleMath.DEG_180_IN_RAD, dist2Ball)
					: Vector2.fromAngleLength(ball2Target, dist2Ball);
			skill.setFinalDest(currentPlacementPos.addNew(offset));
			skill.setFinalOrientation(offset.getAngle() + AngleMath.DEG_180_IN_RAD);
		}
	}
}
