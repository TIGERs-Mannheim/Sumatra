/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.BotBallState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.VirtualBall;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Used by {@link CamFilter} to create "virtual" balls from non-vision sources.
 * E.g. robot barrier or onboard camera.
 */
public class VirtualBallProducer
{
	private IVector2 lastKnownBallPosition = Vector2f.ZERO_VECTOR;
	private final Map<BotID, Long> lastBarrierInterruptedMap = new HashMap<>();
	@Getter
	private List<VirtualBall> virtualBalls = new CopyOnWriteArrayList<>();

	private List<ITube> shadows = new ArrayList<>();

	private final Map<BotID, TreeMap<Long, Pose>> robotInfoPoseHistory = new HashMap<>();

	private long lastFrameId = 0;

	@Configurable(defValue = "750.0", comment = "Max. distance to last know ball location to create virtual balls")
	private static double maxDistanceToLastKnownPos = 750.0;

	@Configurable(defValue = "300.0", comment = "Max. distance of ball to observing robot")
	private static double maxDistanceToObserver = 300.0;

	@Configurable(defValue = "0.05", comment = "Time in [s] after which virtual balls are generated from robot info")
	private static double delayToVirtualBalls = 0.05;

	@Configurable(defValue = "10.0", comment = "Expand shadows by this amount in [mm] in all directions")
	private static double shadowMargin = 10.0;

	@Configurable(defValue = "true", comment = "Use virtual balls which are detected in camera shadows.")
	private static boolean useBallsInShadows = true;

	@Configurable(defValue = "true", comment = "Use virtual balls which are close to the observer.")
	private static boolean useBallsNearby = true;

	@Configurable(defValue = "false", comment = "Always add virtual balls, even if there are balls detected by vision.")
	private static boolean alwaysAddVirtualBalls = false;

	@Configurable(defValue = "true", comment = "If any barrier is interrupted, drop all non-barrier virtual balls.")
	private static boolean preferBarrier = true;

	@Configurable(defValue = "0.05", comment = "How long to treat barrier as interrupted after loosing contact in [s]. Only applies if not on bot cam")
	private static double keepBarrierInterruptedTime = 0.05;

	static
	{
		ConfigRegistration.registerClass("vision", VirtualBallProducer.class);
	}

	public void update(final FilteredVisionFrame frame, final Map<BotID, RobotInfo> robotInfoMap,
			final Collection<CamFilter> cams)
	{
		long timestamp = frame.getTimestamp();

		if (Math.abs(frame.getId() - lastFrameId) > 10)
			reset();

		virtualBalls.clear();

		lastFrameId = frame.getId();
		lastKnownBallPosition = frame.getBall().getPos().getXYVector();

		robotInfoMap.values().forEach(this::updateRobotInfoHistory);

		shadows = frame.getBots().stream()
				.flatMap(bot -> cams.stream()
						.filter(cam -> cam.getValidRobots().containsKey(bot.getBotID()))
						.map(cam -> computeBotShadow(cam.getCameraPosition().orElse(null), bot,
								robotInfoMap.get(bot.getBotID())))
						.flatMap(Optional::stream))
				.toList();

		boolean ballsVisibleOnCam = cams.stream()
				.anyMatch(c -> (timestamp - c.getLastBallOnCamTimestamp()) * 1e-9 < delayToVirtualBalls);

		if (ballsVisibleOnCam && !alwaysAddVirtualBalls)
			return;

		var ballCandidates = robotInfoMap.values().stream()
				.map(i -> getBallCandidate(timestamp, i))
				.flatMap(Optional::stream)
				.filter(this::isCandidateValid)
				.filter(c -> Geometry.getFieldWBorders().isPointInShape(c.getPosition().getXYVector()))
				.filter(c -> isInLineOfSight(c, frame, robotInfoMap))
				.toList();

		var candidatesFromBarrier = ballCandidates.stream()
				.filter(VirtualBall::isFromBarrier)
				.toList();

		if (preferBarrier && !candidatesFromBarrier.isEmpty())
			virtualBalls.addAll(candidatesFromBarrier);
		else
			virtualBalls.addAll(ballCandidates);
	}


	private void reset()
	{
		lastBarrierInterruptedMap.clear();
		robotInfoPoseHistory.clear();
	}


