/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatRater;
import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTargetRating;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTargetRatingFactory;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTargetRatingFactoryInput;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.FreeSpaceRater;
import edu.tigers.sumatra.ai.metis.targetrater.IPassRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.ai.metis.targetrater.PassInterceptionRater;
import edu.tigers.sumatra.ai.metis.targetrater.ReflectorRater;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class DebugGridTestCalc extends ACalculator
{
	@Configurable(defValue = "200")
	private static int numX = 200;
	@Configurable(defValue = "150")
	private static int numY = 150;

	@Configurable(defValue = "PASS_INTERCEPTION_RATING")
	private static EGridType type = EGridType.PASS_INTERCEPTION_RATING;

	@Configurable(defValue = "-1")
	private static DynamicPosition pointOfInterest = new DynamicPosition(BallID.instance());

	@SuppressWarnings("MismatchedReadAndWriteOfArray") // the array changes through reflection
	@Configurable(defValue = "0 B; 0 Y")
	private static BotID[] ignoredBots = new BotID[0];

	@Configurable(defValue = "false")
	private static boolean drawNumbers = false;

	@Configurable(defValue = "500", comment = "[mm] line segment length/2 for AngleRangeRater")
	private static double halfSegmentLength = 500;

	private Collection<ITrackedBot> consideredBots;
	private double maxChipSpeed;
	private double minValue;
	private double maxValue;
	private IColorPicker colorPicker;


	@SuppressWarnings("squid:MethodCyclomaticComplexity") // long switch-case
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		pointOfInterest.update(baseAiFrame.getWorldFrame());
		maxChipSpeed = getNewTacticalField().getTigerClosestToBall().getBot().getRobotInfo().getBotParams()
				.getKickerSpecs().getMaxAbsoluteChipVelocity();

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
			case ANGLE_RANGE_PASS_RATING:
				angleRangeRating();
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
			case PASS_END_VEL_STRAIGHT_PLANNED:
				passEndVelPlanned();
				break;
			case PASS_END_VEL_CHIP_ACTUAL:
				passEndVelChip();
				break;
			case PASS_END_VEL_STRAIGHT_ACTUAL:
				passEndVelActual();
				break;
			case PASS_END_VEL_STRAIGHT_MAX:
				passEndVelMax();
				break;
			case PASS_MAX_DURATION_STRAIGHT:
				passDurationStraight();
				break;
			case PASS_MAX_DURATION_CHIP:
				passDurationChip();
				break;
			case PASS_TARGET_RATING_PASS:
			case PASS_TARGET_RATING_CHIP:
			case PASS_TARGET_RATING_STRAIGHT:
			case PASS_TARGET_RATING_GOAL:
			case PASS_TARGET_RATING_PRESSURE:
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
		colorPicker = ColorPickerFactory.invert(colorPicker);
		consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.collect(Collectors.toList());
		IPassRater passRater = new FreeSpaceRater(consideredBots);
		drawFullField(pos -> passRater.rateStraightPass(pointOfInterest.getPos(), pos));
	}


	private void defenseThreatRating()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		DefenseThreatRater rater = new DefenseThreatRater();
		drawFullField(pos -> rater.getThreatRating(pointOfInterest.getPos(), pos));
	}


	private void reflectorRating()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		ReflectorRater rater = new ReflectorRater(getBall().getStraightConsultant(), getWFrame().getFoeBots());
		drawFullField(pos -> rater.rateTarget(pointOfInterest.getPos(), pos));
	}


	private void angleRangeRating()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.collect(Collectors.toList());

		drawFullField(this::ratePassAngleRange);
	}


	private double ratePassAngleRange(final IVector2 pos)
	{
		IVector2 direction = Line.fromPoints(pointOfInterest.getPos(), pos).directionVector().getNormalVector()
				.scaleTo(halfSegmentLength);
		final ILineSegment poiToPos = Lines.segmentFromPoints(
				pointOfInterest.getPos().addNew(direction),
				pointOfInterest.getPos().subtractNew(direction));
		AngleRangeRater rangeRater = AngleRangeRater.forLineSegment(poiToPos);
		rangeRater.setStraightBallConsultant(getBall().getStraightConsultant());
		rangeRater.setObstacles(consideredBots);

		return rangeRater.rate(pos).map(IRatedTarget::getScore).orElse(0.0);
	}


	private double getMaxRateOfChipOrStraightPass(IPassRater passRater, IVector2 pos)
	{
		return Math.max(passRater.rateChippedPass(pointOfInterest.getPos(), pos, maxChipSpeed),
				passRater.rateStraightPass(pointOfInterest.getPos(), pos));
	}


	private void maxAngleGoalKickRating()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		drawFullField(pos -> MaxAngleKickRater.getDirectShootScoreChance(getWFrame().getBots().values(), pos));
	}


	private void passInterceptionRating()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> !Arrays.asList(ignoredBots).contains(b.getBotId()))
				.collect(Collectors.toList());
		IPassRater passRater = new PassInterceptionRater(consideredBots);
		drawFullField(pos -> getMaxRateOfChipOrStraightPass(passRater, pos));
	}


	private void chipKickReasonable()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		consideredBots = getWFrame().getBots().values();

		drawFullField(this::rateChipkickReasonable);
	}


	private double rateChipkickReasonable(final IVector2 pos)
	{
		final double distance = pointOfInterest.getPos().distanceTo(pos);
		double passSpeedForChipDetection = OffensiveMath.passSpeedChip(distance, maxChipSpeed);
		return new ChipKickReasonableDecider(
				pointOfInterest.getPos(),
				pos,
				consideredBots,
				passSpeedForChipDetection)
						.isChipKickReasonable() ? 1 : 0;
	}


	private void passSpeedStraight()
	{
		maxValue = RuleConstraints.getMaxBallSpeed();
		drawFullField(
				pos -> OffensiveMath.passSpeedStraight(pointOfInterest.getPos(), pos, Geometry.getGoalTheir().getCenter()));
	}


	private void passSpeedChip()
	{
		maxValue = maxChipSpeed;
		drawFullField(pos -> OffensiveMath.passSpeedChip(pointOfInterest.getPos().distanceTo(pos), maxChipSpeed));
	}


	private void passEndVelPlanned()
	{
		maxValue = RuleConstraints.getMaxBallSpeed();
		drawFullField(
				pos -> OffensiveMath.passEndVel(pointOfInterest.getPos(), pos, Geometry.getGoalTheir().getCenter()));
	}


	private void passEndVelActual()
	{
		maxValue = RuleConstraints.getMaxBallSpeed();
		drawFullField(this::ratePassEndVelActual);
	}


	private double ratePassEndVelActual(IVector2 pos)
	{
		final double passSpeed = OffensiveMath.passSpeedStraight(pointOfInterest.getPos(), pos,
				Geometry.getGoalTheir().getCenter());
		double distance = pointOfInterest.getPos().distanceTo(pos);
		final double travelTime = getBall().getStraightConsultant().getTimeForKick(distance, passSpeed);
		return getBall().getStraightConsultant().getVelForKickByTime(passSpeed, travelTime);
	}


	private void passEndVelMax()
	{
		maxValue = RuleConstraints.getMaxBallSpeed();
		drawFullField(this::ratePassEndVelMax);
	}


	private double ratePassEndVelMax(IVector2 pos)
	{
		final double passSpeed = RuleConstraints.getMaxBallSpeed();
		double distance = pointOfInterest.getPos().distanceTo(pos);
		final double travelTime = getBall().getStraightConsultant().getTimeForKick(distance, passSpeed);
		return getBall().getStraightConsultant().getVelForKickByTime(passSpeed, travelTime);
	}


	private void passEndVelChip()
	{
		maxValue = maxChipSpeed;
		drawFullField(this::ratePassEndVelChip);
	}


	private double ratePassEndVelChip(IVector2 pos)
	{
		final double distance = pointOfInterest.getPos().distanceTo(pos);
		double passSpeed = OffensiveMath.passSpeedChip(distance, maxChipSpeed);
		return getBall().getChipConsultant().getVelForKick(distance, passSpeed);
	}


	private void passDurationStraight()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		maxValue = 5;

		drawFullField(this::ratePassDurationStraight);
	}


	private double ratePassDurationStraight(final IVector2 pos)
	{
		double passVelocity = OffensiveMath.passSpeedStraight(
				pointOfInterest.getPos(),
				pos,
				Geometry.getGoalTheir().getCenter());
		double distance = pointOfInterest.getPos().distanceTo(pos);
		return getBall().getStraightConsultant().getTimeForKick(distance, passVelocity);
	}


	private void passDurationChip()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		maxValue = 5;

		drawFullField(this::ratePassDurationChip);
	}


	private double ratePassDurationChip(final IVector2 pos)
	{
		double distance = pointOfInterest.getPos().distanceTo(pos);
		double passVelocity = OffensiveMath.passSpeedChip(distance, maxChipSpeed);
		return getBall().getChipConsultant().getTimeForKick(distance, passVelocity);
	}


	private void passTargetRating()
	{
		colorPicker = ColorPickerFactory.invert(colorPicker);
		PassTargetRatingFactoryInput passRaterParameters = PassTargetRatingFactoryInput.fromAiFrame(getAiFrame());
		consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperFoeId())
				.collect(Collectors.toList());
		IPassRater rater = new PassInterceptionRater(consideredBots);
		drawFullField(p -> ratePassTargetRating(p, passRaterParameters, rater));
	}


	private double ratePassTargetRating(
			final IVector2 pos,
			final PassTargetRatingFactoryInput ratingFactoryInput,
			final IPassRater passRater)
	{
		PassTargetRatingFactory ratingFactory = new PassTargetRatingFactory();
		IPassTarget passTarget = new PassTarget(new DynamicPosition(pos), BotID.noBot());
		IPassTargetRating passTargetRating = ratingFactory.ratingFromPassTargetAndInput(passTarget, passRater,
				ratingFactoryInput);
		switch (type)
		{
			case PASS_TARGET_RATING_GOAL:
				return passTargetRating.getGoalKickScore();
			case PASS_TARGET_RATING_PASS:
				return passTargetRating.getPassScore();
			case PASS_TARGET_RATING_CHIP:
				return passTargetRating.getPassScoreChip() * passTargetRating.getDurationScoreChip();
			case PASS_TARGET_RATING_STRAIGHT:
				return passTargetRating.getPassScoreStraight() * passTargetRating.getDurationScoreStraight();
			case PASS_TARGET_RATING_PRESSURE:
				return passTargetRating.getPressureScore();
			default:
				throw new IllegalStateException("Invalid type: " + type);
		}
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

		PASS_INTERCEPTION_RATING,

		CHIP_KICK_REASONABLE,

		PASS_SPEED_STRAIGHT,
		PASS_SPEED_CHIP,

		PASS_END_VEL_STRAIGHT_PLANNED,
		PASS_END_VEL_CHIP_ACTUAL,
		PASS_END_VEL_STRAIGHT_ACTUAL,
		PASS_END_VEL_STRAIGHT_MAX,

		ANGLE_RANGE_PASS_RATING,

		PASS_MAX_DURATION_STRAIGHT,
		PASS_MAX_DURATION_CHIP,

		PASS_TARGET_RATING_GOAL,
		PASS_TARGET_RATING_PASS,
		PASS_TARGET_RATING_CHIP,
		PASS_TARGET_RATING_PRESSURE,
		PASS_TARGET_RATING_STRAIGHT,

		REFLECTOR_RATING,
		FREE_SPACE_RATING,

		DEFENSE_THREAT_RATING,
	}
}
