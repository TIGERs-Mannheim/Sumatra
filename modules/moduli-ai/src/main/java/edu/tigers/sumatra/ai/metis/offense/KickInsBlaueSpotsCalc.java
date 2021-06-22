/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class KickInsBlaueSpotsCalc extends ACalculator
{
	@Configurable(comment = "Minimum distance between opponent and KickInsBlaue targeted Spot [mm]", defValue = "1000.0")
	private static double minRadiusFreeSpot = 1000.0;
	private final Supplier<List<ICircle>> freeSpots;
	@Getter
	private List<ICircle> kickInsBlaueSpots;
	private double minDistanceToOut;


	@Override
	public boolean isCalculationNecessary()
	{
		return !freeSpots.get().isEmpty() && getBall().getVel().getLength2() < 0.3;
	}


	@Override
	protected void reset()
	{
		kickInsBlaueSpots = Collections.emptyList();
	}


	@Override
	public void doCalc()
	{
		minDistanceToOut = calculateMinDistanceToOut();
		kickInsBlaueSpots = freeSpots.get().stream()
				.filter(this::spotSizeIsSufficient)
				.filter(this::ballWillStayInGame)
				.filter(this::isTowardsOpponents).
						collect(Collectors.toList());
	}


	private boolean ballWillStayInGame(ICircle freeSpot)
	{
		final double ball2GridPointAngle = Vector2.fromPoints(getWFrame().getBall().getPos(), freeSpot.center())
				.getAngle();
		final IHalfLine ballTravelLine = Lines
				.halfLineFromDirection(freeSpot.center(), Vector2.fromAngle(ball2GridPointAngle));
		List<IVector2> ballLeaveFieldPoint = Geometry.getField()
				.lineIntersections(ballTravelLine);
		if (ballLeaveFieldPoint.size() != 1)
		{
			// Ball with infinite speed never leaving or leaving more than once is weird, filter out
			return false;
		}

		if (ballLeaveFieldPoint.get(0).distanceTo(freeSpot.center()) > minDistanceToOut)
		{
			return true;
		}

		getShapes(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE)
				.add(new DrawableLine(ballTravelLine.toLineSegment(minDistanceToOut), Color.ORANGE));
		return false;

	}


	private boolean spotSizeIsSufficient(ICircle freeSpot)
	{
		return freeSpot.radius() > minRadiusFreeSpot;
	}


	private boolean isTowardsOpponents(ICircle freeSpot)
	{
		return freeSpot.center().x() > getWFrame().getBall().getPos().x();
	}


	private double calculateMinDistanceToOut()
	{
		IBallTrajectory ballTrajectory = Geometry.getBallFactory()
				.createTrajectoryFromKickedBallWithoutSpin(Vector2.zero(),
						Vector2.fromX(OffensiveConstants.getBallSpeedAtTargetKickInsBlaue() * 1000).getXYZVector());
		return ballTrajectory.getDistByTime(ballTrajectory.getTimeByVel(0));
	}


}
