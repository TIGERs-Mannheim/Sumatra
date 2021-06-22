/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.circle.ICircular;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class PassGenerator
{
	private static final Color FORBIDDEN_AREA_COLOR = new Color(212, 102, 6, 128);
	private static final Color GENERATION_AREA_COLOR = new Color(6, 212, 188, 128);

	@Configurable(defValue = "10", comment = "How many pass targets to generate per bot in addition to those from last frame")
	private static int maxNewPassTargetsPerBot = 10;

	@Configurable(defValue = "1500.0", comment = "Safety distance to keep to our penalty area")
	private static double safetyDistanceToPenaltyArea = 1500.0;

	@Configurable(defValue = "500.0", comment = " Min distance from pass target to Ball")
	private static double minDistanceToBall = 500.0;

	@Configurable(defValue = "1.5", comment = "Min allowed duration [s] until the ball enters our own penalty area")
	private static double minPassDurationUntilReachingPenaltyArea = 1.5;

	@Configurable(defValue = "2.0", comment = "Max horizon [s] to use for the pass target generation radius")
	private static double maxBotHorizon = 2.0;

	@Configurable(defValue = "0.9", comment = "Relative amount of time based on approx. pass duration for the the pass target generation radius")
	private static double relativBotHorizon = 0.9;

	@Configurable(defValue = "0.3", comment = "The time [s] that the pass receiver should get after it moved to the target pos")
	private static double minPassReceiverPrepareTime = 0.3;

	@Configurable(defValue = "50.0", comment = "Min distance [mm] between generated positions")
	private static double minDistanceBetweenTargets = 50;

	@Configurable(defValue = "0.2", comment = "Minimum angle range in front of kick origin for pass generation")
	private static double minAngleRange = 0.2;

	@Configurable(defValue = "0.3", comment = "Lower impact time at which the minimum angle range applies")
	private static double impactTimeLower = 0.3;

	@Configurable(defValue = "2.0", comment = "Upper impact time at which passes in all directions are generated")
	private static double impactTimeUpper = 2.0;

	static
	{
		ConfigRegistration.registerClass("metis", PassGenerator.class);
	}

	private final PassFactory passFactory = new PassFactory();
	private final PointChecker pointChecker = new PointChecker()
			.checkInsideField()
			.checkNotInPenaltyAreas()
			.checkConfirmWithKickOffRules()
			.checkCustom("keepMinDistanceToBall", this::keepMinDistanceToBall)
			.checkCustom("canBeReceivedOutsidePenArea", this::canBeReceivedOutsidePenArea)
			.checkCustom("isRedirectAngleDoable", this::isRedirectAngleDoable)
			.checkCustom("isTargetApproachAngleAccessible", this::isTargetApproachAngleAccessible)
			.checkCustom("isDoablePassDirection", this::isDoablePassDirection);

	private final Random rnd;
	private final Supplier<Map<BotID, List<AngleRange>>> inaccessibleBallAngles;
	private BaseAiFrame aiFrame = null;

	private Set<BotID> consideredBots;
	private KickOrigin kickOrigin;
	private double botOrientation;
	private double maxAngleRange;


	public void update(BaseAiFrame aiFrame)
	{
		this.aiFrame = aiFrame;
		passFactory.update(getWFrame());
		updatePenaltyAreaMargin();
	}


	public List<Pass> generatePasses(Set<BotID> consideredBots, KickOrigin kickOrigin)
	{
		this.consideredBots = consideredBots;
		this.kickOrigin = kickOrigin;
		botOrientation = getWFrame().getBot(kickOrigin.getShooter()).getOrientation();
		maxAngleRange = findMaxAngleRange();

		Map<BotID, ICircle> dynamicGenerationCircles = new IdentityHashMap<>();
		consideredBots.forEach(botId -> dynamicGenerationCircles.put(botId, createDynamicGenerationCircle(botId)));

		List<PassTargetCandidate> candidates = new ArrayList<>(candidatesFromPreviousFrame(dynamicGenerationCircles));
		candidates.addAll(newCandidates(dynamicGenerationCircles));

		dynamicGenerationCircles.values().forEach(circle -> getShapes(EAiShapesLayer.PASS_GENERATION).add(
				new DrawableCircle(circle).setColor(GENERATION_AREA_COLOR).setFill(true)));
		getShapes(EAiShapesLayer.PASS_GENERATION_FORBIDDEN).add(
				new DrawableCircle(kickOrigin.getPos(), minDistanceToBall, FORBIDDEN_AREA_COLOR).setFill(true));
		drawMaxAngleRange();
		drawForbiddenPenaltyArea();
		drawAllowedRedirectAngles();
		candidates.forEach(this::draw);
		return select(candidates);
	}


	private void drawMaxAngleRange()
	{
		if (maxAngleRange < AngleMath.DEG_180_IN_RAD)
		{
			IArc arc = Arc.createArc(kickOrigin.getPos(), 5000, botOrientation - maxAngleRange, maxAngleRange * 2);
			getShapes(EAiShapesLayer.PASS_GENERATION_PASS_DIR).add(
					new DrawableArc(arc).setFill(true).setColor(new Color(79, 123, 0, 168)));
		}
	}

	private boolean distanceToOtherCandidatesIsValid(PassTargetCandidate candidate, List<PassTargetCandidate> candidates)
	{
		return candidates.stream().noneMatch(c -> c.getPos().distanceTo(candidate.pos) < minDistanceBetweenTargets);
	}

	private double findMaxAngleRange()
	{
		if (kickOrigin.getImpactTime() > 0)
		{
			double relImpactTime = SumatraMath.relative(kickOrigin.getImpactTime(), impactTimeLower, impactTimeUpper);
			return relImpactTime * (AngleMath.DEG_180_IN_RAD - minAngleRange) + minAngleRange;
		}
		return AngleMath.DEG_180_IN_RAD;
	}


	private boolean isDoablePassDirection(IVector2 pos)
	{
		var targetAngle = pos.subtractNew(kickOrigin.getPos()).getAngle();
		return AngleMath.diffAbs(targetAngle, botOrientation) < maxAngleRange;
	}


	private boolean isRedirectAngleDoable(IVector2 point)
	{
		if (kickOrigin.getShooter().isUninitializedID())
		{
			return true;
		}

		IVector2 targetToReceiver = kickOrigin.getPos().subtractNew(point);
		IVector2 senderToReceiver = kickOrigin.getPos().subtractNew(getBall().getPos());

		double angle = targetToReceiver.angleToAbs(senderToReceiver).orElse(0.0);
		return angle < OffensiveConstants.getMaximumReasonableRedirectAngle();
	}


	private boolean isTargetApproachAngleAccessible(IVector2 point)
	{
		var botID = kickOrigin.getShooter();
		var angle = kickOrigin.getPos().subtractNew(point).getAngle();
		return isAngleAccessible(inaccessibleBallAngles.get().get(botID), angle);
	}


	static boolean isAngleAccessible(List<AngleRange> inaccessibleAngles, double angleToCheck)
	{
		for (AngleRange range : inaccessibleAngles)
		{
			if (SumatraMath.isBetween(angleToCheck, range.getRight(), range.getLeft()))
			{
				return false;
			}
		}
		return true;
	}


	private boolean keepMinDistanceToBall(final IVector2 point)
	{
		return kickOrigin.getPos().distanceTo(point) > minDistanceToBall;
	}


	private boolean canBeReceivedOutsidePenArea(final IVector2 point)
	{
		IVector2 receivePos = LineMath.stepAlongLine(point, kickOrigin.getPos(), -Geometry.getBotRadius());
		return !Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius()).isPointInShape(receivePos);
	}


	private void updatePenaltyAreaMargin()
	{
		pointChecker.setTheirPenAreaMargin(theirPenAreaMargin());
		pointChecker.setOurPenAreaMargin(safetyDistanceToPenaltyArea);
	}


	private void drawForbiddenPenaltyArea()
	{
		var penAreaShapes = Geometry.getPenaltyAreaOur().withMargin(safetyDistanceToPenaltyArea).getDrawableShapes();
		penAreaShapes.forEach(p -> p.setColor(FORBIDDEN_AREA_COLOR).setFill(true));
		getShapes(EAiShapesLayer.PASS_GENERATION_FORBIDDEN).addAll(penAreaShapes);
	}


	private void drawAllowedRedirectAngles()
	{
		var botID = kickOrigin.getShooter();
		if (botID.isBot())
		{
			IVector2 originToBall = getBall().getPos().subtractNew(kickOrigin.getPos());
			double startAngle = originToBall.getAngle() - OffensiveConstants.getMaximumReasonableRedirectAngle();
			double rotation = OffensiveConstants.getMaximumReasonableRedirectAngle() * 2;
			getShapes(EAiShapesLayer.PASS_GENERATION_REDIRECT)
					.add(new DrawableArc(Arc.createArc(kickOrigin.getPos(), 1000, startAngle, rotation))
							.setColor(new Color(238, 149, 82, 141)).setFill(true));
		}
	}


	private double theirPenAreaMargin()
	{
		if (getAiFrame().getGameState().isStandardSituation())
		{
			return RuleConstraints.getBotToPenaltyAreaMarginStandard() + Geometry.getBotRadius();
		}
		return Geometry.getBallRadius();
	}


	private List<PassTargetCandidate> candidatesFromPreviousFrame(
			Map<BotID, ICircle> dynamicGenerationCircles)
	{
		return getAiFrame().getPrevFrame().getTacticalField().getSelectedPasses().values().stream()
				.filter(p -> consideredBots.contains(p.getPass().getReceiver()))
				.filter(p -> isPassSourceInGenerationCircle(dynamicGenerationCircles, p))
				.map(RatedPass::getPass)
				.map(p -> createPassTargetCandidate(p.getKick().getTarget(), p.getReceiver()))
				.collect(Collectors.toList());
	}


	private boolean isPassSourceInGenerationCircle(Map<BotID, ICircle> dynamicGenerationCircles, RatedPass p)
	{
		return dynamicGenerationCircles.get(p.getPass().getReceiver())
				.isPointInShape(p.getPass().getKick().getTarget());
	}


	private List<PassTargetCandidate> newCandidates(Map<BotID, ICircle> dynamicGenerationCircles)
	{
		return consideredBots.stream()
				.map(botID -> passTargetsForBot(botID, dynamicGenerationCircles.get(botID)))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}


	private List<Pass> select(final List<PassTargetCandidate> candidates)
	{
		return candidates.stream()
				.filter(p -> p.firstFailedCheck == null)
				.map(this::createPasses)
				.flatMap(Collection::stream)
				.filter(this::passIsNotReachingOurPenaltyAreaSoon)
				.collect(Collectors.toList());
	}


	private List<Pass> createPasses(PassTargetCandidate candidate)
	{
		var minPassDuration = timeUntilBotReached(candidate.pos, candidate.receiver)
				- timeTillBallReachesBot()
				+ minPassReceiverPrepareTime;
		var origin = kickOrigin.getPos();
		var shooter = kickOrigin.getShooter();
		return passFactory.passes(origin, candidate.pos, shooter, candidate.receiver, minPassDuration);
	}


	private PassTargetCandidate createPassTargetCandidate(final IVector2 pos, final BotID receiver)
	{
		var nonLegal = pointChecker.findFirstNonMatching(getAiFrame(), pos, BotID.noBot());
		return new PassTargetCandidate(pos, receiver, nonLegal.orElse(null));
	}


	private double timeUntilBotReached(final IVector2 pos, final BotID botID)
	{
		ITrackedBot tBot = getWFrame().getBot(botID);
		return TrajectoryGenerator.generatePositionTrajectory(tBot, pos).getTotalTime();
	}


	private void draw(final PassTargetCandidate candidate)
	{
		Color pointColor = candidate.firstFailedCheck == null ? Color.BLACK : Color.RED;
		getShapes(EAiShapesLayer.PASS_GENERATION)
				.add(new DrawablePoint(candidate.getPos(), pointColor));
		if (candidate.firstFailedCheck != null)
		{
			getShapes(EAiShapesLayer.PASS_GENERATION)
					.add(new DrawableAnnotation(candidate.getPos(), candidate.firstFailedCheck)
							.withOffset(Vector2f.fromX(100))
							.setColor(Color.RED));
		}
	}


	private ICircle createDynamicGenerationCircle(BotID botID)
	{
		ITrackedBot bot = getWFrame().getBot(botID);

		var minApproxPassDuration = passFactory
				.straight(kickOrigin.getPos(), bot.getPos(), BotID.noBot(), botID)
				.getDuration();
		var botHorizon = Math.min(maxBotHorizon, minApproxPassDuration + timeTillBallReachesBot()) * relativBotHorizon;

		double botSpeed = bot.getVel().getLength2();
		double botMaxAcc = bot.getMoveConstraints().getAccMax();
		double botBrkTime = botSpeed / botMaxAcc;
		double botAdditionalTime = Math.max(0, botHorizon - botBrkTime);
		double distBrk = botSpeed * botBrkTime * 0.5;

		double fwdMaxReachableVel = botSpeed + (botMaxAcc * botAdditionalTime * 0.5);
		double fwdMaxVel = Math.min(fwdMaxReachableVel, bot.getMoveConstraints().getVelMax());
		double fwdAddVel = fwdMaxVel - botSpeed;
		double fwdAccBrkTime = fwdAddVel / botMaxAcc;
		double fwdDistAccBrk = botSpeed * fwdAccBrkTime + fwdAddVel * fwdAccBrkTime * 0.5;
		double fwdDistDrive = fwdMaxVel * (botAdditionalTime - fwdAccBrkTime * 2);
		double fwdDist = fwdDistAccBrk * 2 + fwdDistDrive;

		double bwdMaxReachableVel = (botMaxAcc * botAdditionalTime * 0.5);
		double bwdMaxVel = Math.min(bwdMaxReachableVel, bot.getMoveConstraints().getVelMax());
		double bwdAccBrkTime = bwdMaxVel / botMaxAcc;
		double bwdDistAccBrk = bwdMaxVel * bwdAccBrkTime * 0.5;
		double bwdDistDrive = bwdMaxVel * (botAdditionalTime - bwdAccBrkTime * 2);
		double bwdDist = bwdDistAccBrk * 2 + bwdDistDrive;

		double offset = distBrk + (fwdDist - bwdDist) / 2;
		double radius = Math.max(1e-3, (fwdDist + bwdDist) / 2);

		IVector2 center = bot.getBotKickerPos(Geometry.getBallRadius())
				.addNew(bot.getVel().scaleToNew(offset * 1000));

		return Circle.createCircle(center, radius * 1000);
	}


	private double timeTillBallReachesBot()
	{
		return Double.isFinite(kickOrigin.getImpactTime()) ? kickOrigin.getImpactTime() : 0.0;
	}


	private List<PassTargetCandidate> passTargetsForBot(BotID botID, ICircular circle)
	{
		List<PassTargetCandidate> passTargets = new ArrayList<>(addPreviousPassTargets(botID, circle));

		PassTargetCandidate centerPassTargetCandidate = createPassTargetCandidate(circle.center(), botID);
		passTargets.add(centerPassTargetCandidate);

		int maxIterations = 0;
		while (passTargets.size() < maxNewPassTargetsPerBot && maxIterations < maxNewPassTargetsPerBot * 1.5)
		{
			double angle = AngleMath.PI_TWO * rnd.nextDouble();
			double radius = circle.radius() * rnd.nextDouble();
			IVector2 targetPos = circle.center().addNew(Vector2.fromAngleLength(angle, radius));
			PassTargetCandidate candidate = createPassTargetCandidate(targetPos, botID);
			if (distanceToOtherCandidatesIsValid(candidate, passTargets))
			{
				passTargets.add(candidate);
			}
			maxIterations++;
		}

		return passTargets;
	}


	private List<PassTargetCandidate> addPreviousPassTargets(BotID botID, ICircular circle)
	{
		List<PassTargetCandidate> passTargets = new ArrayList<>();
		var previousPasses = aiFrame.getPrevFrame().getTacticalField().getFilteredAndRatedPassesMap();
		for (var entry : previousPasses.entrySet())
		{
			passTargets.addAll(getPreviousPassTargets(botID, circle, entry.getValue()));
		}
		return passTargets;
	}


	private List<PassTargetCandidate> getPreviousPassTargets(BotID botID, ICircular circle, List<RatedPass> ratedPasses)
	{
		List<PassTargetCandidate> passTargets = new ArrayList<>();
		int addedPasses = 0;
		for (var ratedPass : ratedPasses)
		{
			if (ratedPass.getPass().getReceiver() == botID)
			{
				IVector2 targetPos = ratedPass.getPass().getKick().getTarget();
				if (circle.isPointInShape(targetPos))
				{
					passTargets.add(createPassTargetCandidate(targetPos, botID));
					addedPasses++;
				}
			}
			if (addedPasses > maxNewPassTargetsPerBot / 2)
			{
				break;
			}
		}
		return passTargets;
	}


	private boolean passIsNotReachingOurPenaltyAreaSoon(Pass pass)
	{
		IHalfLine ballLine = Lines.halfLineFromPoints(getBall().getPos(), pass.getKick().getTarget());
		return Geometry.getPenaltyAreaOur().lineIntersections(ballLine).stream()
				.noneMatch(p -> timeTillPosReached(pass, p) < minPassDurationUntilReachingPenaltyArea);
	}


	private double timeTillPosReached(Pass pass, IVector2 pos)
	{
		double distance = pos.distanceTo(pass.getKick().getSource());
		double kickSpeed = pass.getKick().getKickParams().getKickSpeed();
		return getBall().getStraightConsultant().getTimeForKick(distance, kickSpeed);
	}


	private BaseAiFrame getAiFrame()
	{
		return aiFrame;
	}


	private WorldFrame getWFrame()
	{
		return getAiFrame().getWorldFrame();
	}


	private ITrackedBall getBall()
	{
		return getAiFrame().getWorldFrame().getBall();
	}


	private List<IDrawableShape> getShapes(IShapeLayer shapeLayer)
	{
		return getAiFrame().getShapeMap().get(shapeLayer);
	}


	@Value
	private static class PassTargetCandidate
	{
		IVector2 pos;
		BotID receiver;
		String firstFailedCheck;
	}
}
