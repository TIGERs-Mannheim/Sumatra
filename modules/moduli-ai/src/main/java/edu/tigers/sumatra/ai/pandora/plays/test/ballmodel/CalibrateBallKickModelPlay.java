package edu.tigers.sumatra.ai.pandora.plays.test.ballmodel;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ABallPreparationPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.AKickSamplerRole.EKickMode;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.StraightChipKickSamplerRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.TransitionableState;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.NotImplementedException;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;


@Log4j2
public class CalibrateBallKickModelPlay extends ABallPreparationPlay
{
	@Configurable(defValue = "500", comment = "The tolerance for the ball", unit = EConfigUnit.DISTANCE_MM)
	private static double placementTolerance = 500;

	@Setter
	private EBallModelCalibrationKickMode mode;
	@Setter
	private EFieldSide side;
	@Setter
	private double marginToBorder;
	@Setter
	private EKickMode kickMode;
	@Setter
	private double minDurationMs;
	@Setter
	private double maxDurationMs;
	@Setter
	private int numSamples;

	private List<Double> shootDurations;

	private EKickerDevice kickerDevice;
	private IVector2 placementPos;
	private IVector2 kickTarget;
	private IVector2 secondBotPos;

	private int currentDurationIndex;


	public CalibrateBallKickModelPlay()
	{
		super(EPlay.CALIBRATE_BALL_KICK_MODEL);

		var moveIntoPosState = new MoveIntoPosState();
		var executionState = new ExecutionState();
		moveIntoPosState.addTransition("allRolesFinished", moveIntoPosState::allRolesFinished, executionState);
		setExecutionState(moveIntoPosState);
	}


	private ARole closestToBall()
	{
		return getRoles().stream()
				.min(Comparator.comparingDouble(bot -> bot.getPos().distanceToSqr(getBallTargetPos())))
				.orElseThrow();
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();
		setPlacementTolerance(placementTolerance);

		if (shootDurations == null)
		{
			shootDurations = SumatraMath.evenDistribution1D(minDurationMs, maxDurationMs, numSamples);
			determinePlacementAndKickPos();
		}

		getShapes(EAiShapesLayer.TEST_BALL_CALIBRATION).add(
				DrawableArrow.fromPositions(placementPos, kickTarget).setColor(Color.RED)
		);
	}


	@Override
	protected boolean ready()
	{
		return !getRoles().isEmpty() && getRoles().size() <= 2;
	}


	private void determinePlacementAndKickPos()
	{
		int sign = (currentDurationIndex % 2 == 0) ? 1 : -1;
		IVector2 newTargetBallPos;

		switch (mode)
		{
			case CHIP -> newTargetBallPos = chipKickPositions(sign);
			case STRAIGHT -> newTargetBallPos = straightKickPositions(sign);
			default -> throw new NotImplementedException();
		}
		setBallTargetPos(newTargetBallPos);
	}


	private IVector2 chipKickPositions(int sign)
	{
		kickerDevice = EKickerDevice.CHIP;

		switch (side)
		{
			case BOTH ->
			{
				placementPos = Vector2.fromY(sign * (Geometry.getFieldWidth() / 2 + marginToBorder));
				kickTarget = placementPos.multiplyNew(-1);
				secondBotPos = kickTarget.addNew(Vector2.fromX(-1000));
			}
			case OUR, THEIR ->
			{
				placementPos = Vector2.fromXY(
						side.getSign() * Geometry.getFieldLength() / 3,
						sign * (Geometry.getFieldWidth() / 2 + marginToBorder)
				);
				kickTarget = placementPos.multiplyNew(Vector2.fromXY(1, -1));
				secondBotPos = kickTarget;
			}
			default -> throw new NotImplementedException();
		}
		return placementPos;
	}


