package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.ArrayList;
import java.util.Comparator;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryBallPlacementRole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;


public abstract class ABallPreparationPlay extends APlay
{
	private final IdleState idleState = new IdleState();
	private final BallPlacementState ballPlacementState = new BallPlacementState();
	protected final IStateMachine<IState> stateMachine = new StateMachine<>();
	
	private final int numRoles;
	
	private IVector2 ballTargetPos;
	private BotID ballHandlerBot;
	
	
	public ABallPreparationPlay(final EPlay type, final int numRoles)
	{
		super(type);
		this.numRoles = numRoles;
		
		stateMachine.addTransition(idleState, EEvent.START, ballPlacementState);
		stateMachine.addTransition(null, EEvent.STOP, idleState);
		
		stateMachine.setInitialState(idleState);
	}
	
	
	protected void setExecutionState(IState executionState)
	{
		stateMachine.addTransition(ballPlacementState, EEvent.BALL_PLACED, executionState);
		stateMachine.addTransition(null, EEvent.EXECUTED, ballPlacementState);
	}
	
	
	protected ARole getClosestToBall()
	{
		ARole current = getRoles().stream().filter(a -> a.getBotID() == ballHandlerBot).findFirst().orElse(null);
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
	
	
	protected final void setBallTargetPos(final IVector2 ballTargetPos)
	{
		this.ballTargetPos = ballTargetPos;
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		super.doUpdate(frame);
		
		if (stateMachine.getCurrentState() != idleState && getRoles().size() != numRoles)
		{
			stateMachine.triggerEvent(EEvent.STOP);
		}
		stateMachine.update();
	}
	
	
	protected abstract void handleNonPlacingRole(ARole role);
	
	
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
		public void doEntryActions()
		{
			ARole closestToBall = getClosestToBall();
			if (!(closestToBall instanceof PrimaryBallPlacementRole))
			{
				closestToBall = switchRoles(closestToBall, new PrimaryBallPlacementRole(ballTargetPos));
			}
			for (ARole role : new ArrayList<>(getRoles()))
			{
				if (role == closestToBall)
				{
					continue;
				}
				handleNonPlacingRole(role);
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isBallPlacementRequired())
			{
				stateMachine.triggerEvent(EEvent.BALL_PLACED);
			}
		}
		
		
		private boolean isBallPlacementRequired()
		{
			ARole closestToBall = getClosestToBall();
			if (getBall().getPos().distanceTo(ballTargetPos) > 100)
			{
				return true;
			}
			boolean ballPlacementActive = closestToBall.getType() == ERole.PRIMARY_BALL_PLACEMENT;
			return ballPlacementActive && ((PrimaryBallPlacementRole) closestToBall).isBallStillHandled();
		}
	}
	
	private class IdleState extends AState
	{
		@Override
		public void doUpdate()
		{
			if (getRoles().size() == numRoles)
			{
				stateMachine.triggerEvent(ReproducibleRedirectPlay.EEvent.START);
			}
		}
	}
}
