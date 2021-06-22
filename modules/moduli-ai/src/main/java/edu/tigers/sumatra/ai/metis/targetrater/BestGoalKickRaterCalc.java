/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.KickFactory;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Calculates the best goal kick target on the goal for from the point where the ball is or will be when received or redirected.
 * The result is a target in the opponents goal and a probability a shot to this target will score a goal.
 */
@RequiredArgsConstructor
public class BestGoalKickRaterCalc extends ACalculator
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	@Configurable(comment = "The maximum reasonable angle [rad] for redirects", defValue = "1.2")
	private static double maximumReasonableRedirectAngle = 1.2;

	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	private final KickFactory kickFactory = new KickFactory();

	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	@Getter
	private Map<BotID, GoalKick> bestGoalKicks = Collections.emptyMap();

	@Getter
	private GoalKick bestGoalKick;


	@Override
	public void doCalc()
	{
		kickFactory.update(getWFrame());

		var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(getWFrame().getOpponentBots().values());
		bestGoalKicks = kickOrigins.get().values().stream()
				.map(kickOrigin -> rateKickOrigin(rater, kickOrigin))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toUnmodifiableMap(e -> e.getKickOrigin().getShooter(), e -> e));

		bestGoalKick = bestGoalKicks.values().stream()
				.max(Comparator.comparingDouble(g -> g.getRatedTarget().getScore()))
				.orElse(null);

		getShapes(EAiShapesLayer.AI_BEST_GOAL_KICK_DEBUG)
				.addAll(rater.createDebugShapes());

		bestGoalKicks.values().forEach(goalKick -> {
			ILineSegment line = Lines
					.segmentFromPoints(goalKick.getKickOrigin().getPos(), goalKick.getRatedTarget().getTarget());
			getShapes(EAiShapesLayer.AI_BEST_GOAL_KICK)
					.add(new DrawableLine(line, colorPicker.getColor(goalKick.getRatedTarget().getScore())));
			getShapes(EAiShapesLayer.AI_BEST_GOAL_KICK)
					.add(new DrawableAnnotation(line.getEnd(), DF.format(goalKick.getRatedTarget().getScore())).withOffset(
							Vector2f.fromX(100)));
		});
	}


	private Optional<GoalKick> rateKickOrigin(AngleRangeRater rater, KickOrigin kickOrigin)
	{
		var score = rater.rate(kickOrigin.getPos())
				.map(ratedTarget -> new GoalKick(
								kickOrigin,
								ratedTarget,
								kickFactory.goalKick(kickOrigin.getPos(), ratedTarget.getTarget()),
								isBallRedirectReasonable(kickOrigin, ratedTarget.getTarget())
						)
				);
		getShapes(EAiShapesLayer.AI_BEST_GOAL_KICK_DEBUG)
				.addAll(rater.createDebugShapes());
		return score;
	}


	private boolean isBallRedirectReasonable(KickOrigin kickOrigin, final IVector2 target)
	{
		var couldBeRedirected = Optional.ofNullable(bestGoalKicks.get(kickOrigin.getShooter()))
				.map(GoalKick::canBeRedirected)
				.orElse(false);

		var originToBall = getBall().getPos().subtractNew(kickOrigin.getPos());
		var originToTarget = target.subtractNew(kickOrigin.getPos());
		var redirectAngle = originToBall.angleToAbs(originToTarget).orElse(Math.PI);
		var maxAngle = maximumReasonableRedirectAngle - (couldBeRedirected ? 0 : 0.3);
		return redirectAngle <= maxAngle;
	}
}
