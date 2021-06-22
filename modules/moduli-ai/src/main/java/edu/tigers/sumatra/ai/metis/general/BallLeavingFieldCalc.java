/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Calculator tries to detect secure icing of opponent:
 * opponent kick in his own half fast to goal line and cannot reach ball himself anymore
 */
@RequiredArgsConstructor
public class BallLeavingFieldCalc extends ACalculator
{
	@Configurable(comment = "check when ball leaving field detection should be used", defValue = "false")
	private static boolean useBallLeavingFieldDetection = true;


	private final Supplier<Set<BotID>> botsLastTouchedBall;

	@Getter
	private boolean ballLeavingFieldGood;


	@Override
	public boolean isCalculationNecessary()
	{
		return useBallLeavingFieldDetection;
	}


	@Override
	protected void reset()
	{
		ballLeavingFieldGood = false;
	}


	@Override
	public void doCalc()
	{
		ballLeavingFieldGood = false;
		Optional<BotID> lastTouched = botsLastTouchedBall.get().stream()
				.filter(b -> b.getTeamColor() != getAiFrame().getTeamColor())
				.filter(b -> getWFrame().getBots().containsKey(b))
				.findAny();
		if (lastTouched.isPresent())
		{
			ILineSegment ballTravelLine = getBall().getTrajectory().getTravelLineSegment();
			Optional<IVector2> possibleIntersection = Geometry.getGoalOur().getGoalLine()
					.intersectSegment(ballTravelLine);
			Optional<IVector2> possibleGoal = Geometry.getGoalOur().withMargin(2 * Geometry.getBotRadius())
					.getLineSegment().intersectSegment(ballTravelLine);
			if (possibleIntersection.isPresent() && possibleGoal.isEmpty())
			{
				draw(possibleIntersection.get());
				ballLeavingFieldGood = opponentsCanNotReachBall(possibleIntersection.get());
			}
		}
	}


	private boolean opponentsCanNotReachBall(final IVector2 goalLineIntersection)
	{
		long possibleIntersects1 = getWFrame().getOpponentBots().values().stream()
				.filter(bot -> getBall().getTrajectory().getTravelLine().isPointInFront(bot.getPos()))
				.count();
		long possibleIntersects2 = getWFrame().getOpponentBots().values().stream()
				.filter(bot -> (TrajectoryGenerator.generatePositionTrajectory(bot,
						goalLineIntersection, bot.getMoveConstraints()).getTotalTime()) < getBall()
						.getTrajectory().getTimeByPos(goalLineIntersection))
				.count();
		return possibleIntersects1 + possibleIntersects2 < 1;
	}


	private void draw(final IVector2 intersection)
	{
		getShapes(EAiShapesLayer.AI_BALL_LEAVING_FIELD).add(new DrawableCircle(
				Circle.createCircle(getBall().getPos(), Geometry.getBallRadius() * 2), Color.cyan));
		getShapes(EAiShapesLayer.AI_BALL_LEAVING_FIELD).add(new DrawableTube(
				Tube.create(getBall().getPos(), intersection, Geometry.getBotRadius()), Color.cyan));
	}
}
