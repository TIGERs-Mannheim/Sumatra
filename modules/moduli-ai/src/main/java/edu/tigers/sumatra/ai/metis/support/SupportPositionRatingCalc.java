/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class rates the prior generated SupportPositions
 */
public class SupportPositionRatingCalc extends ACalculator
{
	
	@Configurable(defValue = "0.5", comment = "Max time of interception")
	private static double maxTimeOfInterception = 0.5;
	
	@Configurable(defValue = "false", comment = "Fill the whole field with pass score")
	private static boolean drawWholeField = false;
	
	private List<IDrawableShape> fieldRatingShapes;
	private BotID passPlayerID;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		fieldRatingShapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.SUPPORTER_POSITION_FIELD_RATING);
		BotID primaryOffensive = getAiFrame().getKeeperId();
		if (newTacticalField.getOffensiveStrategy().getDesiredBots().iterator().hasNext())
		{
			primaryOffensive = newTacticalField.getOffensiveStrategy().getDesiredBots().iterator().next();
		}
		
		if (primaryOffensive != null)
		{
			passPlayerID = primaryOffensive;
			rateSupportPositions(newTacticalField.getGlobalSupportPositions());
			drawWholeField();
		}
	}
	
	
	private void rateSupportPositions(final List<SupportPosition> positions)
	{
		positions.forEach(pos -> pos.setShootScore(calcShootScore(pos)));
		positions.forEach(pos -> pos.setPassScore(calcPassScore(pos.getPos())));
	}
	
	
	private double calcShootScore(final SupportPosition pos)
	{
		List<ITrackedBot> foes = Collections.list(Collections.enumeration(getWFrame().getFoeBots().values()));
		
		double tDeflect = DefenseMath.calculateTDeflect(pos.getPos(), getBall().getPos(),
				DefenseMath.getBisectionGoal(getBall().getPos()));
		
		return BestDirectShotBallPossessingBot.getBestShot(Geometry.getGoalTheir(), pos.getPos(), foes, tDeflect)
				.orElse(new ValuePoint(Geometry.getGoalTheir().getCenter(), 0.)).getValue();
	}
	
	
	private double calcPassScore(final IVector2 pos)
	{
		Optional<ITrackedBot> offensiveBot = getWFrame().getTigerBotsVisible().values().stream()
				.min(Comparator.comparingDouble(a -> a.getPos().distanceToSqr(getBall().getPos())));
		return offensiveBot.map(iTrackedBot -> Math.min(calcMinFoePassSlackTime(iTrackedBot, pos),
				maxTimeOfInterception)).orElse(Double.NEGATIVE_INFINITY);
	}
	
	
	private double calcMinFoePassSlackTime(final ITrackedBot offensiveBot, final IVector2 target)
	{
		PassTarget passTarget = new PassTarget(target, passPlayerID);
		passTarget.setTimeReached(getWFrame().getTimestamp() + (long) 1e9);
		
		// pass speed for chip pass
		double passSpeedForChipDetection = getBall().getChipConsultant()
				.getInitVelForDistAtTouchdown(getWFrame().getBall().getPos().distanceTo(target), 4);
		
		boolean isChipKickRequired = OffensiveMath.isChipKickRequired(getWFrame(), passPlayerID, passTarget,
				passSpeedForChipDetection);
		
		List<ITrackedBot> consideredBots = Collections.list(Collections.enumeration(getWFrame().getFoeBots().values()))
				.stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperFoeId())
				.collect(Collectors.toList());
		
		if (PassTargetRatingCalc.isAlternativeDistanceRating())
		{
			return PassInterceptionRater.getScoreForPassAlternative(consideredBots, offensiveBot, passTarget, getBall(),
					getWFrame().getTimestamp());
		}
		
		return PassInterceptionRater.getScoreForPass(consideredBots, offensiveBot, passTarget, getBall(),
				getWFrame().getTimestamp(), isChipKickRequired);
	}
	
	
	private void drawWholeField()
	{
		if (drawWholeField)
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
			
			rateSupportPositions(visPositions);
			
			
			double[] data = visPositions.stream()
					.mapToDouble(p -> SumatraMath.relative(p.getPassScore(), -2, maxTimeOfInterception)).toArray();
			ValuedField field = new ValuedField(data, numX, numY, 0, height, width);
			fieldRatingShapes.add(field);
		}
	}
	
	
	public static double getMaxTimeOfInterception()
	{
		return maxTimeOfInterception;
	}
}
