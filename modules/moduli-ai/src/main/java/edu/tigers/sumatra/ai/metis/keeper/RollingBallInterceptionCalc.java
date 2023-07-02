/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.InterceptionIteration;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.pass.PassInterceptionCalc;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class RollingBallInterceptionCalc extends PassInterceptionCalc
{
	@Configurable(defValue = "300.0", comment = "[mm]")
	private static double stepSize = 300.0;
	@Configurable(defValue = "3.0", comment = "[m/s] Max considered ball speed")
	private static double maxBallSpeed = 3.0;

	@Getter
	private Map<BotID, RatedBallInterception> rollingBallInterceptions;

	private BotID keeper;


	public RollingBallInterceptionCalc(
			Supplier<Optional<OngoingPass>> ongoingPass)
	{
		super(ongoingPass);
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId()) && ballIsMoving()
				&& Geometry.getPenaltyAreaOur().withMargin(Geometry.getBallRadius()).isPointInShape(getBall().getPos());
	}


	@Override
	protected void reset()
	{
		super.reset();
		rollingBallInterceptions = Collections.emptyMap();
		keeper = getAiFrame().getKeeperId();
		shapeLayer = EAiShapesLayer.AI_KEEPER;
		shapeColor = Color.BLUE;
	}


	@Override
	public void doCalc()
	{
		super.doCalc();
		List<IVector2> interceptionPoints = generateInterceptionPoints(Geometry.getPenaltyAreaOur(), maxBallSpeed,
				stepSize);

		Map<BotID, RatedBallInterception> newBallInterceptions = new HashMap<>();

		var iterations = iterateOverBallTravelLine(keeper, interceptionPoints);
		previousInterceptionPositions = invalidatePreviousInterceptionPositions();
		var previousInterception = getPreviousInterceptionIteration(keeper);
		if (previousInterception != null)
		{
			// replace bot target iteration with near sampled iteration(s)
			iterations.removeIf(e -> Math.abs(e.getBallTravelTime() - previousInterception.getBallTravelTime()) < 0.1);
			iterations.add(previousInterception);
		}
		iterations.removeIf(e -> !Double.isFinite(e.getBallTravelTime()));
		iterations.sort(Comparator.comparingDouble(InterceptionIteration::getBallTravelTime));

		var zeroCrossings = findZeroCrossings(iterations);
		var corridors = findCorridors(zeroCrossings);


		Optional<RatedBallInterception> ballInterception = findTargetInterception(iterations, corridors, keeper, null,
				-0.35);
		if (ballInterception.isEmpty())
		{
			ballInterception =
					findTargetInterception(iterations, corridors, keeper, null, 0.0);
		}

		ballInterception.ifPresentOrElse(e -> newBallInterceptions.put(keeper, e),
				() -> keepPositionUntilPassVanishes(newBallInterceptions, keeper, null));

		rollingBallInterceptions = Collections.unmodifiableMap(newBallInterceptions);
		previousInterceptionPositions = newBallInterceptions.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().getBallInterception().getPos()));
	}


	@Override
	protected void updatePenaltyAreas()
	{
		super.updatePenaltyAreas();
		var margin = Geometry.getBotRadius() + Geometry.getBallRadius();
		penAreaOur = Geometry.getPenaltyAreaOur().withMargin(-margin);
	}


	@Override
	protected boolean isLegal(IVector2 interceptionPoint)
	{
		// be a bit more pessimistic here for practical reasons (it should center2Dribbler + ballRadius strictly speaking)
		var offset = Geometry.getBotRadius();
		var destination = interceptionPoint.addNew(ballTrajectory.getTravelLine().directionVector().scaleToNew(offset));
		return penAreaOur.isPointInShape(destination) && Geometry.getField().isPointInShape(interceptionPoint);
	}


	@Override
	protected Map<BotID, IVector2> invalidatePreviousInterceptionPositions()
	{
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		return previousInterceptionPositions.entrySet().stream()
				.filter(e -> (ballDir.angleToAbs(e.getValue().subtractNew(getBall().getPos())).orElse(0.0)
						< AngleMath.deg2rad(15)))
				.collect(Collectors.toUnmodifiableMap(
						Map.Entry::getKey, Map.Entry::getValue));
	}
}
