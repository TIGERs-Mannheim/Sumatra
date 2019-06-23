/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryBallPlacementRole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class ReproducibleRedirectPlay extends APlay
{
	@Configurable(defValue = "1300.0,700.0")
	private static IVector2 ballPos = Vector2.fromXY(1300, 700);
	@Configurable(defValue = "-800.0;0.0")
	private static IVector2 receiverPos = Vector2.fromXY(-800, 0);
	@Configurable(defValue = "-800.0;0.0")
	private static IVector2 passTarget = Vector2.fromXY(-800, 0);
	@Configurable(defValue = "0;-1000")
	private static DynamicPosition redirectTarget = new DynamicPosition(Vector2.fromXY(0, -1000));
	
	@Configurable(defValue = "false")
	private static boolean receive = false;
	
	@Configurable(defValue = "false")
	private static boolean allowRoleSwitch = false;
	
	private final IStateMachine<IState> stateMachine = new StateMachine<>();
	private final IdleState idleState = new IdleState();
	private BotID passReceiver = null;
	
	
	public ReproducibleRedirectPlay()
	{
		super(EPlay.REPRODUCIBLE_REDIRECT);
		BallPlacementState ballPlacementState = new BallPlacementState();
		ExecutionState executionState = new ExecutionState();
		
		stateMachine.setInitialState(idleState);
		
		stateMachine.addTransition(idleState, EEvent.START, ballPlacementState);
		stateMachine.addTransition(ballPlacementState, EEvent.BALL_PLACED, executionState);
		stateMachine.addTransition(executionState, EEvent.PASS_EXECUTED, ballPlacementState);
		stateMachine.addTransition(null, EEvent.STOP, idleState);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new RedirectTestRole();
	}
	
	
	private ARole getClosestToBall()
	{
		ARole current = getRoles().stream().filter(a -> a.getBotID() == passReceiver).findFirst().orElse(null);
		if (allowRoleSwitch || current == null)
		{
			IVector2 curBallPos = getBall().getPos();
			ARole closestToBall = getRoles().stream()
					.min(Comparator.comparing(r -> r.getPos().distanceTo(curBallPos)))
					.orElseThrow(IllegalStateException::new);
			passReceiver = closestToBall.getBotID();
			return closestToBall;
		}
		return current;
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		super.doUpdate(frame);
		
		if (stateMachine.getCurrentState() != idleState && getRoles().size() != 2)
		{
			stateMachine.triggerEvent(EEvent.STOP);
		}
		stateMachine.update();
		
		List<IDrawableShape> shapes = frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.TEST_REDIRECT);
		shapes.add(new DrawableLine(Line.fromPoints(ballPos, receiverPos), Color.BLUE));
		shapes.add(new DrawableLine(Line.fromPoints(ballPos, passTarget), Color.GREEN));
		shapes.add(new DrawableLine(Line.fromPoints(receiverPos, redirectTarget), Color.MAGENTA));
	}
	
	
	private enum EEvent implements IEvent
	{
		PLACE_BALL,
		BALL_PLACED,
		STOP,
		START,
		BALL_STOPPED,
		PASS_EXECUTED,
		STOP_BALL
	}
	
	private class BallPlacementState extends AState
	{
		@Override
		public void doEntryActions()
		{
			ARole closestToBall = getClosestToBall();
			if (closestToBall instanceof RedirectTestRole)
			{
				closestToBall = switchRoles(closestToBall, new PrimaryBallPlacementRole(ballPos));
			}
			for (ARole role : new ArrayList<>(getRoles()))
			{
				if (role == closestToBall)
				{
					continue;
				}
				if (role instanceof PrimaryBallPlacementRole)
				{
					role = switchRoles(role, new RedirectTestRole());
				}
				RedirectTestRole redirectTestRole = (RedirectTestRole) role;
				redirectTestRole.changeToWait();
				redirectTestRole.setDesiredDestination(receiverPos);
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
			if (getBall().getPos().distanceTo(ballPos) > 100)
			{
				return true;
			}
			boolean ballPlacementActive = closestToBall.getType() == ERole.PRIMARY_BALL_PLACEMENT;
			return ballPlacementActive && !((PrimaryBallPlacementRole) closestToBall).isBallCleared();
		}
	}
	
	private class ExecutionState extends AState
	{
		private boolean ballMoved;
		
		
		@Override
		public void doEntryActions()
		{
			for (ARole role : new ArrayList<>(getRoles()))
			{
				if (role instanceof PrimaryBallPlacementRole)
				{
					switchRoles(role, new RedirectTestRole());
				}
			}
			ARole passingRole = getClosestToBall();
			
			for (ARole role : getRoles())
			{
				RedirectTestRole redirectTestRole = (RedirectTestRole) role;
				if (redirectTestRole == passingRole)
				{
					redirectTestRole.setTarget(new DynamicPosition(passTarget));
					redirectTestRole.setKickWithChill(true);
					redirectTestRole.changeToPass();
				} else
				{
					redirectTestRole.setTarget(redirectTarget);
					if (receive)
					{
						redirectTestRole.changeToReceive();
					} else
					{
						redirectTestRole.changeToRedirect();
					}
				}
			}
			
			ballMoved = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getPos().distanceTo(ballPos) > 500)
			{
				ballMoved = true;
			}
			if (ballMoved && (getBall().getVel().getLength2() < 1.0
					|| getBall().getTrajectory().getTravelLine().isPointInFront(ballPos)))
			{
				stateMachine.triggerEvent(EEvent.PASS_EXECUTED);
			}
		}
	}
	
	private class IdleState extends AState
	{
		@Override
		public void doUpdate()
		{
			if (getRoles().size() == 2)
			{
				stateMachine.triggerEvent(EEvent.START);
			}
		}
	}
}