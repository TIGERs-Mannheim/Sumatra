/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.math.SumatraMath.min;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class generates PassTargets for each available bot.
 */
public class PassTargetGenerationCalc extends ACalculator
{
	@Configurable(defValue = "500.0", comment = "The radius [mm] from the bot to create pass targets.")
	private static double passTargetRadius = 500.0;
	
	@Configurable(defValue = "5", comment = "How many pass targets to store per bot")
	private static int maxNewPassTargetsPerBot = 5;
	
	@Configurable(defValue = "5", comment = "How many pass targets should be generate per bot (iterations)")
	private static int maxNewPassTargetsPerBotToGenerate = 5;
	
	@Configurable(defValue = "0.5", comment = "Lookahead [s] to use to shift the center position")
	private static double lookAHeadForPassTargetGeneration = 0.5;
	
	@Configurable(defValue = "1500.0", comment = "Safety distance to keep to penalty area")
	private static double safetyDistanceToPenaltyArea = 1500.0;
	
	@Configurable(defValue = "1500.0", comment = " Min distance from passtarget to Ball")
	private static double minDistanceToBall = 1500.0;
	
	@Configurable(defValue = "1000.0")
	private static double minDistanceToOurPenaltyArea = 1000.0;
	
	@Configurable(defValue = "0.2")
	private static double timeReachSafety = 0.2;
	
	private Random rnd;
	private PointChecker pointChecker = new PointChecker();
	private BotID lastAttackerId;
	
