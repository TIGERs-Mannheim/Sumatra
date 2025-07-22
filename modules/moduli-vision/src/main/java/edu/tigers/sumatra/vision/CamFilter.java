/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.filter.FirstOrderMultiSampleEstimator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.RobotCollisionShape;
import edu.tigers.sumatra.vision.data.VirtualBall;
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
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Is responsible for everything that happens on a single camera.
 */
public class CamFilter
{
	private static final Logger log = LogManager.getLogger(CamFilter.class.getName());

	private final int camId;

	private static final int FRAME_FILTER_NUM_SAMPLES = 100;
	private static final int FRAME_FILTER_DIVIDER = 6;
	private static final double MISSING_FRAME_TIMESPAN = 30.0;

	private final FirstOrderMultiSampleEstimator frameIntervalFilter = new FirstOrderMultiSampleEstimator(
			FRAME_FILTER_NUM_SAMPLES);
	private long lastCamFrameId;
	@Getter
	private long timestamp;

	private Optional<CamCalibration> calibration = Optional.empty();
	private Optional<IRectangle> fieldRectWithBoundary = Optional.empty();
	private Optional<IRectangle> fieldRect = Optional.empty();
	private Optional<IRectangle> viewport = Optional.empty();

	private final Map<BotID, RobotTracker> robots = new ConcurrentHashMap<>();

	private final List<BallTracker> balls = Collections.synchronizedList(new ArrayList<>());

	private Map<BotID, RobotInfo> robotInfoMap = new ConcurrentHashMap<>();

	private Queue<CamBall> ballHistory = QueueUtils.synchronizedQueue(new CircularFifoQueue<>(100));

	private final List<Long> missingFrameTimestamps = new CopyOnWriteArrayList<>();

	@Getter
	private long lastBallOnCamTimestamp = 0;

	@Configurable(defValue = "1.0", comment = "Time in [s] after an invisible ball is removed")
	private static double invisibleLifetimeBall = 1.0;

	@Configurable(defValue = "2.0", comment = "Time in [s] after an invisible robot is removed")
	private static double invisibleLifetimeRobot = 2.0;

