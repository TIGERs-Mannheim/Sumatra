/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.ABallPlacementPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.statemachine.TransitionableState;
import lombok.Setter;

import java.awt.Color;
import java.util.Comparator;
import java.util.Objects;


public abstract class ABallPreparationPlay extends ABallPlacementPlay
{
	protected final IStateMachine<IState> stateMachine = new StateMachine<>(this.getClass().getSimpleName());
	private final IdleState idleState = new IdleState();
	private final BallPlacementState ballPlacementState = new BallPlacementState();

	@Setter
	private IVector2 ballTargetPos = Vector2.zero();

	@Setter
	private boolean enableBallPlacement = true;

	private BotID ballHandlerBot;


	protected ABallPreparationPlay(final EPlay type)
	{
		super(type);

		idleState.addTransition(this::ready, ballPlacementState);

		stateMachine.setInitialState(idleState);
	}


	protected void setExecutionState(TransitionableState executionState)
	{
		ballPlacementState.addTransition(this::ballPlacementDone, executionState);
		ballPlacementState.addTransition(() -> !enableBallPlacement, executionState);
		executionState.addTransition(() -> !ready(), idleState);
	}


	public void stopExecution()
	{
		stateMachine.changeState(idleState);
	}


	protected abstract boolean ready();


	protected ARole getClosestToBall()
	{
		ARole current = getRoles().stream().filter(a -> Objects.equals(a.getBotID(), ballHandlerBot)).findFirst()
				.orElse(null);
		if (current == null)
		{
			IVector2 curBallPos = getBall().getPos();
			ARole closestToBall = getRoles().stream()
					.min(Comparator.comparing(r -> r.getPos().distanceTo(curBallPos)))
					.orElseThrow(IllegalStateException::new);
			ballHandlerBot = closestToBall.getBotID();
			return closestToBall;
		}
		return current;
	}


	@Override
	protected IVector2 getBallTargetPos()
	{
		return ballTargetPos;
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();

		stateMachine.update();

		getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
				new DrawableCircle(Circle.createCircle(ballTargetPos, 30)).setColor(Color.cyan)
		);
	}


	private class BallPlacementState extends TransitionableState
	{
		public BallPlacementState()
		{
			super(stateMachine::changeState);
		}


		@Override
		public void onUpdate()
		{
			assignBallPlacementRoles();
		}
	}

	private class IdleState extends TransitionableState
	{
		public IdleState()
		{
			super(stateMachine::changeState);
		}


		@Override
		public void onUpdate()
		{
			findOtherRoles(MoveRole.class).forEach(r -> reassignRole(r, MoveRole.class, MoveRole::new));
		}
	}
}
