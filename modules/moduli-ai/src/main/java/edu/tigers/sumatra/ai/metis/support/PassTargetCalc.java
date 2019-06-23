/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2013
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.ProbabilityMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.support.data.AdvancedPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.support.LegalPointChecker;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates clever pass targets for the support role/redirect skill to recieve a pass.
 * Part of the implementation of intelligent supportive behavior for Small Size League Soccer Robots.
 * 
 * @author JulianT
 * @author TilmanS
 */
public class PassTargetCalc extends ACalculator
{
	@Configurable(comment = "Distance we can chip over other bots [mm]")
	private static int					overBotChipDistance			= 1000;
																					
	@Configurable(comment = "Minimum length of a pass [mm]")
	private static int					minPassLength					= 1000;
																					
	@Configurable(comment = "Number of points on one circle of advanced pass targets")
	private static int					angleSteps						= 8;
																					
																					
	@Configurable(comment = "Number of points on one advanced pass target ray")
	private static int					distanceSteps					= 3;
																					
	@Configurable(comment = "Maximum distance between advanced pass targets and bot [mm]")
	private static double				maxDistance						= 800;
																					
	@Configurable(comment = "Time to wait before bot will be considered marked [ms]")
	private static int					markedWaitTime					= 750;
																					
	@Configurable(comment = "Will be aplied to all advanced pass target scores of marked bots")
	private static double				markedBotFactor				= 0.5;
																					
	@Configurable(comment = "Only bots within this distance will be considered as markers [mm]")
	private static int					maxMarkerDistance				= 1000;
																					
	@Configurable(comment = "Opponent bots whithin this radius from the kicker will be considered as additional markers [mm]")
	private static int					maxSurroundingDistance		= 500;
																					
	@Configurable(comment = "Do not play any passes back beyond this part in the back of our field")
	private static double				criticalBackPassThreshold	= 0.5;
																					
	@Configurable(comment = "Factor for velocity offset")
	private static double				velocityPositionOffest		= 900;
																					
