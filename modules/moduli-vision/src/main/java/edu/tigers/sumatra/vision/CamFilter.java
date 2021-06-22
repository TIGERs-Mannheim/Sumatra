/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamFieldSize;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableEllipse;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.filter.FirstOrderMultiSampleEstimator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.BallFilter.BallFilterOutput;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.RobotCollisionShape;
import edu.tigers.sumatra.vision.tracker.BallTracker;
import edu.tigers.sumatra.vision.tracker.RobotTracker;
import lombok.Getter;
import org.apache.commons.collections4.QueueUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Is responsible for everything that happens on a single camera.
 */
public class CamFilter
{
	private static final Logger log = LogManager.getLogger(CamFilter.class.getName());

	private final int camId;

	private static final int FRAME_FILTER_NUM_SAMPLES = 100;
	private static final int FRAME_FILTER_DIVIDER = 6;

	private final FirstOrderMultiSampleEstimator frameIntervalFilter = new FirstOrderMultiSampleEstimator(
			FRAME_FILTER_NUM_SAMPLES);
	private long lastCamFrameId;
	@Getter
	private long timestamp;

	private Optional<CamCalibration> calibration = Optional.empty();
	private Optional<IRectangle> fieldRectWithBoundary = Optional.empty();
	private Optional<IRectangle> fieldRect = Optional.empty();
	private Optional<IRectangle> viewport = Optional.empty();

	private IVector2 lastKnownBallPosition = Vector2f.ZERO_VECTOR;
	private long lastBallVisibleTimestamp = 0;

	private final Map<BotID, RobotTracker> robots = new ConcurrentHashMap<>();

	private final List<BallTracker> balls = Collections.synchronizedList(new ArrayList<>());

	private Map<BotID, RobotInfo> robotInfoMap = new ConcurrentHashMap<>();

	private Queue<CamBall> ballHistory = QueueUtils.synchronizedQueue(new CircularFifoQueue<>(100));

	@Configurable(defValue = "1.0", comment = "Time in [s] after an invisible ball is removed")
	private static double invisibleLifetimeBall = 1.0;

	@Configurable(defValue = "2.0", comment = "Time in [s] after an invisible robot is removed")
	private static double invisibleLifetimeRobot = 2.0;

	@Configurable(defValue = "0.05", comment = "Time in [s] after which virtual balls are generated from robot info (barrier)")
	private static double delayToVirtualBalls = 0.05;

	@Configurable(defValue = "10", comment = "Maximum number of ball trackers")
	private static int maxBallTrackers = 10;

	@Configurable(defValue = "1000.0", comment = "Max. distance to last know ball location to create virtual balls")
	private static double maxVirtualBallDistance = 1000.0;

	@Configurable(defValue = "true", comment = "Restrict viewport to minimize overlap (from CameraArchitect).")
	private static boolean restrictViewport = true;

	@Configurable(defValue = "0.6", comment = "Max. velocity loss at ball-bot hull collisions")
	private static double maxBallBotHullLoss = 0.6;

	@Configurable(defValue = "1.0", comment = "Max. velocity loss at ball-bot front collisions")
	private static double maxBallBotFrontLoss = 1.0;

	@Configurable(defValue = "130.0", comment = "Max. height for ball-bot collision check")
	private static double maxHeightForCollision = 130.0;

	@Configurable(defValue = "200.0", comment = "Max. distance to copy state from filtered bot to new trackers")
	private static double copyTrackerMaxDistance = 200.0;

	@Configurable(defValue = "false", comment = "Adjust frame times based on estimated frame rate and frame number")
	private static boolean adjustTCapture = false;

	static
	{
		ConfigRegistration.registerClass("vision", CamFilter.class);
	}


	/**
	 * Create a new camera filter.
	 *
	 * @param camId
	 */
	public CamFilter(final int camId)
	{
		this.camId = camId;
	}


