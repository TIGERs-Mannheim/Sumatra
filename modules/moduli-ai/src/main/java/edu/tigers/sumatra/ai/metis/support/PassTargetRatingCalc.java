/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.ai.metis.support.PassTargetGenerationCalc.getFullFieldVisualizationBotId;
import static edu.tigers.sumatra.math.SumatraMath.min;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.math.kick.BestDirectShotBallPossessingBot;
import edu.tigers.sumatra.ai.math.kick.PassInterceptionRater;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class rates the PassTargets created by PassTargetGenerationCalc
 */
public class PassTargetRatingCalc extends ACalculator
{
	// Other configurable
	@Configurable(comment = "Upper x-ball position for Situation weight", defValue = "2500")
	private static double upperBallSituationPosition = 2500;
	
	@Configurable(comment = "Lower x-ball position for situation weight", defValue = "-2500")
	private static double lowerBallSituationPosition = -2500;
	
	@Configurable(comment = "ReceiveWeightBias (guaranteed percentage of receive weight)", defValue = "0.3")
	private static double receiveWeightBias = 0.3;
	
	@Configurable(defValue = "10")
	private static double redirectAngleSafety = 10;
	
	@Configurable(comment = "Use distance passed pass target rating", defValue = "false")
	private static boolean alternativeDistanceRating = false;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (getWFrame().getTigerBotsVisible().isEmpty())
		{
			return;
		}
		final List<WeightedScoreFunction> receiveRatingFunctions = new ArrayList<>();
		receiveRatingFunctions
				.add(new WeightedScoreFunction(this::pBallReachesPassTarget, 1));
		
		final List<WeightedScoreFunction> shootRatingFunctions = new ArrayList<>();
		shootRatingFunctions.add(new WeightedScoreFunction(this::pBotCanScoreGoal, 1));
		
		for (PassTarget passTarget : newTacticalField.getAllPassTargets())
		{
			passTarget.setReceiveScore(calcWeightedScore(receiveRatingFunctions, passTarget));
			passTarget.setShootScore(calcWeightedScore(shootRatingFunctions, passTarget));
			double situationWeight = calcSituationShootWeight();
			passTarget.setScore(
					(passTarget.getReceiveScore() * (1 - situationWeight)) + (passTarget.getShootScore() * situationWeight));
		}
		drawFullField(newTacticalField);
	}
	
	
	private double calcSituationShootWeight()
	{
		double ballX = getBall().getPos().x();
		double situationScoreWeight = SumatraMath.relative(ballX, lowerBallSituationPosition, upperBallSituationPosition);
		situationScoreWeight = min(situationScoreWeight, 1 - receiveWeightBias);
		return situationScoreWeight;
		
	}
	
	
	private double calcWeightedScore(final List<WeightedScoreFunction> scoreFunctions, final IPassTarget passTarget)
	{
		List<Double> scores = scoreFunctions.stream()
				.map(rf -> rf.apply(passTarget))
				.collect(Collectors.toList());
		
		passTarget.getIntermediateScores().addAll(scores);
		
		double sumWeights = scoreFunctions.stream().map(WeightedScoreFunction::getWeight).reduce(0.0,
				(a, b) -> a + b);
		
		double score = scores.stream().reduce(1d, (a, b) -> a * b);
		if (!SumatraMath.isEqual(sumWeights, 1.0, 1e-3))
		{
			score = pow(score, 1.0 / sumWeights); // geometric mean
		}
		return score;
	}
	
	
	private double pBallReachesPassTarget(final IPassTarget passTarget)
	{
		ITrackedBot offensiveBot = getWFrame().getTigerBotsVisible().values().stream()
				.min(Comparator.comparingDouble(a -> a.getPos().distanceToSqr(getBall().getPos())))
				.orElse(getWFrame().getBot(getAiFrame().getKeeperId()));
		
		if (offensiveBot == null)
		{
			return 0;
		}
		
		double passSpeedForChipDetection = getBall().getChipConsultant()
				.getInitVelForDistAtTouchdown(getWFrame().getBall().getPos().distanceTo(passTarget.getKickerPos()), 4);
		boolean isChipKickRequired = OffensiveMath.isChipKickRequired(getWFrame(), passTarget.getBotId(), passTarget,
				passSpeedForChipDetection);
		
		List<ITrackedBot> consideredBots = Collections.list(Collections.enumeration(getWFrame().getFoeBots().values()))
				.stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperFoeId())
				.collect(Collectors.toList());
		
		if (alternativeDistanceRating)
		{
			return PassInterceptionRater.getScoreForPassAlternative(consideredBots, offensiveBot, passTarget, getBall(),
					getWFrame().getTimestamp());
		}
		
		return PassInterceptionRater.getScoreForPass(consideredBots, offensiveBot, passTarget, getBall(),
				getWFrame().getTimestamp(),
				isChipKickRequired);
	}
	
	
	private double pBotCanScoreGoal(final IPassTarget passTarget)
	{
		List<ITrackedBot> foes = Collections.list(Collections.enumeration(getWFrame().getFoeBots().values()));
		
		double tDeflect = DefenseMath.calculateTDeflect(passTarget.getKickerPos(), getBall().getPos(),
				DefenseMath.getBisectionGoal(getBall().getPos()));
		
		return BestDirectShotBallPossessingBot
				.getBestShot(Geometry.getGoalTheir(), passTarget.getKickerPos(), foes, tDeflect)
				.orElse(new ValuePoint(Geometry.getGoalTheir().getCenter(), 0.)).getValue();
	}
	
	
	private void drawFullField(final TacticalField newTacticalField)
	{
		if (getFullFieldVisualizationBotId() >= 0)
		{
			double width = Geometry.getFieldWidth();
			double height = Geometry.getFieldLength();
			int numX = 200;
			int numY = 150;
			double[] data = newTacticalField.getAllPassTargets().stream().mapToDouble(PassTarget::getScore).toArray();
			ValuedField field = new ValuedField(data, numX, numY, 0, height, width);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS_GRID).add(field);
		}
	}
	
	
	/**
	 * @return the alternativeDistanceRating
	 */
	public static boolean isAlternativeDistanceRating()
	{
		return alternativeDistanceRating;
	}
}
