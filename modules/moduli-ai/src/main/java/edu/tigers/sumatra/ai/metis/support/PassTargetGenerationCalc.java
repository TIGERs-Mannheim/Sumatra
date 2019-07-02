/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTarget;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class generates PassTargets for each available bot.
 */
public class PassTargetGenerationCalc extends ACalculator
{
	@Configurable(defValue = "2", comment = "How many pass targets to store per bot")
	private static int maxNewPassTargetsPerBot = 2;

	@Configurable(defValue = "10", comment = "How many pass targets should be generate per bot (iterations)")
	private static int maxNewPassTargetsPerBotToGenerate = 10;

	@Configurable(defValue = "1500.0", comment = "Safety distance to keep to penalty area")
	private static double safetyDistanceToPenaltyArea = 1500.0;

	@Configurable(defValue = "1500.0", comment = " Min distance from passtarget to Ball")
	private static double minDistanceToBall = 1500.0;

	@Configurable(defValue = "0.2", comment = "Additional time [s] to add to the time the bot reaches a pass target")
	private static double additionalTimeToReachSafety = 0.2;

	@Configurable(defValue = "1.5", comment = "Min allowed duration [s] until the ball enters our own penalty area")
	private static double minPassDurationUntilReachingPenaltyArea = 1.5;

	@Configurable(defValue = "0.3", comment = "Offset [s] applied to 'timeUntilPassReachedPos'")
	private static double timeUntilPassReachedOffset = 0.3;

	@Configurable(defValue = "1.0", comment = "Max lookahead [s] to use for estimating the pass target radius")
	private static double maxDynamicPassTargetRadiusLookahead = 1.0;

	@Configurable(defValue = "0.1", comment = "Min lookahead [s] to use for estimating the pass target radius")
	private static double minDynamicPassTargetRadiusLookahead = 0.1;

	private Random rnd;
	private Set<BotID> consideredBots;

	private PointChecker pointChecker = new PointChecker()
			.checkBallDistances()
			.checkInsideField()
			.checkNotInPenaltyAreas()
			.checkConfirmWithKickOffRules()
			.checkCustom(this::keepMinDistanceToBall)
			.checkCustom(this::passIsNotReachingOurPenaltyAreaSoon)
			.checkCustom(this::canBeReceivedOutsidePenArea);