	private boolean isInLineOfSight(final VirtualBall ball, final FilteredVisionFrame frame,
			final Map<BotID, RobotInfo> robotInfoMap)
	{
		var line = Lines.segmentFromPoints(ball.getObservedFromPosition(), ball.getPosition().getXYVector());
		for (var robot : frame.getBots())
		{
			var info = robotInfoMap.get(robot.getBotID());
			if (info == null || robot.getBotID() == ball.getObservingBot())
				continue;

			IVector2 pos = info.getInternalState().map(State::getPos).orElse(robot.getPos());
			double botRadius = info.getBotParams().getDimensions().getDiameter() / 2 + Geometry.getBallRadius();
			if (line.distanceTo(pos) < botRadius && pos.distanceTo(line.getPathStart()) > botRadius
					&& pos.distanceTo(line.getPathEnd()) > botRadius)
				return false;
		}

		return true;
	}


	private void updateRobotInfoHistory(final RobotInfo info)
	{
		var timePoseMap = robotInfoPoseHistory.computeIfAbsent(info.getBotId(), k -> new TreeMap<>());
		info.getInternalState().ifPresent(s -> timePoseMap.put(info.getTimestamp(), s.getPose()));
		while (!timePoseMap.isEmpty() && timePoseMap.firstKey() < info.getTimestamp() - 1e9)
		{
			timePoseMap.remove(timePoseMap.firstKey());
		}

		if (info.isBarrierInterrupted())
		{
			lastBarrierInterruptedMap.put(info.getBotId(), info.getTimestamp());
		}
	}


	private Optional<Pose> getRobotPoseAtTimestamp(final BotID botID, long timestamp)
	{
		var timePoseMap = robotInfoPoseHistory.get(botID);

		if (timePoseMap == null || timestamp < timePoseMap.firstKey() || timestamp > timePoseMap.lastKey())
			return Optional.empty();

		var floorEntry = timePoseMap.floorEntry(timestamp);
		var ceilingEntry = timePoseMap.ceilingEntry(timestamp);
		long timespan = ceilingEntry.getKey() - floorEntry.getKey();
		if (timespan <= 0)
			return Optional.of(floorEntry.getValue());

		double fraction = (timestamp - floorEntry.getKey()) / (double) timespan;

		return Optional.of(floorEntry.getValue().interpolate(ceilingEntry.getValue(), fraction));
	}


	private Optional<ITube> computeBotShadow(IVector3 cameraPos, FilteredVisionBot bot, RobotInfo info)
	{
		if (cameraPos == null || cameraPos.z() <= 150)
			return Optional.empty();

		IVector2 botPos = Optional.ofNullable(info)
				.map(RobotInfo::getInternalState)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(State::getPos)
				.orElse(bot.getPos());

		// compute shadow area
		IVector2 farBotEdge = LineMath.stepAlongLine(botPos, cameraPos.getXYVector(), -Geometry.getBotRadius());
		IVector2 projectedFarEdge = Vector3.from2d(farBotEdge, 150).projectToGroundNew(cameraPos);
		IVector2 projectedCenter = Vector3.from2d(botPos, 150).projectToGroundNew(cameraPos);

		double projectedRadius = projectedFarEdge.distanceTo(projectedCenter);

		return Optional.of(Tube.create(botPos, projectedCenter, projectedRadius + shadowMargin));
	}


	private boolean isCandidateValid(VirtualBall candidate)
	{
		boolean isInShadow = shadows.stream().anyMatch(s -> s.isPointInShape(candidate.getPosition().getXYVector()));
		boolean isCloseToObserver = candidate.getPosition().getXYVector().distanceTo(candidate.getObservedFromPosition().getXYVector()) < maxDistanceToObserver;
		boolean isCloseToLastKnownPosition = lastKnownBallPosition.distanceTo(candidate.getPosition().getXYVector()) < maxDistanceToLastKnownPos;

		if (!isCloseToLastKnownPosition)
			return false;

		return (isInShadow && useBallsInShadows) || (isCloseToObserver && useBallsNearby);
	}


