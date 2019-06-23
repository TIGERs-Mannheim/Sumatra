/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.vision.BallFilter.BallFilterOutput;
import edu.tigers.sumatra.vision.BallFilterPreprocessor.BallFilterPreprocessorOutput;
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
public class VisionFilterImpl extends AVisionFilter
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(VisionFilterImpl.class.getName());
	
	private final BallFilterPreprocessor ballFilterPreprocessor = new BallFilterPreprocessor();
	private final BallFilter ballFilter = new BallFilter();
	private final QualityInspector qualityInspector = new QualityInspector();
	private ViewportArchitect viewportArchitect;
	
	private Map<Integer, CamFilter> cams = new HashMap<>();
	private FilteredVisionFrame lastFilteredFrame;
	private KickEvent lastKickEvent;
	private BallFilterOutput lastBallFilterOutput;
	
	
	/**
	 * @param config moduli config
	 */
	public VisionFilterImpl(final SubnodeConfiguration config)
	{
		super(config);
		
		lastFilteredFrame = FilteredVisionFrame.Builder.createEmptyFrame();
		lastBallFilterOutput = new BallFilterOutput(lastFilteredFrame.getBall(), lastFilteredFrame.getBall().getPos(),
				EBallState.ROLLING, new BallFilterPreprocessorOutput(null, null, null));
	}
	
	
	@Override
	protected void updateCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		// use newest timestamp to prevent negative delta time in filtered frames
		long timestamp = Math.max(lastFilteredFrame.getTimestamp(), camDetectionFrame.gettCapture());
		
		int camId = camDetectionFrame.getCameraId();
		
		// add camera if it does not exist yet
		cams.putIfAbsent(camId, new CamFilter(camId));
		
		// update robot infos on all camera filters
		cams.get(camId).setRobotInfoFrame(getRobotInfoFrames());
		
		// set latest ball info on all camera filters (to generate virtual balls from barrier info)
		cams.get(camId).setBallInfo(lastBallFilterOutput);
		
		// update camera filter with new detection frame
		cams.get(camId).update(camDetectionFrame, lastFilteredFrame);
		
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
		
		// publish new frame
		publishFilteredVisionFrame(frame);
		
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
			IVector3 robotAcc = getRobotInfo(entry.getKey())
					.map(i -> i.getTrajectory().orElse(null)).map(t -> t.getAcceleration(0).multiplyNew(1e3))
					.orElse(Vector3.zero());
			
			mergedBots.add(RobotTracker.mergeRobotTrackers(entry.getKey(), entry.getValue(), timestamp, robotAcc));
		}
		
		return mergedBots;
	}
	
	
	private FilteredVisionBall selectAndMergeBall(final Collection<CamFilter> camFilters, final long timestamp,
			final List<FilteredVisionBot> mergedRobots)
	{
		List<BallTracker> allTrackers = camFilters.stream()
				.flatMap(f -> f.getBalls().stream())
				.filter(BallTracker::isGrownUp)
				.collect(Collectors.toList());
		
		BallFilterPreprocessorOutput preOutput = ballFilterPreprocessor.update(lastFilteredFrame.getBall(), allTrackers,
				mergedRobots, timestamp);
		
		lastKickEvent = preOutput.getKickEvent().orElse(null);
		
		lastBallFilterOutput = ballFilter.update(preOutput, lastFilteredFrame.getBall(), timestamp);
		
		return lastBallFilterOutput.getFilteredBall();
	}
	
	
	@Override
	public void onNewCameraGeometry(final CamGeometry geometry)
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
			c.updateViewport(viewportArchitect.getViewport(c.getCamId()));
		}
	}
	
	
	@Override
	protected void start()
	{
		super.start();
		try
		{
			ABotManager botmanager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			viewportArchitect = new ViewportArchitect(botmanager.getBaseStations());
		} catch (ModuleNotFoundException e)
		{
			viewportArchitect = new ViewportArchitect(Collections.emptyList());
			log.debug("No Botmanager found. No Basestation will be notified", e);
		}
	}
	
	
	@Override
	protected void stop()
	{
		super.stop();
		cams.clear();
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
				camId.setOffset(Vector2.fromY(-100));
				camId.setColor(Color.WHITE);
				shapes.add(camId);
				
				DrawableAnnotation unc = new DrawableAnnotation(pos,
						String.format("%.2f",
								tracker.getFilter().getPositionUncertainty().getLength() * tracker.getUncertainty()));
				unc.setOffset(Vector2.fromX(-80));
				unc.setColor(Color.WHITE);
				shapes.add(unc);
				
				DrawableAnnotation age = new DrawableAnnotation(pos,
						String.format("%d: %.3fs", camFilter.getCamId(),
								(timestamp - tracker.getLastUpdateTimestamp()) * 1e-9));
				age.setOffset(Vector2.fromXY(120, (camFilter.getCamId() * 45.0) - 100.0));
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
