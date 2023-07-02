/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Let the PenArea defender drive from left to right and back around the Penalty Area
 * How to use:
 * - Put ball in own penalty area
 * - Set robots on the field
 * - 1 Keeper that will not do anything
 * - n Ball defender
 * - Activate Test Mode in the AI view
 * - Assign ball defender to the normal defense play
 */
@RequiredArgsConstructor
public class TestDefensePenAreaGroupMovementCalc extends ACalculator
{
	@Configurable(defValue = "SMOOTH", comment = "Type of the simulated movement")
	private static EMovementType movementType = EMovementType.SMOOTH;
	@Configurable(defValue = "false", comment = "FOR TESTING ONLY - drive around penalty area")
	private static boolean testDefensePenAreaGroupMovement = false;
	@Configurable(defValue = "1.0", comment = "[m/s] How fast the virtual ball target moves")
	private static double movementSpeed = 1.0;
	@Configurable(defValue = "0.3", comment = "[s] Wait for this time at the corner before moving the other direction")
	private static double waitTimeBeforeReversingDirection = 0.3;
	@Configurable(defValue = "300", comment = "[mm] Distance to accelerate after starting at the corner")
	private static double accelerationDistance = 300;
	@Configurable(defValue = "4", comment = "# jumps for jumping movement")
	private static int numJumps = 4;

	private final Supplier<List<DefenseThreatAssignment>> defensePenAreaThreatAssignmentsInput;
	private final Supplier<Set<BotID>> penAreaDefender;
	private final Supplier<IShapeBoundary> penAreaBoundary;

	@Getter
	List<DefenseThreatAssignment> defensePenAreaThreatAssignments = List.of();

	private double position = 0;
	private EDirection direction = EDirection.POSITIVE;

	private long lastTimeStamp = -1;
	private long reachedCornerTime = -1;
	private long lastJumpTime = -1;


	@Override
	protected void doCalc()
	{
		if (testDefensePenAreaGroupMovement)
		{
			defensePenAreaThreatAssignments = getTestAssignments();
			updatePosition();
			getShapes(EAiShapesLayer.TEST_MOVE_AROUND_PEN_AREA).add(
					new DrawableShapeBoundary(penAreaBoundary.get(), Color.RED)
			);

		} else
		{
			defensePenAreaThreatAssignments = defensePenAreaThreatAssignmentsInput.get();
		}
	}


	private List<DefenseThreatAssignment> getTestAssignments()
	{
		var boundary = penAreaBoundary.get();
		var threatPosProjected = boundary.stepAlongBoundary(position).orElseGet(Vector2::zero);
		var threatPosProjected2 = boundary.stepAlongBoundary(position + getSignFromDirection())
				.orElse(threatPosProjected);

		var goalCenter = Geometry.getGoalOur().getCenter();
		var threatPos = threatPosProjected.subtractNew(goalCenter).scaleTo(Geometry.getFieldWidth() / 2).add(goalCenter);
		var threatSpeed = threatPosProjected2.subtractNew(threatPosProjected);
		var threatLine = Lines.segmentFromPoints(threatPosProjected, Geometry.getGoalOur().getCenter());


		var ballThreat = new DefenseBallThreat(
				threatSpeed.isZeroVector() ? threatSpeed : threatSpeed.scaleTo(movementSpeed), threatLine, null, null);


		getShapes(EAiShapesLayer.TEST_MOVE_AROUND_PEN_AREA).add(
				new DrawableCircle(Circle.createCircle(threatPos, Geometry.getBotRadius()), Color.RED));
		getShapes(EAiShapesLayer.TEST_MOVE_AROUND_PEN_AREA).add(
				new DrawableLine(threatPosProjected, threatPos, Color.RED));
		return List.of(new DefenseThreatAssignment(ballThreat, penAreaDefender.get()));
	}


