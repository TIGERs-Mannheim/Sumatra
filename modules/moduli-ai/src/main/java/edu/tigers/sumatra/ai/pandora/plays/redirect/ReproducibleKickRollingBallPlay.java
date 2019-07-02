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
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.KickTestRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.ABallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryBallPlacementRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class ReproducibleKickRollingBallPlay extends ABallPreparationPlay
{
	private static final int NUM_ROLES = 2;
	
	@Configurable(defValue = "-700.0;-700.0")
	private static IVector2 ballTargetPos = Vector2.zero();
	@Configurable(defValue = "800.0;0.0")
	private static IVector2 passTarget = Vector2.zero();
	@Configurable(defValue = "-700.0;0.0")
	private static IVector2 botTargetPos = Vector2.zero();
	@Configurable(defValue = "2.0")
	private static double passSpeed = 0.0;
	
	private IVector2 passerTargetPos;
	
	
	public ReproducibleKickRollingBallPlay()
	{
		super(EPlay.REPRODUCIBLE_KICK_ROLLING_BALL, NUM_ROLES);
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
		passerTargetPos = LineMath.stepAlongLine(ballTargetPos, passTarget, -(Geometry.getBotRadius() + 150));
		
		List<IDrawableShape> shapes = frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.TEST_REPRODUCIBLE);
		shapes.add(new DrawableLine(Line.fromPoints(ballTargetPos, passTarget), Color.ORANGE));
		shapes.add(new DrawableCircle(Circle.createCircle(passerTargetPos, Geometry.getBotRadius()), Color.ORANGE));
		shapes.add(new DrawableCircle(Circle.createCircle(botTargetPos, Geometry.getBotRadius()), Color.CYAN));
	}
	
	
	@Override
	protected void handleNonPlacingRole(ARole role)
	{
		if (!(role instanceof SecondaryBallPlacementRole))
		{
			switchRoles(role, new SecondaryBallPlacementRole(ballTargetPos));
		}
		getRoles().stream().map(r -> (ABallPlacementRole) r).forEach(r -> r.setHasCompanion(getRoles().size() > 1));
	}
	
	private class PreparationState extends AState
	{
		MoveRole passer;
		MoveRole kicker;
		TimestampTimer timer = new TimestampTimer(0.5);
		
		
		@Override
		public void doEntryActions()
		{
			passer = new MoveRole();
			passer.getMoveCon().updateDestination(passerTargetPos);
			passer.getMoveCon().updateLookAtTarget(passTarget);
			
			kicker = new MoveRole();
			kicker.getMoveCon().updateDestination(botTargetPos);
			kicker.getMoveCon().updateLookAtTarget(passTarget);
			
			ARole closest = getClosestToBall();
			ARole otherRole = getRoles().stream().filter(r -> r != closest).findFirst()
					.orElseThrow(IllegalStateException::new);
			
			switchRoles(closest, passer);
			switchRoles(otherRole, kicker);
			timer.reset();
		}
		
		
		@Override
		public void doUpdate()
		{
			if (passer.isDestinationReached() && kicker.isDestinationReached())
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
		boolean ballMoved;
		IVector2 ballMoveDir;
		TimestampTimer timer = new TimestampTimer(1.0);
		KickTestRole passer;
		
		
		@Override
		public void doEntryActions()
		{
			ARole closest = getClosestToBall();
			
			passer = new KickTestRole(new DynamicPosition(passTarget), EKickerDevice.STRAIGHT, passSpeed);
			
			switchRoles(closest, passer);
			
			ballMoved = false;
			ballMoveDir = null;
			timer.reset();
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!ballMoved && getBall().getPos().distanceTo(ballTargetPos) > 100)
			{
				ballMoved = true;
				ballMoveDir = getBall().getPos().subtractNew(ballTargetPos).normalize();
				passer.switchToWait();
				
				ARole otherRole = getRoles().stream().filter(r -> r != passer).findFirst()
						.orElseThrow(IllegalStateException::new);
				switchRoles(otherRole, new AttackerRole());
			}
			if (ballMoved)
			{
				if (getBall().getVel().getLength2() < 0.1
						|| getBall().getTrajectory().getTravelLine().isPointInFront(ballTargetPos))
				{
					stateMachine.triggerEvent(EEvent.EXECUTED);
				} else
				{
					IVector2 passDir = passTarget.subtractNew(ballTargetPos);
					double moveAngleDiff = getBall().getVel().angleToAbs(passDir).orElse(0.0);
					if (moveAngleDiff > 0.3)
					{
						timer.update(getWorldFrame().getTimestamp());
					}
				}
			}
			if (timer.isTimeUp(getWorldFrame().getTimestamp()))
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