	private final Map<BotID, Long>	lastUnmarkedTime;
												
												
	// private static final Logger log = Logger.getLogger(SupportPositionCalc.class.getName());
	
	
	/**
	 * Initialize last unmarked times
	 */
	public PassTargetCalc()
	{
		super();
		lastUnmarkedTime = new HashMap<BotID, Long>();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<AdvancedPassTarget> advancedPassTargets = newTacticalField.getAdvancedPassTargetsRanked();
		
		for (ITrackedBot bot : baseAiFrame.getWorldFrame().getTigerBotsVisible().values())
		{
			if (baseAiFrame.getKeeperId().equals(bot.getBotId()))
			{
				continue;
			}
			if (newTacticalField.getCrucialDefenders().contains(bot.getBotId()))
			{
				continue;
			}
			
			List<AdvancedPassTarget> currentBotAdvancedPassTargets = calculateAdvancedPassTargets(bot,
					newTacticalField, baseAiFrame);
					
			if (isMarked(bot, baseAiFrame))
			{
				currentBotAdvancedPassTargets.stream().forEach(apt -> apt.setValue(markedBotFactor * apt.getValue()));
			}
			
			advancedPassTargets.addAll(currentBotAdvancedPassTargets);
		}
		
		advancedPassTargets.sort(ValuePoint.VALUE_HIGH_COMPARATOR);
		drawAdvancedPassTargets(newTacticalField, baseAiFrame);
	}
	
	
	/**
	 * Calculates a certain number of advanced pass targets up to stepsAngle * distanceSteps.
	 * The pass targets are returned as {@link ValuePoint}, the value is a rating in the interval [0;1] with 1 being the
	 * best.
	 * The points are generated in a star-/snowflake-shape, which looks something like this (not to scale)
	 * 
	 * <pre>
	 *  x_       x       _x
	 *    \      |      /
	 *     x_    x    _x
	 *       \   |   /
	 *        x_ x_x
	 *          \|/
	 *  x--x--x--O--x--x--x
	 *         _/|\_
	 *        x  x  x
	 *      _/   |   \_
	 *     x     x     x
	 *   _/      |      \_
	 *  x        x        x
	 * </pre>
	 * 
	 * @param bot The bot to calulate advanced pass targets for
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return List of advanced pass targets as {@link AdvancedPassTarget}
	 */
	private List<AdvancedPassTarget> calculateAdvancedPassTargets(final ITrackedBot bot,
			final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IVector2 pos = bot.getBotKickerPos();
		
		// generate some advanced pass targets in a sort of star-shape around the bot
		double angleStep = AngleMath.PI_TWO / angleSteps;
		double distanceStep = maxDistance / distanceSteps;
		
		List<IVector2> possibleTargetVectors = new ArrayList<>();
		possibleTargetVectors.add(pos);
		
		// Only generate advanced pass targets is available for control.
		// This is necessary for mixed team matches, because running passes with our partner team most likely won't work
		if (baseAiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(bot.getBotId()))
		{
			for (int radiusMultiplier = 0; radiusMultiplier < distanceSteps; radiusMultiplier++)
			{
				IVector2 current = pos.addNew(new Vector2(distanceStep * (radiusMultiplier + 1), 0));
				for (int i = 0; i < angleSteps; i++)
				{
					current = GeoMath.stepAlongCircle(current, pos, angleStep);
					possibleTargetVectors.add(current);
				}
			}
		}
		
		// Filter invisible targets and estimate best visible target
		List<AdvancedPassTarget> visibleTargets = new ArrayList<>();
		for (IVector2 targetVector : possibleTargetVectors)
		{
			targetVector = targetVector.addNew(bot.getVel().multiplyNew(velocityPositionOffest));
			boolean isLegalPoint = LegalPointChecker.checkPoint(targetVector, baseAiFrame, newTacticalField);
			boolean isInsideField = Geometry.getField().isPointInShape(targetVector);
			boolean isPassLongEnough = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBall().getPos(),
					targetVector) > minPassLength;
			boolean canBePassedTo = AiMath.p2pVisibility(baseAiFrame.getWorldFrame(), baseAiFrame.getWorldFrame()
					.getBall().getPos(), targetVector, Geometry.getBotRadius(), bot.getBotId());
			boolean canBeChippedTo = canBeChippedTo(baseAiFrame, targetVector, bot.getBotId());
			
			// check for dangerous passes which may lead to own goal
			boolean ownGoalDanger = hasOwnGoalDanger(baseAiFrame, targetVector);
			
			if (isLegalPoint && isInsideField && isPassLongEnough && (canBePassedTo || canBeChippedTo) && !ownGoalDanger)
			{
				// we ignore passes, so don't use targetScore
				// double targetScore = getTargetScore(pos.addNew(targetVector), passSenderPos,
				// bestDirectShootTarget, bestPassTarget);
				
				// use gpu grid
				ValuedField valuedField = newTacticalField.getSupporterValuedField();
				
				// [0; 1], 1 is best
				double redirectChance;
				boolean minDistSet = false;
				if (valuedField != null)
				{
					Line line = Line.newLine(baseAiFrame.getWorldFrame().getBall().getPos(), targetVector);
					
					double minDist = Double.MAX_VALUE;
					for (BotID enemyID : baseAiFrame.getWorldFrame().foeBots.keySet())
					{
						IVector2 enemyPos = baseAiFrame.getWorldFrame().getFoeBot(enemyID).getPos();
						IVector2 leadpoint = GeoMath.leadPointOnLine(enemyPos, line);
						
						// if lead point not on line, continue
						if (!GeoMath.isPointOnPath(line, leadpoint))
						{
							continue;
						}
						
						double dist = GeoMath.distancePP(leadpoint, enemyPos);
						dist = Math.min(2000, dist);
						if (dist < minDist)
						{
							minDist = dist;
							minDistSet = true;
						}
					}
					if (!minDistSet)
					{
						minDist = 2000;
					}
					// normalize between 2000 and 0
					double normalizedScore = (minDist / 2000.0); // 1 = best
					redirectChance = normalizedScore;
					redirectChance = ((1 - valuedField.getValueForPoint(targetVector)) + normalizedScore) / 2.0; // 0 = best
				} else
				{
					// support use w/o gpu grid
					// TODO use a more optimal calculation method
					redirectChance = ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), targetVector,
							false);
				}
				
