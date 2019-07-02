/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.IBallModelIdentificationObserver;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.data.StraightBallTrajectory;
import edu.tigers.sumatra.vision.kick.detectors.EarlyKickDetector;
import edu.tigers.sumatra.vision.kick.detectors.KickDetector;
import edu.tigers.sumatra.vision.kick.estimators.ChipKickEstimator;
import edu.tigers.sumatra.vision.kick.estimators.EKickEstimatorType;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import edu.tigers.sumatra.vision.kick.estimators.IKickEstimator;
import edu.tigers.sumatra.vision.kick.estimators.KickFitResult;
import edu.tigers.sumatra.vision.kick.estimators.StraightKickEstimator;
import edu.tigers.sumatra.vision.tracker.BallTracker;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;


/**
 * The ball filter preprocessor offers additional data for the ball filter.
 * It merges raw ball trackers, detects kicks and runs kick estimators.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BallFilterPreprocessor
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(BallFilterPreprocessor.class.getName());
	
	private final BallTrackerMerger ballTrackerMerger = new BallTrackerMerger();
	private final KickDetectors kickDetectors = new KickDetectors();
	private final KickEstimators kickEstimators = new KickEstimators();
	
	private final List<IBallModelIdentificationObserver> observers = new CopyOnWriteArrayList<>();
	
	@Configurable(comment = "Minimum search radius for cam balls around last known position [mm]")
	private static double minSearchRadius = 300;
	
	@Configurable(defValue = "0.2", comment = "Factor by which a estimator must be better than the last one to use it")
	private static double estimatorSwitchHysteresis = 0.2;
	
	@Configurable(comment = "Enable model identification solver", defValue = "false")
	private boolean doModelIdentification = false;
	
	
	static
	{
		ConfigRegistration.registerClass("vision", BallFilterPreprocessor.class);
	}
	
	
	/**
	 * Update ball preprocessor with new information.
	 * 
	 * @param lastFilteredBall
	 * @param ballTrackers All ball trackers on the field.
	 * @param mergedRobots Already merged robots.
	 * @param robotInfos Robot info map
	 * @param timestamp Prediction/Frame timestamp.
	 * @return
	 */
	public BallFilterPreprocessorOutput update(final FilteredVisionBall lastFilteredBall,
			final List<BallTracker> ballTrackers,
			final List<FilteredVisionBot> mergedRobots,
			final Map<BotID, RobotInfo> robotInfos,
			final long timestamp)
	{
		MergedBall optMergedBall = ballTrackerMerger.process(ballTrackers, timestamp, lastFilteredBall);
		KickEvent optKickEvent = kickDetectors.process(optMergedBall, mergedRobots);
		FilteredVisionBall optKickFitState = kickEstimators.process(optKickEvent,
				optMergedBall, mergedRobots, robotInfos, timestamp);
		
		return new BallFilterPreprocessorOutput(optMergedBall, optKickEvent, optKickFitState);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBallModelIdentificationObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBallModelIdentificationObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * Clear all internal states.
	 */
	public void clear()
	{
		ballTrackerMerger.reset();
		kickDetectors.reset();
		kickEstimators.reset();
	}
	
	
	/**
	 * @param doModelIdentification the doModelIdentification to set
	 */
	public void setDoModelIdentification(final boolean doModelIdentification)
	{
		this.doModelIdentification = doModelIdentification;
	}
	
	
	public List<IDrawableShape> getShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		
		shapes.addAll(ballTrackerMerger.getShapes());
		shapes.addAll(kickDetectors.getShapes());
		shapes.addAll(kickEstimators.getShapes());
		
		return shapes;
	}
	
	
	private class KickEstimators
	{
		private final List<IKickEstimator> estimators = new ArrayList<>();
		private IKickEstimator lastBestEstimator;
		private long lastKickTimestamp = 0;
		private CircularFifoQueue<KickEvent> kickEventHistory = new CircularFifoQueue<>(10);
		
		
		private KickEstimators()
		{
			StraightKickEstimator.touch();
			ChipKickEstimator.touch();
		}
		
		
		private void notifyBallModelIdentificationResult(final IBallModelIdentResult ident)
		{
			observers.forEach(o -> o.onBallModelIdentificationResult(ident));
		}
		
		
		private FilteredVisionBall process(final KickEvent kickEvent, final MergedBall ball,
				final List<FilteredVisionBot> mergedRobots, final Map<BotID, RobotInfo> robotInfos,
				final long timestamp)
		{
			if ((ball != null) && ball.getLatestCamBall().isPresent())
			{
				// add cam ball to all estimators
				estimators.forEach(k -> k.addCamBall(ball.getLatestCamBall().get()));
			}
			
			// run completed check
			for (Iterator<IKickEstimator> iter = estimators.iterator(); iter.hasNext();)
			{
				IKickEstimator est = iter.next();
				if (est.isDone(mergedRobots, timestamp))
				{
					if (doModelIdentification)
					{
						Optional<IBallModelIdentResult> identResult = est.getModelIdentResult();
						identResult.ifPresent(this::notifyBallModelIdentificationResult);
					}
					iter.remove();
				}
			}
			estimators.removeIf(k -> k.isDone(mergedRobots, timestamp));
			
			// add event to history for visualization
			if (kickEvent != null)
			{
				kickEventHistory.add(kickEvent);
			}
			
			// handle new kick event and spawn/merge estimators
			updateEstimators(kickEvent, robotInfos, timestamp);
			
			// get best kick fit state
			return getBestKickFitState(timestamp);
		}
		
		
		@SuppressWarnings("squid:MethodCyclomaticComplexity")
		private void updateEstimators(final KickEvent kickEvent,
				final Map<BotID, RobotInfo> robotInfos, final long timestamp)
		{
			// check for kick event (needs direction vector)
			if ((kickEvent == null) || !kickEvent.getKickDirection().isPresent())
			{
				// get best kick fit state
				return;
			}
			
			// we have a kick event
			IKickEstimator chipEstimator = estimators.stream()
					.filter(e -> e.getType() == EKickEstimatorType.CHIP)
					.findFirst().orElse(null);
			
			IKickEstimator flatEstimator = estimators.stream()
					.filter(e -> e.getType() == EKickEstimatorType.FLAT)
					.findFirst().orElse(null);
			
			// ignore event when best fit state is chipped
			if ((chipEstimator != null) && (chipEstimator == lastBestEstimator)
					&& chipEstimator.getFitResult().isPresent()
					&& chipEstimator.getFitResult().get().getState(timestamp).isChipped())
			{
				log.debug("Ignoring kick event due to chipped ball state");
				
				// get best kick fit state
				return;
			}
			
			RobotInfo kickRobotInfo = robotInfos.get(kickEvent.getKickingBot());
			if ((kickRobotInfo != null) && kickRobotInfo.isArmed() && kickRobotInfo.isChip())
			{
				log.debug("Angle: " + kickRobotInfo.getBotParams().getKickerSpecs().getChipAngle());
				log.debug("Speed: " + (kickRobotInfo.getKickSpeed() * 1000.0));
				
				// always spawn a new chip estimator if there is a kicking robot nearby
				chipEstimator = new ChipKickEstimator(Geometry.getLastCamGeometry().getCalibrations(),
						kickEvent, kickRobotInfo.getKickSpeed() * 1000.0,
						kickRobotInfo.getBotParams().getKickerSpecs().getChipAngle());
				
				log.debug("Spawned chip estimator with prior knowledge from RobotInfo");
			}
			
			if ((chipEstimator == null) && !kickEvent.isEarlyDetection())
			{
				// spawn a new chip estimator if this is a slow kick detection event and no estimator exists yet
				chipEstimator = new ChipKickEstimator(Geometry.getLastCamGeometry().getCalibrations(), kickEvent);
				
				log.debug("Spawned chip estimator");
			}
			
			if (flatEstimator == null)
			{
				flatEstimator = new StraightKickEstimator(kickEvent);
				
				log.debug("Spawned flat estimator");
			}
			
			// try to merge kick event if we already have an estimator running
			if (lastBestEstimator != null)
			{
				IVector2 bestEstimatorVelDir = lastBestEstimator.getFitResult().get().getKickVel().getXYVector();
				IVector2 kickDir = kickEvent.getKickDirection().get();
				
				if ((bestEstimatorVelDir.angleToAbs(kickDir).orElse(Math.PI) > AngleMath.deg2rad(20)) ||
						(lastBestEstimator.getFitResult().get().getKickPos().distanceTo(kickEvent.getPosition()) > 500.0))
				{
					// large angle deviation or some distance away from last kick, spawn new estimator
					flatEstimator = new StraightKickEstimator(kickEvent);
					log.debug("Spawned flat estimator due to angle/pos deviation");
				} else
				{
					log.debug("Merged kick event into previous estimator");
				}
			}
			
			estimators.clear();
			estimators.add(flatEstimator);
			if (chipEstimator != null)
			{
				estimators.add(chipEstimator);
			}
			
			lastKickTimestamp = kickEvent.getTimestamp();
		}
		
		
		private FilteredVisionBall getBestKickFitState(final long timestamp)
		{
			Optional<IKickEstimator> bestEstimator = estimators.stream()
					.filter(k -> k.getFitResult().isPresent())
					.sorted((k1, k2) -> Double.compare(k1.getFitResult().get().getAvgDistance(),
							k2.getFitResult().get().getAvgDistance()))
					.findFirst();
			
			if (bestEstimator.isPresent())
			{
				IKickEstimator est = bestEstimator.get();
				boolean noLastBestEstimator = (lastBestEstimator == null) || !estimators.contains(lastBestEstimator);
				if (noLastBestEstimator || ((est != lastBestEstimator) && (est.getFitResult().get()
						.getAvgDistance() < (lastBestEstimator.getFitResult().get().getAvgDistance()
								* (1.0 - estimatorSwitchHysteresis))))
						|| (lastBestEstimator.getType() == est.getType()))
				{
					lastBestEstimator = est;
				}
			} else
			{
				lastBestEstimator = null;
			}
			
			if (lastBestEstimator != null)
			{
				KickFitResult bestKickFitResult = lastBestEstimator.getFitResult().get();
				if (((timestamp - lastKickTimestamp) * 1e-9) > 0.5)
				{
					// only keep the best estimator 0.5s after kick
					estimators
							.removeIf(k -> k.getFitResult()
									.orElse(new KickFitResult(null, 0,
											new StraightBallTrajectory(Vector2f.ZERO_VECTOR, Vector3f.ZERO_VECTOR, 0)))
									.getAvgDistance() > bestKickFitResult.getAvgDistance());
				}
				
				return bestKickFitResult.getState(timestamp);
			}
			
			return null;
		}
		
		
		private List<IDrawableShape> getShapes()
		{
			List<IDrawableShape> shapes = new ArrayList<>();
			
			estimators.forEach(k -> shapes.addAll(k.getShapes()));
			
			for (KickEvent kick : kickEventHistory)
			{
				Color col = kick.isEarlyDetection() ? Color.GRAY : Color.YELLOW;
				DrawableCircle pos = new DrawableCircle(kick.getPosition(), 10, col);
				pos.setStrokeWidth(2);
				pos.setFill(false);
				shapes.add(pos);
				
				List<IVector2> points = kick.getRecordsSinceKick().stream()
						.map(MergedBall::getCamPos)
						.collect(Collectors.toList());
				
				Optional<Line> line = Line.fromPointsList(points);
				if (line.isPresent())
				{
					DrawableLine dir = new DrawableLine(line.get(), col);
					dir.setStrokeWidth(2);
					shapes.add(dir);
				}
			}
			
			return shapes;
		}
		
		
		private void reset()
		{
			estimators.clear();
		}
	}
	
	private class KickDetectors
	{
		private final KickDetector slowDetector = new KickDetector();
		private final EarlyKickDetector earlyDetector = new EarlyKickDetector();
		
		
		private KickEvent process(final MergedBall mergedBall, final List<FilteredVisionBot> mergedRobots)
		{
			if (mergedBall == null)
			{
				return null;
			}
			
			// is this merged ball based on a real measurement?
			if (!mergedBall.getLatestCamBall().isPresent())
			{
				return null;
			}
			
			KickEvent earlyEvent = earlyDetector.addRecord(mergedBall, mergedRobots);
			KickEvent slowEvent = slowDetector.addRecord(mergedBall, mergedRobots);
			
			// Prefer a slow event if one exists
			if (slowEvent != null)
			{
				return slowEvent;
			}
			
			return earlyEvent;
		}
		
		
		private List<IDrawableShape> getShapes()
		{
			List<IDrawableShape> shapes = new ArrayList<>();
			
			shapes.addAll(slowDetector.getDrawableShapes());
			shapes.addAll(earlyDetector.getDrawableShapes());
			
			return shapes;
		}
		
		
		private void reset()
		{
			slowDetector.reset();
			earlyDetector.reset();
		}
	}
	
	private class BallTrackerMerger
	{
		private double lastBallSearchRadius = 1;
		private List<IVector2> lastSearchPositions = new ArrayList<>();
		private long lastBallUpdateTimestamp = 0;
		
		
		private MergedBall process(final List<BallTracker> ballTrackers, final long timestamp,
				final FilteredVisionBall lastFilteredBall)
		{
			if (ballTrackers.isEmpty())
			{
				// no valid ball trackers at all
				return null;
			}
			
			if (lastBallUpdateTimestamp == 0)
			{
				lastBallUpdateTimestamp = lastFilteredBall.getLastVisibleTimestamp();
			}
			
			lastBallSearchRadius = Math.abs((timestamp - lastBallUpdateTimestamp) * 1e-9 * BallTracker.getMaxLinearVel());
			lastBallSearchRadius = Math.max(lastBallSearchRadius, minSearchRadius);
			
			lastSearchPositions.clear();
			List<BallTracker> primaryTrackers;
			if (lastFilteredBall.isChipped() && !Geometry.getLastCamGeometry().getCalibrations().isEmpty())
			{
				// if the ball is airborne we project its position to the ground from all cameras and use these locations as
				// search point
				List<IVector2> projectedPos = Geometry.getLastCamGeometry().getCalibrations().values().stream()
						.map(c -> lastFilteredBall.getPos().projectToGroundNew(c.getCameraPosition()))
						.collect(Collectors.toList());
				
				lastSearchPositions.addAll(projectedPos);
				
				primaryTrackers = ballTrackers.stream()
						.filter(t -> projectedPos.stream()
								.anyMatch(p -> t.getPosition(timestamp).distanceTo(p) < lastBallSearchRadius))
						.collect(Collectors.toList());
			} else
			{
				// if the ball is not airborne we simply use the last known location as search point
				lastSearchPositions.add(lastFilteredBall.getPos().getXYVector());
				
				primaryTrackers = ballTrackers.stream()
						.filter(BallTracker::isGrownUp)
						.filter(t -> t.getPosition(timestamp)
								.distanceTo(lastFilteredBall.getPos().getXYVector()) < lastBallSearchRadius)
						.collect(Collectors.toList());
			}
			
			if (primaryTrackers.isEmpty())
			{
				// no valid trackers in search radius
				return null;
			}
			
			// -- select only one tracker per cam at max --
			// group ball trackers by camera id
			Map<Integer, List<BallTracker>> trackersByCam = primaryTrackers.stream()
					.collect(Collectors.groupingBy(BallTracker::getCameraId));
			
			// select only one tracker from each group (most recently updated one)
			List<BallTracker> distinctTrackers = trackersByCam.values().stream()
					.map(li -> li.stream()
							.max((b1, b2) -> Long.compare(b1.getLastCamBall().getTimestamp(),
									b2.getLastCamBall().getTimestamp()))
							.get())
					.collect(Collectors.toList());
			
			// Merge these few trackers
			MergedBall mergedBall = BallTracker.mergeBallTrackers(distinctTrackers, timestamp);
			
			if (mergedBall.getLatestCamBall().isPresent())
			{
				lastBallUpdateTimestamp = mergedBall.getLatestCamBall().get().gettCapture();
			}
			
			return mergedBall;
		}
		
		
		private List<IDrawableShape> getShapes()
		{
			List<IDrawableShape> shapes = new ArrayList<>();
			
			for (IVector2 pos : lastSearchPositions)
			{
				DrawableCircle search = new DrawableCircle(pos, lastBallSearchRadius + 0.1, Color.PINK);
				shapes.add(search);
			}
			
			return shapes;
		}
		
		
		private void reset()
		{
			lastBallUpdateTimestamp = 0;
		}
	}
	
	/**
	 * Output data structure of the ball filter preprocessor.
	 * 
	 * @author AndreR <andre@ryll.cc>
	 */
	public static class BallFilterPreprocessorOutput
	{
		private final MergedBall mergedBall;
		private final KickEvent kickEvent;
		private final FilteredVisionBall kickFitState;
		
		
		/**
		 * @param optMergedBall
		 * @param optKickEvent
		 * @param optKickFitState
		 */
		public BallFilterPreprocessorOutput(final MergedBall optMergedBall, final KickEvent optKickEvent,
				final FilteredVisionBall optKickFitState)
		{
			mergedBall = optMergedBall;
			kickEvent = optKickEvent;
			kickFitState = optKickFitState;
		}
		
		
		public Optional<MergedBall> getMergedBall()
		{
			return Optional.ofNullable(mergedBall);
		}
		
		
		public Optional<KickEvent> getKickEvent()
		{
			return Optional.ofNullable(kickEvent);
		}
		
		
		public Optional<FilteredVisionBall> getKickFitState()
		{
			return Optional.ofNullable(kickFitState);
		}
	}
}
