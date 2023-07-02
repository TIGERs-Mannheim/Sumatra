/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.ai.metis.pass.PassStats;
import edu.tigers.sumatra.ai.metis.pass.rating.DynamicPassInterceptionMovingRobotRater;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.FreeSpaceRater;
import edu.tigers.sumatra.ai.metis.pass.rating.IPassRater;
import edu.tigers.sumatra.ai.metis.pass.rating.PassInterceptionMovingRobotRater;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.ReflectorRater;
import edu.tigers.sumatra.ai.metis.targetrater.BestGoalKickRater;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
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

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;


public class DebugGridTestCalc extends ACalculator
{
	@Configurable(defValue = "OFF")
	private static EMode mode = EMode.OFF;

	@Configurable(defValue = "100")
	private static int numX = 100;
	@Configurable(defValue = "75")
	private static int numY = 75;

	@Configurable(defValue = "BEST_GOAL_KICK_RATING")
	private static EGridType type = EGridType.BEST_GOAL_KICK_RATING;

	@Configurable(defValue = "INTERCEPTION")
	private static EPassRating passRating = EPassRating.INTERCEPTION;

	@Configurable(defValue = "-1")
	private static DynamicPosition poi = new DynamicPosition(BallID.instance());

	@SuppressWarnings("MismatchedReadAndWriteOfArray") // the array changes through reflection
	@Configurable(defValue = "0 B; 0 Y")
	private static BotID[] ignoredBots = new BotID[0];

	@Configurable(defValue = "true")
	private static boolean drawNumbers = true;

	@Configurable(defValue = "0,0")
	private static IVector2 singlePoint = Vector2.zero();

	@Configurable(defValue = "STRAIGHT")
	private static EKickerDevice kickerDevice = EKickerDevice.STRAIGHT;

	@Configurable(defValue = "0.0")
	private static double minPassDuration = 0.0;

	private final Supplier<PassStats> passStats;

	private final Supplier<OffensiveZones> offensiveZones;

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratingFactory = new RatedPassFactory();
	private Collection<ITrackedBot> consideredBots;
	private IColorPicker colorPicker;
	private DynamicPosition pointOfInterest;


	public DebugGridTestCalc(
			Supplier<PassStats> passStats,
			Supplier<OffensiveZones> offensiveZones)
	{
		this.passStats = passStats;
		this.offensiveZones = offensiveZones;
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return mode != EMode.OFF;
	}


	@Override
	public void doCalc()
	{
		pointOfInterest = poi.update(getWFrame());
		ratingFactory.update(getWFrame().getOpponentBots().values());
		passFactory.update(getWFrame());
		colorPicker = ColorPickerFactory.greenRedGradient();
		switch (type)
		{
			case BEST_GOAL_KICK_RATING -> bestGoalKickRating();
			case MAX_ANGLE_GOAL_KICK_RATING -> maxAngleGoalKickRating();
			case PASS_INTERCEPTION_RATING -> passInterceptionRating();
			case PASS_INTERCEPTION_RATING_DYNAMIC -> passInterceptionRatingDynamic();
			case PASS_TARGET_RATING -> passTargetRating();
			case REFLECTOR_RATING -> reflectorRating();
			case FREE_SPACE_RATING -> freeSpaceRating();
			case DEFENSE_THREAT_RATING -> defenseThreatRating();
		}
	}


	private void freeSpaceRating()
	{
		consideredBots = getWFrame().getOpponentBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.toList();
		IPassRater passRater = new FreeSpaceRater(consideredBots);
		draw(pos -> passRater.rate(passFactory.straight(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot())));
	}


	private void defenseThreatRating()
	{
		var rater = new DefenseThreatRater();
		draw(pos -> rater.getThreatRating(pointOfInterest.getPos(), pos));
	}


	private void reflectorRating()
	{
		var rater = new ReflectorRater(getWFrame().getOpponentBots().values());
		draw(pos -> rater.rate(passFactory.straight(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot())));
	}