	private Optional<VirtualBall> getBallCandidate(final long timestamp, final RobotInfo info)
	{
		if (info == null)
			return Optional.empty();

		var botState = info.getInternalState();
		if(botState.isEmpty())
			return Optional.empty();

		Pose curBotPose = botState.get().getPose();
		IVector2 curBotPos = curBotPose.getPos();
		double curBotOrient = curBotPose.getOrientation();

		IVector2 centerDribblerPos = curBotPos.addNew(Vector2.fromAngle(curBotOrient).scaleTo(info.getCenter2DribblerDist()));
		IVector2 ballAtDribblerPos = curBotPos.addNew(Vector2.fromAngle(curBotOrient).scaleTo(info.getCenter2DribblerDist() + Geometry.getBallRadius()));

		boolean barrierInterrupted = Math.abs(timestamp - lastBarrierInterruptedMap.getOrDefault(info.getBotId(), 0L)) / 1e9 < keepBarrierInterruptedTime;

		var ballState = info.getBallState().orElse(null);
		if (ballState == null)
		{
			if (barrierInterrupted)
			{
				var ball = VirtualBall.builder()
						.withTimestamp(timestamp)
						.withObservingBot(info.getBotId())
						.withObservedFromPosition(centerDribblerPos)
						.withFromBarrier(true)
						.withPosition(Vector3.from2d(ballAtDribblerPos, 0))
						.withObservedPosition(Vector3.from2d(ballAtDribblerPos, 0))
						.build();

				return Optional.of(ball);
			}
		} else
		{
			return getVirtualBallFromState(timestamp, info, curBotPose, barrierInterrupted, ballState, centerDribblerPos);
		}

		return Optional.empty();
	}


	private Optional<VirtualBall> getVirtualBallFromState(long timestamp, RobotInfo info, Pose curBotPose,
			boolean barrierInterrupted, BotBallState ballState, IVector2 centerDribblerPos)
	{
		IVector2 ballPos;

		long ballTimestamp = timestamp - (long) (ballState.getAge() * 1e9);
		var optRobotPose = getRobotPoseAtTimestamp(info.getBotId(), ballTimestamp);
		if (optRobotPose.isPresent())
		{
			IVector2 botToBallLocal = BotMath.convertGlobalBotVector2Local(
					Vector2.fromPoints(optRobotPose.get().getPos(), ballState.getPos()),
					optRobotPose.get().getOrientation());
			ballPos = curBotPose.getPos().addNew(BotMath.convertLocalBotVector2Global(botToBallLocal, curBotPose.getOrientation()));
		} else
		{
			ballPos = ballState.getPos();
		}

		if (barrierInterrupted)
		{
			var botShape = BotShape.fromFullSpecification(curBotPose.getPos(),
					info.getBotParams().getDimensions().getDiameter() / 2,
					info.getBotParams().getDimensions().getCenter2DribblerDist(),
					curBotPose.getOrientation());
			var frontBallLine = botShape.withMargin(Geometry.getBallRadius()).getKickerLine().withMargin(-Geometry.getBallRadius());
			ballPos = frontBallLine.closestPointOnPath(ballPos);
		}

		var ball = VirtualBall.builder()
				.withTimestamp(timestamp)
				.withObservingBot(info.getBotId())
				.withObservedFromPosition(centerDribblerPos)
				.withFromBarrier(barrierInterrupted)
				.withPosition(Vector3.from2d(ballPos, 0))
				.withObservedPosition(Vector3.from2d(ballState.getPos(), 0))
				.build();

		return Optional.of(ball);
	}


	public List<IDrawableShape> getVirtualBallShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		for (var ball : virtualBalls)
		{
			DrawableCircle ballPos = new DrawableCircle(ball.getPosition().getXYVector(), 25, Color.ORANGE.darker());
			ballPos.setStrokeWidth(5);
			shapes.add(ballPos);

			if (ball.isFromBarrier())
			{
				DrawableLine barrierInfo = new DrawableLine(ball.getPosition().getXYVector().addNew(Vector2.fromX(-35)),
						ball.getPosition().getXYVector().addNew(Vector2.fromX(35)), Color.ORANGE.darker());
				barrierInfo.setStrokeWidth(5);
				shapes.add(barrierInfo);
			}

			DrawableLine toBall = new DrawableLine(ball.getObservedFromPosition(), ball.getPosition().getXYVector(),
					Color.GRAY);
			toBall.setStrokeWidth(2);
			shapes.add(toBall);

			DrawableCircle ballCandidatePos = new DrawableCircle(ball.getObservedPosition().getXYVector(), 30, Color.GRAY);
			ballCandidatePos.setStrokeWidth(2);
			shapes.add(ballCandidatePos);
		}

		for (var shadow : shadows)
		{
			DrawableTube tube = new DrawableTube(shadow, Color.BLACK);
			tube.setStrokeWidth(2);
			shapes.add(tube);
		}

		return shapes;
	}
}
