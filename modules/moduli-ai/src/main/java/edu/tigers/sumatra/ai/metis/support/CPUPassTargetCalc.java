/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2016
 * Author(s): Chris Carstensen
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.ProbabilityMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.support.data.AdvancedPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.support.LegalPointChecker;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Chris Carstensen
 */
public class CPUPassTargetCalc extends ACalculator
{
	// Weights
	@Configurable(comment = "Weight of AngleFactor")
	private static double	angleWeight						= 2;
	@Configurable(comment = "Weight of ScoreChance")
	private static double	scoreWeight						= 3;
	@Configurable(comment = "weight of time to target")
	private static double	timeWeight						= 2;
	@Configurable(comment = "weight of distanceToGoalScore")
	private static double	distanceToGoalScoreWeight	= 1;
	@Configurable(comment = "visibilityWeight")
	private static double	visibilityWeight				= 1;
	
	// Limits
	@Configurable(comment = "Upper time limit ")
	private static double	upperTimeLimit					= 2;
	@Configurable(comment = "Upper bound distance to goal")
	private static double	upperBoundDistanceToGoal	= 5000;
	@Configurable(comment = "Upperbound of openAngle")
	private static double	upperBoundOfOpenAngle		= 90;
	
	// Stuff for PasstargetPositions
	@Configurable(comment = "Distance between pass-target circles [mm]")
	private static int		circleDistance					= 300;
	@Configurable(comment = "Number of pass-target circles [1]")
	private static int		circleCount						= 3;
	@Configurable(comment = "velocityScaleFactor")
	
	// Factors and thresholds
	private static double	velocityScaleFactor			= 0.5;
	@Configurable(comment = "Factor of angle (static)")
	private static double	redirectAngleFactor			= 0.5;
	@Configurable(comment = "Threshhold deltaTimeToTarget")
	private static double	deltaTimeThreshhold			= -0.4;
	@Configurable(comment = "Line where no passTargets are calculated")
	private static double	passtargetLine					= 3000;
	
	// Rest
	@Configurable(comment = "Angle between pass targets on innermost circle [deg]")
	private static int		passTargetAngle				= 90;
	@Configurable(comment = "Maximum redirectangle (grad)")
	private static double	maxRedirectAngle				= 125;
	@Configurable(comment = "angleOfInterception")
	private static double	angleOfInterception			= 25;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<AdvancedPassTarget> advancedPassTargets = newTacticalField.getAdvancedPassTargetsRanked();
		
