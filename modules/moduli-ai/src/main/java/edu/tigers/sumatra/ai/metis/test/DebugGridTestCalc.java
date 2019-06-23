/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.ai.metis.targetrater.PassInterceptionRater;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class DebugGridTestCalc extends ACalculator
{
	@Configurable(defValue = "200")
	private static int numX = 200;
	@Configurable(defValue = "150")
	private static int numY = 150;
	@Configurable(defValue = "PASS_INTERCEPTION_RATING_STRAIGHT")
	private static EGridType type = EGridType.PASS_INTERCEPTION_RATING_STRAIGHT;
	
	@Configurable(defValue = "-1")
	private static DynamicPosition pointOfInterest = new DynamicPosition(BallID.instance());
	
	@SuppressWarnings("MismatchedReadAndWriteOfArray") // the array changes through reflection
	@Configurable(defValue = "0 B; 0 Y")
	private static BotID[] ignoredBots = new BotID[0];
	
	@Configurable(defValue = "false")
	private static boolean drawNumbers = false;
	
	private Collection<ITrackedBot> consideredBots;
	private double minValue;
	private double maxValue;
	private IColorPicker colorPicker;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		pointOfInterest.update(baseAiFrame.getWorldFrame());
		minValue = 0;
		maxValue = 1;
		colorPicker = ColorPickerFactory.greenRedGradient();
		switch (type)
		{
			case MAX_ANGLE_GOAL_KICK_RATING:
				maxAngleGoalKickRating();
				break;
			case PASS_INTERCEPTION_RATING_STRAIGHT:
				passInterceptionRatingStraight();
				break;
			case PASS_INTERCEPTION_RATING_CHIP:
				passInterceptionRatingChip();
				break;
			case CHIP_KICK_REASONABLE:
				chipKickReasonable();
				break;
			case PASS_SPEED_STRAIGHT:
				passSpeedStraight();
				break;
			case PASS_SPEED_CHIP:
				passSpeedChip();
				break;
			case PASS_END_VEL:
				passEndVel();
				break;
		}
	}
	
	
	private void maxAngleGoalKickRating()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		drawFullField(pos -> MaxAngleKickRater.getDirectShootScoreChance(getWFrame().getBots().values(), pos));
	}
	
	
	private void passInterceptionRatingStraight()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.collect(Collectors.toList());
		drawFullField(pos -> PassInterceptionRater.rateStraightPass(pointOfInterest, pos, consideredBots));
	}
	
	
	private void passInterceptionRatingChip()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.collect(Collectors.toList());
		drawFullField(pos -> PassInterceptionRater.rateChippedPass(pointOfInterest, pos, consideredBots));
	}
	
	
	private void chipKickReasonable()
	{
		consideredBots = getWFrame().getBots().values();
		
		drawFullField(this::rateChipkickReasonable);
	}
	
	
	private double rateChipkickReasonable(final IVector2 pos)
	{
		final double distance = pointOfInterest.distanceTo(pos);
		double passSpeedForChipDetection = OffensiveMath.passSpeedChip(distance);
		return new ChipKickReasonableDecider(
				pointOfInterest,
				pos,
				consideredBots,
				passSpeedForChipDetection)
						.isChipKickReasonable() ? 1 : 0;
	}
	
	
	private void passSpeedStraight()
	{
		maxValue = RuleConstraints.getMaxBallSpeed();
		drawFullField(pos -> OffensiveMath.passSpeedStraight(pointOfInterest, pos, Geometry.getGoalTheir().getCenter()));
	}
	
	
	private void passSpeedChip()
	{
		maxValue = RuleConstraints.getMaxBallSpeed();
		drawFullField(pos -> OffensiveMath.passSpeedChip(pointOfInterest.distanceTo(pos)));
	}
	
	
	private void passEndVel()
	{
		maxValue = RuleConstraints.getMaxBallSpeed();
		drawFullField(pos -> OffensiveMath.passEndVel(pointOfInterest, pos, Geometry.getGoalTheir().getCenter()));
	}
	
	
	private void drawFullField(final Function<IVector2, Double> ratingFunction)
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
				points.add(ratingFunction.apply(Vector2.fromXY(x, y)));
			}
		}
		
		
		double[] data = points.stream().mapToDouble(d -> d).toArray();
		ValuedField field = new ValuedField(data, numX, numY, 0);
		field.setDrawDebug(drawNumbers);
		field.setColorPicker(colorPicker);
		field.setMinValue(minValue);
		field.setMaxValue(maxValue);
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.TEST_DEBUG_GRID).add(field);
	}
	
	private enum EGridType
	{
		MAX_ANGLE_GOAL_KICK_RATING,
		PASS_INTERCEPTION_RATING_STRAIGHT,
		PASS_INTERCEPTION_RATING_CHIP,
		CHIP_KICK_REASONABLE,
		PASS_SPEED_STRAIGHT,
		PASS_SPEED_CHIP,
		PASS_END_VEL,
	}
}
