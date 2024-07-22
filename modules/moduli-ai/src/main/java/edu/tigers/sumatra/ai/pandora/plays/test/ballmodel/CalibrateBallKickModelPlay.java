/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.ballmodel;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ABallPreparationPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.StraightChipKickSamplerRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.statemachine.AState;
import org.apache.commons.lang.NotImplementedException;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;


public class CalibrateBallKickModelPlay extends ABallPreparationPlay
{
	private EKickerDevice kickerDevice;
	private IVector2 placementPos;
	private IVector2 kickTarget;
	private IVector2 secondBotPos;
	private final EBallModelCalibrationKickMode mode;
	private final List<Double> shootDurations;
	private int currentDurationIndex;
	private double marginToBorder;

	private final ExecutionState executionState = new ExecutionState();


	public CalibrateBallKickModelPlay(
			EBallModelCalibrationKickMode mode,
			double marginToBorder,
			double minDurationMs,
			double maxDurationMs,
			int numSamples
	)
	{
		super(EPlay.CALIBRATE_BALL_KICK_MODEL);

		this.mode = mode;
		this.marginToBorder = marginToBorder;

		currentDurationIndex = 0;
		shootDurations = SumatraMath.evenDistribution1D(minDurationMs, maxDurationMs, numSamples);
		determinePlacementAndKickPos();
		setExecutionState(new MoveIntoPosState());
		setUseAssistant(true);
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

		getShapes(EAiShapesLayer.TEST_BALL_CALIBRATION).add(
				DrawableArrow.fromPositions(placementPos, kickTarget).setColor(Color.RED)
		);

		if (getRoles().size() > 2)
		{
			throw new IllegalStateException("Too many roles assigned to the play - max 2");
		}
	}


	@Override
	protected boolean ready()
	{
		return !getRoles().isEmpty() && getRoles().size() <= 2;
	}


	private void determinePlacementAndKickPos()
	{
		int sign = (currentDurationIndex % 2 == 0) ? 1 : -1;
		switch (mode)
		{
			case CHIP_FULL_FIELD ->
			{
				placementPos = Vector2.fromY(sign * (Geometry.getFieldWidth() / 2 + marginToBorder));
				kickTarget = Vector2.fromY(sign * (-Geometry.getFieldWidth() / 2 - marginToBorder));
				kickerDevice = EKickerDevice.CHIP;
				secondBotPos = kickTarget.addNew(Vector2.fromX(-1000));
			}
			case CHIP_HALF_FIELD ->
			{
				placementPos = Vector2.fromXY(-Geometry.getFieldLength() / 3,
						sign * (Geometry.getFieldWidth() / 2 + marginToBorder));
				kickTarget = Vector2.fromXY(-Geometry.getFieldLength() / 3,
						sign * (-Geometry.getFieldWidth() / 2 - marginToBorder));
				kickerDevice = EKickerDevice.CHIP;
				secondBotPos = kickTarget.addNew(Vector2.fromX(1000));

			}
			case STRAIGHT_FULL_FIELD ->
			{
				placementPos = Geometry.getField().withMargin(marginToBorder).getCorner(IRectangle.ECorner.BOTTOM_LEFT)
						.multiplyNew(sign);
				kickTarget = Geometry.getField().withMargin(marginToBorder).getCorner(IRectangle.ECorner.TOP_RIGHT)
						.multiplyNew(sign);
				kickerDevice = EKickerDevice.STRAIGHT;
				secondBotPos = kickTarget.addNew(Vector2.fromX(sign * -1500.0));
			}
			case STRAIGHT_HALF_FIELD ->
			{
				placementPos = Geometry.getFieldHalfOur().withMargin(marginToBorder)
						.getCorner(IRectangle.ECorner.BOTTOM_RIGHT);
				kickTarget = Geometry.getField().withMargin(marginToBorder).getCorner(IRectangle.ECorner.TOP_LEFT);
				kickerDevice = EKickerDevice.STRAIGHT;
				secondBotPos = kickTarget.addNew(Vector2.fromX(1500));
			}
			default -> throw new NotImplementedException();
		}
		setBallTargetPos(placementPos);
	}


	private class MoveIntoPosState extends AState
	{
		private IVector2 destination;


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			destination = Vector2.fromPoints(getBall().getPos(), kickTarget)
					.scaleTo(-Geometry.getBotRadius() * 3)
					.add(getBall().getPos());
		}

		@Override
		public void doUpdate()
		{
			super.doUpdate();
			findOtherRoles(MoveRole.class).forEach(r -> switchRoles(r, new MoveRole()));
			var allFinished = true;

			for (var role : getRoles())
			{
				var moveRole = (MoveRole) role;
				moveRole.getMoveCon().physicalObstaclesOnly();
				moveRole.updateLookAtTarget(getBall());
				((MoveToSkill) moveRole.getCurrentSkill()).setMinTimeAtDestForSuccess(0.3);

				if (moveRole == closestToBall())
				{
					moveRole.updateDestination(destination);
				} else
				{
					moveRole.updateDestination(secondBotPos);
				}

				allFinished = allFinished && moveRole.isSkillStateSuccess();
			}

			if (allFinished)
			{
				stateMachine.changeState(executionState);
			}
		}

	}

	private class ExecutionState extends AState
	{

		private StraightChipKickSamplerRole role;


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			if (currentDurationIndex >= shootDurations.size())
			{
				role = null;
				return;
			}
			role = new StraightChipKickSamplerRole(
					mode == EBallModelCalibrationKickMode.CHIP_HALF_FIELD
							|| mode == EBallModelCalibrationKickMode.STRAIGHT_HALF_FIELD,
					kickerDevice,
					getBall().getPos(),
					kickTarget,
					shootDurations.get(currentDurationIndex++)
			);
			switchRoles(closestToBall(), role);
		}


		@Override
		public void doUpdate()
		{
			super.doUpdate();
			boolean doneInSimulation = SumatraModel.getInstance().isSimulation() && getBall().getVel().getLength2() > 0.3;
			if ((role != null && role.isCompleted()) || doneInSimulation)
			{
				determinePlacementAndKickPos();
				stateMachine.triggerEvent(EEvent.EXECUTED);
			}
		}
	}
}
