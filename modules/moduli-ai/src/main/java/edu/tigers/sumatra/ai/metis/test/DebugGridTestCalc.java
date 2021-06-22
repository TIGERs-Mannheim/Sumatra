/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatRater;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.FreeSpaceRater;
import edu.tigers.sumatra.ai.metis.pass.rating.IPassRater;
import edu.tigers.sumatra.ai.metis.pass.rating.PassInterceptionRater;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.ReflectorRater;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;


public class DebugGridTestCalc extends ACalculator
{
	@Configurable(defValue = "OFF")
	private static EMode mode = EMode.OFF;

	@Configurable(defValue = "100")
	private static int numX = 100;
	@Configurable(defValue = "75")
	private static int numY = 75;

	@Configurable(defValue = "PASS_TARGET_RATING")
	private static EGridType type = EGridType.PASS_TARGET_RATING;

	@Configurable(defValue = "PASS_DURATION")
	private static EPassRating passRating = EPassRating.PASS_DURATION;

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

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratingFactory = new RatedPassFactory();
	private Collection<ITrackedBot> consideredBots;
	private double minValue;
	private double maxValue;
	private IColorPicker colorPicker;
	private DynamicPosition pointOfInterest;


	@Override
	protected boolean isCalculationNecessary()
	{
		return mode != EMode.OFF;
	}


	@SuppressWarnings("squid:MethodCyclomaticComplexity") // long switch-case
	@Override
	public void doCalc()
	{
		pointOfInterest = poi.update(getWFrame());
		ratingFactory.update(getWFrame().getOpponentBots().values());

		minValue = 0;
		maxValue = 1;
		colorPicker = ColorPickerFactory.greenRedGradient();
		switch (type)
		{
			case MAX_ANGLE_GOAL_KICK_RATING:
				maxAngleGoalKickRating();
				break;
			case PASS_INTERCEPTION_RATING:
				passInterceptionRating();
				break;
			case PASS_TARGET_RATING:
				passTargetRating();
				break;
			case REFLECTOR_RATING:
				reflectorRating();
				break;
			case FREE_SPACE_RATING:
				freeSpaceRating();
				break;
			case DEFENSE_THREAT_RATING:
				defenseThreatRating();
				break;
		}
	}


	private void freeSpaceRating()
	{
		consideredBots = getWFrame().getOpponentBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.collect(Collectors.toList());
		IPassRater passRater = new FreeSpaceRater(consideredBots);
		draw(pos -> {
			var pass = passFactory.straight(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot());
			return passRater.rate(pass);
		});
	}


	private void defenseThreatRating()
	{
		DefenseThreatRater rater = new DefenseThreatRater();
		draw(pos -> rater.getThreatRating(pointOfInterest.getPos(), pos));
	}


	private void reflectorRating()
	{
		ReflectorRater rater = new ReflectorRater(getWFrame().getOpponentBots().values());
		draw(pos -> {
			var pass = passFactory.straight(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot());
			return rater.rate(pass);
		});
	}


	private double getMaxRateOfChipOrStraightPass(IPassRater passRater, IVector2 pos)
	{
		return passFactory.passes(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot(), 0.0)
				.stream()
				.mapToDouble(passRater::rate)
				.max()
				.orElse(0.0);
	}


	private void maxAngleGoalKickRating()
	{
		draw(pos -> MaxAngleKickRater.getDirectShootScoreChance(getWFrame().getBots().values(), pos));
	}


	private void passInterceptionRating()
	{
		consideredBots = getWFrame().getOpponentBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.collect(Collectors.toList());
		IPassRater passRater = new PassInterceptionRater(consideredBots);
		draw(pos -> getMaxRateOfChipOrStraightPass(passRater, pos));
	}


	private void passTargetRating()
	{
		draw(this::ratePassTargetRating);
	}


	private double ratePassTargetRating(final IVector2 pos)
	{
		passFactory.update(getWFrame());
		Pass pass;
		if (kickerDevice == EKickerDevice.STRAIGHT)
		{
			pass = passFactory.straight(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot(), 0.0);
		} else
		{
			pass = passFactory.chip(pointOfInterest.getPos(), pos, BotID.noBot(), BotID.noBot(), 0.0);
		}
		var score = ratingFactory.rate(pass).getScore(passRating);
		if (mode == EMode.SINGLE_POINT)
		{
			getShapes(EAiShapesLayer.TEST_DEBUG_GRID).addAll(ratingFactory.createShapes());
		}
		return score;
	}


	private void draw(final ToDoubleFunction<IVector2> ratingFunction)
	{
		if (mode == EMode.GRID)
		{
			drawFullField(ratingFunction);
		} else
		{
			double score = ratingFunction.applyAsDouble(singlePoint);
			Color color = colorPicker.getColor(score);
			getShapes(EAiShapesLayer.TEST_DEBUG_GRID).add(new DrawablePoint(singlePoint, color));
			getShapes(EAiShapesLayer.TEST_DEBUG_GRID)
					.add(new DrawableAnnotation(singlePoint, String.format("%.2f", score)).withOffset(Vector2.fromX(10)));
		}
	}


	private void drawFullField(final ToDoubleFunction<IVector2> ratingFunction)
	{
		double width = Geometry.getFieldWidth();
		double height = Geometry.getFieldLength();
		List<Double> points = new ArrayList<>();
		for (int iy = 0; iy < numY; iy++)
		{
			for (int ix = 0; ix < numX; ix++)
			{
				double x = (-height / 2) + (ix * (height / (numX - 1)));
				double y = (-width / 2) + (iy * (width / (numY - 1)));
				points.add(ratingFunction.applyAsDouble(Vector2.fromXY(x, y)));
			}
		}

		double[] data = points.stream().mapToDouble(d -> d).toArray();
		ValuedField field = new ValuedField(data, numX, numY, 0);
		field.setDrawDebug(drawNumbers);
		field.setColorPicker(colorPicker);
		field.setMinValue(minValue);
		field.setMaxValue(maxValue);
		getShapes(EAiShapesLayer.TEST_DEBUG_GRID).add(field);
	}


	private enum EGridType
	{
		MAX_ANGLE_GOAL_KICK_RATING,

		PASS_INTERCEPTION_RATING,

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
