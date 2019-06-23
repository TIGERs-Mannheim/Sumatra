/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.math.SumatraMath.min;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.MultiTeamRobotPlan;
import edu.tigers.sumatra.ai.data.OffensiveTimeEstimation;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.roles.support.PointChecker;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.util.BallInterceptor;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class generates PassTargets for each available bot.
 */
public class PassTargetGenerationCalc extends ACalculator
{
	
	@Configurable(defValue = "2000", comment = "The radius [mm] from the bot to create pass targets.")
	private static int passTargetRadius = 2000;
	
	@Configurable(defValue = "5", comment = "How many pass targets to generate per bot")
	private static int maxPassTargetsPerBot = 5;
	
	@Configurable(defValue = "5")
	private static int maxPassTargetsPerBotToGenerate = 5;
	
	@Configurable(defValue = "500.0")
	private static double lookAHeadForPassTargetGeneration = 500;
	
	@Configurable(defValue = "3000", comment = "Safe distance of ball to goal if enemy is near ball")
	private static double safetyDistanceToPenaltyArea = 3000;
	
	@Configurable(defValue = "2000", comment = " Min distance from passtarget to Ball")
	private static double minDistanceToBall = 2000;
	
	@Configurable(defValue = "1000.0")
	private static double minDistanceToOurPenaltyArea = 1000.0;
	
	@Configurable(defValue = "-1", comment = "If set to valid botId, render a full valued field.")
	private static int fullFieldVisualizationBotId = -1;
	
	@Configurable(defValue = "0.")
	private static double timeReachSafety = 0.0;
	
	private TacticalField newTacticalField;
	private final Random rnd = new Random(0);
	private PointChecker pointChecker = new PointChecker();
	private BotID primaryOffensiveBotID;
	
