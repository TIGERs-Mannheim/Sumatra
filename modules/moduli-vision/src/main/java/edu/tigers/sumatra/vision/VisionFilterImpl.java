/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.vision.BallFilter.BallFilterOutput;
import edu.tigers.sumatra.vision.BallFilterPreprocessor.BallFilterPreprocessorOutput;
import edu.tigers.sumatra.vision.ViewportArchitect.IViewportArchitect;
import edu.tigers.sumatra.vision.data.EBallState;
import edu.tigers.sumatra.vision.data.EVisionFilterShapesLayer;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.tracker.BallTracker;
import edu.tigers.sumatra.vision.tracker.RobotTracker;


/**
 * @author AndreR
 */
public class VisionFilterImpl extends AVisionFilter implements Runnable, IViewportArchitect
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(VisionFilterImpl.class.getName());
	
	private static final long CLOCK_DT = 10_000_000;
	
	private final BallFilterPreprocessor ballFilterPreprocessor = new BallFilterPreprocessor();
	private final BallFilter ballFilter = new BallFilter();
	private final QualityInspector qualityInspector = new QualityInspector();
	private final ViewportArchitect viewportArchitect = new ViewportArchitect();
	
	private Map<Integer, CamFilter> cams = new ConcurrentHashMap<>();
	private FilteredVisionFrame lastFilteredFrame;
	private KickEvent lastKickEvent;
	private BallFilterOutput lastBallFilterOutput;
	
	private ExecutorService filterExecutor = null;
	private Thread publisherThread = null;
	private long lastDetectionFrameTimestamp = 0;
	
	
	/**
	 * Create new instance
	 */
	public VisionFilterImpl()
	{
		lastFilteredFrame = FilteredVisionFrame.Builder.createEmptyFrame();
		lastBallFilterOutput = new BallFilterOutput(lastFilteredFrame.getBall(), lastFilteredFrame.getBall().getPos(),
				EBallState.ROLLING, new BallFilterPreprocessorOutput(null, null, null));
	}
	
	
	@Override
	public void run()
	{
		long nextRuntime = System.nanoTime() + CLOCK_DT;
		
		while (!Thread.interrupted())
		{
			try
			{
				if ((System.nanoTime() - lastDetectionFrameTimestamp) < 1_000_000_000L)
				{
					publishFilteredVisionFrame(extrapolateFilteredFrame(lastFilteredFrame, nextRuntime));
				}
			} catch (Throwable e)
			{
				log.error("Exception in VisionFilter.", e);
			}
			
			long sleep = nextRuntime - System.nanoTime();
			if (sleep > 0)
			{
				assert sleep < (long) 1e9;
				ThreadUtil.parkNanosSafe(sleep);
			}
			
			nextRuntime += CLOCK_DT;
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
				.collect(Collectors.toList());
		
		// construct extrapolated vision frame
		return FilteredVisionFrame.Builder.create()
				.withId(frame.getId())
				.withTimestamp(timestampFuture)
				.withBall(frame.getBall().extrapolate(timestampNow, timestampFuture))
				.withBots(extrapolatedBots)
				.withKickEvent(frame.getKickEvent().orElse(null))
				.withKickFitState(frame.getBall())
				.withShapeMap(frame.getShapeMap())
				.build();
	}
	
	
	@Override
	protected void updateCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		lastDetectionFrameTimestamp = System.nanoTime();
		
		if (filterExecutor == null)
		{
			processDetectionFrame(camDetectionFrame);
			publishFilteredVisionFrame(lastFilteredFrame);
		} else
		{
			filterExecutor.submit(() -> processDetectionFrame(camDetectionFrame));
		}
	}
	
	
	private void processDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		int camId = camDetectionFrame.getCameraId();
		
		// let viewport architect adjust
		viewportArchitect.newDetectionFrame(camDetectionFrame);
		
		// add camera if it does not exist yet
		cams.computeIfAbsent(camId, CamFilter::new);
		
		// set viewport
		cams.get(camId).updateViewport(viewportArchitect.getViewport(camId));
		
		// update robot infos on all camera filters
		cams.get(camId).setRobotInfoMap(getRobotInfoMap());
		
		// set latest ball info on all camera filters (to generate virtual balls from barrier info)
		cams.get(camId).setBallInfo(lastBallFilterOutput);
		
		// update camera filter with new detection frame
		long timestamp = cams.get(camId).update(camDetectionFrame, lastFilteredFrame);
		
		// use newest timestamp to prevent negative delta time in filtered frames
		timestamp = Math.max(lastFilteredFrame.getTimestamp(), timestamp);
		
		// merge all camera filters (robots on multiple cams)
		List<FilteredVisionBot> mergedRobots = mergeRobots(cams.values(), timestamp);
		
		// check robot quality
		qualityInspector.inspectRobots(cams.values(), timestamp);
		
		// merge all balls and select primary one
		FilteredVisionBall ball = selectAndMergeBall(cams.values(), timestamp, mergedRobots);
		
		// construct filtered vision frame
		FilteredVisionFrame frame = FilteredVisionFrame.Builder.create()
				.withId(lastFilteredFrame.getId() + 1)
				.withTimestamp(timestamp)
				.withBall(ball)
				.withBots(mergedRobots)
				.withKickEvent(lastKickEvent)
				.withKickFitState(lastBallFilterOutput.getPreprocessorOutput().getKickFitState().orElse(null))
				.build();
		
		// forward frame for inspection
		qualityInspector.inspectFilteredVisionFrame(frame);
		
		// add debug and info shapes for visualizer
		frame.getShapeMap().get(EVisionFilterShapesLayer.VIEWPORT_SHAPES).addAll(viewportArchitect.getInfoShapes());
		frame.getShapeMap().get(EVisionFilterShapesLayer.QUALITY_SHAPES).addAll(qualityInspector.getInfoShapes());
		frame.getShapeMap().get(EVisionFilterShapesLayer.CAM_INFO_SHAPES).addAll(getCamInfoShapes());
		frame.getShapeMap().get(EVisionFilterShapesLayer.ROBOT_TRACKER_SHAPES)
				.addAll(getRobotTrackerShapes(timestamp));
		frame.getShapeMap().get(EVisionFilterShapesLayer.BALL_TRACKER_SHAPES)
				.addAll(getBallTrackerShapes(timestamp));
		
		// store this frame
		lastFilteredFrame = frame;
	}
	
	
	private List<FilteredVisionBot> mergeRobots(final Collection<CamFilter> camFilters, final long timestamp)
	{
		// just get all RobotTracker in one list
		List<RobotTracker> allTrackers = camFilters.stream()
				.flatMap(f -> f.getValidRobots().values().stream())
				.collect(Collectors.toList());
		
		// group trackers by BotID
		Map<BotID, List<RobotTracker>> trackersById = allTrackers.stream()
				.collect(Collectors.groupingBy(RobotTracker::getBotId));
		
		List<FilteredVisionBot> mergedBots = new ArrayList<>();
		
		// merge all trackers in each group and get filtered vision bot from it
		for (Entry<BotID, List<RobotTracker>> entry : trackersById.entrySet())
		{
			mergedBots.add(RobotTracker.mergeRobotTrackers(entry.getKey(), entry.getValue(), timestamp));
		}
		
		return mergedBots;
	}
	
	
	private FilteredVisionBall selectAndMergeBall(final Collection<CamFilter> camFilters, final long timestamp,
			final List<FilteredVisionBot> mergedRobots)
	{
		List<BallTracker> allTrackers = camFilters.stream()
				.flatMap(f -> f.getBalls().stream())
				.collect(Collectors.toList());
		
		BallFilterPreprocessorOutput preOutput = ballFilterPreprocessor.update(lastFilteredFrame.getBall(), allTrackers,
				mergedRobots, getRobotInfoMap(), timestamp);
		
		lastKickEvent = preOutput.getKickEvent().orElse(null);
		
		lastBallFilterOutput = ballFilter.update(preOutput, lastFilteredFrame.getBall(), timestamp);
		
		return lastBallFilterOutput.getFilteredBall();
	}
	
	
	@Override
	public void onNewCameraGeometry(final CamGeometry geometry)
	{
		if (filterExecutor == null)
		{
			processGeometryFrame(geometry);
		} else
		{
			filterExecutor.submit(() -> processGeometryFrame(geometry));
		}
	}
	
	
	private void processGeometryFrame(final CamGeometry geometry)
	{
		for (CamCalibration c : geometry.getCalibrations().values())
		{
			int camId = c.getCameraId();
			if (cams.containsKey(camId))
			{
				cams.get(camId).update(c);
			}
		}
		
		// forward to quality inspector for sanity checks
		qualityInspector.inspectCameraGeometry(geometry);
		
		// and to camera architect to lay out viewports
		viewportArchitect.newCameraGeometry(geometry);
		
		for (CamFilter c : cams.values())
		{
			c.update(geometry.getField());
		}
	}
	
	
	@Override
	protected void start()
	{
		super.start();
		
		viewportArchitect.addObserver(this);
		
		boolean useThreads = getSubnodeConfiguration().getBoolean("useThreads", true);
		
		if (useThreads)
		{
			filterExecutor = Executors
					.newSingleThreadScheduledExecutor(new NamedThreadFactory("VisionFilter Proccessor"));
			publisherThread = new Thread(this, "VisionFilter Publisher");
			publisherThread.start();
			log.info("Using threaded VisionFilter");
		}
	}
	
	
	@Override
	protected void stop()
	{
		super.stop();
		if (filterExecutor != null)
		{
			filterExecutor.shutdown();
			filterExecutor = null;
		}
		if (publisherThread != null)
		{
			publisherThread.interrupt();
			publisherThread = null;
		}
		cams.clear();
		viewportArchitect.removeObserver(this);
		ballFilterPreprocessor.clear();
		lastFilteredFrame = FilteredVisionFrame.Builder.createEmptyFrame();
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
		lastFilteredFrame = FilteredVisionFrame.Builder.createEmptyFrame();
	}
	
	
	@Override
	public void onViewportUpdated(final int cameraId, final IRectangle viewport)
	{
		publishUpdatedViewport(cameraId, viewport);
	}
	
	
	private List<IDrawableShape> getCamInfoShapes()
	{
		return cams.values().stream()
				.flatMap(c -> c.getInfoShapes().stream())
				.collect(Collectors.toList());
	}
	
	
	private List<IDrawableShape> getRobotTrackerShapes(final long timestamp)
	{
		return cams.values().stream()
				.flatMap(c -> c.getRobotTrackerShapes(timestamp).stream())
				.collect(Collectors.toList());
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
				
				DrawableAnnotation unc = new DrawableAnnotation(pos,
						String.format("%.2f",
								tracker.getFilter().getPositionUncertainty().getLength() * tracker.getUncertainty()));
				unc.withOffset(Vector2.fromX(-80));
				unc.setColor(Color.WHITE);
				shapes.add(unc);
				
				DrawableAnnotation age = new DrawableAnnotation(pos,
						String.format("%d: %.3fs", camFilter.getCamId(),
								(timestamp - tracker.getLastUpdateTimestamp()) * 1e-9));
				age.withOffset(Vector2.fromXY(120, (camFilter.getCamId() * 45.0) - 100.0));
				age.setColor(Color.GREEN);
				shapes.add(age);
			}
			
			shapes.addAll(camFilter.getBallInfoShapes());
		}
		
		shapes.addAll(ballFilterPreprocessor.getShapes());
		shapes.addAll(ballFilter.getShapes());
		
		return shapes;
	}
}
