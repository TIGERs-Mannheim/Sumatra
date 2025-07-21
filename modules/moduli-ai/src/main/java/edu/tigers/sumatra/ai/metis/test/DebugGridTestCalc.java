/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatRater;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.OffensiveZones;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.PassCreator;
import edu.tigers.sumatra.ai.metis.pass.PassStats;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.ai.metis.targetrater.BestGoalKickRater;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableGrid;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotDistanceComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;


@Log4j2
@RequiredArgsConstructor
public class DebugGridTestCalc extends ACalculator
{
	@Configurable(defValue = "false")
	private static boolean enableGrid = false;
	@Configurable(defValue = "false")
	private static boolean enableSinglePoint = false;
	@Configurable(defValue = "YELLOW")
	private static ETeamColor enabledForTeam = ETeamColor.YELLOW;

	@Configurable(defValue = "120")
	private static int numX = 120;
	@Configurable(defValue = "90")
	private static int numY = 90;

	@Configurable(defValue = "PASS_TARGET_RATING", comment = "The type of rating to display")
	private static EGridType type = EGridType.PASS_TARGET_RATING;

	@Configurable(defValue = "INTERCEPTION", comment = "The pass rating to display, if type is PASS_TARGET_RATING")
	private static EPassRating passRating = EPassRating.INTERCEPTION;

	@Configurable(defValue = "false", comment = "Use dynamic pass interception rating based on pass stats")
	private static boolean useDynamicInterceptionRating = false;

	@Configurable(defValue = "-1", comment = "The point of interest, meaning depends on rating. -1 -> ball")
	private static DynamicPosition poi = new DynamicPosition(BallID.instance());

	@SuppressWarnings("MismatchedReadAndWriteOfArray") // the array changes through reflection
	@Configurable(defValue = "15 B; 15 Y", comment = "ignored bots, meaning depends on rating")
	private static BotID[] ignoredBots = new BotID[0];

	@Configurable(defValue = "-1", comment = "shooter bot, -1: choose automatically")
	private static int shooter = -1;

	@Configurable(defValue = "-1", comment = "receiver bot, -1: no receiver")
	private static int receiver = -1;

	@Configurable(defValue = "false", comment = "Choose receiver automatically")
	private static boolean chooseReceiverAutomatically = false;

	@Configurable(defValue = "false", comment = "Draw the scores into the grid")
	private static boolean drawNumbers = false;

	@Configurable(defValue = "0,0", comment = "Single point to rate and display, for individual debugging")
	private static IVector2 singlePoint = Vector2.zero();

	@Configurable(defValue = "true")
	private static boolean considerStraight = true;

	@Configurable(defValue = "true")
	private static boolean considerChip = true;

	@Configurable(defValue = "false")
	private static boolean printGridCalculationTime = false;


	private final Supplier<PassStats> passStats;
	private final Supplier<OffensiveZones> offensiveZones;
	private final Supplier<List<BotID>> opponentMan2ManMarkers;

	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	private final PassFactory passFactory = new PassFactory();
	private final PassCreator passCreator = new PassCreator();

	private IVector2 pointOfInterest;


	@Override
	protected boolean isCalculationNecessary()
	{
		pointOfInterest = poi.update(getWFrame()).getPos();
		return enabledForTeam == getAiFrame().getTeamColor()
				&& shooter().isBot()
				&& (enableGrid || enableSinglePoint);
	}


	@Override
	public void doCalc()
	{
		passFactory.update(getWFrame());
		passCreator.update(getWFrame());

		switch (type)
		{
			case DEFENSE_THREAT_RATING -> defenseThreatRating();
			case BEST_GOAL_KICK_RATING -> bestGoalKickRating();
			case PASS_TARGET_RATING -> passTargetRating();
			default ->
			{
				// do nothing
			}
		}
	}


	private BotID closestBotId(IVector2 pos)
	{
		return getWFrame().getTigerBotsVisible().values().stream()
				.min(new BotDistanceComparator(pos))
				.map(ITrackedBot::getBotId)
				.orElse(BotID.noBot());
	}


	private BotID receiver(IVector2 pos)
	{
		if (chooseReceiverAutomatically)
		{
			return closestBotId(pos);
		}
		if (receiver >= 0)
		{
			return BotID.createBotId(receiver, getWFrame().getTeamColor());
		}
		return BotID.noBot();
	}


	private BotID shooter()
	{
		if (shooter >= 0)
		{
			var botId = BotID.createBotId(shooter, getWFrame().getTeamColor());
			if (getWFrame().getBot(botId) != null)
			{
				return botId;
			}
		}
		return closestBotId(pointOfInterest);
	}


