/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2013
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.ProbabilityMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support.LegalPointChecker;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	private static int			overBotChipDistance			= 1000;
	
	@Configurable(comment = "Minimum length of a pass [mm]")
	private static int			minPassLength					= 1000;
	
	@Configurable(comment = "Number of points on one circle of advanced pass targets")
	private static int			angleSteps						= 8;
	
	
	@Configurable(comment = "Number of points on one advanced pass target ray")
	private static int			distanceSteps					= 3;
	
	@Configurable(comment = "Maximum distance between advanced pass targets and bot [mm]")
	private static float			maxDistance						= 800;
	
	@Configurable(comment = "Time to wait before bot will be considered marked [ms]")
	private static int			markedWaitTime					= 750;
	
	@Configurable(comment = "Will be aplied to all advanced pass target scores of marked bots")
	private static float			markedBotFactor				= 0.5f;
	
	@Configurable(comment = "Only bots within this distance will be considered as markers [mm]")
	private static int			maxMarkerDistance				= 1000;
	
	@Configurable(comment = "Opponent bots whithin this radius from the kicker will be considered as additional markers [mm]")
	private static int			maxSurroundingDistance		= 500;
	
	@Configurable(comment = "Do not play any passes back beyond this part in the back of our field")
	private static float			criticalBackPassThreshold	= 0.5f;
	
	private Map<BotID, Long>	lastUnmarkedTime;
	
	
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
		
		for (TrackedTigerBot bot : baseAiFrame.getWorldFrame().getTigerBotsVisible().values())
		{
			if (baseAiFrame.getKeeperId().equals(bot.getId()))
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
	private List<AdvancedPassTarget> calculateAdvancedPassTargets(final TrackedTigerBot bot,
			final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IVector2 pos = AiMath.getBotKickerPos(bot);
		
		// generate some advanced pass targets in a sort of star-shape around the bot
		float angleStep = AngleMath.PI_TWO / angleSteps;
		float distanceStep = maxDistance / distanceSteps;
		
		
		List<IVector2> possibleTargetVectors = new ArrayList<>();
		possibleTargetVectors.add(pos);
		
		// Only generate advanced pass targets is available for control.
		// This is necessary for mixed team matches, because running passes with our partner team most likely won't work
		if (baseAiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(bot.getId()))
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
			boolean isLegalPoint = LegalPointChecker.checkPoint(targetVector, baseAiFrame, newTacticalField);
			boolean isInsideField = AIConfig.getGeometry().getField().isPointInShape(targetVector);
			boolean isPassLongEnough = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBall(), targetVector) > minPassLength;
			boolean canBePassedTo = GeoMath.p2pVisibility(baseAiFrame.getWorldFrame(), baseAiFrame.getWorldFrame()
					.getBall().getPos(), targetVector, AIConfig.getGeometry().getBotRadius(), bot.getId());
			boolean canBeChippedTo = canBeChippedTo(baseAiFrame, targetVector, bot.getId());
			
			// check for dangerous passes which may lead to own goal
			boolean ownGoalDanger = hasOwnGoalDanger(baseAiFrame, targetVector);
			
			if (isLegalPoint && isInsideField && isPassLongEnough && (canBePassedTo || canBeChippedTo) && !ownGoalDanger)
			{
				// we ignore passes, so don't use targetScore
				// float targetScore = getTargetScore(pos.addNew(targetVector), passSenderPos,
				// bestDirectShootTarget, bestPassTarget);
				
				// use gpu grid
				ValuedField valuedField = newTacticalField.getSupporterValuedField();
				
				// [0; 1], 1 is best
				float redirectChance;
				if (valuedField != null)
				{
					redirectChance = 1 - valuedField.getValueForPoint(targetVector);
				}
				else
				{
					// support use w/o gpu grid
					// TODO use a more optimal calculation method
					redirectChance = ProbabilityMath.getDirectShootScoreChanceNew(baseAiFrame.getWorldFrame(), targetVector,
							false);
				}
				
				// if we can't pass, we must needs chip
				boolean mustChip = !canBePassedTo;
				
				visibleTargets.add(new AdvancedPassTarget(targetVector, redirectChance, mustChip, bot.getId()));
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
		for (Entry<BotID, TrackedTigerBot> entry : baseAiFrame.getWorldFrame().getBots())
		{
			if (GeoMath.distancePP(entry.getValue(), baseAiFrame.getWorldFrame().getBall().getPos()) < overBotChipDistance)
			{
				forChipKickIgnoredBots.add(entry.getKey());
			}
		}
		
		return GeoMath.p2pVisibility(baseAiFrame.getWorldFrame(), baseAiFrame.getWorldFrame()
				.getBall().getPos(), targetVector, AIConfig.getGeometry().getBotRadius(), forChipKickIgnoredBots);
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
		boolean ballInOurHalf = AIConfig.getGeometry().getOurHalf()
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos());
		
		if (ballInOurHalf)
		{
			
			// line between ball and pass target position
			ILine ballTargetLine = Line.newLine(baseAiFrame.getWorldFrame().getBall().getPos(), targetVector);
			
			// will the pass travel through our goal (if it continues linearly)
			boolean passPathThroughOwnGoal;
			try
			{
				Goal ourGoal = AIConfig.getGeometry().getGoalOur();
				ILine goalLine = Line.newLine(ourGoal.getGoalPostLeft(), ourGoal.getGoalPostRight());
				
				IVector2 intersectionGoalLine = GeoMath.intersectionPoint(goalLine, ballTargetLine);
				passPathThroughOwnGoal = GeoMath.isPointOnLine(goalLine, intersectionGoalLine);
			} catch (MathException err)
			{
				// lines are parallel
				passPathThroughOwnGoal = false;
			}
			
			boolean passThroughPenaltyArea = false;
			for (int i = 0; !passThroughPenaltyArea && ((i * 100) <= ballTargetLine.directionVector().getLength2()); i++)
			{
				if (AIConfig
						.getGeometry()
						.getPenaltyAreaOur()
						.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos(),
								3 * AIConfig.getGeometry().getBotRadius()))
				{
					break;
				}
				IVector2 point = GeoMath.stepAlongLine(ballTargetLine.supportVector(),
						ballTargetLine.supportVector().addNew(ballTargetLine.directionVector()), i * 100);
				passThroughPenaltyArea = AIConfig.getGeometry().getPenaltyAreaOur()
						.isPointInShape(point, 3 * AIConfig.getGeometry().getBotRadius());
			}
			
			boolean criticalBackPass = (baseAiFrame.getWorldFrame().getBall().getPos().x() < (-0.5
					* AIConfig.getGeometry().getFieldLength() * criticalBackPassThreshold))
					&& (baseAiFrame.getWorldFrame().getBall().getPos().x() > targetVector.x());
			
			return (passPathThroughOwnGoal || passThroughPenaltyArea || criticalBackPass);
		}
		
		return false;
	}
	
	
	private boolean isMarked(final TrackedTigerBot bot, final BaseAiFrame aiFrame)
	{
		if (!lastUnmarkedTime.containsKey(bot.getId()))
		{
			lastUnmarkedTime.put(bot.getId(), SumatraClock.nanoTime());
			return false;
		}
		
		List<TrackedBot> blockingBots = GeoMath.findBlockingBots(aiFrame.getWorldFrame(), bot.getPos(), aiFrame
				.getWorldFrame().getBall().getPos(), 4 * AIConfig.getGeometry().getBallRadius(), aiFrame.getWorldFrame()
				.getTigerBotsAvailable().keySet());
		
		boolean isSurrounded = !AiMath.isShapeFreeOfBots(new Circle(AiMath.getBotKickerPos(bot), maxSurroundingDistance),
				aiFrame.getWorldFrame().getFoeBots(), bot);
		boolean isCovered = !blockingBots.isEmpty() && blockingBots.stream().anyMatch(
				blockingBot -> GeoMath.distancePP(bot.getPos(), blockingBot.getPos()) < maxMarkerDistance);
		boolean wasCovered = ((SumatraClock.nanoTime() - lastUnmarkedTime.get(bot.getId())) > (markedWaitTime * 1000000L));
		
		if (wasCovered && (isSurrounded || isCovered))
		{
			return true;
		}
		
		lastUnmarkedTime.put(bot.getId(), SumatraClock.nanoTime());
		return false;
	}
}
