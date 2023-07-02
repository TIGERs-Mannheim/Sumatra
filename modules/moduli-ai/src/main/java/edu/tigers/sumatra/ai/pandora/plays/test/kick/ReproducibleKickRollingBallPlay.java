/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.KickTestRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;

import java.awt.Color;
import java.util.List;


/**
 * Kick a rolling ball reproducible.
 * The ball is kicked slowly to simulate a rolling ball and to achieve a fairly reproducible velocity.
 */
public class ReproducibleKickRollingBallPlay extends ABallPreparationPlay
{
	private final IVector2 ballTargetPos;
	private final IVector2 passTarget;
	private final IVector2 botTargetPos;
	private final double passSpeed;

	private IVector2 passerTargetPos;


	public ReproducibleKickRollingBallPlay(
			IVector2 ballTargetPos,
			IVector2 passTarget,
			IVector2 botTargetPos,
			double passSpeed
	)
	{
		super(EPlay.REPRODUCIBLE_KICK_ROLLING_BALL);
		this.ballTargetPos = ballTargetPos;
		this.passTarget = passTarget;
		this.botTargetPos = botTargetPos;
		this.passSpeed = passSpeed;

		setUseAssistant(true);

		setExecutionState(new PreparationState());
		stateMachine.addTransition(null, EKickEvent.PREPARED, new ExecutionState());
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		setBallTargetPos(ballTargetPos);
		passerTargetPos = LineMath.stepAlongLine(ballTargetPos, passTarget, -(Geometry.getBotRadius() + 150));

		List<IDrawableShape> shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_KICK);
		shapes.add(new DrawableLine(ballTargetPos, passTarget, Color.ORANGE));
		shapes.add(new DrawableCircle(Circle.createCircle(passerTargetPos, Geometry.getBotRadius()), Color.ORANGE));
		shapes.add(new DrawableCircle(Circle.createCircle(botTargetPos, Geometry.getBotRadius()), Color.CYAN));
	}


	@Override
	protected void handleNonPlacingRole(final ARole role)
	{
		var moveRole = reassignRole(role, MoveRole.class, MoveRole::new);
		moveRole.updateDestination(botTargetPos);
		moveRole.updateLookAtTarget(passTarget);
	}


	@Override
	protected boolean ready()
	{
		return getRoles().size() == 2;
	}


	private enum EKickEvent implements IEvent
	{
		PREPARED
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
			passer.updateDestination(passerTargetPos);
			passer.updateLookAtTarget(passTarget);

			kicker = new MoveRole();
			kicker.updateDestination(botTargetPos);
			kicker.updateLookAtTarget(passTarget);

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

			passer = new KickTestRole(passTarget, EKickerDevice.STRAIGHT, passSpeed);

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
}