	private void defenseThreatRating()
	{
		var rater = new DefenseThreatRater();
		drawFullField(pos -> rater.getThreatRatingOfPosition(pointOfInterest, pos));
		drawSinglePoint(pos -> rater.getThreatRatingOfPosition(pointOfInterest, pos));
	}


	private void bestGoalKickRating()
	{
		var rater = new BestGoalKickRater();
		rater.update(getWFrame());
		ToDoubleFunction<IVector2> ratingFunction = pos -> rater.rateKickOrigin(
						new KickOrigin(pos, shooter(), Double.POSITIVE_INFINITY)).goalKick()
				.map(e -> e.getRatedTarget().getScore())
				.orElse(0.0);
		drawFullField(ratingFunction);
		drawSinglePoint(ratingFunction);
	}


	private void passTargetRating()
	{
		var ratingFactory = new RatedPassFactory();
		var consideredBots = getWFrame().getBots().values().stream()
				// Ignore the keeper
				.filter(e -> e.getBotId() != getAiFrame().getKeeperId())
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				// Sort by distance to ball as rough estimate to get the closest bots first
				.sorted(new BotDistanceComparator(getBall().getPos()))
				.toList();
		var consideredBotsIntercept = consideredBots.stream().filter(e -> e.getBotId() != getAiFrame().getKeeperOpponentId()).toList();
		if (useDynamicInterceptionRating)
		{
			ratingFactory.updateDynamic(
					consideredBots,
					consideredBotsIntercept,
					passStats.get(),
					offensiveZones.get(),
					opponentMan2ManMarkers.get());
		} else
		{
			ratingFactory.update(consideredBots, consideredBotsIntercept, opponentMan2ManMarkers.get());
		}

		drawFullField(pos -> ratePassTargetRating(ratingFactory, pos, EMode.GRID));
		ratingFactory.setShapes(getShapes(EAiShapesLayer.TEST_GRID_ADDITIONAL));
		drawSinglePoint(pos -> ratePassTargetRating(ratingFactory, pos, EMode.SINGLE_POINT));
	}


	private double ratePassTargetRating(RatedPassFactory ratingFactory, IVector2 pos, EMode mode)
	{
		KickOrigin kickOrigin = new KickOrigin(pointOfInterest, shooter(), Double.POSITIVE_INFINITY);
		List<Pass> passes = passCreator.createPasses(kickOrigin, pos, receiver(pos))
				.stream()
				.filter(pass -> (pass.isChip() && considerChip) || (!pass.isChip() && considerStraight))
				.toList();
		if (mode == EMode.SINGLE_POINT)
		{
			passes.forEach(p -> getShapes(EAiShapesLayer.TEST_GRID_ADDITIONAL).addAll(
					p.createDrawables()
			));
		}
		return passes.stream()
				.mapToDouble(pass -> ratingFactory.rate(pass).getScore(passRating))
				.max()
				.orElse(0.0);
	}


	private void drawSinglePoint(final ToDoubleFunction<IVector2> ratingFunction)
	{
		if (!enableSinglePoint)
		{
			return;
		}
		double score = ratingFunction.applyAsDouble(singlePoint);
		Color color = colorPicker.getColor(score);
		getShapes(EAiShapesLayer.TEST_GRID_ADDITIONAL).add(new DrawablePoint(singlePoint, color));
		getShapes(EAiShapesLayer.TEST_GRID_ADDITIONAL)
				.add(new DrawableAnnotation(singlePoint, String.format("%.2f", score))
						.withFontHeight(12.5)
						.withCenterHorizontally(true)
						.withOffsetY(25));
	}


	private void drawFullField(final ToDoubleFunction<IVector2> ratingFunction)
	{
		if (!enableGrid)
		{
			return;
		}
		long start = System.nanoTime();
		getShapes(EAiShapesLayer.TEST_GRID_DEBUG).add(
				DrawableGrid.generate(numX, numY, Geometry.getFieldWidth(), Geometry.getFieldLength(), ratingFunction)
						.setColorPicker(colorPicker)
						.setDrawNumbers(drawNumbers)
		);
		if (printGridCalculationTime)
		{
			long end = System.nanoTime();
			long duration = (end - start) / 1_000_000;
			log.debug("Generating grid took {} ms", duration);
		}
	}


	private enum EGridType
	{
		BEST_GOAL_KICK_RATING,
		PASS_TARGET_RATING,
		DEFENSE_THREAT_RATING,
	}

	private enum EMode
	{
		GRID,
		SINGLE_POINT,
	}
}
