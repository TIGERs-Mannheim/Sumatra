package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.PassInterceptionMovingRobotRater;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * Do a dribbling move to do a nice pass
 */
@RequiredArgsConstructor
public class FinisherPassActionMove extends AOffensiveActionMove
{
	private final Supplier<DribbleToPos> dribbleToPos;

	private final TimestampTimer activateTimeout = new TimestampTimer(0.5);

	private final Supplier<Map<KickOrigin, List<RatedPass>>> ratedPasses;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	private RatedPass lastValidPass;

	@Configurable(comment = "safety margin to avoid dribbling moves near our pen area", defValue = "1500.0", unit = EConfigUnit.DISTANCE_MM)
	private static double penAreaOurMargin = 1500.0;

	@Configurable(comment = "safety margin to avoid dribbling moves near their pen area", defValue = "200.0", unit = EConfigUnit.DISTANCE_MM)
	private static double penAreaTheirMargin = 200.0;

	@Configurable(comment = "enable finisher passes", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean enableFinisherPasses = true;


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		if (!enableFinisherPasses)
		{
			return Optional.empty();
		}

		if (!getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().containsKey(botId))
		{
			return Optional.empty();
		}

		var lastMove = getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().get(botId).getMove();
		boolean lastMoveWasProtect = lastMove == EOffensiveActionMove.PROTECT_MOVE;

		if ((lastMoveWasProtect || lastMove == EOffensiveActionMove.FINISHER_PASS_MOVE) && getWFrame().getBot(botId)
				.getBallContact().hadContact(0.1))
		{
			// The idea is we want to activate the finisher pass if the protect has been active a given amount of time
			activateTimeout.update(getWFrame().getTimestamp());
		} else
		{
			activateTimeout.reset();
			lastValidPass = null;
		}

		var kickOrigin = kickOrigins.get().get(botId);
		if (kickOrigin == null)
		{
			return Optional.empty();
		}

		if (Geometry.getPenaltyAreaOur().withMargin(penAreaOurMargin).isPointInShape(kickOrigin.getPos()))
		{
			return Optional.empty();
		}

		if (dribbleToPos.get().getDribbleToDestination() == null ||
				dribbleToPos.get().getDribbleToDestination().distanceTo(getBall().getPos()) < Geometry.getBotRadius())
		{
			// the role will not be able to do any significant movement. Thus, we dont allow it to continue
			return Optional.empty();
		}

		if (!ratedPasses.get().containsKey(kickOrigin) && lastValidPass != null)
		{
			// no pass available
			return Optional.empty();
		}

		if (!ratedPasses.get().get(kickOrigin).isEmpty() && lastValidPass == null)
		{
			// we try to stick to the first selected pass and execute it. The pass structure
			// will be updated and recalculated in the according finisher pass role state
			lastValidPass = findTheMostAwesomePassTargetForANiceFinisherPass(kickOrigin).orElse(null);
		} else if (lastValidPass == null)
		{
			return Optional.empty();
		}

		if (lastValidPass == null)
		{
			// no pass has been found. Exit.
			return Optional.empty();
		}

		RatedPass optimizedPass = optimizePass(lastValidPass, botId);

		return Optional.of(RatedOffensiveAction.buildFinisherPass(
				EOffensiveActionMove.FINISHER_PASS_MOVE,
				calcViability(botId, lastMove),
				dribbleToPos.get(),
				optimizedPass
		));
	}


	private RatedPass optimizePass(RatedPass lastValidPass, BotID botID)
	{
		var botsToConsider = Stream.concat(
				getWFrame().getOpponentBots().values().stream(),
				getWFrame().getTigerBotsAvailable().values().stream()
						.filter(e -> e.getBotId() != lastValidPass.getPass().getReceiver())
						.filter(e -> e.getBotId() != botID)
						.filter(e -> e.getBotId() != getAiFrame().getKeeperId())
		).toList();

		PassInterceptionMovingRobotRater rater = new PassInterceptionMovingRobotRater(
				botsToConsider, Collections.emptyList());

		// we expect opponents to react slightly slower than usual
		// due to their astonishment towards this awesome dribbling play
		rater.setRobotMovementLimitFactor(0.9);

		PassFactory passFactory = new PassFactory();
		passFactory.update(getWFrame());

		var source = lastValidPass.getPass().getKick().getSource();
		var sourceToTarget = lastValidPass.getPass().getKick().getTarget().subtractNew(source).normalizeNew();
		var normal = sourceToTarget.getNormalVector();

		Pass bestPass = null;
		double bestRating = 0;
		for (int i = -2; i <= 2; i++)
		{
			double step = i * Geometry.getBotRadius() * 2.0;
			var samplePassTarget = lastValidPass.getPass().getKick().getTarget().addNew(normal.scaleToNew(step));

			if (Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 2.0).isPointInShape(samplePassTarget)
					|| Geometry.getPenaltyAreaOur().withMargin(penAreaOurMargin).isPointInShape(samplePassTarget)
					|| Geometry.getPenaltyAreaTheir().withMargin(penAreaTheirMargin).isPointInShape(samplePassTarget)
					|| !Geometry.getField().withMargin(-Geometry.getBotRadius()).isPointInShape(samplePassTarget))
			{
				continue;
			}

			getShapes(EAiShapesLayer.OFFENSE_FINISHER_PASS)
					.add(new DrawableCircle(Circle.createCircle(samplePassTarget, 40)));
			var newPass = passFactory.straight(
					getBall().getPos(), samplePassTarget, botID,
					lastValidPass.getPass().getReceiver(), 1.0, 0.0, EBallReceiveMode.DONT_CARE
			);
			if (newPass.isPresent())
			{
				double rating = rater.rate(newPass.get());
				if (rating > bestRating)
				{
					bestRating = rating;
					bestPass = newPass.get();
				}
			}
		}

