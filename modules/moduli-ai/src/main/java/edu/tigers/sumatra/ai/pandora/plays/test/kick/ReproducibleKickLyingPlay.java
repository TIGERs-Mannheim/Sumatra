/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.KickTestRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.List;


/**
 * Kick the ball reproducible from a dedicated position towards a target, starting from an offset behind the ball.
 */
@Log4j2
public class ReproducibleKickLyingPlay extends ABallPreparationPlay
{
	private final IVector2 ballTargetPos;
	private final IVector2 botOffset;
	private final IVector2 kickTarget;
	private final double kickSpeed;


	public ReproducibleKickLyingPlay(
			IVector2 ballTargetPos,
			IVector2 botOffset,
			IVector2 kickTarget,
			double kickSpeed
	)
	{
		super(EPlay.REPRODUCIBLE_KICK_LYING_BALL);
		this.ballTargetPos = ballTargetPos;
		this.botOffset = botOffset;
		this.kickTarget = kickTarget;
		this.kickSpeed = kickSpeed;

		setExecutionState(new PreparationState());
		stateMachine.addTransition(null, EKickEvent.PREPARED, new ExecutionState());
	}


	@Override
	protected ARole onAddRole()
	{
		return new MoveRole();
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		setBallTargetPos(ballTargetPos);

		List<IDrawableShape> shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_KICK);
		shapes.add(new DrawableLine(Line.fromPoints(ballTargetPos, kickTarget), Color.GREEN));
		shapes.add(new DrawableCircle(Circle.createCircle(getBotDest(), Geometry.getBotRadius()), Color.GREEN));
	}


	private IVector2 getBotDest()
	{
		return getBall().getPos().addNew(
				getBall().getPos().subtractNew(kickTarget).scaleTo(botOffset.getLength2()).turn(botOffset.getAngle()));
	}


	@Override
	protected void handleNonPlacingRole(ARole role)
	{
		throw new IllegalStateException("Should not have any non placing roles");
	}


	@Override
	protected boolean ready()
	{
		return getRoles().size() == 1;
	}


	private class PreparationState extends AState
	{
		MoveRole move;
		TimestampTimer timer = new TimestampTimer(0.5);


		@Override
		public void doEntryActions()
		{
			move = new MoveRole();
			move.updateDestination(getBotDest());
			move.updateLookAtTarget(getBall());
			move.getMoveCon().setPenaltyAreaTheirObstacle(false);
			move.getMoveCon().setPenaltyAreaOurObstacle(false);
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
		private double closestDistToTarget;


		@Override
		public void doEntryActions()
		{
			KickTestRole kick = new KickTestRole(kickTarget, EKickerDevice.STRAIGHT, kickSpeed);
			switchRoles(getRoles().get(0), kick);
			ballMoved = false;
			closestDistToTarget = Double.MAX_VALUE;
		}


		@Override
		public void doUpdate()
		{
			double dist2Target = kickTarget.distanceTo(getBall().getPos());
			if (dist2Target < closestDistToTarget)
			{
				closestDistToTarget = dist2Target;
			}

			if (!ballMoved && getBall().getPos().distanceTo(ballTargetPos) > 500)
			{
				ballMoved = true;
				((KickTestRole) getRoles().get(0)).switchToWait();
			}
			if (ballMoved && (getBall().getVel().getLength2() < 1.0
					|| getBall().getTrajectory().getTravelLine().isPointInFront(ballTargetPos)))
			{
				log.info("Closest dist to target: {}", String.format("%.0f", closestDistToTarget));
				stateMachine.triggerEvent(EEvent.EXECUTED);
			}
		}
	}

	private enum EKickEvent implements IEvent
	{
		PREPARED
	}
}