	/**
	 * Update this camera filter with a new detection frame.
	 * Merged robots may serve as prior knowledge to initialize new trackers.
	 *
	 * @param frame
	 * @param lastFilteredFrame
	 */
	public void update(final CamDetectionFrame frame, final FilteredVisionFrame lastFilteredFrame)
	{
		checkForNonConsecutiveFrames(frame);
		CamDetectionFrame adjustedFrame = adjustTCapture(frame);

		processRobots(adjustedFrame, lastFilteredFrame.getBots());
		processBalls(adjustedFrame, lastFilteredFrame.getBall(), lastFilteredFrame.getBots());

		timestamp = adjustedFrame.gettCapture();
	}


	/**
	 * Update camera calibration data.
	 * Needed for flying ball detection.
	 *
	 * @param calib
	 */
	public void update(final CamCalibration calib)
	{
		calibration = Optional.of(calib);
	}


	/**
	 * Update field size from vision.
	 *
	 * @param field
	 */
	public void update(final CamFieldSize field)
	{
		fieldRectWithBoundary = Optional.of(field.getFieldWithBoundary().withMargin(500));
		fieldRect = Optional.of(field.getField());
	}


	/**
	 * Update viewport for this cam filter.
	 *
	 * @param rect
	 */
	public void updateViewport(final IRectangle rect)
	{
		viewport = Optional.ofNullable(rect);
	}


	public void setRobotInfoMap(final Map<BotID, RobotInfo> robotInfos)
	{
		robotInfoMap = robotInfos;
	}


	/**
	 * Set ball info from BallFilter.
	 *
	 * @param ballFilterOutput
	 */
	public void setBallInfo(final BallFilterOutput ballFilterOutput)
	{
		lastKnownBallPosition = ballFilterOutput.getLastKnownPosition().getXYVector();
		lastBallVisibleTimestamp = ballFilterOutput.getFilteredBall().getLastVisibleTimestamp();
	}


	private void reset()
	{
		frameIntervalFilter.reset();
		lastKnownBallPosition = Vector2f.ZERO_VECTOR;
		lastBallVisibleTimestamp = 0;
		robots.clear();
		balls.clear();
		ballHistory.clear();
	}


	private CamDetectionFrame adjustTCapture(final CamDetectionFrame frame)
	{
		if ((frame.getCamFrameNumber() % FRAME_FILTER_DIVIDER) == 0)
		{
			frameIntervalFilter.addSample(frame.getCamFrameNumber(), frame.gettCapture());
		}

		if (adjustTCapture)
		{
			IVector2 estimate = frameIntervalFilter.getBestEstimate().orElse(Vector2.fromXY(frame.gettCapture(), 0.0));

			double tCapture = estimate.x() + (estimate.y() * frame.getCamFrameNumber());

			return new CamDetectionFrame(frame, (long) tCapture);
		}

		return frame;
	}


	private void checkForNonConsecutiveFrames(CamDetectionFrame frame)
	{
		if (frame.getCamFrameNumber() != (lastCamFrameId + 1))
		{
			if (lastCamFrameId != 0)
			{
				log.warn("Non-consecutive cam frame for cam {}: {} -> {}", frame.getCameraId(), lastCamFrameId,
						frame.getCamFrameNumber());
			}
			if (Math.abs(frame.getCamFrameNumber() - lastCamFrameId + 1) > 10)
			{
				log.info("Resetting cam filter for cam {}", camId);
				reset();
			}
		}

		lastCamFrameId = frame.getCamFrameNumber();
	}


	private List<RobotCollisionShape> getRobotCollisionShapes(final List<FilteredVisionBot> mergedRobots)
	{
		List<RobotCollisionShape> shapes = new ArrayList<>();

		for (FilteredVisionBot bot : mergedRobots)
		{
			RobotInfo robotInfo = robotInfoMap.get(bot.getBotID());

			final double center2Drib;
			final double botRadius;
			if (robotInfo != null)
			{
				center2Drib = robotInfo.getCenter2DribblerDist();
				botRadius = robotInfo.getBotParams().getDimensions().getDiameter() * 0.5;
			} else
			{
				center2Drib = Geometry.getOpponentCenter2DribblerDist();
				botRadius = Geometry.getBotRadius();
			}

			shapes.add(new RobotCollisionShape(bot.getPos(), bot.getOrientation(), botRadius, center2Drib,
					maxBallBotHullLoss, maxBallBotFrontLoss));
		}

		return shapes;
	}


