/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.ABallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryBallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryBallPlacementRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
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
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class AutoKickSamplePlay extends APlay
{
	private static final Logger log = Logger.getLogger(AutoKickSamplePlay.class.getName());
	
	@Configurable
	private static IVector2 ballPos = Vector2.fromXY(-2800, -1800);
	
	@Configurable
	private static IVector2 target = Vector2.fromXY(0, 0);
	
	@Configurable(defValue = "1.0")
	private static double kickDurationMin = 1.0;
	
	@Configurable(defValue = "8.0")
	private static double kickDurationMax = 8.0;
	
	@Configurable(defValue = "0.5")
	private static double kickDurationStep = 0.5;
	
	@Configurable(defValue = "STRAIGHT")
	private static EKickerDevice kickerDevice = EKickerDevice.STRAIGHT;
	
	@Configurable(defValue = "500.0;500.0")
	private static Double[] idlePositionsX = new Double[] { 500.0, 500.0 };
	@Configurable(defValue = "500.0;700.0")
	private static Double[] idlePositionsY = new Double[] { 500.0, 700.0 };
	
	private final IStateMachine<IState> stateMachine = new StateMachine<>();
	private final IdleState idleState = new IdleState();
	private BotID primaryBot = null;
	private double kickDuration;
	
	
	public AutoKickSamplePlay()
	{
		super(EPlay.AUTO_KICK_SAMPLE);
		BallPlacementState ballPlacementState = new BallPlacementState();
		ExecutionState executionState = new ExecutionState();
		
		stateMachine.setInitialState(idleState);
		
		stateMachine.addTransition(idleState, EEvent.START, ballPlacementState);
		stateMachine.addTransition(ballPlacementState, EEvent.BALL_PLACED, executionState);
		stateMachine.addTransition(executionState, EEvent.PASS_EXECUTED, ballPlacementState);
		stateMachine.addTransition(null, EEvent.STOP, idleState);
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		super.doUpdate(frame);
		
		if (stateMachine.getCurrentState() != idleState && getRoles().isEmpty())
		{
			stateMachine.triggerEvent(EEvent.STOP);
		}
		stateMachine.update();
		
		List<IDrawableShape> shapes = frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.TEST_AUTO_SAMPLING);
		shapes.add(new DrawableLine(Line.fromPoints(ballPos, target), Color.BLUE));
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
		ARole current = getRoles().stream().filter(a -> a.getBotID() == primaryBot).findFirst().orElse(null);
		if (current == null)
		{
			IVector2 curBallPos = getBall().getPos();
			ARole closestToBall = getRoles().stream()
					.min(Comparator.comparing(r -> r.getPos().distanceTo(curBallPos)))
					.orElseThrow(IllegalStateException::new);
			primaryBot = closestToBall.getBotID();
			return closestToBall;
		}
		return current;
	}
	
	
	private void moveOtherBotsToIdlePos()
	{
		int iIdlePos = 0;
		for (ARole role : getRoles())
		{
			if (role != getClosestToBall() && !(role instanceof RedirectTestRole))
			{
				switchRoles(role, new RedirectTestRole());
			}
		}
		
		for (ARole role : getRoles())
		{
			if (role == getClosestToBall() || !(role instanceof RedirectTestRole))
			{
				continue;
			}
			RedirectTestRole redirectTestRole = (RedirectTestRole) role;
			redirectTestRole.changeToReceive();
			
			if (iIdlePos < idlePositionsX.length && iIdlePos < idlePositionsY.length)
			{
				IVector2 p = Vector2.fromXY(idlePositionsX[iIdlePos], idlePositionsY[iIdlePos]);
				redirectTestRole.setDesiredDestination(p);
				iIdlePos++;
			}
		}
	}
	
	private enum EEvent implements IEvent
	{
		BALL_PLACED,
		PASS_EXECUTED,
		START,
		STOP,
	}
	
	private class BallPlacementState extends AState
	{
		@Override
		public void doUpdate()
		{
			ARole closestToBall = getClosestToBall();
			if (closestToBall instanceof RedirectTestRole)
			{
				closestToBall = switchRoles(closestToBall, new PrimaryBallPlacementRole(ballPos));
			}
			if (getBall().getPos().distanceTo(ballPos) > 1000)
			{
				activateSecondaryRole(closestToBall);
			} else
			{
				moveOtherBotsToIdlePos();
				
				getRoles().stream()
						.filter(r -> r instanceof ABallPlacementRole)
						.map(r -> (ABallPlacementRole) r)
						.forEach(r -> r.setHasCompanion(false));
			}
			
			if (!isBallPlacementRequired())
			{
				stateMachine.triggerEvent(EEvent.BALL_PLACED);
			}
		}
		
		
		private void activateSecondaryRole(final ARole closestToBall)
		{
			for (ARole role : new ArrayList<>(getRoles()))
			{
				if (role == closestToBall)
				{
					continue;
				}
				if (role instanceof RedirectTestRole)
				{
					switchRoles(role, new SecondaryBallPlacementRole(ballPos));
				}
			}
			getRoles().stream()
					.filter(r -> r instanceof ABallPlacementRole)
					.map(r -> (ABallPlacementRole) r)
					.forEach(r -> r.setHasCompanion(getRoles().size() > 1));
		}
		
		
		private boolean isBallPlacementRequired()
		{
			ARole closestToBall = getClosestToBall();
			if (getBall().getPos().distanceTo(ballPos) > 100)
			{
				return true;
			}
			boolean ballPlacementActive = closestToBall.getType() == ERole.PRIMARY_BALL_PLACEMENT;
			return ballPlacementActive && ((PrimaryBallPlacementRole) closestToBall).isBallStillHandled();
		}
	}
	
	
	private class ExecutionState extends AState
	{
		private boolean ballMoved;
		private IVector2 initBallPos;
		private TimestampTimer timer = new TimestampTimer(5.0);
		
		
		@Override
		public void doEntryActions()
		{
			for (ARole role : new ArrayList<>(getRoles()))
			{
				if (!(role instanceof RedirectTestRole))
				{
					switchRoles(role, new RedirectTestRole());
				}
			}
			RedirectTestRole passingRole = (RedirectTestRole) getClosestToBall();
			passingRole.setTarget(new DynamicPosition(target));
			passingRole.setKickMode(RedirectTestRole.EKickMode.SAMPLE);
			passingRole.changeToPass();
			passingRole.setDesiredPassKickSpeed(kickDuration);
			passingRole.setKickerDevice(kickerDevice);
			
			moveOtherBotsToIdlePos();
			
			ballMoved = false;
			initBallPos = getBall().getPos();
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getPos().distanceTo(initBallPos) > 100)
			{
				ballMoved = true;
			} else
			{
				timer.reset();
			}
			timer.update(getWorldFrame().getTimestamp());
			if (ballMoved &&
					(timer.isTimeUp(getWorldFrame().getTimestamp())
							|| getBall().getVel().getLength2() < 0.1))
			{
				stateMachine.triggerEvent(EEvent.PASS_EXECUTED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			kickDuration += kickDurationStep;
			if (kickDuration > kickDurationMax)
			{
				kickDuration = kickDurationMin;
			}
			log.info("Continue with kickDuration=" + kickDuration);
		}
	}
	
	private class IdleState extends AState
	{
		@Override
		public void doUpdate()
		{
			if (!getRoles().isEmpty())
			{
				kickDuration = kickDurationMin;
				stateMachine.triggerEvent(EEvent.START);
				log.info("Start with kickDuration=" + kickDuration);
			}
		}
	}
}