	private double additionalMarginToOurPE = safetyDistanceToPenaltyArea;
	
	
	/**
	 * Default
	 */
	public PassTargetGenerationCalc()
	{
		pointChecker.useRuleEnforcement();
		
		pointChecker.addFunction(
				point -> Lines.segmentFromLine(getBall().getTrajectory().getTravelLine())
						.distanceTo(point) > minDistanceToBall);
		
		pointChecker.addFunction(point -> !Geometry.getPenaltyAreaOur()
				.withMargin(minDistanceToOurPenaltyArea)
				.isPointInShape(point));
		
		pointChecker.addFunction(this::isPassGoingTroughOurPenaltyArea);
	}
	
	
	private void updateGlobalFields(TacticalField newTacticalField)
	{
		this.newTacticalField = newTacticalField;
		Set<BotID> offensiveBots = getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getDesiredBots();
		offensiveBots.removeIf(botId -> getWFrame().getBots().containsKey(botId));
		primaryOffensiveBotID = offensiveBots.isEmpty() ? BotID.noBot() : offensiveBots.iterator().next();
		updatePointChecker();
	}
	
	
	private void updatePointChecker()
	{
		if (getAiFrame().getGamestate().isStandardSituation())
		{
			pointChecker.setTheirPenAreaMargin(Geometry.getBotToPenaltyAreaMarginStandard() + Geometry.getBotRadius());
		} else
		{
			pointChecker.setTheirPenAreaMargin(Geometry.getBotRadius());
		}
		
		double ballPEDistance = Geometry.getGoalOur().getCenter().distanceTo(getBall().getPos())
				- Geometry.getPenaltyAreaOur().getRadius() - Geometry.getBotRadius()
				- Geometry.getGoalOur().getWidth() / 2.;
		additionalMarginToOurPE = min(safetyDistanceToPenaltyArea, ballPEDistance);
		pointChecker.setOurPenAreaMargin(additionalMarginToOurPE);
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		updateGlobalFields(newTacticalField);
		
		List<PassTarget> generatedPassTargets = new ArrayList<>();
		if (fullFieldVisualizationBotId < 0)
		{
			generatedPassTargets.addAll(getPreviousPassTargets());
			generatedPassTargets.addAll(generateNewOwnPassTargets());
			generatedPassTargets.addAll(generateForeignMixedTeamPassTargets());
		} else
		{
			generatedPassTargets
					.addAll(generateFullField(BotID.createBotId(fullFieldVisualizationBotId, getAiFrame().getTeamColor())));
		}
		newTacticalField.setAllPassTargets(generatedPassTargets);
	}
	
	
	private List<PassTarget> getPreviousPassTargets()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getPassTargetsRanked().stream()
				.filter(target -> target.getBotId() != primaryOffensiveBotID)
				.map(PassTarget::new)
				.map(this::setTimeReached)
				.filter(tBot -> getWFrame().getBots().containsKey(tBot.getBotId()))
				.filter(this::isReachable)
				.filter(this::isLegalPoint)
				.filter(tBot -> tBot.getBotId() != getAiFrame().getKeeperId())
				.filter(tBot -> tBot.getBotId() != primaryOffensiveBotID)
				.filter(tBot -> !newTacticalField.getCrucialDefender().contains(tBot.getBotId()))
				.filter(
						tBot -> tBot.getBotPos().distanceTo(getWFrame().getBot(tBot.getBotId()).getPos()) < passTargetRadius)
				.collect(Collectors.toList());
	}
	
	
	private List<PassTarget> generateNewOwnPassTargets()
	{
		return getWFrame().getTigerBotsAvailable().values().stream()
				.filter(tBot -> tBot.getBotId() != getAiFrame().getKeeperId())
				.filter(tBot -> tBot.getBotId() != primaryOffensiveBotID)
				.filter(tBot -> !newTacticalField.getCrucialDefender().contains(tBot.getBotId()))
				.map(this::generatePassTargetsForBot)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
	
	
	private List<PassTarget> generateForeignMixedTeamPassTargets()
	{
		List<PassTarget> foreignPassTargets = new ArrayList<>();
		for (MultiTeamRobotPlan plan : getAiFrame().getIncomingMultiTeamMessage().getTeamPlan().getRobotPlans().values())
		{
			if (!getWFrame().getTigerBotsAvailable().containsKey(plan.getBotID())
					&& plan.getRole() == MultiTeamRobotPlan.EMultiTeamRobotRole.OFFENSE)
			{
				ITrackedBot bot = getWFrame().getBot(plan.getBotID());
				IVector2 kickerPos = bot.getBotKickerPos();
				PassTarget passTarget = new PassTarget(kickerPos, plan.getBotID());
				passTarget.setBirth(getWFrame().getTimestamp());
				setTimeReached(passTarget);
				foreignPassTargets.add(passTarget);
			}
		}
		return foreignPassTargets;
	}
	
	
	private PassTarget setTimeReached(final PassTarget passTarget)
	{
		ITrackedBot tBot = getWFrame().getBot(passTarget.getBotId());
		if (tBot == null)
		{
			return passTarget;
		}
		MoveConstraints moveConstraints = new MoveConstraints(tBot.getMoveConstraints());
		moveConstraints.setAccMax(BallInterceptor.getMinAcc());
		double trajTime = TrajectoryGenerator.generatePositionTrajectory(tBot, passTarget.getBotPos()).getTotalTime();
		long timeReached = getWFrame().getTimestamp() + (long) (trajTime * 1e9);
		passTarget.setTimeReached(timeReached);
		return passTarget;
	}
	
	
	private List<PassTarget> generatePassTargetsForBot(final ITrackedBot bot)
	{
		List<PassTarget> passTargets = new ArrayList<>(maxPassTargetsPerBotToGenerate);
		
		IVector2 shiftedCenterPosition = bot.getPos()
				.addNew(bot.getVel().scaleToNew(bot.getVel().getLength2() * lookAHeadForPassTargetGeneration));
		
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS_DEBUG)
				.add(new DrawableCircle(shiftedCenterPosition, passTargetRadius, Color.CYAN));
		
		PassTarget curKickerPassTarget = new PassTarget(shiftedCenterPosition, bot.getBotId());
		curKickerPassTarget.setBirth(getWFrame().getTimestamp());
		if (isLegalPoint(curKickerPassTarget))
		{
			passTargets.add(curKickerPassTarget);
		}
		for (int i = 0; i < (maxPassTargetsPerBotToGenerate - 1); i++)
		{
			double angle = AngleMath.PI_TWO * rnd.nextDouble();
			double radius = passTargetRadius * rnd.nextDouble();
			IVector2 targetPos = shiftedCenterPosition.addNew(Vector2.fromAngle(angle).multiply(radius));
			PassTarget passTarget = new PassTarget(targetPos, bot.getBotId());
			passTarget.setBirth(getWFrame().getTimestamp());
			setTimeReached(passTarget);
			if (isLegalPoint(passTarget) && isReachable(passTarget))
			{
				passTargets.add(passTarget);
				
			}
			if (passTargets.size() >= maxPassTargetsPerBot)
			{
				break;
			}
		}
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS_DEBUG)
				.addAll(passTargets.stream()
						.map(passTarget -> new DrawableCircle(passTarget.getKickerPos(), Geometry.getBallRadius(), Color.RED))
						.collect(Collectors.toList()));
		return passTargets;
	}
	
	
	private boolean isLegalPoint(final IPassTarget target)
	{
		return pointChecker.allMatch(getAiFrame(), target.getKickerPos()) &&
				isPositionFreeFromBots(target.getKickerPos(), target.getBotId());
	}
	
	
	private boolean isReachable(final IPassTarget target)
	{
		return target.getTimeUntilReachedInS(getWFrame().getTimestamp()) + timeReachSafety < calcTimeForKick(target);
	}
	
	
	private double calcTimeForKick(final IPassTarget passTarget)
	{
		double distance = getWFrame().getBall().getPos().distanceTo(passTarget.getKickerPos());
		double time = Optional.ofNullable(newTacticalField.getOffensiveTimeEstimations().get(primaryOffensiveBotID))
				.map(OffensiveTimeEstimation::getBallContactTime).orElse(1d);
		
		IVector2 redirectTarget = DefenseMath.getBisectionGoal(passTarget.getKickerPos());
		double kickSpeed = OffensiveMath.calcPassSpeedRedirect(
				passTarget.getTimeUntilReachedInS(getWFrame().getTimestamp()), getBall().getPos(),
				passTarget.getKickerPos(), redirectTarget);
		time += getWFrame().getBall().getStraightConsultant().getTimeForKick(distance, kickSpeed);
		return time;
	}
	
	
	private boolean isPositionFreeFromBots(IVector2 position, BotID botID)
	{
		return getWFrame().getBots().values().stream()
				.filter(tBot -> !tBot.getBotId().equals(botID))
				.map(tBot -> tBot.getPos().distanceTo(position))
				.noneMatch(dist -> dist < Geometry.getBotRadius() * 2);
	}
	
	
	private boolean isPassGoingTroughOurPenaltyArea(IVector2 pos)
	{
		ILine passLine = Line.fromPoints(getBall().getPos(), pos);
		return !Geometry.getPenaltyAreaOur().withMargin(additionalMarginToOurPE).isIntersectingWithLine(passLine);
	}
	
	
	private List<PassTarget> generateFullField(BotID botID)
	{
		List<PassTarget> fullField = new ArrayList<>();
		double width = Geometry.getFieldWidth();
		double height = Geometry.getFieldLength();
		int numX = 200;
		int numY = 150;
		for (int iy = 0; iy < numY; iy++)
		{
			for (int ix = 0; ix < numX; ix++)
			{
				double x = (-height / 2) + (ix * (height / (numX - 1)));
				double y = (-width / 2) + (iy * (width / (numY - 1)));
				PassTarget passTarget = new PassTarget(Vector2.fromXY(x, y),
						botID);
				fullField.add(passTarget);
			}
			
		}
		fullField.forEach(this::setTimeReached);
		return fullField;
	}
	
	
	public static int getFullFieldVisualizationBotId()
	{
		return fullFieldVisualizationBotId;
	}
	
	
	public static int getMaxPassTargetsPerBot()
	{
		return maxPassTargetsPerBot;
	}
	
	
	public static double getSafetyDistanceToPenaltyArea()
	{
		return safetyDistanceToPenaltyArea;
	}
}