				// if we can't pass, we must needs chip
				boolean mustChip = !canBePassedTo;
				visibleTargets.add(new AdvancedPassTarget(targetVector, redirectChance, mustChip, bot.getBotId()));
			}
		}
		
		visibleTargets.sort(ValuePoint.VALUE_HIGH_COMPARATOR);
		
		return visibleTargets;
	}
	
	
	/**
	 * Checks if a chip can be performed from ball to a given target vector
	 * 
	 * @param baseAiFrame
	 * @param targetVector
	 * @param ignoreBot a bot to be ignored, usually the bot to be passed to (because it can move out of the way easily)
	 */
	private boolean canBeChippedTo(final BaseAiFrame baseAiFrame, final IVector2 targetVector,
			final BotID ignoreBot)
	{
		List<BotID> forChipKickIgnoredBots = new ArrayList<>();
		// ignore the given ignored bot
		forChipKickIgnoredBots.add(ignoreBot);
		
		// if we chip, we can also ignore bots in a radius around the ball
		for (Entry<BotID, ITrackedBot> entry : baseAiFrame.getWorldFrame().getBots())
		{
			if (GeoMath.distancePP(entry.getValue().getPos(),
					baseAiFrame.getWorldFrame().getBall().getPos()) < overBotChipDistance)
			{
				forChipKickIgnoredBots.add(entry.getKey());
			}
		}
		
		return AiMath.p2pVisibility(baseAiFrame.getWorldFrame(), baseAiFrame.getWorldFrame()
				.getBall().getPos(), targetVector, Geometry.getBotRadius(), forChipKickIgnoredBots);
	}
	
	
	/**
	 * Checks if a pass from ball position to a given vector may lead to an own goal
	 * 
	 * @param baseAiFrame
	 * @param targetVector
	 * @return
	 */
	private boolean hasOwnGoalDanger(final BaseAiFrame baseAiFrame, final IVector2 targetVector)
	{
		boolean ballInOurHalf = Geometry.getHalfOur()
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos());
				
		if (ballInOurHalf)
		{
			
			// line between ball and pass target position
			ILine ballTargetLine = Line.newLine(baseAiFrame.getWorldFrame().getBall().getPos(), targetVector);
			
			// will the pass travel through our goal (if it continues linearly)
			boolean passPathThroughOwnGoal;
			try
			{
				Goal ourGoal = Geometry.getGoalOur();
				ILine goalLine = Line.newLine(ourGoal.getGoalPostLeft(), ourGoal.getGoalPostRight());
				
				IVector2 intersectionGoalLine = GeoMath.intersectionPoint(goalLine, ballTargetLine);
				passPathThroughOwnGoal = GeoMath.isPointOnPath(goalLine, intersectionGoalLine);
			} catch (MathException err)
			{
				// lines are parallel
				passPathThroughOwnGoal = false;
			}
			
			boolean passThroughPenaltyArea = false;
			for (int i = 0; !passThroughPenaltyArea && ((i * 100) <= ballTargetLine.directionVector().getLength2()); i++)
			{
				if (Geometry
						.getPenaltyAreaOur()
						.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos(),
								3 * Geometry.getBotRadius()))
				{
					break;
				}
				IVector2 point = GeoMath.stepAlongLine(ballTargetLine.supportVector(),
						ballTargetLine.supportVector().addNew(ballTargetLine.directionVector()), i * 100);
				passThroughPenaltyArea = Geometry.getPenaltyAreaOur()
						.isPointInShape(point, 5 * Geometry.getBotRadius());
			}
			
			boolean criticalBackPass = (baseAiFrame.getWorldFrame().getBall().getPos().x() < (-0.5
					* Geometry.getFieldLength() * criticalBackPassThreshold))
					&& (baseAiFrame.getWorldFrame().getBall().getPos().x() > targetVector.x());
					
			return (passPathThroughOwnGoal || passThroughPenaltyArea || criticalBackPass);
		}
		
		return false;
	}
	
	
	private boolean isMarked(final ITrackedBot bot, final BaseAiFrame aiFrame)
	{
		if (!lastUnmarkedTime.containsKey(bot.getBotId()))
		{
			lastUnmarkedTime.put(bot.getBotId(), aiFrame.getWorldFrame().getTimestamp());
			return false;
		}
		
		List<ITrackedBot> blockingBots = AiMath.findBlockingBots(aiFrame.getWorldFrame(), bot.getPos(), aiFrame
				.getWorldFrame().getBall().getPos(), 4 * Geometry.getBallRadius(), aiFrame.getWorldFrame()
						.getTigerBotsAvailable().keySet());
						
		boolean isSurrounded = !AiMath.isShapeFreeOfBots(new Circle(bot.getBotKickerPos(), maxSurroundingDistance),
				aiFrame.getWorldFrame().getFoeBots(), bot);
		boolean isCovered = !blockingBots.isEmpty() && blockingBots.stream().anyMatch(
				blockingBot -> GeoMath.distancePP(bot.getPos(), blockingBot.getPos()) < maxMarkerDistance);
		boolean wasCovered = ((aiFrame.getWorldFrame().getTimestamp()
				- lastUnmarkedTime.get(bot.getBotId())) > (markedWaitTime
						* 1000000L));
						
		if (wasCovered && (isSurrounded || isCovered))
		{
			return true;
		}
		
		lastUnmarkedTime.put(bot.getBotId(), aiFrame.getWorldFrame().getTimestamp());
		return false;
	}
	
	
	private void drawAdvancedPassTargets(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final List<AdvancedPassTarget> passTargets = newTacticalField.getAdvancedPassTargetsRanked();
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.PASS_TARGETS);
		
		Color orange = new Color(222, 222, 222, 255);
		Color pink = new Color(255, 0, 170, 100);
		Color magenta = new Color(255, 120, 0, 120);
		
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
				color = orange;
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
