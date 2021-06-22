/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.pass.rating.PassInterceptionRater;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.PassReceiver;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * This class rates the prior generated SupportPositions
 */
@RequiredArgsConstructor
public class SupportPositionRatingCalc extends ACalculator
{
	@Configurable(defValue = "0.5", comment = "Max time of interception")
	private static double maxTimeOfInterception = 0.5;

	@Configurable(defValue = "false", comment = "Fill the whole field with pass score")
	private static boolean drawWholeField = false;

	@Configurable(comment = "Upper direct reflect threshold (rad)", defValue = "0.4")
	private static double upperDirectReflectThreshold = 0.4;

	@Configurable(comment = "Max angle to stop and control a ball (rad)", defValue = "3.14")
	private static double maxBallAcceptAngle = 3.14;

	@Configurable(comment = "Max time to turn and shoot a ball directed (s)", defValue = "0.3")
	private static double maxBallAcceptTime = 0.3;

	private final PassFactory passFactory = new PassFactory();
	private AngleRangeRater targetRater;

	private final Supplier<OffensiveStrategy> offensiveStrategy;
	private final Supplier<List<SupportPosition>> globalSupportPositions;


	@Override
	public void doCalc()
	{
		targetRater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		targetRater.setObstacles(getWFrame().getOpponentBots().values());
		targetRater.setTimeToKick(0);

		IVector2 alternativeBallPos = getBall().getPos();
		rateSupportPositions(globalSupportPositions.get(), alternativeBallPos);

		if (drawWholeField)
		{
			drawWholeField();
		}
	}


	@Override
	public boolean isCalculationNecessary()
	{
		return PassReceiver.isActive();
	}


	private void rateSupportPositions(final List<SupportPosition> positions, final IVector2 passOrigin)
	{
		positions.forEach(pos -> pos.setShootScore(shootScore(pos)));
		// prefer shoot score over pass score by reducing pass score to at most 0.5
		positions.forEach(pos -> pos.setPassScore(0.5 * passScore(passOrigin, pos.getPos())));
	}


	private double shootScore(final SupportPosition supportPosition)
	{
		double tDeflect = calculateTDeflect(supportPosition.getPos(), getBall().getPos(),
				Geometry.getGoalOur().bisection(getBall().getPos()));

		targetRater.setTimeToKick(tDeflect);
		return targetRater.rate(supportPosition.getPos()).map(IRatedTarget::getScore).orElse(0.0);
	}


	/**
	 * Time a bot needs to catch a ball and rotate to a new angle
	 *
	 * @param botPos
	 * @param ballPos
	 * @param target
	 * @return
	 */
	private double calculateTDeflect(final IVector2 botPos, final IVector2 ballPos, final IVector2 target)
	{
		final IVector2 bot2ball = Vector2.fromPoints(botPos, ballPos);
		final IVector2 bot2target = Vector2.fromPoints(botPos, target);

		final double angleDiff = bot2ball.angleToAbs(bot2target).orElse(Math.PI);

		double factor = SumatraMath.relative(angleDiff, upperDirectReflectThreshold, maxBallAcceptAngle);

		return factor * maxBallAcceptTime;
	}


	private double passScore(final IVector2 passOrigin, final IVector2 passTarget)
	{
		List<ITrackedBot> consideredBots = getWFrame().getOpponentBots().values().stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperOpponentId())
				.collect(Collectors.toList());

		passFactory.update(getWFrame());
		var shooter = offensiveStrategy.get().getAttackerBot().orElse(getAiFrame().getKeeperId());
		var receiver = BotID.noBot();
		var chipPass = passFactory.chip(passOrigin, passTarget, shooter, receiver);
		var straightPass = passFactory.straight(passOrigin, passTarget, shooter, receiver);
		var passRater = new PassInterceptionRater(consideredBots);
		var chipScore = passRater.rate(chipPass);
		var straightScore = passRater.rate(straightPass);
		return Math.max(chipScore, straightScore);
	}


	private void drawWholeField()
	{
		double width = Geometry.getFieldWidth();
		double height = Geometry.getFieldLength();
		int numX = 200;
		int numY = 150;
		List<SupportPosition> visPositions = new ArrayList<>();
		for (int iy = 0; iy < numY; iy++)
		{
			for (int ix = 0; ix < numX; ix++)
			{
				double x = (-height / 2) + (ix * (height / (numX - 1)));
				double y = (-width / 2) + (iy * (width / (numY - 1)));
				SupportPosition passTarget = new SupportPosition(Vector2.fromXY(x, y), getWFrame().getTimestamp());
				visPositions.add(passTarget);
			}
		}

		rateSupportPositions(visPositions, getBall().getPos());


		double[] data = visPositions.stream()
				.mapToDouble(p -> SumatraMath.relative(p.getPassScore(), -2, maxTimeOfInterception)).toArray();
		ValuedField field = new ValuedField(data, numX, numY, 0);
		getShapes(EAiShapesLayer.SUPPORTER_POSITION_FIELD_RATING).add(field);
	}
}
