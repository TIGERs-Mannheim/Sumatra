/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.ABallPlacementPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.placement.BallPlacementRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import lombok.Setter;

import java.awt.Color;
import java.util.Comparator;
import java.util.Objects;


public abstract class ABallPreparationPlay extends ABallPlacementPlay
{
	@Configurable(defValue = "300", comment = "Custom ball placement tolerance [mm]")
	private static double ballPlacementTolerance = 300;

	private final IdleState idleState = new IdleState();
	private final BallPlacementState ballPlacementState = new BallPlacementState();
	protected final IStateMachine<IState> stateMachine = new StateMachine<>(this.getClass().getSimpleName());

	@Setter
	private IVector2 ballTargetPos = Vector2.zero();
	@Setter
	private boolean useAssistant;

	private BotID ballHandlerBot;


	protected ABallPreparationPlay(final EPlay type)
	{
		super(type);

		stateMachine.addTransition(idleState, EEvent.START, ballPlacementState);
		stateMachine.addTransition(null, EEvent.STOP, idleState);

		stateMachine.setInitialState(idleState);
	}


	protected void setExecutionState(IState executionState)
	{
		stateMachine.addTransition(ballPlacementState, EEvent.BALL_PLACED, executionState);
		stateMachine.addTransition(null, EEvent.EXECUTED, ballPlacementState);
	}


	public void stopExecution()
	{
		stateMachine.triggerEvent(EEvent.STOP);
	}


	@Override
	protected void onNumberOfBotsChanged()
	{
		stopExecution();
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
	protected boolean useAssistant()
	{
		return useAssistant
				&& getBall().getTrajectory().distanceTo(ballTargetPos) > 1000;
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();

		if (stateMachine.getCurrentState() != idleState && !ready())
		{
			stopExecution();
		}
		stateMachine.update();

		getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
				new DrawableCircle(Circle.createCircle(ballTargetPos, 30)).setColor(Color.cyan)
		);

		findRoles(BallPlacementRole.class).forEach(r -> r.setPlacementTolerance(ballPlacementTolerance));
	}


	protected enum EEvent implements IEvent
	{
		BALL_PLACED,
		STOP,
		START,
		EXECUTED,
	}


	private class BallPlacementState extends AState
	{
		@Override
		public void doUpdate()
		{
			assignBallPlacementRoles();
			if (ballPlacementDone())
			{
				stateMachine.triggerEvent(EEvent.BALL_PLACED);
			}
		}
	}

	private class IdleState extends AState
	{
		@Override
		public void doUpdate()
		{
			findOtherRoles(MoveRole.class).forEach(r -> reassignRole(r, MoveRole.class, MoveRole::new));
			if (ready())
			{
				stateMachine.triggerEvent(EEvent.START);
			}
		}
	}
}
