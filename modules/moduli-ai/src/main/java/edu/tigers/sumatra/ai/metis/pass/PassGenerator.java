/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.circle.ICircular;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;
import edu.tigers.sumatra.pathfinder.IPathFinder;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.finder.PathFinder;
import edu.tigers.sumatra.pathfinder.finder.PathFinderInput;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class PassGenerator
{
	private static final Color FORBIDDEN_AREA_COLOR = new Color(212, 102, 6, 128);
	private static final Color GENERATION_AREA_COLOR = new Color(6, 212, 188, 128);

	@Configurable(defValue = "6", comment = "How many pass targets to choose per bot in addition to those from last frame")
	private static int maxNewPassTargetsPerBot = 6;

	@Configurable(defValue = "3", comment = "How many pass targets to choose per bot from last frame")
	private static int maxOldPassTargetsPerBot = 3;

	@Configurable(defValue = "15", comment = "How many pass targets to generate per bot in addition to those from last frame")
	private static int maxGeneratePassTargetsPerBot = 15;

	@Configurable(defValue = "1500.0", comment = "Safety distance to keep to our penalty area")
	private static double safetyDistanceToPenaltyArea = 1500.0;

	@Configurable(defValue = "200.0", comment = "Safety distance to their goal line")
	private static double safetyDistanceToGoalLine = 200;

	@Configurable(defValue = "1500.0", comment = " Min distance from pass target to Ball")
	private static double minDistanceToBall = 1500.0;

	@Configurable(defValue = "1.5", comment = "Min allowed duration [s] until the ball enters our own penalty area")
	private static double minPassDurationUntilReachingPenaltyArea = 1.5;

	@Configurable(defValue = "2.0", comment = "Max horizon [s] to use for the pass target generation radius")
	private static double maxBotHorizon = 2.0;

	@Configurable(defValue = "0.9", comment = "Relative amount of time based on approx. pass duration for the the pass target generation radius")
	private static double relativeBotHorizon = 0.9;

	@Configurable(defValue = "50.0", comment = "Min distance [mm] between generated positions")
	private static double minDistanceBetweenTargets = 50;

	@Configurable(defValue = "0.08", comment = "Min angle tolerance for filtering passes during free kick")
	private static double minAngleTolerance = 0.08;

	@Configurable(defValue = "0.15", comment = "[s]")
	private static double minSlackTimeForPassReception = 0.15;

	@Configurable(defValue = "-150", comment = "Margin around the field to include passes in")
	private static double fieldMargin = -150;

	private final IPathFinder finder = new PathFinder();

	static
	{
		ConfigRegistration.registerClass("metis", PassGenerator.class);
	}

	private final PassFactory passFactory = new PassFactory();
	private final PassCreator passCreator = new PassCreator();
	private final Random rnd;
	private final Supplier<Map<BotID, List<AngleRange>>> inaccessibleBallAngles;

	private BaseAiFrame aiFrame = null;
	private Set<BotID> consideredBots;
	private KickOrigin kickOrigin;
	private final PointChecker pointChecker = new PointChecker()
			.checkInsideField()
			.checkNotInPenaltyAreas()
			.checkConfirmWithKickOffRules()
			.checkCustom("inFieldWithMargin", this::inFieldWithMargin)
			.checkCustom("keepDirectionDuringFreeKick", this::keepDirectionDuringFreeKick)
			.checkCustom("keepMinDistanceToBall", this::keepMinDistanceToBall)
			.checkCustom("canBeReceivedOutsidePenArea", this::canBeReceivedOutsidePenArea)
			.checkCustom("isTargetApproachAngleAccessible", this::isTargetApproachAngleAccessible)
			.checkCustom("isNotNearGoalLine", this::isNotNearGoalLine);


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


	public void update(BaseAiFrame aiFrame)
	{
		this.aiFrame = aiFrame;
		passFactory.update(getWFrame());
		passCreator.update(getWFrame());
		updatePenaltyAreaMargin();
	}


	public List<Pass> generatePasses(Set<BotID> consideredBots, KickOrigin kickOrigin)
	{
		this.consideredBots = consideredBots;
		this.kickOrigin = kickOrigin;

		Map<BotID, ICircle> dynamicGenerationCircles = new HashMap<>();
		consideredBots.forEach(botId -> dynamicGenerationCircles.put(botId, createDynamicGenerationCircle(botId)));

		// adds the (best) selected pass from last frame
		List<PassTargetCandidate> candidates = new ArrayList<>(candidatesFromPreviousFrame(dynamicGenerationCircles));

		// generates new passes + previous passes for each bot
		candidates.addAll(newCandidates(dynamicGenerationCircles));

		dynamicGenerationCircles.values().forEach(circle -> getShapes(EAiShapesLayer.PASS_GENERATION).add(
				new DrawableCircle(circle).setColor(GENERATION_AREA_COLOR).setFill(true)));
		getShapes(EAiShapesLayer.PASS_GENERATION_FORBIDDEN).add(
				new DrawableCircle(kickOrigin.getPos(), minDistanceToBall, FORBIDDEN_AREA_COLOR).setFill(true));
		drawForbiddenPenaltyArea();
		drawAllowedRedirectAngles();
		candidates.forEach(this::draw);
		return select(candidates);
	}


	private boolean distanceToOtherCandidatesIsValid(PassTargetCandidate candidate, List<PassTargetCandidate> candidates)
	{
		return candidates.stream().noneMatch(c -> c.getPos().distanceTo(candidate.pos) < minDistanceBetweenTargets);
	}


	private PathFinderInput getPathFinderInput(
			IVector2 destination, BotID botID, MoveConstraints currentMoveConstraints,
			List<IObstacle> obstacles
	)
	{
		var tBot = getWFrame().getBot(botID);
		long timestamp = getWFrame().getTimestamp();
		PathFinderInput.PathFinderInputBuilder inputBuilder = PathFinderInput.fromBot(tBot.getBotState());

		return inputBuilder
				.timestamp(timestamp)
				.moveConstraints(currentMoveConstraints)
				.obstacles(obstacles)
				.dest(destination)
				.build();
	}


	private boolean isReachable(IVector2 pos, BotID receiverId)
	{
		MovementCon moveCon = new MovementCon();
		ObstacleGenerator obstacleGen = new ObstacleGenerator(moveCon);

		var receiver = getWFrame().getBot(receiverId);
		moveCon.update(receiver);
		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWFrame(), receiverId, getAiFrame().getGameState());

		PathFinderInput pathFinderInput = getPathFinderInput(
				pos, receiverId, receiver.getMoveConstraints(),
				obstacles
		);

		var path = finder.calcValidDirectPath(pathFinderInput);
		return path.map(PathFinderResult::isCollisionFree).orElse(false);
	}


	private boolean isNotNearGoalLine(IVector2 pos)
	{
		return pos.x() < Geometry.getFieldLength() / 2.0 - safetyDistanceToGoalLine;
	}


	private boolean isTargetApproachAngleAccessible(IVector2 point)
	{
		var botID = kickOrigin.getShooter();
		var angle = kickOrigin.getPos().subtractNew(point).getAngle();
		return isAngleAccessible(inaccessibleBallAngles.get().get(botID), angle);
	}


	private boolean keepMinDistanceToBall(final IVector2 point)
	{
		return kickOrigin.getPos().distanceTo(point) > minDistanceToBall;
	}


	private boolean inFieldWithMargin(IVector2 point)
	{
		return Geometry.getField().withMargin(fieldMargin).isPointInShape(point);
	}


	private boolean keepDirectionDuringFreeKick(final IVector2 point)
	{
		if (!getAiFrame().getGameState().isFreeKickForUs() &&
				!getAiFrame().getGameState().isKickoffForUs())
		{
			return true;
		}

		RatedPass ratedPass = getAiFrame().getPrevFrame().getTacticalField().getSelectedPasses().entrySet().stream()
				.filter(k -> k.getKey().getPos().distanceTo(kickOrigin.getPos()) < 100)
				.findAny()
				.map(Map.Entry::getValue)
				.orElse(null);
		if (ratedPass == null)
		{
			return true;
		}

		ITrackedBot shooter = getWFrame().getBot(kickOrigin.getShooter());

		double shooterToPosDist = shooter.getBotKickerPos().distanceTo(kickOrigin.getPos()) - Geometry.getBallRadius();
		double relDist = SumatraMath.relative(
				shooterToPosDist,
				80,
				100
		);

		double angleTolerance = minAngleTolerance + relDist * (AngleMath.DEG_180_IN_RAD - minAngleTolerance);
		double dir = point.subtractNew(kickOrigin.getPos()).getAngle();
		double curDir = ratedPass.getPass().getKick().getKickVel().getXYVector().getAngle();

		return AngleMath.diffAbs(dir, curDir) < angleTolerance;
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
		var penaltyArea = Geometry.getPenaltyAreaOur().withMargin(safetyDistanceToPenaltyArea);
		getShapes(EAiShapesLayer.PASS_GENERATION_FORBIDDEN).add(
				new DrawableShapeBoundary(penaltyArea, FORBIDDEN_AREA_COLOR));
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
			return RuleConstraints.getPenAreaMarginStandard() + Geometry.getBotRadius() + Geometry.getBallRadius();
		}
		return Geometry.getBallRadius();
	}


	private List<PassTargetCandidate> candidatesFromPreviousFrame(
			Map<BotID, ICircle> dynamicGenerationCircles
	)
	{
		return getAiFrame().getPrevFrame().getTacticalField().getSelectedPasses().values().stream()
				.filter(p -> consideredBots.contains(p.getPass().getReceiver()))
				.filter(p -> isPassSourceInGenerationCircle(dynamicGenerationCircles, p))
				.map(RatedPass::getPass)
				.map(p -> createPassTargetCandidate(p.getKick().getTarget(), p.getReceiver()))
				.toList();
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
				.toList();
	}


	private List<Pass> select(final List<PassTargetCandidate> candidates)
	{
		return candidates.stream()
				.filter(p -> p.firstFailedCheck == null)
				.map(this::createPasses)
				.flatMap(Collection::stream)
				.filter(this::passIsNotReachingOurPenaltyAreaSoon)
				.toList();
	}


	private List<Pass> createPasses(PassTargetCandidate candidate)
	{
		return passCreator.createPasses(kickOrigin, candidate.pos, candidate.receiver);
	}


	private PassTargetCandidate createPassTargetCandidate(final IVector2 pos, final BotID receiver)
	{
		var nonLegal = pointChecker.findFirstNonMatching(getAiFrame(), pos, BotID.noBot());
		if (nonLegal.isEmpty() && !isReachable(pos, receiver))
		{
			nonLegal = Optional.of("isReachable");
		}
		return new PassTargetCandidate(pos, receiver, nonLegal.orElse(null));
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
				.straight(kickOrigin.getPos(), bot.getPos(), kickOrigin.getShooter(), botID, EBallReceiveMode.DONT_CARE)
				.map(Pass::getDuration)
				.orElse(Double.POSITIVE_INFINITY);
		var t = Math.min(
				maxBotHorizon,
				minApproxPassDuration + kickOrigin.impactTimeOrZero() - minSlackTimeForPassReception
		) * relativeBotHorizon;

		double aLimit = bot.getMoveConstraints().getAccMax();
		double vLimit = bot.getMoveConstraints().getVelMax();

		return MovingRobotFactory.stoppingRobot(
				bot.getBotKickerPos(Geometry.getBallRadius()),
				bot.getVel(),
				vLimit,
				aLimit,
				aLimit,
				Geometry.getBotRadius(),
				0
		).getMovingHorizon(t);
	}


	private List<PassTargetCandidate> passTargetsForBot(BotID botID, ICircular circle)
	{
		List<PassTargetCandidate> passTargets = new ArrayList<>(addPreviousPassTargets(botID, circle));

		PassTargetCandidate centerPassTargetCandidate = createPassTargetCandidate(circle.center(), botID);
		passTargets.add(centerPassTargetCandidate);

		int maxIterations = 0;
		while (passTargets.size() < maxNewPassTargetsPerBot && maxIterations < maxGeneratePassTargetsPerBot)
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
			var prevTargets = getPreviousPassTargets(botID, circle, entry.getValue());
			prevTargets.forEach(e -> getShapes(EAiShapesLayer.PASS_GENERATION).add(
					new DrawableCircle(Circle.createCircle(e.pos, 65)).setColor(Color.RED)));
			passTargets.addAll(prevTargets);
		}
		return passTargets;
	}


	private List<PassTargetCandidate> getPreviousPassTargets(BotID botID, ICircular circle, List<RatedPass> ratedPasses)
	{
		List<PassTargetCandidate> passTargets = new ArrayList<>();
		for (var ratedPass : ratedPasses)
		{
			if (ratedPass.getPass().getReceiver().equals(botID))
			{
				IVector2 targetPos = ratedPass.getPass().getKick().getTarget();
				if (circle.isPointInShape(targetPos))
				{
					passTargets.add(createPassTargetCandidate(targetPos, botID));
				}
			}
			if (passTargets.size() >= maxOldPassTargetsPerBot)
			{
				break;
			}
		}
		return passTargets;
	}


	private boolean passIsNotReachingOurPenaltyAreaSoon(Pass pass)
	{
		IHalfLine ballLine = Lines.halfLineFromPoints(getBall().getPos(), pass.getKick().getTarget());
		return Geometry.getPenaltyAreaOur().intersectPerimeterPath(ballLine).stream()
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


	private List<IDrawableShape> getShapes(IShapeLayerIdentifier shapeLayer)
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