	private double penaltyAreaOurMargin = safetyDistanceToPenaltyArea;
	
	
	public PassTargetGenerationCalc()
	{
		pointChecker.useRuleEnforcement();
		pointChecker.useKickOffRuleEnforcement();
		pointChecker.addFunction(this::keepMinDistanceToBall);
		pointChecker.addFunction(this::keepMinDistanceToOurPenaltyArea);
		pointChecker.addFunction(this::forbidPassingThroughOurPenaltyArea);
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		updateGlobalFields();
		
		List<PassTarget> generatedPassTargets = new ArrayList<>();
		generatedPassTargets.addAll(getPreviousPassTargets());
		generatedPassTargets.addAll(generateNewOwnPassTargets());
		newTacticalField.setAllPassTargets(generatedPassTargets);
	}
	
	
	private boolean keepMinDistanceToBall(final IVector2 point)
	{
		return Lines.segmentFromLine(getBall().getTrajectory().getTravelLine())
				.distanceTo(point) > minDistanceToBall;
	}
	
	
	private boolean keepMinDistanceToOurPenaltyArea(final IVector2 point)
	{
		return !Geometry.getPenaltyAreaOur()
				.withMargin(minDistanceToOurPenaltyArea)
				.isPointInShape(point);
	}
	
	
	private void updateGlobalFields()
	{
		if (rnd == null)
		{
			rnd = new Random(getWFrame().getTimestamp());
		}
		lastAttackerId = lastAttackerId();
		double distBallToPenaltyArea = Geometry.getPenaltyAreaOur().distanceTo(getBall().getPos());
		penaltyAreaOurMargin = min(safetyDistanceToPenaltyArea, distBallToPenaltyArea);
		updatePointChecker();
	}
	
	
	private BotID lastAttackerId()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.filter(this::botIsVisible)
				.orElse(BotID.noBot());
	}
	
	
	private void updatePointChecker()
	{
		pointChecker.setTheirPenAreaMargin(theirPenAreaMargin());
		pointChecker.setOurPenAreaMargin(penaltyAreaOurMargin);
	}
	
	
	private double theirPenAreaMargin()
	{
		final double theirPenAreaMargin;
		if (getAiFrame().getGamestate().isStandardSituation())
		{
			theirPenAreaMargin = RuleConstraints.getBotToPenaltyAreaMarginStandard() + Geometry.getBotRadius();
		} else
		{
			theirPenAreaMargin = Geometry.getBotRadius();
		}
		return theirPenAreaMargin;
	}
	
	
	private List<PassTarget> getPreviousPassTargets()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getPassTargetsRanked().stream()
				.filter(target -> target.getBotId() != lastAttackerId)
				.filter(p -> botIsVisible(p.getBotId()))
				.map(PassTarget::new)
				.map(this::withTimeReached)
				.filter(this::isReachable)
				.filter(this::isLegalPoint)
				.filter(p -> p.getBotId() != getAiFrame().getKeeperId())
				.filter(p -> p.getBotId() != lastAttackerId)
				.filter(p -> !getNewTacticalField().getCrucialDefender().contains(p.getBotId()))
				.filter(p -> p.getBotPos().distanceTo(getWFrame().getBot(p.getBotId()).getPos()) < passTargetRadius)
				.collect(Collectors.toList());
	}
	
	
	private boolean botIsVisible(final BotID botId)
	{
		return getWFrame().getBots().containsKey(botId);
	}
	
	
	private List<PassTarget> generateNewOwnPassTargets()
	{
		return getWFrame().getTigerBotsAvailable().values().stream()
				.filter(p -> p.getBotId() != getAiFrame().getKeeperId())
				.filter(p -> p.getBotId() != lastAttackerId)
				.filter(p -> !getNewTacticalField().getCrucialDefender().contains(p.getBotId()))
				.map(this::passTargetsForBot)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
	
	
	private PassTarget withTimeReached(final PassTarget passTarget)
	{
		ITrackedBot tBot = getWFrame().getBot(passTarget.getBotId());
		if (tBot == null)
		{
			return passTarget;
		}
		double trajTime = TrajectoryGenerator.generatePositionTrajectory(tBot, passTarget.getBotPos()).getTotalTime();
		long timeReached = getWFrame().getTimestamp() + (long) (trajTime * 1e9);
		passTarget.setTimeReached(timeReached);
		return passTarget;
	}
	
	
	private List<PassTarget> passTargetsForBot(final ITrackedBot bot)
	{
		List<PassTarget> passTargets = new ArrayList<>();
		
		IVector2 shiftedCenterPosition = shiftedCenterPosition(bot);
		
		PassTarget curKickerPassTarget = new PassTarget(shiftedCenterPosition, bot.getBotId());
		if (isLegalPoint(curKickerPassTarget))
		{
			passTargets.add(curKickerPassTarget);
		}
		
		for (int i = 0; i < (maxNewPassTargetsPerBotToGenerate - 1)
				&& passTargets.size() < maxNewPassTargetsPerBot; i++)
		{
			double angle = AngleMath.PI_TWO * rnd.nextDouble();
			double radius = passTargetRadius * rnd.nextDouble();
			IVector2 targetPos = shiftedCenterPosition.addNew(Vector2.fromAngleLength(angle, radius));
			PassTarget passTarget = new PassTarget(targetPos, bot.getBotId());
			withTimeReached(passTarget);
			
			if (isLegalPoint(passTarget) && isReachable(passTarget))
			{
				passTargets.add(passTarget);
			}
		}
		
		passTargets.forEach(p -> p.setBirth(getWFrame().getTimestamp()));
		
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS_DEBUG)
				.add(new DrawableCircle(shiftedCenterPosition, passTargetRadius, Color.CYAN));
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS_DEBUG)
				.addAll(passTargets.stream()
						.map(passTarget -> new DrawableCircle(passTarget.getKickerPos(), Geometry.getBallRadius(), Color.RED))
						.collect(Collectors.toList()));
		
		return passTargets;
	}
	
	
	private IVector2 shiftedCenterPosition(final ITrackedBot bot)
	{
		final double shiftLength = bot.getVel().getLength2() * lookAHeadForPassTargetGeneration * 1000;
		return bot.getPos().addNew(bot.getVel().scaleToNew(shiftLength));
	}
	
	
	private boolean isLegalPoint(final IPassTarget target)
	{
		return pointChecker.allMatch(getAiFrame(), target.getKickerPos()) &&
				positionIsFreeFromBots(target.getKickerPos(), target.getBotId());
	}
	
	
	private boolean isReachable(final IPassTarget target)
	{
		return target.getTimeUntilReachedInS(getWFrame().getTimestamp()) + timeReachSafety < calcTimeForKick(target);
	}
	
	
	private double calcTimeForKick(final IPassTarget passTarget)
	{
		double distance = getWFrame().getBall().getPos().distanceTo(passTarget.getKickerPos());
		
		IVector2 redirectTarget = DefenseMath.getBisectionGoal(passTarget.getKickerPos());
		double kickSpeed = OffensiveMath.passSpeedStraight(
				getBall().getPos(),
				passTarget.getKickerPos(),
				redirectTarget);
		return getWFrame().getBall().getStraightConsultant().getTimeForKick(distance, kickSpeed);
	}
	
	
	private boolean positionIsFreeFromBots(IVector2 position, BotID botID)
	{
		return getWFrame().getBots().values().stream()
				.filter(tBot -> !tBot.getBotId().equals(botID))
				.map(tBot -> tBot.getPos().distanceTo(position))
				.noneMatch(dist -> dist < Geometry.getBotRadius() * 2);
	}
	
	
	private boolean forbidPassingThroughOurPenaltyArea(IVector2 pos)
	{
		IHalfLine line = Lines.halfLineFromDirection(getBall().getPos(), pos.subtractNew(getBall().getPos()));
		return Geometry.getPenaltyAreaOur().withMargin(penaltyAreaOurMargin).lineIntersections(line).stream()
				.noneMatch(e -> getBall().getStraightConsultant().getTimeForKick(e.distanceTo(getBall().getPos()),
						RuleConstraints.getMaxBallSpeed()) < 1.0);
	}
}