	private List<CamBall> getVirtualBalls(final long timestamp, final long frameId)
	{
		List<CamBall> virtualBalls = new ArrayList<>();

		if (((timestamp - lastBallVisibleTimestamp) * 1e-9) < delayToVirtualBalls)
		{
			return virtualBalls;
		}

		for (RobotInfo r : robotInfoMap.values())
		{
			RobotTracker tracker = robots.get(r.getBotId());
			if (tracker == null)
			{
				continue;
			}

			IVector2 ballAtDribblerPos = tracker.getPosition(timestamp)
					.addNew(Vector2.fromAngle(tracker.getOrientation(timestamp))
							.scaleTo(r.getCenter2DribblerDist() + Geometry.getBallRadius()));

			if ((ballAtDribblerPos.distanceTo(lastKnownBallPosition) < maxVirtualBallDistance) && r.isBarrierInterrupted())
			{
				CamBall camBall = new CamBall(0, 0, Vector3.from2d(ballAtDribblerPos, 0), Vector2f.ZERO_VECTOR,
						timestamp, camId, frameId);
				virtualBalls.add(camBall);
			}
		}

		return virtualBalls;
	}


	/**
	 * Get camera position from calibration data.
	 *
	 * @return
	 */
	public Optional<IVector3> getCameraPosition()
	{
		return calibration.map(CamCalibration::getCameraPosition);

	}


	/**
	 * Get average time between two detection frames.
	 *
	 * @return
	 */
	public double getAverageFrameDt()
	{
		return frameIntervalFilter.getBestEstimate().map(IVector2::y).map(d -> d * 1e-9).orElse(0.01);
	}


	private void processRobots(final CamDetectionFrame frame, final List<FilteredVisionBot> mergedRobots)
	{
		// remove trackers of bots that have not been visible for some time
		robots.entrySet()
				.removeIf(
						e -> ((frame.gettCapture() - e.getValue().getLastUpdateTimestamp()) * 1e-9) > invisibleLifetimeRobot);

		// remove trackers out of field
		fieldRectWithBoundary.ifPresent(iRectangle -> robots.entrySet()
				.removeIf(
						e -> !iRectangle.isPointInShape(e.getValue().getPosition(frame.gettCapture()))));

		// do a prediction on all trackers
		for (RobotTracker r : robots.values())
		{
			r.predict(frame.gettCapture(), getAverageFrameDt());
		}

		for (CamRobot r : frame.getRobots())
		{
			// ignore robots outside our viewport
			if (restrictViewport && viewport.isPresent() && !viewport.get().isPointInShape(r.getPos()))
			{
				continue;
			}

			// check if there are other robots very close by, could be a false vision detection then
			// we filter out the robot with the cam bots id before to allow trackers at the same location
			long numCloseTrackers = mergedRobots.stream()
					.filter(m -> m.getBotID() != r.getBotId())
					.filter(m -> m.getPos().distanceTo(r.getPos()) < (Geometry.getBotRadius() * 1.5)).count();

			if (numCloseTrackers > 0)
			{
				log.debug("[" + r.getCameraId() + "] Ignoring new robot " + r.getBotId());
			} else
			{
				if (robots.containsKey(r.getBotId()))
				{
					// we already have a tracker for that robot, update it
					robots.get(r.getBotId()).update(r);
				} else
				{
					// completely new robot on the field
					createNewRobotTracker(r, mergedRobots);
				}
			}
		}
	}