	private double getMaxRateOfChipOrStraightPass(IPassRater passRater, IVector2 pos)
	{
		return passFactory.passes(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot(), 0.0)
				.stream()
				.mapToDouble(passRater::rate)
				.max()
				.orElse(0.0);
	}


	private void bestGoalKickRating()
	{
		BotID bot = BotID.createBotId(3, ETeamColor.YELLOW);
		var rater = new BestGoalKickRater();
		rater.update(getWFrame());
		draw(pos -> rater.rateKickOrigin(new KickOrigin(pos, bot, Double.POSITIVE_INFINITY))
				.map(e -> e.getRatedTarget().getScore())
				.orElse(0.0));
	}


	private void maxAngleGoalKickRating()
	{
		draw(pos -> MaxAngleKickRater.getDirectShootScoreChance(getWFrame().getBots().values(), pos));
	}


	private void passInterceptionRating()
	{
		consideredBots = getWFrame().getOpponentBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.toList();
		IPassRater passRater = new PassInterceptionMovingRobotRater(consideredBots);
		draw(pos -> getMaxRateOfChipOrStraightPass(passRater, pos));
	}


	private void passInterceptionRatingDynamic()
	{
		consideredBots = getWFrame().getOpponentBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.toList();
		IPassRater passRater = new DynamicPassInterceptionMovingRobotRater(consideredBots, passStats.get(),
				offensiveZones.get());
		draw(pos -> getMaxRateOfChipOrStraightPass(passRater, pos));
	}


	private void passTargetRating()
	{
		draw(this::ratePassTargetRating);
	}


	private double ratePassTargetRating(final IVector2 pos)
	{
		Pass pass;
		if (kickerDevice == EKickerDevice.STRAIGHT)
		{
			pass = passFactory.straight(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot(), minPassDuration);
		} else
		{
			pass = passFactory.chip(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot(), minPassDuration);
		}
		if (mode == EMode.SINGLE_POINT)
		{
			ratingFactory.setShapes(getShapes(EAiShapesLayer.TEST_DEBUG_GRID));
		}
		var score = ratingFactory.rate(pass).getScore(passRating);
		ratingFactory.setShapes(null);
		return score;
	}


	private void draw(final ToDoubleFunction<IVector2> ratingFunction)
	{
		switch (mode)
		{
			case GRID -> drawFullField(ratingFunction);
			case SINGLE_POINT ->
			{
				double score = ratingFunction.applyAsDouble(singlePoint);
				Color color = colorPicker.getColor(score);
				getShapes(EAiShapesLayer.TEST_DEBUG_GRID).add(new DrawablePoint(singlePoint, color));
				getShapes(EAiShapesLayer.TEST_DEBUG_GRID)
						.add(new DrawableAnnotation(singlePoint, String.format("%.2f", score)).withOffset(Vector2.fromX(10)));
			}
			default ->
			{
				// nothing
			}
		}
	}


	private void drawFullField(final ToDoubleFunction<IVector2> ratingFunction)
	{
		getShapes(EAiShapesLayer.TEST_DEBUG_GRID).add(
				DrawableGrid.generate(numX, numY, Geometry.getFieldWidth(), Geometry.getFieldLength(), ratingFunction)
						.setColorPicker(colorPicker)
						.setDrawNumbers(drawNumbers)
		);
	}


	private enum EGridType
	{
		BEST_GOAL_KICK_RATING,
		MAX_ANGLE_GOAL_KICK_RATING,
		PASS_INTERCEPTION_RATING,
		PASS_INTERCEPTION_RATING_DYNAMIC,
		PASS_TARGET_RATING,
		REFLECTOR_RATING,
		FREE_SPACE_RATING,
		DEFENSE_THREAT_RATING,
	}

	private enum EMode
	{
		OFF,
		GRID,
		SINGLE_POINT,
	}
}