	@Configurable(defValue = "10", comment = "Maximum number of ball trackers")
	private static int maxBallTrackers = 10;

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
	public void update(
			final CamDetectionFrame frame, final FilteredVisionFrame lastFilteredFrame,
			final List<VirtualBall> virtualBalls
	)
	{
		checkForNonConsecutiveFrames(frame);

		if ((frame.getCamFrameNumber() % FRAME_FILTER_DIVIDER) == 0)
		{
			frameIntervalFilter.addSample(frame.getCamFrameNumber(), frame.getTimestamp());
		}

		if (frameIntervalFilter.getNumSamples() > FRAME_FILTER_NUM_SAMPLES / 10)
		{
			double frameDt = (frame.getTimestamp() - timestamp) * 1e-9;

			if (frameDt > getAverageFrameDt() * 1.5)
			{
				missingFrameTimestamps.add(frame.getTimestamp() - (long) (getAverageFrameDt() * 1e9));
			}

			missingFrameTimestamps.removeIf(t -> (frame.getTimestamp() - t) * 1e-9 > MISSING_FRAME_TIMESPAN);
		}

		processRobots(frame, lastFilteredFrame.getBots());
		processBalls(frame, lastFilteredFrame.getBall(), lastFilteredFrame.getBots(), virtualBalls);

		timestamp = frame.getTimestamp();
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
		fieldRect = Optional.of(
				Rectangle.fromCenter(Vector2f.ZERO_VECTOR, field.getFieldLength(), field.getFieldWidth())
		);
		fieldRectWithBoundary = fieldRect.map(r -> r.withMargin(500 + field.getBoundaryWidth()));
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


	private void reset()
	{
		frameIntervalFilter.reset();
		missingFrameTimestamps.clear();
		robots.clear();
		balls.clear();
		ballHistory.clear();
	}


	private void checkForNonConsecutiveFrames(CamDetectionFrame frame)
	{
		if (frame.getCamFrameNumber() != (lastCamFrameId + 1))
		{
			if (lastCamFrameId != 0)
			{
				log.warn(
						"Non-consecutive cam frame for cam {}: {} -> {}", frame.getCameraId(), lastCamFrameId,
						frame.getCamFrameNumber()
				);
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

			shapes.add(new RobotCollisionShape(
					bot.getPos(), bot.getOrientation(), bot.getVel().multiplyNew(1e3), botRadius, center2Drib,
					maxBallBotHullLoss, maxBallBotFrontLoss
			));
		}

		return shapes;
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


	/**
	 * Get number of missed frames in the last MISSING_FRAME_TIMESPAN seconds.
	 *
	 * @return
	 */
	public int getNumMissingFrames()
	{
		return missingFrameTimestamps.size();
	}


	/**
	 * Get rate of missed frames versus expected frames.
	 *
	 * @return
	 */
	public double getFrameMissRate()
	{
		double expectedNumFrames = 1.0 / getAverageFrameDt() * MISSING_FRAME_TIMESPAN;
		return getNumMissingFrames() / expectedNumFrames;
	}


	private void processRobots(final CamDetectionFrame frame, final List<FilteredVisionBot> mergedRobots)
	{
		// remove trackers of bots that have not been visible for some time
		robots.entrySet()
				.removeIf(
						e -> ((frame.getTimestamp() - e.getValue().getLastUpdateTimestamp()) * 1e-9)
								> invisibleLifetimeRobot);

		// remove trackers out of field
		fieldRectWithBoundary.ifPresent(iRectangle -> robots.entrySet()
				.removeIf(
						e -> !iRectangle.isPointInShape(e.getValue().getPosition(frame.getTimestamp()))));

		// do a prediction on all trackers
		for (RobotTracker r : robots.values())
		{
			r.predict(frame.getTimestamp(), getAverageFrameDt());
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
					.filter(m -> !m.getBotID().equals(r.getBotId()))
					.filter(m -> m.getPos().distanceTo(r.getPos()) < (Geometry.getBotRadius() * 1.5)).count();

			if (numCloseTrackers > 0)
			{
				log.debug("[{}] Ignoring new robot {}", r.getCameraId(), r.getBotId());
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
				.filter(m -> m.getBotID().equals(robot.getBotId()))
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


	private void processBalls(
			final CamDetectionFrame frame, final FilteredVisionBall ball,
			final List<FilteredVisionBot> mergedRobots, final List<VirtualBall> virtualBalls
	)
	{
		// remove trackers of balls that have not been visible for some time
		balls.removeIf(e -> ((frame.getTimestamp() - e.getLastUpdateTimestamp()) * 1e-9) > invisibleLifetimeBall);

		// remove trackers of balls that were out of the field for too long
		balls.removeIf(e -> ((frame.getTimestamp() - e.getLastInFieldTimestamp()) * 1e-9) > invisibleLifetimeBall);

		List<RobotCollisionShape> colShapes = getRobotCollisionShapes(mergedRobots);

		// do a prediction on all trackers
		for (BallTracker b : balls)
		{
			b.predict(frame.getTimestamp(), colShapes, ball.getPos().z() > maxHeightForCollision);
		}

		if (!frame.getBalls().isEmpty())
		{
			lastBallOnCamTimestamp = timestamp;
		}

		List<CamBall> camBalls = new ArrayList<>(frame.getBalls());

		var mappedBalls = virtualBalls.stream()
				.map(b -> b.toCamBall(camId, frame.getGlobalFrameId()))
				.toList();
		camBalls.addAll(mappedBalls);

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
	 * Get all the robot parameters
	 *
	 * @return
	 */
	public Map<BotID, RobotInfo> getRobotInfoMap()
	{
		return robotInfoMap;
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
				Lines.segmentFromPoints(pos.subtractNew(Vector2.fromX(100)), pos.addNew(Vector2.fromX(100))));
		xLine.setStrokeWidth(20);
		xLine.setColor(Color.CYAN);
		shapes.add(xLine);

		DrawableLine yLine = new DrawableLine(
				Lines.segmentFromPoints(pos.subtractNew(Vector2.fromY(100)), pos.addNew(Vector2.fromY(100))));
		yLine.setStrokeWidth(20);
		yLine.setColor(Color.CYAN);
		shapes.add(yLine);

		// Draw camera id
		DrawableAnnotation id = new DrawableAnnotation(pos, Integer.toString(camId));
		id.withOffset(Vector2.fromXY(-100, -80));
		id.setColor(Color.CYAN);
		id.withFontHeight(120);
		shapes.add(id);

		// Draw update rate of this camera (smoothed)
		DrawableAnnotation camRate = new DrawableAnnotation(
				pos,
				String.format("Rate: %.1fHz", 1.0 / getAverageFrameDt())
		);
		camRate.withOffset(Vector2.fromXY(40, -60));
		camRate.withFontHeight(50);
		camRate.setColor(Color.LIGHT_GRAY);
		shapes.add(camRate);

		// Annotate mounting height of this cam
		DrawableAnnotation height = new DrawableAnnotation(
				pos,
				String.format("Height: %.2fm", pos3D.z() * 0.001)
		);
		height.withOffset(Vector2.fromXY(40, 60));
		height.withFontHeight(50);
		height.setColor(Color.LIGHT_GRAY);
		shapes.add(height);

		// Annotate missed frames and rate
		DrawableAnnotation missedFrames = new DrawableAnnotation(
				pos,
				String.format("Missed: %d (%.2f%%)", getNumMissingFrames(), getFrameMissRate() * 100.0)
		);
		missedFrames.withOffset(Vector2.fromXY(40, 110));
		missedFrames.withFontHeight(50);
		missedFrames.setColor(Color.LIGHT_GRAY);
		shapes.add(missedFrames);

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

			DrawableAnnotation age = new DrawableAnnotation(
					pos,
					String.format(
							"%d: %.3fs", camId,
							(timestamp - tracker.getLastUpdateTimestamp()) * 1e-9
					)
			);
			age.withOffset(Vector2.fromXY(150, (camId * 45.0) - 100.0));
			age.setColor(Color.GREEN);
			shapes.add(age);
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