		for (ITrackedBot bot : baseAiFrame.getWorldFrame().getTigerBotsVisible().values())
		{
			// Skip Keeper and crucial defender
			if (baseAiFrame.getKeeperId().equals(bot.getBotId()))
			{
				continue;
			}
			if (newTacticalField.getCrucialDefenders().contains(bot.getBotId()))
			{
				continue;
			}
			
			// Generate and calculate passtargets for the other bots
			List<AdvancedPassTarget> currentBotAdvancedPassTargets = generatePassTargetPositions(bot, newTacticalField,
					baseAiFrame);
			
			calculateAdvancedPassTargets(bot, currentBotAdvancedPassTargets,
					newTacticalField, baseAiFrame);
			
			advancedPassTargets.addAll(currentBotAdvancedPassTargets);
		}
		advancedPassTargets.sort(ValuePoint.VALUE_HIGH_COMPARATOR);
		drawAdvancedPassTargets(newTacticalField, baseAiFrame);
	}
	
	
	private boolean isReachable(final AdvancedPassTarget target, final BaseAiFrame baseAiFrame)
	{
		return AiMath.p2pVisibility(baseAiFrame.getWorldFrame(),
				baseAiFrame.getWorldFrame().getTigerBotsVisible().get(target.getBotId()).getPos(), target,
				Geometry.getBotRadius() * 2, new ArrayList<BotID>());
	}
	
	
	/**
	 * Calculates the value for the given List of AdvancedPassTargets
	 * 
	 * @param bot - which passtargets should be calculated
	 * @param currentBotAdvancedPassTargetPositions - the Positions of the Passtargets (values will be overriden)
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @author ChrisC
	 */
	private List<AdvancedPassTarget> calculateAdvancedPassTargets(final ITrackedBot bot,
			final List<AdvancedPassTarget> currentBotAdvancedPassTargetPositions,
			final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		for (AdvancedPassTarget target : currentBotAdvancedPassTargetPositions)
		{
			// Calc time to Target
			double timeBotToTarget = new TrajectoryGenerator().generatePositionTrajectory(bot, target).getTotalTime();
			double minTimeFoeBotToTarget = Double.MAX_VALUE;
			
			for (ITrackedBot foeBot : baseAiFrame.getWorldFrame().foeBots.values())
			{
				BangBangTrajectory2D traj = new TrajectoryGenerator().generatePositionTrajectory(foeBot, target);
				double curTimeToTarget = traj.getTotalTime();
				
				if (curTimeToTarget < minTimeFoeBotToTarget)
				{
					minTimeFoeBotToTarget = curTimeToTarget;
				}
			}
			double deltaTimeToTarget = minTimeFoeBotToTarget - timeBotToTarget;
			
			// who is first at Target? True -> our Bot
			if (deltaTimeToTarget > deltaTimeThreshhold)
			{
				// Upper time limit
				if (deltaTimeToTarget > upperTimeLimit)
				{
					deltaTimeToTarget = upperTimeLimit;
				}
				
				double shootScoreChance = ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(),
						target.getXYVector(), false);
				
				// Value for distance
				double distanceToGoalScore = calcDistanceToGoalValue(target);
				
				// Value for Visibility
				double visibilityFactor = calcVisibilityValue(baseAiFrame, target);
				
				// Value for Redirect Angle
				double angleFactor = calcRedirectAngleValue(baseAiFrame, target);
				
				target.value = (distanceToGoalScore * distanceToGoalScoreWeight)
						+ ((deltaTimeToTarget / upperTimeLimit) * timeWeight)
						+ (visibilityFactor * visibilityWeight)
						+ (angleFactor * angleWeight)
						+ (shootScoreChance * scoreWeight);
				// Average
				target.value /= (distanceToGoalScoreWeight + timeWeight + visibilityWeight + angleWeight + scoreWeight);
			} else
			{
				// FOE is first at Target
				target.value = 0;
			}
		}
		return null;
	}
	
	
	private double calcDistanceToGoalValue(final IVector2 target)
	{
		double distanceToGoal = GeoMath.distancePP(target.getXYVector(),
				Geometry.getGoalTheir().getGoalCenter());
		if (distanceToGoal > upperBoundDistanceToGoal)
		{
			distanceToGoal = upperBoundDistanceToGoal;
		}
		double distanceToGoalScore = 1 - (distanceToGoal / upperBoundDistanceToGoal);
		return distanceToGoalScore;
	}
	
	
	private double calcRedirectAngleValue(final BaseAiFrame baseAiFrame, final IVector2 target)
	{
		double redirectAngle = AngleMath.normalizeAngle(GeoMath.angleBetweenVectorAndVector(
				target.subtractNew(Geometry.getGoalTheir().getGoalCenter()),
				target.subtractNew(baseAiFrame.getWorldFrame().getBall().getPos())));
		double angleFactor;
		if (Math.abs(redirectAngle) < AngleMath.deg2rad(maxRedirectAngle))
		{
			angleFactor = 1;
		} else
		{
			angleFactor = CPUPassTargetCalc.redirectAngleFactor;
		}
		return angleFactor;
	}
	
	
	private double calcVisibilityValue(final BaseAiFrame baseAiFrame, final IVector2 target)
	{
		// double visibilityFactor;
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		
		Line passLine = Line.newLine(ballPos, target);
		double minDistanceToPassLine = Double.MAX_VALUE;
		IVector2 nearestFoe = Geometry.getCenter();// maybe a better value?
		for (ITrackedBot foeBot : baseAiFrame.getWorldFrame().foeBots.values())
		{
			double distance = GeoMath.leadPointOnLine(foeBot.getPos(), passLine).subtractNew(foeBot.getPos()).getLength2();
			if ((minDistanceToPassLine > distance)
					&& passLine.isPointOnLine(GeoMath.leadPointOnLine(foeBot.getPos(), passLine), 1))
			{
				minDistanceToPassLine = distance;
				nearestFoe = foeBot.getPos();
			}
		}
		
		IVector2 ballToTarget = target.subtractNew(ballPos);
		IVector2 ballToNearestFoe = nearestFoe.subtractNew(ballPos);
		double angle = GeoMath.angleBetweenVectorAndVector(ballToTarget, ballToNearestFoe);
		if (angle > AngleMath.deg2rad(upperBoundOfOpenAngle))
		{
			angle = AngleMath.deg2rad(upperBoundOfOpenAngle);
		}
		angle /= AngleMath.deg2rad(upperBoundOfOpenAngle);
		return angle;
	}
	
	
	/**
	 * Generate the positions of the passtargets for a given bot
	 *
	 * @author ChrisC
	 */
	private List<AdvancedPassTarget> generatePassTargetPositions(final ITrackedBot bot,
			final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<AdvancedPassTarget> targetList = new ArrayList<AdvancedPassTarget>();
		
		IVector2 pos = bot.getBotKickerPos();
		IVector2 velOffset = bot.getVel().scaleToNew(velocityScaleFactor * bot.getVel().getLength2());
		// IVector2 velOffset = bot.getPosByTime(timeLookAhead).subtractNew(pos);
		pos = pos.addNew(velOffset);
		
		
		IVector2 baseDir = new Vector2(circleDistance, 0);
		double baseAngle = (passTargetAngle / 180.0) * Math.PI;
		int baseCount = 360 / passTargetAngle;
		
		
		AdvancedPassTarget centerTarget = new AdvancedPassTarget(pos, -1, false, bot.getBotId());
		if (isReachable(centerTarget, baseAiFrame)
				&& LegalPointChecker.checkPoint(centerTarget.getXYVector(), baseAiFrame, newTacticalField))
		{
			targetList.add(centerTarget);
		}
		for (int i = 1; i <= circleCount; i++)
		{
			IVector2 dir = baseDir.multiplyNew(i);
			double angle = baseAngle / i;
			
			for (int j = 0; j < (i * baseCount); j++)
			{
				AdvancedPassTarget curAdvancedPasstarget = new AdvancedPassTarget(pos.addNew(dir), -1, false,
						bot.getBotId());
				
				if (isReachable(curAdvancedPasstarget, baseAiFrame)
						&& LegalPointChecker.checkPoint(curAdvancedPasstarget.getXYVector(), baseAiFrame, newTacticalField)
						&& (curAdvancedPasstarget.x() > passtargetLine))
				{
					targetList.add(curAdvancedPasstarget);
				}
				dir = dir.turnNew(angle);
			}
		}
		
		return targetList;
	}
	
	
	private void drawAdvancedPassTargets(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final List<AdvancedPassTarget> passTargets = newTacticalField.getAdvancedPassTargetsRanked();
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.PASS_TARGETS);
		
		// Color orange = new Color(222, 222, 222, 255);
		Color pink = new Color(255, 0, 170, 100);
		Color magenta = new Color(255, 120, 100, 120);
		Color blue = new Color(20, 20, 220, 150);
		
		for (AdvancedPassTarget target : passTargets)
		{
			
			BotID botId = target.getBotId();
			ITrackedBot bot = baseAiFrame.getWorldFrame().getTigerBotsVisible().get(botId);
			
			IVector2 kickerPos = bot.getBotKickerPos();
			
			if (!kickerPos.equals(target))
			{
				shapes.add(new DrawableLine(Line.newLine(kickerPos, target), new Color(55, 55, 55, 70)));
			}
		}
		
		List<BotID> seenBots = new ArrayList<>();
		int i = 1;
		for (AdvancedPassTarget target : passTargets)
		{
			BotID botId = target.getBotId();
			
			final Color color;
			if (i == 1)
			{
				seenBots.add(botId);
				color = blue;
			} else
			{
				if (!seenBots.contains(botId))
				{
					seenBots.add(botId);
					color = pink;
				} else
				{
					color = magenta;
				}
			}
			
			DrawableCircle dTargetCircle = new DrawableCircle(target, 30, color);
			dTargetCircle.setFill(true);
			shapes.add(dTargetCircle);
			
			DrawableText dTxti = new DrawableText(target, Integer.toString(i), Color.black);
			dTxti.setFontSize(5);
			shapes.add(dTxti);
			
			DrawableText dTxtValue = new DrawableText(target, (Long.toString(Math.round(target.getValue() * 1000))),
					Color.black);
			dTxtValue.setFontSize(2);
			shapes.add(dTxtValue);
			
			i++;
		}
	}
}
