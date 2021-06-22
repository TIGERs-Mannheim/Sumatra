/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Role for RoboCup 2021 Hardware Challenge 3: Dribbling
 * https://robocup-ssl.github.io/ssl-hardware-challenge-rules/rules.html#_challenge_3_dribbling
 * Takes the ball and passes through gates of robots. This simple implementation uses some intermediate points
 * and always comes to a full stop at these.
 */
public class DribbleChallengeSimpleRole extends ARole
{
	private List<ILine> gates = new ArrayList<>();
	private List<IVector2> obstacles = new ArrayList<>();
	private List<IVector2> waypoints = new ArrayList<>();

	private long tStart = 0;


	public DribbleChallengeSimpleRole()
	{
		super(ERole.DRIBBLE_CHALLENGE_SIMPLE_ROLE);

		var waitForRunningState = new WaitForRunningState();
		var prepareState = new PrepareState();
		var getBallContactState = new RoleState<>(GetBallContactSkill::new);
		var pushBallState = new PushBallState();
		var clearBallState = new ClearBallState();
		var completeWaypointState = new CompleteWaypointState();

		setInitialState(waitForRunningState);

		addTransition(waitForRunningState, EEvent.DONE, prepareState);
		prepareState.addTransition(prepareState::isAtTarget, getBallContactState);
		getBallContactState.addTransition(ESkillState.SUCCESS, pushBallState);
		getBallContactState.addTransition(ESkillState.FAILURE, prepareState);
		pushBallState.addTransition(ESkillState.SUCCESS, clearBallState);
		pushBallState.addTransition(ESkillState.FAILURE, prepareState);
		clearBallState.addTransition(clearBallState::isCleared, completeWaypointState);
		addTransition(completeWaypointState, EEvent.DONE, prepareState);
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
					.collect(Collectors.toList());

			waypoints.clear();

			if (obstacles.size() >= 2)
			{
				gates = IntStream.range(0, obstacles.size() - 1)
						.boxed()
						.map(i -> Line.fromPoints(obstacles.get(i), obstacles.get(i + 1)))
						.collect(Collectors.toList());

				for (int i = 0; i < gates.size(); i++)
				{
					var gate = gates.get(i);
					var passpoint = gate.getStart().addNew(gate.getEnd()).multiplyNew(0.5);

					var rotation = i % 2 == 0 ? AngleMath.DEG_090_IN_RAD : -AngleMath.DEG_090_IN_RAD;

					var first = passpoint.addNew(gate.directionVector().turnNew(-rotation).scaleTo(500.0));
					var second = passpoint.addNew(gate.directionVector().turnNew(rotation).scaleTo(500.0));

					if (i == 0)
					{
						var entry = gate.getStart().addNew(gate.directionVector().turnNew(-rotation).scaleTo(500.0));
						waypoints.add(entry);
					}

					waypoints.add(first);
					waypoints.add(second);

					if (i == gates.size() - 1)
					{
						waypoints.add(first);
						waypoints.add(second);
					}
				}
			}

			if (getAiFrame().getGameState().isGameRunning())
			{
				tStart = getWFrame().getTimestamp();
				triggerEvent(EEvent.DONE);
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
			IVector2 nextWaypoint = waypoints.get(0);

			skill.updateLookAtTarget(getBall());

			skill.updateDestination(
					getBall().getPos().addNew(Vector2.fromPoints(getBall().getPos(), nextWaypoint).scaleTo(-200.0)));
		}


		public boolean isAtTarget()
		{
			return skill.getSkillState() == ESkillState.SUCCESS && getBall().getVel().getLength() < 0.1;
		}
	}

	private class PushBallState extends RoleState<MoveWithBallSkill>
	{
		public PushBallState()
		{
			super(MoveWithBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			IVector2 nextWaypoint = waypoints.get(0);

			double dist2Ball = Geometry.getBallRadius() + getBot().getCenter2DribblerDist();

			skill.setFinalDest(nextWaypoint.addNew(Vector2.fromPoints(getPos(), nextWaypoint).scaleTo(-dist2Ball)));
		}
	}

	private class ClearBallState extends RoleState<MoveToSkill>
	{
		private final TimestampTimer calmDownTimer = new TimestampTimer(0.0);


		public ClearBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill = new MoveToSkill();
			skill.getMoveCon().physicalObstaclesOnly();
			calmDownTimer.setDuration(0.5);
			calmDownTimer.start(getWFrame().getTimestamp());
		}


		@Override
		protected void onUpdate()
		{
			if (!calmDownTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				return;
			}

			double clearanceRadius = 100.0;
			IVector2 dest = LineMath.stepAlongLine(getBall().getPos(), getPos(), clearanceRadius);
			skill.updateLookAtTarget(getBall());
			skill.updateDestination(dest);
		}


		public boolean isCleared()
		{
			return getBall().getPos().distanceTo(getPos()) > Geometry.getBotRadius() + 50;
		}
	}

	private class CompleteWaypointState extends AState
	{
		@Override
		public void doUpdate()
		{
			waypoints.remove(0);

			if (waypoints.isEmpty())
			{
				setCompleted();
			} else
			{
				triggerEvent(EEvent.DONE);
			}
		}
	}
}