	private double timeUntilBallMeetsAttacker;
	private IVector2 passOrigin;
	private boolean attackerCanCatch;


	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (rnd == null)
		{
			rnd = new Random(getWFrame().getTimestamp());
		}
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGET_GENERATION).add(
				new DrawableCircle(getBall().getPos(), minDistanceToBall, Color.RED));
		consideredBots = consideredBots();
		updatePenaltyAreaMargin();
		updateAttackerMetaData();

		final List<PassTargetCandidate> candidates = new ArrayList<>(candidatesFromPreviousFrame());
		candidates.addAll(newCandidates());
		candidates.forEach(this::draw);

		List<IPassTarget> passTargets = select(candidates);
		newTacticalField.setAllPassTargets(passTargets);
	}


	private void updateAttackerMetaData()
	{
		final Optional<ITrackedBot> attacker = getAttacker();

		attackerCanCatch = false;
		if (attacker.isPresent())
		{
			passOrigin = attacker.get().getRobotInfo().getTrajectory()
					.map(trajectory -> trajectory.getFinalDestination().getXYVector())
					.orElse(getBall().getPos());
			final ARole aRole = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles()
					.getWithNull(attacker.get().getBotId());
			if (aRole != null && aRole.getType() == ERole.ATTACKER)
			{
				AttackerRole attackerRole = ((AttackerRole) aRole);
				attackerCanCatch = attackerRole.canKickOrCatchTheBall();
			}
		} else
		{
			passOrigin = getBall().getPos();
		}

		timeUntilBallMeetsAttacker = getAttacker()
				.map(this::timeUntilBallMeetsAttacker)
				.orElse(0.0);
	}


	private double timeUntilBallMeetsAttacker(ITrackedBot attacker)
	{
		if (attackerCanCatch)
		{
			// attacker will intercept the ball on its ball travel line
			return getWFrame().getBall().getTrajectory().getTimeByDist(distanceFromBotToBall(attacker));
		}

		return timeUntilBotAtBall(attacker);
	}


	private Set<BotID> consideredBots()
	{
		BotID lastAttackerId = lastAttackerId();
		return getWFrame().getTigerBotsAvailable().values().stream()
				.map(ITrackedBot::getBotId)
				.filter(this::notTheKeeper)
				.filter(this::notADefender)
				.filter(this::notAnInterchangeBot)
				.filter(id -> id != lastAttackerId)
				.collect(Collectors.toSet());
	}


	private boolean notADefender(final BotID id)
	{
		return notInPlay(EPlay.DEFENSIVE, id);
	}


	private boolean notAnInterchangeBot(final BotID id)
	{
		return notInPlay(EPlay.INTERCHANGE, id);
	}


	private boolean notInPlay(final EPlay play, final BotID id)
	{
		return getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(play).stream()
				.noneMatch(bot -> bot.getBotID() == id);
	}


	private boolean notTheKeeper(final BotID id)
	{
		return id != getAiFrame().getKeeperId();
	}


	private boolean keepMinDistanceToBall(final IVector2 point)
	{
		return passOrigin.distanceTo(point) > minDistanceToBall;
	}


	private boolean canBeReceivedOutsidePenArea(final IVector2 point)
	{
		IVector2 receivePos = LineMath.stepAlongLine(point, passOrigin, -Geometry.getBotRadius());
		return !Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius()).isPointInShape(receivePos);
	}


	private void updatePenaltyAreaMargin()
	{
		double distBallToPenaltyArea = Geometry.getPenaltyAreaOur().distanceTo(getBall().getPos());
		double penaltyAreaOurMargin = min(safetyDistanceToPenaltyArea, distBallToPenaltyArea);
		pointChecker.setTheirPenAreaMargin(theirPenAreaMargin());
		pointChecker.setOurPenAreaMargin(penaltyAreaOurMargin);

		final List<IDrawableShape> penAreaShapes = Geometry.getPenaltyAreaOur().withMargin(penaltyAreaOurMargin)
				.getDrawableShapes();
		penAreaShapes.forEach(p -> p.setColor(Color.RED));
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGET_GENERATION).addAll(penAreaShapes);
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


	private BotID lastAttackerId()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.filter(this::botIsVisible)
				.orElse(BotID.noBot());
	}


	private boolean botIsVisible(final BotID botId)
	{
		return getWFrame().getBots().containsKey(botId);
	}


	private List<PassTargetCandidate> candidatesFromPreviousFrame()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getRatedPassTargetsRanked().stream()
				.filter(p -> consideredBots.contains(p.getBotId()))
				.map(p -> createPassTargetCandidate(p.getPos(), p.getBotId()))
				.map(PassTargetCandidate::withFromPrevFrame)
				.collect(Collectors.toList());
	}


	private List<PassTargetCandidate> newCandidates()
	{
		return consideredBots.stream()
				.map(this::passTargetsForBot)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}


	private List<IPassTarget> select(final List<PassTargetCandidate> candidates)
	{
		return candidates.stream()
				.filter(PassTargetCandidate::isLegal)
				.filter(PassTargetCandidate::isReachable)
				.map(c -> new PassTarget(new DynamicPosition(c.pos), c.botID))
				.collect(Collectors.toList());
	}


	private PassTargetCandidate createPassTargetCandidate(final IVector2 pos, final BotID botID)
	{
		PassTargetCandidate candidate = new PassTargetCandidate();
		candidate.pos = pos;
		candidate.botID = botID;
		candidate.legal = isLegalPoint(pos, botID);
		if (candidate.legal)
		{
			candidate.timeUntilBallReached = timeUntilPassReachedPos(pos);
			candidate.timeUntilBotReached = timeUntilBotReached(pos, botID);
			candidate.reachable = candidate.timeUntilBotReached < candidate.timeUntilBallReached;
		}
		return candidate;
	}


	private double timeUntilBotReached(final IVector2 pos, final BotID botID)
	{
		ITrackedBot tBot = getWFrame().getBot(botID);
		double timeReached = TrajectoryGenerator.generatePositionTrajectory(tBot, pos).getTotalTime();
		return timeReached + additionalTimeToReachSafety;
	}


	private double timeUntilPassReachedPos(final IVector2 pos)
	{
		IVector2 redirectTarget = DefenseMath.getBisectionGoal(pos);
		double kickSpeed = OffensiveMath.passSpeedStraight(
				getBall().getPos(),
				pos,
				redirectTarget);
		double passDistance = passOrigin.distanceTo(pos);
		double passDuration = getWFrame().getBall().getStraightConsultant().getTimeForKick(passDistance, kickSpeed);

		return timeUntilPassReachedOffset + passDuration + timeUntilBallMeetsAttacker;
	}


	private Optional<ITrackedBot> getAttacker()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy()
				.getAttackerBot()
				.map(botId -> getWFrame().getBot(botId));
	}


	private double distanceFromBotToBall(final ITrackedBot bot)
	{
		return bot.getBotKickerPos().distanceTo(getWFrame().getBall().getPos());
	}


	private double timeUntilBotAtBall(final ITrackedBot b)
	{
		return TrajectoryGenerator.generatePositionTrajectory(b, getWFrame().getBall().getPos()).getTotalTime();
	}


	private void draw(final PassTargetCandidate candidate)
	{
		Color pointColor;
		if (!candidate.legal)
		{
			pointColor = Color.RED;
		} else if (candidate.fromPrevFrame)
		{
			pointColor = Color.gray;
		} else
		{
			pointColor = Color.black;
		}

		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGET_GENERATION)
				.add(new DrawablePoint(candidate.getPos(), pointColor));

		if (candidate.legal)
		{
			getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGET_TIMING).add(
					new DrawableAnnotation(candidate.getPos(),
							String.format("ball %.2fs", candidate.timeUntilBallReached))
									.withOffset(Vector2.fromXY(15, -6))
									.withFontHeight(10));

			Color color = candidate.reachable ? Color.GREEN : Color.RED;
			getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGET_TIMING).add(
					new DrawableAnnotation(candidate.getPos(),
							String.format("bot %.2fs", candidate.timeUntilBotReached), color)
									.withOffset(Vector2.fromXY(15, 6))
									.withFontHeight(10));
		}
	}


	private List<PassTargetCandidate> passTargetsForBot(final BotID botID)
	{
		List<PassTargetCandidate> passTargets = new ArrayList<>();

		IVector2 shiftedCenterPosition = shiftedCenterPosition(botID);
		final PassTargetCandidate kickerCandidate = createPassTargetCandidate(shiftedCenterPosition, botID);
		passTargets.add(kickerCandidate);

		double dynamicRadius;
		if (kickerCandidate.legal)
		{
			dynamicRadius = dynamicPassTargetRadius(botID, kickerCandidate.timeUntilBallReached);
		} else
		{
			dynamicRadius = dynamicPassTargetRadius(botID, 2.0);
		}
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGET_GENERATION).add(
				new DrawableCircle(shiftedCenterPosition, dynamicRadius, Color.CYAN));

		for (int i = 0; i < (maxNewPassTargetsPerBotToGenerate - 1)
				&& passTargets.size() < maxNewPassTargetsPerBot; i++)
		{
			double angle = AngleMath.PI_TWO * rnd.nextDouble();
			double radius = dynamicRadius * rnd.nextDouble();
			IVector2 targetPos = shiftedCenterPosition.addNew(Vector2.fromAngleLength(angle, radius));
			passTargets.add(createPassTargetCandidate(targetPos, botID));
		}

		return passTargets;
	}


	private double dynamicPassTargetRadius(final BotID botID, final double timeUntilBallReached)
	{
		ITrackedBot bot = getWFrame().getBot(botID);
		double ballTravelTime = SumatraMath.cap(timeUntilBallReached - additionalTimeToReachSafety,
				minDynamicPassTargetRadiusLookahead,
				maxDynamicPassTargetRadiusLookahead);
		double botAccTime = ballTravelTime / 2;
		double maxVel = bot.getMoveConstraints().getAccMax() * botAccTime;
		return maxVel * botAccTime * 1000;
	}


	private IVector2 shiftedCenterPosition(final BotID botID)
	{
		final ITrackedBot bot = getWFrame().getBot(botID);

		final double vel = bot.getVel().getLength2();
		double brakeTime = vel / bot.getMoveConstraints().getAccMax();
		double brakeDistance = vel * brakeTime / 2;

		final double shiftLength = brakeDistance * 1000;
		return bot.getBotKickerPos().addNew(bot.getVel().scaleToNew(shiftLength));
	}


	private boolean isLegalPoint(final IVector2 pos, final BotID botID)
	{
		return pointChecker.allMatch(getAiFrame(), pos) && positionIsFreeFromBots(pos, botID);
	}


	private boolean positionIsFreeFromBots(IVector2 position, BotID botID)
	{
		IVector2 ownPos = getWFrame().getBot(botID).getPos();
		return getWFrame().getBots().values().stream()
				.filter(tBot -> !tBot.getBotId().equals(botID))
				.map(tBot -> Lines.segmentFromPoints(ownPos, position).distanceTo(tBot.getPos()))
				.noneMatch(dist -> dist < Geometry.getBotRadius() * 2);
	}


	private boolean passIsNotReachingOurPenaltyAreaSoon(final IVector2 pos)
	{
		return Geometry.getPenaltyAreaOur().withMargin(500).isPointInShape(getBall().getPos())
				|| Geometry.getPenaltyAreaOur().lineIntersections(Lines.halfLineFromPoints(getBall().getPos(), pos))
						.stream().noneMatch(p -> passTakesLessThan(p, minPassDurationUntilReachingPenaltyArea));
	}


	private boolean passTakesLessThan(final IVector2 pos, final double duration)
	{
		double passDistance = pos.distanceTo(getBall().getPos());
		double passSpeed = OffensiveMath.passSpeedStraight(getBall().getPos(), pos, Geometry.getGoalTheir().getCenter());
		return getBall().getStraightConsultant().getTimeForKick(passDistance, passSpeed) < duration;
	}


	private class PassTargetCandidate
	{
		IVector2 pos;
		BotID botID;
		double timeUntilBotReached = 0;
		double timeUntilBallReached = 0;
		boolean reachable = false;
		boolean legal;
		boolean fromPrevFrame = false;


		PassTargetCandidate withFromPrevFrame()
		{
			fromPrevFrame = true;
			return this;
		}


		private IVector2 getPos()
		{
			return pos;
		}


		private boolean isReachable()
		{
			return reachable;
		}


		private boolean isLegal()
		{
			return legal;
		}
	}
}
