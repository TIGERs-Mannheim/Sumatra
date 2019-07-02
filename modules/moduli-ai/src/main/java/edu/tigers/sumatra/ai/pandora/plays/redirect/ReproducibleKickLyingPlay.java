/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.awt.Color;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.test.KickTestRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class ReproducibleKickLyingPlay extends ABallPreparationPlay
{
	private static final int NUM_ROLES = 1;
	
	@Configurable(defValue = "0.0,0.0")
	private static IVector2 ballTargetPos = Vector2.zero();
	@Configurable(defValue = "-500.0,0.0")
	private static IVector2 botTargetPos = Vector2.zero();
	@Configurable(defValue = "500.0;0.0")
	private static IVector2 kickTarget = Vector2.zero();
	@Configurable(defValue = "3.0")
	private static double kickSpeed = 3.0;
	
	
	public ReproducibleKickLyingPlay()
	{
		super(EPlay.REPRODUCIBLE_KICK_LYING_BALL, NUM_ROLES);
		setExecutionState(new PreparationState());
		stateMachine.addTransition(null, EKickEvent.PREPARED, new ExecutionState());
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new MoveRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		super.doUpdate(frame);
		setBallTargetPos(ballTargetPos);
		
		List<IDrawableShape> shapes = frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.TEST_REPRODUCIBLE);
		shapes.add(new DrawableLine(Line.fromPoints(ballTargetPos, kickTarget), Color.GREEN));
		shapes.add(new DrawableCircle(Circle.createCircle(botTargetPos, Geometry.getBotRadius()), Color.GREEN));
	}
	
	
	@Override
	protected void handleNonPlacingRole(ARole role)
	{
		throw new IllegalStateException("Should not have any non placing roles");
	}
	
	private class PreparationState extends AState
	{
		MoveRole move;
		TimestampTimer timer = new TimestampTimer(0.5);
		
		
		@Override
		public void doEntryActions()
		{
			move = new MoveRole();
			move.getMoveCon().updateDestination(botTargetPos);
			move.getMoveCon().updateLookAtTarget(getBall());
			switchRoles(getRoles().get(0), move);
			timer.reset();
		}
		
		
		@Override
		public void doUpdate()
		{
			if (move.isDestinationReached())
			{
				timer.update(getWorldFrame().getTimestamp());
			}
			if (timer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				stateMachine.triggerEvent(EKickEvent.PREPARED);
			}
		}
	}
	
	private class ExecutionState extends AState
	{
		private boolean ballMoved;
		
		
		@Override
		public void doEntryActions()
		{
			KickTestRole kick = new KickTestRole(new DynamicPosition(kickTarget), EKickerDevice.STRAIGHT, kickSpeed);
			switchRoles(getRoles().get(0), kick);
			ballMoved = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!ballMoved && getBall().getPos().distanceTo(ballTargetPos) > 500)
			{
				ballMoved = true;
				((KickTestRole) getRoles().get(0)).switchToWait();
			}
			if (ballMoved && (getBall().getVel().getLength2() < 1.0
					|| getBall().getTrajectory().getTravelLine().isPointInFront(ballTargetPos)))
			{
				stateMachine.triggerEvent(EEvent.EXECUTED);
			}
		}
	}
	
	private enum EKickEvent implements IEvent
	{
		PREPARED
	}
}
