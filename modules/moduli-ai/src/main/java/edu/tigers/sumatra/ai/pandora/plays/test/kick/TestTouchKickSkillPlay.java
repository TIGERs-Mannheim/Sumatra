/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.KickTestRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Test the precision of the TouchKickSkill in situations that require sideways movement and rotations
 * For best effectiveness use two robots
 */
public class TestTouchKickSkillPlay extends ABallPreparationPlay
{
	@Configurable(defValue = "1000.0", comment = "[mm] distance between target and receiver")
	private static double distanceBetweenTargetAndReceiver = 1000.0;

	private final IVector2 startPos;
	private final IVector2 targetPos;
	private final double kickVelocity;
	private final List<SampleInput> inputs;
	private final List<Sample> samples;

	private BotID shooterID;
	private BotID receiverID;


	public TestTouchKickSkillPlay(
			IVector2 startPos,
			IVector2 targetPos,
			int numIterations,
			int numSamplesPerIteration,
			double kickVelocity,
			double switchingVelocity,
			double rotationOffset
	)
	{
		super(EPlay.TEST_TOUCH_KICK_SKILL);

		this.startPos = startPos;
		this.targetPos = targetPos;
		this.kickVelocity = kickVelocity;


		List<SampleInput> newInputs = new ArrayList<>();

		for (int i = 0; i < numIterations; i++)
		{
			for (int s = 0; s < numSamplesPerIteration; s++)
			{
				var angle = s * AngleMath.PI_TWO / numSamplesPerIteration;
				newInputs.add(SampleInput.create(angle, switchingVelocity, AngleMath.deg2rad(rotationOffset)));
			}
		}

		this.inputs = Collections.unmodifiableList(newInputs);
		this.samples = new ArrayList<>();

		setUseAssistant(true);
		setExecutionState(new PreparationState());
		stateMachine.addTransition(null, EKickEvent.PREPARED, new StartMovementState());
		stateMachine.addTransition(null, EKickEvent.MOVING, new KickState());
		stateMachine.addTransition(null, EKickEvent.KICKED, new MeasureState());
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		setBallTargetPos(startPos);


		var color = ready() ? Color.GREEN : Color.RED;

		List<IDrawableShape> shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_PASSING);
		shapes.add(new DrawableArrow(startPos, Vector2.fromPoints(startPos, targetPos), color));

