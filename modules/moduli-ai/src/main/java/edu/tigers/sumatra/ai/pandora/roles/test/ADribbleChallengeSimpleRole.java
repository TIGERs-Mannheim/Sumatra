/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple dribble role.
 * Takes the ball and passes through gates of robots. This simple implementation uses some intermediate points
 * and always comes to a full stop.
 */
public abstract class ADribbleChallengeSimpleRole extends ARole
{
	private List<ILineSegment> gates = new ArrayList<>();
	private List<IVector2> waypoints = new ArrayList<>();
	private List<IVector2> obstacles = new ArrayList<>();

	private long tStart = 0;


	protected ADribbleChallengeSimpleRole(ERole type)
	{
		super(type);

		var waitForRunningState = new WaitForRunningState();
		var prepareState = new PrepareState();
		var getBallContactState = new RoleState<>(GetBallContactSkill::new);
		var pushBallState = new PushBallState();
		var clearBallState = new ClearBallState();
		var completeWaypointState = new CompleteWaypointState();

		setInitialState(waitForRunningState);

		waitForRunningState.addTransition(waitForRunningState::isChallengeDataValidAndGameRunning, prepareState);

		prepareState.addTransition(prepareState::isAtTarget, getBallContactState);
		getBallContactState.addTransition(ESkillState.SUCCESS, pushBallState);
		getBallContactState.addTransition(ESkillState.FAILURE, prepareState);
		pushBallState.addTransition(ESkillState.SUCCESS, clearBallState);
		pushBallState.addTransition(ESkillState.FAILURE, prepareState);
		clearBallState.addTransition(clearBallState::isCleared, completeWaypointState);
		completeWaypointState.addTransition(completeWaypointState::hasMoreWaypoints, prepareState);
	}


	protected abstract ChallengeData getChallengeData();


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


	@RequiredArgsConstructor
	protected static class ChallengeData
	{
		public final List<ILineSegment> gates;
		public final List<IVector2> waypoints;
		public final List<IVector2> obstacles;
	}

	private class WaitForRunningState extends RoleState<IdleSkill>
	{
		private boolean isChallengeDataValid = false;


		public WaitForRunningState()
		{
			super(IdleSkill::new);
		}


		@Override
		public void onUpdate()
		{
			ChallengeData data = getChallengeData();

			if (data.waypoints.isEmpty())
			{
				isChallengeDataValid = false;
				return;
			}

			obstacles = data.obstacles;
			waypoints = data.waypoints;
			gates = data.gates;

			isChallengeDataValid = true;

			if (getAiFrame().getGameState().isGameRunning())
			{
				tStart = getWFrame().getTimestamp();
			}
		}


		public boolean isChallengeDataValidAndGameRunning()
		{
			return isChallengeDataValid && getAiFrame().getGameState().isGameRunning();
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
			calmDownTimer.setDuration(0.75);
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

	private class CompleteWaypointState extends RoleState<IdleSkill>
	{

		public CompleteWaypointState()
		{
			super(IdleSkill::new);
		}


		@Override
		protected void onInit()
		{
			waypoints.remove(0);
		}


		@Override
		protected void onUpdate()
		{
			if (waypoints.isEmpty())
			{
				setCompleted();
			}
		}


		public boolean hasMoreWaypoints()
		{
			return !waypoints.isEmpty();
		}
	}
}