	private void createNewRobotTracker(final CamRobot robot, final List<FilteredVisionBot> mergedRobots)
	{
		RobotTracker tracker;
		Optional<FilteredVisionBot> filteredBot = mergedRobots.stream()
				.filter(m -> m.getBotID() == robot.getBotId())
				.findFirst();

		if (filteredBot.isPresent() && fieldRectWithBoundary.isPresent()
				&& fieldRectWithBoundary.get().isPointInShape(filteredBot.get().getPos())
				&& filteredBot.get().getPos().distanceTo(robot.getPos()) < copyTrackerMaxDistance)
		{
			// but it is on a different camera already, copy its state from there
			tracker = new RobotTracker(robot, filteredBot.get());
		} else
		{
			// completely new robot on the field
			tracker = new RobotTracker(robot);
		}
		robots.put(robot.getBotId(), tracker);
	}


	private void processBalls(final CamDetectionFrame frame, final FilteredVisionBall ball,
			final List<FilteredVisionBot> mergedRobots)
	{
		// remove trackers of balls that have not been visible for some time
		balls.removeIf(e -> ((frame.gettCapture() - e.getLastUpdateTimestamp()) * 1e-9) > invisibleLifetimeBall);

		// remove trackers of balls that were out of the field for too long
		balls.removeIf(e -> ((frame.gettCapture() - e.getLastInFieldTimestamp()) * 1e-9) > invisibleLifetimeBall);

		List<RobotCollisionShape> colShapes = getRobotCollisionShapes(mergedRobots);

		// do a prediction on all trackers
		for (BallTracker b : balls)
		{
			b.predict(frame.gettCapture(), colShapes, ball.getPos().z() > maxHeightForCollision);
		}

		List<CamBall> camBalls = new ArrayList<>(frame.getBalls());
		if (frame.getBalls().isEmpty())
		{
			camBalls.addAll(getVirtualBalls(frame.gettCapture(), frame.getFrameNumber()));
		}

		// iterate over all balls on the camera
		for (CamBall b : camBalls)
		{
			boolean consumed = false;

			for (BallTracker t : balls)
			{
				// offer ball to all trackers
				if (t.update(b, fieldRect))
				{
					// tracker accepted this ball
					ballHistory.add(b);
					consumed = true;
					break;
				}
			}

			if (!consumed)
			{
				createNewBallTracker(b, ball);
			}
		}
	}


	private void createNewBallTracker(final CamBall cam, final FilteredVisionBall filtBall)
	{
		if (balls.size() > maxBallTrackers)
		{
			return;
		}

		// if this ball is not used by any other tracker we may do:
		// - if we know the field size => only accept new balls on the field (not in boundary area)
		// - if we don't know the field size => simply accept the ball
		if (fieldRectWithBoundary.isEmpty() || fieldRectWithBoundary.get().isPointInShape(cam.getPos().getXYVector()))
		{
			// if nobody else wanted this ball we create a new tracker, very gentle :)
			BallTracker tracker;
			if (filtBall == null)
			{
				tracker = new BallTracker(cam);
			} else
			{
				tracker = new BallTracker(cam, filtBall);
			}
			tracker.setMaxDistance(500);
			balls.add(tracker);
		}
	}


	/**
	 * Get all robot trackers.
	 *
	 * @return
	 */
	public Map<BotID, RobotTracker> getValidRobots()
	{
		return robots;
	}


	/**
	 * Get camera id.
	 *
	 * @return the camId
	 */
	public int getCamId()
	{
		return camId;
	}


	/**
	 * @return the balls
	 */
	public List<BallTracker> getBalls()
	{
		return new ArrayList<>(balls);
	}


	/**
	 * Camera info shapes.
	 *
	 * @return
	 */
	public List<IDrawableShape> getInfoShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		if (calibration.isEmpty())
		{
			return shapes;
		}

		IVector3 pos3D = calibration.get().getCameraPosition();
		IVector2 pos = pos3D.getXYVector();