		var distances = samples.stream()
				.map(sample -> targetPos.distanceTo(sample.closestPoint))
				.toList();
		var avg = StatisticsMath.mean(distances);
		var std = StatisticsMath.std(distances);
		shapes.add(new DrawableAnnotation(targetPos, String.format("%.2f +- %.2f", avg, std), color));
	}


	@Override
	protected boolean ready()
	{
		return !getRoles().isEmpty() && getCurrentSampleInput().isPresent();
	}


	private Optional<SampleInput> getCurrentSampleInput()
	{
		if (samples.size() >= inputs.size())
		{
			return Optional.empty();
		}
		return Optional.of(inputs.get(samples.size()));
	}


	private enum EKickEvent implements IEvent
	{
		PREPARED,
		MOVING,
		KICKED
	}

	private record SampleInput(double movementDirection, double switchingVelocity, double lookAtAngle)
	{
		static SampleInput create(double movementDirection, double switchingVelocity, double rotationOffset)
		{
			return new SampleInput(
					AngleMath.normalizeAngle(movementDirection),
					switchingVelocity,
					AngleMath.normalizeAngle(movementDirection + rotationOffset)
			);
		}
	}

	private record Sample(SampleInput input, IVector2 closestPoint)
	{
	}

	private class PreparationState extends AState
	{
		TimestampTimer timer = new TimestampTimer(0.5);

		MoveRole shooter;
		MoveRole receiver;


		@Override
		public void doEntryActions()
		{
			timer.reset();
			var input = getCurrentSampleInput().orElseThrow();
			shooter = new MoveRole();
			switchRoles(getClosestToBall(), shooter);
			shooter.updateDestination(getBall().getPos().addNew(Vector2.fromAngleLength(input.movementDirection, -200)));
			shooter.updateTargetAngle(input.lookAtAngle);
			shooter.getMoveCon().physicalObstaclesOnly();
			shooterID = shooter.getBotID();

			if (getRoles().size() == 1)
			{
				receiver = null;
				receiverID = BotID.noBot();
			} else
			{
				receiver = new MoveRole();
				switchRoles(allRolesExcept(shooter).getFirst(), receiver);
				receiver.updateDestination(targetPos.addNew(
						Vector2.fromPoints(startPos, targetPos).scaleTo(distanceBetweenTargetAndReceiver)));
				receiver.updateLookAtTarget(getBall());
				receiver.getMoveCon().physicalObstaclesOnly();
				receiver.getMoveCon().setBallObstacle(false);
				receiverID = receiver.getBotID();
			}
			timer.reset();
		}


		@Override
		public void doUpdate()
		{
			if (shooter.isDestinationReached() && (receiver == null || receiver.isDestinationReached()))
			{
				timer.update(getWorldFrame().getTimestamp());
			}
			if (timer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				stateMachine.triggerEvent(EKickEvent.PREPARED);
			}
		}
	}

	private class StartMovementState extends AState
	{
		TimestampTimer timer = new TimestampTimer(0.5);

		MoveRole shooter;
		SampleInput input;


		@Override
		public void doEntryActions()
		{
			timer.reset();
			input = getCurrentSampleInput().orElseThrow();
			shooter = findRoles(MoveRole.class).stream()
					.filter(role -> role.getBotID() == shooterID)
					.findAny().orElseThrow();
			shooter.updateDestination(getBall().getPos().addNew(Vector2.fromAngleLength(input.movementDirection, 1000)));
			shooter.getMoveCon().physicalObstaclesOnly();
			shooter.getMoveCon().setBallObstacle(false);
			shooter.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
			shooter.setVelMax(input.switchingVelocity);
		}


		@Override
		public void doUpdate()
		{
			if (shooter.getBot().getVel().getLength() > input.switchingVelocity - 0.1 && shooter.getBot().getBallContact()
					.hasContact())
			{
				timer.update(getWorldFrame().getTimestamp());
			}
			if (timer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				stateMachine.triggerEvent(EKickEvent.MOVING);
			}
		}
	}

	private class KickState extends AState
	{
		KickTestRole shooter;


		@Override
		public void doEntryActions()
		{
			var oldShooter = findRoles(MoveRole.class).stream()
					.filter(role -> role.getBotID() == shooterID)
					.findAny().orElseThrow();
			shooter = new KickTestRole(targetPos, EKickerDevice.STRAIGHT, kickVelocity);
			switchRoles(oldShooter, shooter);
		}


		@Override
		public void doUpdate()
		{
			if (shooter.getPos().distanceTo(getBall().getPos()) > 500)
			{
				stateMachine.triggerEvent(EKickEvent.KICKED);
			}
		}
	}

	private class MeasureState extends AState
	{
		IVector2 initialBallPos;
		IVector2 closestPoint;
		double closestDistance;
		MoveRole receiver;


		@Override
		public void doEntryActions()
		{
			initialBallPos = getBall().getPos();
			var newShooter = new MoveRole();
			var oldShooter = findRoles(KickTestRole.class).stream()
					.filter(role -> role.getBotID() == shooterID)
					.findAny()
					.orElseThrow();
			switchRoles(oldShooter, newShooter);
			newShooter.updateDestination(startPos);
			newShooter.updateLookAtTarget(getBall());
			newShooter.getMoveCon().physicalObstaclesOnly();
			newShooter.getMoveCon().setBallObstacle(false);

			receiver = findRoles(MoveRole.class).stream()
					.filter(role -> role.getBotID() == receiverID)
					.findAny()
					.orElse(null);

			closestPoint = Vector2.zero();
			closestDistance = Double.POSITIVE_INFINITY;
		}


		@Override
		public void doUpdate()
		{
			var ballPos = getBall().getPos();
			var travelLine = getBall().getTrajectory().getTravelLine();
			var currentDistance = ballPos.distanceTo(targetPos);
			if (currentDistance < closestDistance)
			{
				closestPoint = ballPos;
				closestDistance = currentDistance;
			} else if (!travelLine.isPointInFront(targetPos))
			{
				samples.add(new Sample(getCurrentSampleInput().orElseThrow(), closestPoint));
				stopExecution();
			}

			if (receiver != null)
			{
				var distance = initialBallPos.distanceTo(targetPos) + distanceBetweenTargetAndReceiver - ballPos.distanceTo(
						initialBallPos);
				receiver.updateDestination(ballPos.addNew(travelLine.directionVector().scaleToNew(distance)));
			}
		}
	}
}