		if (bestPass != null)
		{
			var newInterceptRating = rater.rate(bestPass);

			getShapes(EAiShapesLayer.OFFENSE_FINISHER_PASS)
					.add(new DrawableAnnotation(
							getWFrame().getBot(botID).getPos(), String.format(
							"intercept: %.2f | kick speed: %.2f", newInterceptRating,
							bestPass.getKick().getKickParams().getKickSpeed()
					)
					));

			Map<EPassRating, Double> scores = new HashMap<>();
			scores.put(EPassRating.INTERCEPTION, bestRating);
			return RatedPass.builder()
					.pass(bestPass)
					.scores(scores)
					.build();
		}
		return lastValidPass;
	}


	private Optional<RatedPass> findTheMostAwesomePassTargetForANiceFinisherPass(KickOrigin kickOrigin)
	{
		var passes = ratedPasses.get().get(kickOrigin);

		// the idea is to start looking in growing circles for the best
		// pass in a given circle. This will create a bias towards
		// choosing short passes. The intercept score of short passes will be
		// more influenced by dribbling movement than for passes far away.
		for (int i = 0; i < 4; i++)
		{
			double searchRadius = 1500 + i * 750;
			var searchCircle = Circle.createCircle(kickOrigin.getPos(), searchRadius);
			getShapes(EAiShapesLayer.OFFENSE_FINISHER_PASS).add(
					new DrawableCircle(searchCircle, new Color(76, 0, 97, 255)));

			List<EPassRating> passRatings = new ArrayList<>();
			passRatings.add(EPassRating.PRESSURE);
			if (getBall().getPos().x() > 0)
			{
				passRatings.add(EPassRating.GOAL_KICK);
				passRatings.add(EPassRating.REFLECT_GOAL_KICK);
			}

			var bestPass = passes.stream()
					.filter(pass -> searchCircle.isPointInShape(pass.getPass().getKick().getTarget()))
					.filter(pass -> Geometry.getPenaltyAreaTheir().withMargin(penAreaTheirMargin)
							.isPointInShape(pass.getPass().getKick().getTarget()))
					.sorted(Comparator.comparingDouble(pass -> pass.getMaxScore(passRatings.toArray(new EPassRating[0]))))
					.filter(pass -> pass.getScore(EPassRating.PRESSURE) > 0.05)
					.toList()
					.reversed()
					.stream()
					.findFirst();

			if (bestPass.isEmpty())
			{
				continue;
			}

			getShapes(EAiShapesLayer.OFFENSE_FINISHER_PASS).add(
					new DrawableCircle(
							Circle.createCircle(bestPass.get().getPass().getKick().getTarget(), 20),
							new Color(151, 255, 231, 255)
					));

			return bestPass;
		}

		return Optional.empty();
	}


	private OffensiveActionViability calcViability(BotID botId, EOffensiveActionMove lastMove)
	{
		// we always need ball contact. No ball contact -> no Finisher pass
		if (getWFrame().getBot(botId).getBallContact().hadContact(0.2))
		{
			if (lastMove == EOffensiveActionMove.FINISHER_PASS_MOVE)
			{
				// we already started FinisherPassMove. we want to continue with it. Thus, we use
				// a forced true viability
				return new OffensiveActionViability(EActionViability.TRUE, 0.5);
			}

			if (activateTimeout.isTimeUp(getWFrame().getTimestamp()))
			{
				// here we initially start the finisher pass
				return new OffensiveActionViability(EActionViability.PARTIALLY, 0.4);
			}
		}

		// no ball contact, no finisher pass possible.
		return new OffensiveActionViability(EActionViability.FALSE, 0.0);
	}
}
