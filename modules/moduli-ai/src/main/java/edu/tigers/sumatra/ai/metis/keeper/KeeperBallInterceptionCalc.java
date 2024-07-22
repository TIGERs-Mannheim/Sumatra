/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballinterception.InterceptionIteration;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.general.BallInterceptor;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public class KeeperBallInterceptionCalc extends ACalculator
{
	@Configurable(defValue = "300.0", comment = "[mm]")
	private static double stepSize = 300.0;
	@Configurable(defValue = "4.0", comment = "[m/s] Max considered ball speed")
	private static double maxBallSpeed = 4.0;
	@Configurable(defValue = "-0.3", comment = "[s] Time difference between keeper reaching the intercept pos vs. ball travel time")
	private static double slackTime = -0.3;

	private BallInterceptor ballInterceptor;
	private IVector2 previousInterceptionPosition = null;
	@Getter
	private RatedBallInterception keeperBallInterception;
	private IBallTrajectory ballTrajectory;
	private BotID keeper;


	public KeeperBallInterceptionCalc()
	{
		ballInterceptor = new BallInterceptor(Optional::empty);
		ballInterceptor.setShapeColor(Color.BLUE);
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId())
				&& ballInterceptor.ballIsNotUnderOurControl(getWFrame())
				&& Geometry.getField().isPointInShape(getBall().getPos())
				&& getWFrame().getTiger(getAiFrame().getKeeperId()) != null;
	}


	@Override
	protected void reset()
	{
		ballTrajectory = null;
		keeperBallInterception = null;
		keeper = getAiFrame().getKeeperId();
	}


	@Override
	public void doCalc()
	{
		ballInterceptor.setShapes(getShapes(EAiShapesLayer.KEEPER_INTERCEPT));
		ballTrajectory = findBallTrajectory();
		ballInterceptor.setBallTrajectory(ballTrajectory);
		ballInterceptor.updateInitialBallVelocity(getBall());
		ballInterceptor.setAreaOfInterest(
				Geometry.getPenaltyAreaOur().withMargin(KeeperBehaviorCalc.ballMarginMiddle().atBorder()).getRectangle());
		ballInterceptor.setExclusionAreas(List.of());
		List<IVector2> interceptionPoints = ballInterceptor.generateInterceptionPoints(maxBallSpeed, stepSize);

		var iterations = ballInterceptor.iterateOverBallTravelLine(getWFrame().getBot(keeper), interceptionPoints);
		previousInterceptionPosition = invalidatePreviousInterceptionPositions().orElse(null);
		var previousInterception = getPreviousInterceptionIteration();
		if (previousInterception != null)
		{
			// replace bot target iteration with near sampled iteration(s)
			iterations.removeIf(e -> Math.abs(e.getBallTravelTime() - previousInterception.getBallTravelTime()) < 0.1);
			iterations.add(previousInterception);
		}
		iterations.removeIf(e -> !Double.isFinite(e.getBallTravelTime()));
		iterations.forEach(this::drawDebugShapes);
		iterations.sort(Comparator.comparingDouble(InterceptionIteration::getBallTravelTime));

		var zeroCrossings = ballInterceptor.findZeroCrossings(iterations);
		var corridors = ballInterceptor.findCorridors(zeroCrossings);


		keeperBallInterception = ballInterceptor.findTargetInterception(iterations, corridors, keeper,
						previousInterception, Math.min(slackTime, -0.1))
				.or(() -> ballInterceptor.findTargetInterception(iterations, corridors, keeper,
						previousInterception, -0.1))
				.or(() -> ballInterceptor.keepPositionUntilPassVanishes(getBall(), getWFrame().getBot(keeper),
						previousInterception))
				.orElse(null);

		if (keeperBallInterception != null)
		{
			previousInterceptionPosition = keeperBallInterception.getBallInterception().getPos();
		} else
		{
			previousInterceptionPosition = null;
		}
	}


	private void drawDebugShapes(InterceptionIteration iteration)
	{
		var pos = getBall().getTrajectory().getPosByTime(iteration.getBallTravelTime()).getXYVector();
		var text = String.format("%.2f s (%.2f)", iteration.getSlackTime(), iteration.getIncludedSlackTimeBonus());
		getShapes(EAiShapesLayer.KEEPER_INTERCEPT).add(new DrawableAnnotation(pos, text, true));
	}


	private IBallTrajectory findBallTrajectory()
	{
		return getWFrame().getBall().getTrajectory();
	}


	private InterceptionIteration getPreviousInterceptionIteration()
	{
		return Optional.ofNullable(previousInterceptionPosition)
				.map(point -> ballTrajectory.closestPointTo(point))
				.filter(point -> ballInterceptor.isPositionLegal(point))
				.map(point -> ballInterceptor.createInterceptionIterationFromPreviousTarget(getWFrame().getBot(keeper),
						point, getBall())).orElse(null);
	}


	private Optional<IVector2> invalidatePreviousInterceptionPositions()
	{
		if (getAiFrame().getPrevFrame().getTacticalField().getKeeperBehavior() != EKeeperActionType.INTERCEPT_PASS)
		{
			return Optional.empty();
		}
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		return Optional.ofNullable(previousInterceptionPosition)
				.filter(
						pos -> (ballDir.angleToAbs(pos.subtractNew(getBall().getPos())).orElse(0.0) < AngleMath.deg2rad(15)));
	}
}
