/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ABallPreparationPlay;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.statemachine.TransitionableState;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


/**
 * Position robots and ball based on a snapshot.
 */
@Log4j2
public class SnapshotPlay extends ABallPreparationPlay
{
	@Configurable(defValue = "true", comment = "enables the ball placement for the snapshot play")
	private static boolean enableBallPlacement = true;

	@Configurable(defValue = "false", comment = "syncs movements in snapshot plays")
	private static boolean syncMovements = false;

	@Configurable(defValue = "1.0", comment = "[m/s] maximum velocity in execute state")
	private static double maxVelocity = 1.0;

	@Configurable(defValue = "true", comment = "ignore opponent bot obstacles")
	private static boolean ignoreOpponentObstacles = true;

	private Snapshot snapshot;
	private Map<BotID, Boolean> patrollingToMovementDestination = new HashMap<>();
	private boolean isSyncToggleAllowed = false;
	private boolean syncPatrollingToMovementDestination = false;


	public SnapshotPlay()
	{
		super(EPlay.SNAPSHOT);
		var runningState = new RunningState();
		var preparationState = new PreparationState();
		var haltedState = new HaltedState();
		setExecutionState(haltedState);

		haltedState.addTransition(() -> !getAiFrame().getGameState().isIdleGame(), preparationState);
		preparationState.addTransition(() -> getAiFrame().getGameState().isIdleGame(), haltedState);
		runningState.addTransition(() -> getAiFrame().getGameState().isIdleGame(), haltedState);
		runningState.addTransition(() -> getAiFrame().getGameState().isStoppedGame(), preparationState);

		preparationState.addTransition(() -> getAiFrame().getGameState().isRunning(), runningState);

	}


	public void setSnapshotFile(Path snapshot)
	{
		try
		{
			this.snapshot = Snapshot.loadFromFile(snapshot);
		} catch (IOException e)
		{
			log.warn("Failed to open snapshot", e);
		}
	}


	@Override
	protected boolean ready()
	{
		return snapshot != null && !getRoles().isEmpty();
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		if (snapshot != null && snapshot.getBall() != null)
		{

			setBallTargetPos(snapshot.getBall().getPos().getXYVector());

			getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
					new DrawableCircle(Circle.createCircle(snapshot.getBall().getPos().getXYVector(), 300)).setColor(
							Color.green));
			getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
					new DrawableAnnotation(snapshot.getBall().getPos().getXYVector(), "Start").withOffsetY(200)
							.withCenterHorizontally(true).withFontHeight(100).setColor(Color.cyan));
			if (snapshot.getPlacementPos() != null)
			{
				getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
						new DrawableCircle(Circle.createCircle(snapshot.getPlacementPos(), 300)).setColor(Color.cyan));
			}
		}

		isSyncToggleAllowed = findRoles(MoveRole.class).stream()
				.allMatch(MoveRole::isDestinationReached);

		if (isSyncToggleAllowed)
		{
			syncPatrollingToMovementDestination = !syncPatrollingToMovementDestination;
		}

		setEnableBallPlacement(enableBallPlacement);
		super.doUpdateBeforeRoles();
	}


	private class RunningState extends TransitionableState
	{
		public RunningState()
		{
			super(stateMachine::changeState);
		}


		@Override
		public void onUpdate()
		{
			findOtherRoles(MoveRole.class).forEach(r -> reassignRole(r, MoveRole.class, MoveRole::new));
			findRoles(MoveRole.class).forEach(this::updateRole);
		}


		private void updateRole(MoveRole role)
		{
			SnapObject snapObject = snapshot.getBots().get(role.getBotID());

			if (snapObject != null)
			{
				if (snapObject.getMovement() == null)
				{
					role.updateDestination(snapObject.getPos().getXYVector());
				} else if (isPositionToggleAllowed(role))
				{
					patrollingToMovementDestination.putIfAbsent(role.getBotID(), false);
					role.updateDestination(getNextDestination(role, snapObject));
				} else
				{
					role.updateDestination(role.getDestination());
				}
				role.updateTargetAngle(snapObject.getPos().z());
			}
			role.setVelMax(maxVelocity);
			role.getMoveCon().physicalObstaclesOnly();
			role.getMoveCon().setTheirBotsObstacle(!ignoreOpponentObstacles);
		}


		private boolean isPositionToggleAllowed(MoveRole role)
		{
			if (!syncMovements)
			{
				return role.isDestinationReached();
			}

			return isSyncToggleAllowed;
		}


		private IVector2 getNextDestination(MoveRole role, SnapObject snapObject)
		{
			boolean toggleAllowed = syncPatrollingToMovementDestination;

			if (!syncMovements)
			{
				toggleAllowed = patrollingToMovementDestination.get(role.getBotID());
			}

			if (toggleAllowed)
			{
				patrollingToMovementDestination.put(role.getBotID(), false);
				return snapObject.getPos().getXYVector();
			} else
			{
				patrollingToMovementDestination.put(role.getBotID(), true);
				return snapObject.getMovement().getXYVector();
			}
		}
	}

	private class PreparationState extends TransitionableState
	{
		public PreparationState()
		{
			super(stateMachine::changeState);
		}


		@Override
		public void onUpdate()
		{
			findOtherRoles(MoveRole.class).forEach(r -> reassignRole(r, MoveRole.class, MoveRole::new));
			findRoles(MoveRole.class).forEach(this::updateRole);
		}


		private void updateRole(MoveRole role)
		{
			SnapObject snapObject = snapshot.getBots().get(role.getBotID());

			if (snapObject != null)
			{
				role.updateDestination(snapObject.getPos().getXYVector());
				role.updateTargetAngle(snapObject.getPos().z());
			}
			role.setVelMax(maxVelocity);
			role.getMoveCon().physicalObstaclesOnly();
			role.getMoveCon().setTheirBotsObstacle(true);
			isSyncToggleAllowed = false;
			patrollingToMovementDestination.put(role.getBotID(), false);
		}
	}

	private class HaltedState extends TransitionableState
	{
		public HaltedState()
		{
			super(stateMachine::changeState);
		}


		@Override
		public void doEntryActions()
		{
			findRoles(MoveRole.class).forEach(MoveRole::disableMotors);

			super.doEntryActions();
		}
	}
}