	private IVector2 straightKickPositions(int sign)
	{
		kickerDevice = EKickerDevice.STRAIGHT;

		switch (side)
		{
			case BOTH ->
			{
				placementPos = Geometry.getField().withMargin(marginToBorder).getCorner(IRectangle.ECorner.BOTTOM_LEFT)
						.multiplyNew(sign);
				kickTarget = placementPos.multiplyNew(-1);
				secondBotPos = kickTarget;
			}

			// No sign change to make sure to not shoot in the opponents half
			case OUR ->
			{
				placementPos = Geometry.getFieldHalfOur().withMargin(marginToBorder)
						.getCorner(IRectangle.ECorner.BOTTOM_RIGHT);
				kickTarget = Geometry.getFieldHalfOur().withMargin(marginToBorder)
						.getCorner(IRectangle.ECorner.TOP_LEFT);
				if (sign < 0)
				{
					var pos = kickTarget;
					kickTarget = placementPos;
					placementPos = pos;
				}
				secondBotPos = kickTarget;
			}
			case THEIR ->
			{
				placementPos = Geometry.getFieldHalfTheir().withMargin(marginToBorder)
						.getCorner(IRectangle.ECorner.BOTTOM_LEFT);
				kickTarget = Geometry.getFieldHalfTheir().withMargin(marginToBorder)
						.getCorner(IRectangle.ECorner.TOP_RIGHT);
				if (sign < 0)
				{
					var pos = kickTarget;
					kickTarget = placementPos;
					placementPos = pos;
				}
				secondBotPos = kickTarget;
			}
			default -> throw new NotImplementedException();
		}
		return placementPos;
	}


	@Override
	protected void handleNonPlacingRole(ARole role)
	{
		MoveRole moveRole = reassignRole(role, MoveRole.class, MoveRole::new);
		moveRole.updateDestination(secondBotPos);
		moveRole.setMoveCon(moveRole.getMoveCon().withPhysicalObstaclesOnly());
		moveRole.updateLookAtTarget(getBall());
	}


	private class MoveIntoPosState extends TransitionableState
	{
		private IVector2 destination;


		public MoveIntoPosState()
		{
			super(stateMachine::changeState);
		}


		@Override
		public void onInit()
		{
			getRoles().forEach(r -> switchRoles(r, new MoveRole()));

			var distToBall = Geometry.getBotRadius() * 3;
			destination = Vector2.fromPoints(getBall().getPos(), kickTarget)
					.scaleTo(-distToBall)
					.add(getBall().getPos());

			var fieldWithMargin = Geometry.getFieldWBorders().withMargin(-1.5 * Geometry.getBotRadius());
			var keepOutAreaBall = Circle.createCircle(getBall().getPos(), distToBall);
			var intersections = fieldWithMargin.intersectShape(keepOutAreaBall);

			if (!intersections.isEmpty())
			{
				destination = destination.nearestTo(intersections);
			}
		}


		@Override
		public void onUpdate()
		{
			for (var role : findRoles(MoveRole.class))
			{
				role.setMinTimeAtDest(0.3);
				role.setMoveCon(role.getMoveCon().withPhysicalObstaclesOnly());
				role.updateLookAtTarget(getBall());

				if (role == closestToBall())
				{
					role.updateDestination(destination);
				} else
				{
					role.updateDestination(secondBotPos);
				}
			}
		}


		private boolean allRolesFinished()
		{
			return findRoles(MoveRole.class).stream().allMatch(MoveRole::isTargetReached);
		}
	}

	private class ExecutionState extends AState
	{
		@Override
		public void doEntryActions()
		{
			var role = new StraightChipKickSamplerRole(
					kickerDevice,
					getBall().getPos(),
					kickTarget,
					shootDurations.get(currentDurationIndex++ % shootDurations.size()),
					kickMode
			);
			switchRoles(closestToBall(), role);

			allRolesExcept(role).stream()
					.map(r -> reassignRole(r, AttackerRole.class, AttackerRole::new))
					.forEach(receiverRole -> {
								receiverRole.setAction(OffensiveAction.buildReceive(kickTarget));
								receiverRole.setPhysicalObstaclesOnly(true);
							}
					);
		}


		@Override
		public void doUpdate()
		{
			if (findRoles(StraightChipKickSamplerRole.class).stream().anyMatch(ARole::isCompleted))
			{
				stopExecution();
			}
		}


		@Override
		public void doExitActions()
		{
			determinePlacementAndKickPos();
		}
	}
}