	private void updatePosition()
	{

		var now = getAiFrame().getWorldFrame().getTimestamp();
		if (lastTimeStamp == -1)
		{
			lastTimeStamp = now;
		}
		var diff = (now - lastTimeStamp) * 1e-9;
		lastTimeStamp = now;
		if (movementType == EMovementType.SMOOTH)
		{
			updatePositionSmooth(diff, now);
		} else
		{
			updatePositionJumping(now);
		}
	}


	private void updatePositionSmooth(double diff, long now)
	{
		var length = penAreaBoundary.get().getShape().getPerimeterLength();
		var posDiff = getMovementSpeed(length) * diff * 1e3;
		switch (direction)
		{
			case POSITIVE ->
			{
				if (position + posDiff > length)
				{
					position = length;
					direction = EDirection.WAITING_AT_END;
					reachedCornerTime = now;
				} else
				{
					position += posDiff;
				}
			}
			case NEGATIVE ->
			{
				if (position - posDiff < 0)
				{
					position = 0;
					direction = EDirection.WAITING_AT_START;
					reachedCornerTime = now;
				} else
				{
					position -= posDiff;
				}
			}
			case WAITING_AT_START ->
			{
				if ((now - reachedCornerTime) * 1e-9 > waitTimeBeforeReversingDirection)
				{
					direction = EDirection.POSITIVE;
					position += posDiff;
				}
			}
			case WAITING_AT_END ->
			{
				if ((now - reachedCornerTime) * 1e-9 > waitTimeBeforeReversingDirection)
				{
					direction = EDirection.NEGATIVE;
					position -= posDiff;
				}
			}
		}
	}


	private void updatePositionJumping(long now)
	{
		if (lastJumpTime == -1)
		{
			lastJumpTime = now;
		}
		var length = penAreaBoundary.get().getShape().getPerimeterLength();
		var jumpLength = length / numJumps;
		var jumpTime = (jumpLength * 1e-3) / movementSpeed;
		switch (direction)
		{
			case POSITIVE ->
			{
				if (position + jumpLength > length + 1e-3)
				{
					position = length;
					direction = EDirection.WAITING_AT_END;
					reachedCornerTime = now;
				}
				if ((now - lastJumpTime) * 1e-9 > jumpTime)
				{
					position += jumpLength;
					lastJumpTime = now;
				}
			}
			case NEGATIVE ->
			{
				if (position - jumpLength < -1e-3)
				{
					position = 0;
					direction = EDirection.WAITING_AT_START;
					reachedCornerTime = now;
				}
				if ((now - lastJumpTime) * 1e-9 > jumpTime)
				{
					position -= jumpLength;
					lastJumpTime = now;
				}
			}
			case WAITING_AT_START ->
			{
				if ((now - reachedCornerTime) * 1e-9 > waitTimeBeforeReversingDirection + jumpTime)
				{
					direction = EDirection.POSITIVE;
					position += jumpLength;
					lastJumpTime = now;
				}
			}
			case WAITING_AT_END ->
			{
				if ((now - reachedCornerTime) * 1e-9 > waitTimeBeforeReversingDirection + jumpTime)
				{
					direction = EDirection.NEGATIVE;
					position -= jumpLength;
					lastJumpTime = now;
				}
			}
		}
	}


	private double getMovementSpeed(double length)
	{
		var distToCorner = SumatraMath.min(position, Math.abs(length - position));
		if (distToCorner < accelerationDistance)
		{
			double factor = 0.1 + 0.9 * distToCorner / accelerationDistance;
			return movementSpeed * factor;
		}
		return movementSpeed;
	}


	private int getSignFromDirection()
	{
		return switch (direction)
		{
			case POSITIVE, WAITING_AT_START -> 1;
			case NEGATIVE, WAITING_AT_END -> -1;
		};
	}


	private enum EDirection
	{
		POSITIVE, NEGATIVE, WAITING_AT_START, WAITING_AT_END,
	}

	private enum EMovementType
	{
		SMOOTH, JUMPING
	}
}