		// Draw camera origin cross
		DrawableLine xLine = new DrawableLine(
				Line.fromPoints(pos.subtractNew(Vector2.fromX(100)), pos.addNew(Vector2.fromX(100))));
		xLine.setStrokeWidth(20);
		xLine.setColor(Color.CYAN);
		shapes.add(xLine);

		DrawableLine yLine = new DrawableLine(
				Line.fromPoints(pos.subtractNew(Vector2.fromY(100)), pos.addNew(Vector2.fromY(100))));
		yLine.setStrokeWidth(20);
		yLine.setColor(Color.CYAN);
		shapes.add(yLine);

		// Draw camera id
		DrawableAnnotation id = new DrawableAnnotation(pos, Integer.toString(camId));
		id.withOffset(Vector2.fromXY(-30, -80));
		id.setColor(Color.CYAN);
		id.withFontHeight(120);
		shapes.add(id);

		// Draw update rate of this camera (smoothed)
		DrawableAnnotation camRate = new DrawableAnnotation(pos,
				String.format("Rate: %.1fHz", 1.0 / getAverageFrameDt()));
		camRate.withOffset(Vector2.fromXY(40, -60));
		camRate.withFontHeight(50);
		camRate.setColor(Color.GRAY);
		shapes.add(camRate);

		// Annotate mounting height of this cam
		DrawableAnnotation height = new DrawableAnnotation(pos,
				String.format("Height: %.2fm", pos3D.z() * 0.001));
		height.withOffset(Vector2.fromXY(40, 60));
		height.withFontHeight(50);
		height.setColor(Color.GRAY);
		shapes.add(height);

		return shapes;
	}


	/**
	 * Robot tracker info shapes.
	 *
	 * @param timestamp
	 * @return
	 */
	public List<IDrawableShape> getRobotTrackerShapes(final long timestamp)
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		for (RobotTracker tracker : robots.values())
		{
			shapes.addAll(tracker.getInfoShapes(timestamp));

			IVector2 pos = tracker.getPosition(timestamp);

			DrawableAnnotation id = new DrawableAnnotation(pos, Integer.toString(camId), true);
			id.withOffset(Vector2.fromY(-150));
			id.setColor(tracker.getBotId().getTeamColor().getColor());
			shapes.add(id);

			DrawableAnnotation age = new DrawableAnnotation(pos,
					String.format("%d: %.3fs", camId,
							(timestamp - tracker.getLastUpdateTimestamp()) * 1e-9));
			age.withOffset(Vector2.fromXY(150, (camId * 45.0) - 100.0));
			age.setColor(Color.GREEN);
			shapes.add(age);

			final Optional<IVector3> cameraPosition = getCameraPosition();
			if (cameraPosition.isPresent() && (cameraPosition.get().z() > 120.0))
			{
				DrawableEllipse shadow = new DrawableEllipse(
						Circle.createCircle(pos, 90).projectToGround(cameraPosition.get(), 120),
						Color.BLACK);
				shadow.setFill(false);
				shadow.setStrokeWidth(5);
				shapes.add(shadow);
			}
		}

		return shapes;
	}


	/**
	 * Get ball info shapes.
	 *
	 * @return
	 */
	public List<IDrawableShape> getBallInfoShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		var ballHistorySnapshot = new ArrayList<>(ballHistory);
		for (CamBall b : ballHistorySnapshot)
		{
			DrawableCircle pos = new DrawableCircle(b.getFlatPos(), 15, Color.BLACK);
			pos.setFill(false);
			pos.setStrokeWidth(3);
			shapes.add(pos);

			DrawableAnnotation id = new DrawableAnnotation(b.getFlatPos(), Integer.toString(camId));
			id.withFontHeight(24);
			id.withCenterHorizontally(true);
			id.setColor(Color.BLACK);
			id.setStrokeWidth(2);
			shapes.add(id);
		}

		return shapes;
	}


	/**
	 * @return the viewport
	 */
	public Optional<IRectangle> getViewport()
	{
		return viewport;
	}
}
