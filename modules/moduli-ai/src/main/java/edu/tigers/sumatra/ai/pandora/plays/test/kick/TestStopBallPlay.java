/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.offense.dribble.EDribblingCondition;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states.ProtectState;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.KickTestRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.TransitionableState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.List;


/**
 * Test play with one or two robots that tests the capabilities of the Attacker to stop a rolling ball.
 * One robot kicks the ball and this or an optional second bot stops the ball after the kick.
 * This usually tests the ApproachAndStopSkill effectively, but this depends on the starting positions.
 */
@Log4j2
public class TestStopBallPlay extends ABallPreparationPlay
{
	@Setter
	private IVector2 kickStartPos;
	@Setter
	private IVector2 kickTargetPos;
	@Setter
	private Pose approachStartPos;
	@Setter
	private double kickSpeed;


	public TestStopBallPlay()
	{
		super(EPlay.TEST_STOP_BALL);

		var prepareState = new PrepareState();
		var kickState = new KickState();
		var stopBallState = new StopBallState();

		prepareState.addTransition(prepareState::isDone, kickState);
		kickState.addTransition(kickState::isDone, stopBallState);
		setExecutionState(prepareState);
	}


	@Override
	protected boolean ready()
	{
		return getRoles().size() == 1 || getRoles().size() == 2;
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		setBallTargetPos(kickStartPos);

		List<IDrawableShape> shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_PASSING);
		shapes.add(
				new DrawableBotShape(
						approachStartPos.getPos(),
						approachStartPos.getOrientation(),
						100,
						Geometry.getOpponentCenter2DribblerDist()
				)
		);
		shapes.add(new DrawableArrow(kickStartPos, kickTargetPos.subtractNew(kickStartPos), Color.magenta));
	}


	private record DataPoint(long timestamp, IVector2 position, double ballVel)
	{
		static DataPoint create(WorldFrame worldFrame)
		{
			return new DataPoint(
					worldFrame.getTimestamp(), worldFrame.getBall().getPos(), worldFrame.getBall().getVel().getLength());
		}


		String diff(DataPoint other)
		{
			return String.format(
					"%.2f s, %.0f mm, %.2f m/s",
					Math.abs(timestamp - other.timestamp) / 1e9,
					position.distanceTo(other.position),
					ballVel
			);
		}
	}

	private class PrepareState extends TransitionableState
	{
		private final TimestampTimer timer = new TimestampTimer(0.5);


		public PrepareState()
		{
			super(stateMachine::changeState);
		}


		@Override
		public void onInit()
		{
			var shooterRole = reassignRole(
					getClosestToBall(),
					MoveRole.class,
					MoveRole::new
			);
			shooterRole.updateDestination(LineMath.stepAlongLine(
					kickStartPos,
					kickTargetPos,
					-200
			));
			shooterRole.updateLookAtTarget(getBall());

			allRolesExcept(shooterRole).stream().findFirst().ifPresent(role -> {
				MoveRole approachRole = reassignRole(
						role,
						MoveRole.class, MoveRole::new
				);
				approachRole.updateDestination(approachStartPos.getPos());
				approachRole.updateTargetAngle(approachStartPos.getOrientation());
			});
		}


		public boolean isDone()
		{
			return timer.isTimeUpWithCondition(
					getWorldFrame().getTimestamp(),
					() -> findRoles(MoveRole.class).stream().allMatch(MoveRole::isDestinationReached)
			);
		}
	}

	private class KickState extends TransitionableState
	{
		public KickState()
		{
			super(stateMachine::changeState);
		}


		@Override
		protected void onInit()
		{
			reassignRole(
					getClosestToBall(),
					KickTestRole.class,
					() -> new KickTestRole(kickTargetPos, EKickerDevice.STRAIGHT, kickSpeed)
			);
		}


		public boolean isDone()
		{
			return getBall().getVel().getLength2() > 0.3;
		}
	}

	private class StopBallState extends TransitionableState
	{
		AttackerRole attackerRole;
		private DataPoint dataStart;
		private DataPoint dataContact;
		private DataPoint dataEnd;


		public StopBallState()
		{
			super(stateMachine::changeState);
		}


		@Override
		protected void onInit()
		{
			dataStart = DataPoint.create(getWorldFrame());
			dataContact = null;
			dataEnd = null;

			if (getRoles().size() > 1)
			{
				reassignRole(findRole(MoveRole.class), AttackerRole.class, AttackerRole::new);
				reassignRole(findRole(KickTestRole.class), MoveRole.class, MoveRole::new);
			} else
			{
				reassignRole(findRole(KickTestRole.class), AttackerRole.class, AttackerRole::new);
			}

			attackerRole = findRole(AttackerRole.class);
			attackerRole.setAction(OffensiveAction.buildProtect(new DribbleToPos(
					Vector2.fromPoints(kickStartPos, kickTargetPos).getNormalVector().scaleTo(500).add(kickTargetPos),
					kickTargetPos,
					EDribblingCondition.DEFAULT,
					null
			)));
		}


		@Override
		protected void onUpdate()
		{
			if (dataStart.position.distanceTo(getBall().getPos()) < 1000)
			{
				return;
			}

			if (attackerRole.getBot().getBallContact().hasContact() && dataContact == null)
			{
				dataContact = DataPoint.create(getWorldFrame());
			}

			if (attackerRole.getCurrentState().getClass() == ProtectState.class)
			{
				stopped();
			}
		}


		private void stopped()
		{
			if (dataContact == null)
			{
				dataContact = DataPoint.create(getWorldFrame());
			}
			dataEnd = DataPoint.create(getWorldFrame());
			log.info(
					"Total " + dataEnd.diff(dataStart) + " | Contact " + dataContact.diff(dataStart) + " | Stop "
							+ dataEnd.diff(dataContact));
			stopExecution();
		}
	}
}
