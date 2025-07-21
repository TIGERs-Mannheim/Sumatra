/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.Safe;
import edu.tigers.sumatra.vision.BallFilter.BallFilterOutput;
import edu.tigers.sumatra.vision.BallFilterPreprocessor.BallFilterPreprocessorOutput;
import edu.tigers.sumatra.vision.ViewportArchitect.IViewportArchitect;
import edu.tigers.sumatra.vision.data.EVisionFilterShapesLayer;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.IBallModelIdentificationObserver;
import edu.tigers.sumatra.vision.data.Viewport;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import edu.tigers.sumatra.vision.tracker.BallTracker;
import edu.tigers.sumatra.vision.tracker.RobotTracker;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Vision filter implementation.
 */
@Log4j2
public class VisionFilterImpl extends AVisionFilter
		implements IViewportArchitect, IBallModelIdentificationObserver
{
	private static final int CAM_FRAME_BUFFER_SIZE = 10;

	@Configurable(defValue = "0.0125", comment = "Publish frequency (requires restart)")
	private static double publishDt = 0.0125;

	static
	{
		ConfigRegistration.registerClass("vision", VisionFilterImpl.class);
	}

	private final BallFilterPreprocessor ballFilterPreprocessor = new BallFilterPreprocessor();
	private final BallFilter ballFilter = new BallFilter();
	private final QualityInspector qualityInspector = new QualityInspector();
	private final ViewportArchitect viewportArchitect = new ViewportArchitect();
	private final RobotQualityInspector robotQualityInspector = new RobotQualityInspector();
	private final VirtualBallProducer virtualBallProducer = new VirtualBallProducer();

	private Map<Integer, CamFilter> cams = new ConcurrentHashMap<>();
	private FilteredVisionFrame lastFrame = FilteredVisionFrame.createEmptyFrame();
	private BallFilterOutput lastBallFilterOutput = new BallFilterOutput(
			lastFrame.getBall(),
			null,
			lastFrame.getBall().getPos(),
			new BallFilterPreprocessorOutput(null, null, null)
	);

	private ScheduledExecutorService scheduledExecutorService;
	private final BlockingDeque<CamDetectionFrame> camDetectionFrameQueue = new LinkedBlockingDeque<>(
			CAM_FRAME_BUFFER_SIZE);


	private void publish()
	{
		try
		{
			lastFrame = constructFilteredVisionFrame(lastFrame);
			var extrapolatedFrame = extrapolateFilteredFrame(lastFrame, lastFrame.getTimestamp());
			publishFilteredVisionFrame(extrapolatedFrame);
			virtualBallProducer.update(extrapolatedFrame, getRobotInfoMap(), cams.values());
		} catch (Throwable e)
		{
			log.error("Uncaught exception while publishing vision filter frames", e);
		}
	}


	private FilteredVisionFrame extrapolateFilteredFrame(final FilteredVisionFrame frame, final long timestampFuture)
	{
		final long timestampNow = frame.getTimestamp();

		if (timestampFuture < timestampNow)
		{
			return frame;
		}

		List<FilteredVisionBot> extrapolatedBots = frame.getBots().stream()
				.map(b -> b.extrapolate(timestampNow, timestampFuture))
				.toList();

		// construct extrapolated vision frame
		return FilteredVisionFrame.builder()
				.withId(frame.getId())
				.withTimestamp(timestampFuture)
				.withBall(frame.getBall().extrapolate(timestampNow, timestampFuture))
				.withBots(extrapolatedBots)
				.withKick(frame.getKick().orElse(null))
				.withShapeMap(frame.getShapeMap())
				.build();
	}


	@Override
	protected void updateCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		if (camDetectionFrame.getTimestamp() <= 0)
		{
			// skip negative timestamps. They can produce unexpected behavior
			return;
		}
		if (scheduledExecutorService == null)
		{
			processCamDetectionFrame(camDetectionFrame);
			publish();
		} else
		{
			if (camDetectionFrameQueue.size() >= CAM_FRAME_BUFFER_SIZE)
			{
				camDetectionFrameQueue.pollLast();
			}
			camDetectionFrameQueue.addFirst(camDetectionFrame);
		}
	}


	private void processCamFrameQueue()
	{
		while (scheduledExecutorService != null && !scheduledExecutorService.isShutdown())
		{
			try
			{
				var camFrame = camDetectionFrameQueue.pollLast(15, TimeUnit.MILLISECONDS);
				if (camFrame != null)
				{
					processCamDetectionFrame(camFrame);
				}
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			} catch (Throwable e)
			{
				log.error("Uncaught exception while processing cam frame", e);
			}
		}
	}


	private void processCamDetectionFrame(CamDetectionFrame camDetectionFrame)
	{
		int camId = camDetectionFrame.getCameraId();

		// let viewport architect adjust
		viewportArchitect.newDetectionFrame(camDetectionFrame);

		// add camera if it does not exist yet
		var camFilter = cams.computeIfAbsent(camId, CamFilter::new);

		// set viewport
		camFilter.updateViewport(viewportArchitect.getViewport(camId));

		// update robot infos on all camera filters
		camFilter.setRobotInfoMap(getRobotInfoMap());

		// update camera filter with new detection frame
		camFilter.update(camDetectionFrame, lastFrame, virtualBallProducer.getVirtualBalls());

		// update robot quality inspector
		camDetectionFrame.getRobots().forEach(robotQualityInspector::addDetection);
	}


	private FilteredVisionFrame constructFilteredVisionFrame(FilteredVisionFrame lastFrame)
	{
		// remove old camera filters (taking care of overflow in average())
		long avgTimestamp = (long) (cams.values().stream()
				.mapToDouble(c -> c.getTimestamp() / 1e9)
				.average()
				.orElse(0) * 1e9);
		cams.values().removeIf(f -> Math.abs(avgTimestamp - f.getTimestamp()) / 1e9 > 0.5);

		long timestamp = cams.values().stream().mapToLong(CamFilter::getTimestamp).max().orElse(lastFrame.getTimestamp());

		// use newest timestamp to prevent negative delta time in filtered frames
		timestamp = Math.max(lastFrame.getTimestamp(), timestamp);

		// merge all camera filters (robots on multiple cams)
		List<FilteredVisionBot> mergedRobots = mergeRobots(cams.values(), timestamp);

		// update robot quality inspector
		robotQualityInspector.prune(timestamp);
		final double averageDt = cams.values().stream().mapToDouble(CamFilter::getAverageFrameDt).max().orElse(0.01);
		robotQualityInspector.updateAverageDt(averageDt);

		// filter merged robots by quality
		List<FilteredVisionBot> filteredRobots = mergedRobots.stream()
				.filter(b -> robotQualityInspector.passesQualityInspection(b.getBotID()))
				.toList();

		// check robot quality
		qualityInspector.inspectRobots(cams.values(), timestamp);

		// merge all balls and select primary one
		FilteredVisionBall ball = selectAndMergeBall(cams.values(), timestamp, filteredRobots, lastFrame.getBall());

		// construct filtered vision frame
		FilteredVisionFrame frame = FilteredVisionFrame.builder()
				.withId(lastFrame.getId() + 1)
				.withTimestamp(timestamp)
				.withBall(ball)
				.withBots(filteredRobots)
				.withKick(lastBallFilterOutput.getFilteredKick())
				.withShapeMap(new ShapeMap())
				.build();

		// forward frame for inspection
		qualityInspector.inspectFilteredVisionFrame(frame);

		// Inspect the current state of the cameras
		qualityInspector.inspectCameras(cams.values());

		// Update active cameras in viewport architect
		viewportArchitect.updateCameras(cams.keySet());

		// add debug and info shapes for visualizer
		frame.getShapeMap().get(EVisionFilterShapesLayer.VIEWPORT_SHAPES).addAll(viewportArchitect.getInfoShapes());
		frame.getShapeMap().get(EVisionFilterShapesLayer.QUALITY_SHAPES).addAll(qualityInspector.getInfoShapes());
		frame.getShapeMap().get(EVisionFilterShapesLayer.CAM_INFO_SHAPES).addAll(getCamInfoShapes());
		frame.getShapeMap().get(EVisionFilterShapesLayer.BALL_TRACKER_SHAPES_IMPORTANT)
				.addAll(ballFilterPreprocessor.getShapes());
		frame.getShapeMap().get(EVisionFilterShapesLayer.ROBOT_TRACKER_SHAPES)
				.addAll(getRobotTrackerShapes(timestamp));
		frame.getShapeMap().get(EVisionFilterShapesLayer.BALL_TRACKER_SHAPES)
				.addAll(getBallTrackerShapes(timestamp));
		frame.getShapeMap().get(EVisionFilterShapesLayer.ROBOT_QUALITY_INSPECTOR)
				.addAll(getRobotQualityInspectorShapes(mergedRobots));
		frame.getShapeMap().get(EVisionFilterShapesLayer.VIRTUAL_BALL_SHAPES)
				.addAll(getVirtualBallShapes());

		return frame;
	}


	private List<FilteredVisionBot> mergeRobots(final Collection<CamFilter> camFilters, final long timestamp)
	{
		Map<BotID, List<RobotTracker>> trackersById = camFilters.stream()
				.flatMap(f -> f.getValidRobots().values().stream())
				// group trackers by BotID
				.collect(Collectors.groupingBy(RobotTracker::getBotId));

		List<FilteredVisionBot> mergedBots = new ArrayList<>();

		// merge all trackers in each group and get filtered vision bot from it
		for (Entry<BotID, List<RobotTracker>> entry : trackersById.entrySet())
		{
			mergedBots.add(RobotTracker.mergeRobotTrackers(entry.getKey(), entry.getValue(), timestamp));
		}

		return mergedBots;
	}


	private FilteredVisionBall selectAndMergeBall(
			Collection<CamFilter> camFilters,
			long timestamp,
			List<FilteredVisionBot> mergedRobots,
			FilteredVisionBall lastBall
	)
	{
		List<BallTracker> allTrackers = camFilters.stream()
				.flatMap(f -> f.getBalls().stream())
				.toList();

		BallFilterPreprocessorOutput preOutput = ballFilterPreprocessor.update(
				lastBall, allTrackers,
				mergedRobots, getRobotInfoMap(), timestamp
		);

		lastBallFilterOutput = ballFilter.update(preOutput, lastBall, timestamp);

		return lastBallFilterOutput.getFilteredBall();
	}


	@Override
	public void onNewCameraGeometry(final CamGeometry geometry)
	{
		processGeometryFrame(geometry);
	}


	private void processGeometryFrame(final CamGeometry geometry)
	{
		for (CamCalibration c : geometry.getCameraCalibrations().values())
		{
			int camId = c.getCameraId();
			CamFilter camFilter = cams.get(camId);
			if (camFilter != null)
			{
				camFilter.update(c);
			}
		}

		// forward to quality inspector for sanity checks
		qualityInspector.inspectCameraGeometry(geometry);

		// and to camera architect to lay out viewports
		viewportArchitect.newCameraGeometry(geometry);

		for (CamFilter c : cams.values())
		{
			c.update(geometry.getFieldSize());
		}
	}


	@Override
	public void startModule()
	{
		super.startModule();
		SumatraModel.getInstance().getModule(ACam.class).addObserver(this);

		viewportArchitect.addObserver(this);
		ballFilterPreprocessor.addObserver(this);

		boolean useThreads = getSubnodeConfiguration().getBoolean("useThreads", true);

		if (useThreads)
		{
			scheduledExecutorService = Executors
					.newSingleThreadScheduledExecutor(new NamedThreadFactory("VisionFilter Publisher"));
			new Thread(this::processCamFrameQueue, "VisionFilter Processor").start();
			scheduledExecutorService
					.scheduleAtFixedRate(() -> Safe.run(this::publish), 0, (long) (publishDt * 1e9), TimeUnit.NANOSECONDS);
			log.debug("Using threaded VisionFilter");
		}
	}


	@Override
	public void stopModule()
	{
		super.stopModule();
		SumatraModel.getInstance().getModule(ACam.class).removeObserver(this);
		if (scheduledExecutorService != null)
		{
			scheduledExecutorService.shutdown();
			scheduledExecutorService = null;
			camDetectionFrameQueue.clear();
		}
		cams.clear();
		viewportArchitect.removeObserver(this);
		ballFilterPreprocessor.removeObserver(this);
		ballFilterPreprocessor.clear();
		robotQualityInspector.reset();
		lastFrame = FilteredVisionFrame.createEmptyFrame();
	}


	@Override
	public void resetBall(final IVector3 pos, final IVector3 vel)
	{
		ballFilter.resetBall(pos.getXYVector());
		ballFilterPreprocessor.clear();
	}


	@Override
	public void onClearCamFrame()
	{
		super.onClearCamFrame();
		cams.clear();
		ballFilterPreprocessor.clear();
		robotQualityInspector.reset();
		lastFrame = FilteredVisionFrame.createEmptyFrame();
	}


	@Override
	public void onViewportUpdated(final int cameraId, final IRectangle viewport)
	{
		publishUpdatedViewport(new Viewport(cameraId, viewport));
	}


	@Override
	public void onBallModelIdentificationResult(final IBallModelIdentResult ident)
	{
		publishBallModelIdentification(ident);
	}


	private List<IDrawableShape> getCamInfoShapes()
	{
		return cams.values().stream()
				.flatMap(c -> c.getInfoShapes().stream())
				.toList();
	}


	private List<IDrawableShape> getRobotTrackerShapes(final long timestamp)
	{
		return cams.values().stream()
				.flatMap(c -> c.getRobotTrackerShapes(timestamp).stream())
				.toList();
	}


	private List<IDrawableShape> getBallTrackerShapes(final long timestamp)
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		for (CamFilter camFilter : cams.values())
		{
			for (BallTracker tracker : camFilter.getBalls())
			{
				IVector2 pos = tracker.getPosition(timestamp);

				DrawableCircle ballPos = new DrawableCircle(pos, 60, Color.WHITE);
				shapes.add(ballPos);

				DrawableAnnotation camId = new DrawableAnnotation(pos, Integer.toString(camFilter.getCamId()), true);
				camId.withOffset(Vector2.fromY(-100));
				camId.setColor(Color.WHITE);
				shapes.add(camId);

				DrawableAnnotation unc = new DrawableAnnotation(
						pos,
						String.format(
								"%.2f",
								tracker.getFilter().getPositionUncertainty().getLength() * tracker.getUncertainty()
						)
				);
				unc.withOffset(Vector2.fromX(-80));
				unc.setColor(Color.WHITE);
				shapes.add(unc);

				DrawableAnnotation age = new DrawableAnnotation(
						pos,
						String.format(
								"%d: %.3fs", camFilter.getCamId(),
								(timestamp - tracker.getLastUpdateTimestamp()) * 1e-9
						)
				);
				age.withOffset(Vector2.fromXY(120, (camFilter.getCamId() * 45.0) - 100.0));
				age.setColor(Color.GREEN);
				shapes.add(age);
			}

			shapes.addAll(camFilter.getBallInfoShapes());
		}

		shapes.addAll(ballFilter.getShapes());

		return shapes;
	}


	private List<IDrawableShape> getVirtualBallShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		boolean virtualBallUsed = !virtualBallProducer.getVirtualBalls().isEmpty();
		if (virtualBallUsed)
		{
			DrawableAnnotation virtHint = new DrawableAnnotation(
					lastBallFilterOutput.getFilteredBall().getPos().getXYVector(), "VIRTUAL", true);
			virtHint.setColor(Color.ORANGE);
			virtHint.withOffset(Vector2.fromY(40));
			virtHint.withFontHeight(30);
			shapes.add(virtHint);
		}

		shapes.addAll(virtualBallProducer.getVirtualBallShapes());

		return shapes;
	}


	private Collection<IDrawableShape> getRobotQualityInspectorShapes(final List<FilteredVisionBot> bots)
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		for (FilteredVisionBot bot : bots)
		{
			BotID botID = bot.getBotID();
			long maxNumDetections = (long) robotQualityInspector.getPossibleDetections();
			long numDetections = robotQualityInspector.getNumDetections(botID);
			long percentage = Math.round(100.0 * numDetections / maxNumDetections);
			String text = numDetections + "/" + maxNumDetections + "=" + percentage + "%";
			shapes.add(new DrawableAnnotation(bot.getPos(), text)
					.withOffset(Vector2f.fromY(200))
					.withCenterHorizontally(true));
		}
		return shapes;
	}
}
