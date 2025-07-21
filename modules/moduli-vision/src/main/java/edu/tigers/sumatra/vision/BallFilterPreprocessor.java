/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.IBallModelIdentificationObserver;
import edu.tigers.sumatra.vision.data.KickEvent;
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
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * The ball filter preprocessor offers additional data for the ball filter.
 * It merges raw ball trackers, detects kicks and runs kick estimators.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class BallFilterPreprocessor
{
	private static final Logger log = LogManager.getLogger(BallFilterPreprocessor.class.getName());
	@Configurable(defValue = "300.0", comment = "Minimum search radius for cam balls around last known position [mm]")
	private static double minSearchRadius = 300.0;
	@Configurable(defValue = "0.2", comment = "Factor by which a estimator must be better than the last one to use it")
	private static double estimatorSwitchHysteresis = 0.2;

	static
	{
		ConfigRegistration.registerClass("vision", BallFilterPreprocessor.class);
	}

	private final BallTrackerMerger ballTrackerMerger = new BallTrackerMerger();
	private final KickDetectors kickDetectors = new KickDetectors();
	private final KickEstimators kickEstimators = new KickEstimators();
	private final List<IBallModelIdentificationObserver> observers = new CopyOnWriteArrayList<>();


	/**
	 * Update ball preprocessor with new information.
	 *
	 * @param lastFilteredBall
	 * @param ballTrackers     All ball trackers on the field.
	 * @param mergedRobots     Already merged robots.
	 * @param robotInfos       Robot info map
	 * @param timestamp        Prediction/Frame timestamp.
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
		KickFitResult optBestKickFitResult = kickEstimators.process(optKickEvent,
				optMergedBall, mergedRobots, robotInfos, timestamp, lastFilteredBall);

		return new BallFilterPreprocessorOutput(optMergedBall, optKickEvent, optBestKickFitResult);
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


	public List<IDrawableShape> getShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		shapes.addAll(ballTrackerMerger.getShapes());
		shapes.addAll(kickDetectors.getShapes());
		shapes.addAll(kickEstimators.getShapes());

		return shapes;
	}


	private static class KickDetectors
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
			if (mergedBall.getLatestCamBall().isEmpty())
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

	private static class BallTrackerMerger
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
			if (lastFilteredBall.getBallState().isChipped() && !Geometry.getLastCamGeometry().getCameraCalibrations()
					.isEmpty())
			{
				// if the ball is airborne we project its position to the ground from all cameras and use these locations as
				// search point
				List<IVector2> projectedPos = Geometry.getLastCamGeometry().getCameraCalibrations().values().stream()
						.map(c -> lastFilteredBall.getPos().projectToGroundNew(c.getCameraPosition()))
						.map(IVector2.class::cast)
						.toList();

				lastSearchPositions.addAll(projectedPos);

				primaryTrackers = ballTrackers.stream()
						.filter(t -> projectedPos.stream()
								.anyMatch(p -> t.getPosition(timestamp).distanceTo(p) < lastBallSearchRadius))
						.toList();
			} else
			{
				// if the ball is not airborne we simply use the last known location as search point
				lastSearchPositions.add(lastFilteredBall.getPos().getXYVector());

				primaryTrackers = ballTrackers.stream()
						.filter(BallTracker::isGrownUp)
						.filter(t -> t.getPosition(timestamp)
								.distanceTo(lastFilteredBall.getPos().getXYVector()) < lastBallSearchRadius)
						.toList();
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
					.map(li -> li.stream().max(Comparator.comparingLong(b -> b.getLastCamBall().getTimestamp())))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.toList();

			// Merge these few trackers
			MergedBall mergedBall = BallTracker.mergeBallTrackers(distinctTrackers, timestamp);

			mergedBall.getLatestCamBall().ifPresent(latestBall -> lastBallUpdateTimestamp = latestBall.getTimestamp());

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
	@RequiredArgsConstructor
	public static class BallFilterPreprocessorOutput
	{
		private final MergedBall mergedBall;
		private final KickEvent kickEvent;
		private final KickFitResult bestKickFitResult;


		public Optional<MergedBall> getMergedBall()
		{
			return Optional.ofNullable(mergedBall);
		}


		public Optional<KickEvent> getKickEvent()
		{
			return Optional.ofNullable(kickEvent);
		}


		public Optional<KickFitResult> getBestKickFitResult()
		{
			return Optional.ofNullable(bestKickFitResult);
		}
	}

	private class KickEstimators
	{
		private final List<IKickEstimator> estimators = new ArrayList<>();
		private IKickEstimator lastBestEstimator;
		private long lastKickTimestamp = 0;
		private CircularFifoQueue<KickEvent> kickEventHistory = new CircularFifoQueue<>(10);
		private CircularFifoQueue<FilteredVisionBall> filteredBallHistory = new CircularFifoQueue<>(20);


		private KickEstimators()
		{
			StraightKickEstimator.touch();
			ChipKickEstimator.touch();
		}


		private void notifyBallModelIdentificationResult(final IBallModelIdentResult ident)
		{
			observers.forEach(o -> o.onBallModelIdentificationResult(ident));
		}


		private KickFitResult process(final KickEvent kickEvent, final MergedBall ball,
				final List<FilteredVisionBot> mergedRobots, final Map<BotID, RobotInfo> robotInfos,
				final long timestamp, final FilteredVisionBall lastFilteredBall)
		{
			filteredBallHistory.add(lastFilteredBall);

			if ((ball != null) && ball.getLatestCamBall().isPresent())
			{
				// add cam ball to all estimators
				estimators.forEach(k -> k.addCamBall(ball.getLatestCamBall().get()));
			}

			estimators.stream()
					.filter(e -> e.isDone(mergedRobots, timestamp))
					.forEach(estimator -> CompletableFuture.runAsync(() ->
							estimator.getModelIdentResult()
									.forEach(this::notifyBallModelIdentificationResult)
					));
			if (lastBestEstimator != null && lastBestEstimator.isDone(mergedRobots, timestamp))
			{
				// remove all estimators, if the currently active one finished
				// Example: If the straight kick estimator stops because the ball hits a robot
				// the chip estimator should also be stopped, as it else might take over for a short time.
				estimators.clear();
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
			return getBestKickFitResult(timestamp);
		}


		@SuppressWarnings("squid:MethodCyclomaticComplexity")
		private void updateEstimators(final KickEvent kickEvent,
				final Map<BotID, RobotInfo> robotInfos, final long timestamp)
		{
			// check for kick event (needs direction vector)
			if ((kickEvent == null) || kickEvent.getKickDirection().isEmpty())
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
					&& chipEstimator.getFitResult()
					.map(fitResult -> fitResult.getState(timestamp))
					.map(BallState::isChipped)
					.orElse(false))
			{
				log.debug("Ignoring kick event due to airborne ball state");

				// get best kick fit state
				return;
			}

			RobotInfo kickRobotInfo = robotInfos.get(kickEvent.getKickingBot());
			if ((kickRobotInfo != null) && kickRobotInfo.isArmed() && kickRobotInfo.isChip())
			{
				log.debug("Angle: {}", kickRobotInfo.getBotParams().getKickerSpecs().getChipAngle());
				log.debug("Speed: {}", kickRobotInfo.getKickSpeed() * 1000.0);

				// always spawn a new chip estimator if there is a kicking robot nearby
				chipEstimator = new ChipKickEstimator(Geometry.getLastCamGeometry().getCameraCalibrations(),
						kickEvent, kickRobotInfo.getKickSpeed() * 1000.0,
						kickRobotInfo.getBotParams().getKickerSpecs().getChipAngle());

				log.debug("Spawned chip estimator with prior knowledge from RobotInfo");
			}

			if ((chipEstimator == null) && !kickEvent.isEarlyDetection())
			{
				// spawn a new chip estimator if this is a slow kick detection event and no estimator exists yet
				chipEstimator = new ChipKickEstimator(Geometry.getLastCamGeometry().getCameraCalibrations(), kickEvent);

				log.debug("Spawned chip estimator");
			}

			if (flatEstimator == null)
			{
				flatEstimator = new StraightKickEstimator(kickEvent,
						filteredBallHistory.stream().toList());

				log.debug("Spawned flat estimator");
			}

			// try to merge kick event if we already have an estimator running
			if (lastBestEstimator != null)
			{
				IVector2 bestEstimatorVelDir = lastBestEstimator.getFitResult().orElseThrow().getKickVel().getXYVector();
				IVector2 kickDir = kickEvent.getKickDirection().orElseThrow();

				if ((bestEstimatorVelDir.angleToAbs(kickDir).orElse(Math.PI) > AngleMath.deg2rad(20)) ||
						(lastBestEstimator.getFitResult().orElseThrow().getKickPos().distanceTo(kickEvent.getPosition())
								> 500.0))
				{
					// large angle deviation or some distance away from last kick, spawn new estimator
					flatEstimator = new StraightKickEstimator(kickEvent,
							filteredBallHistory.stream().toList());
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


		private KickFitResult getBestKickFitResult(final long timestamp)
		{
			Optional<IKickEstimator> bestEstimator = estimators.stream()
					.filter(k -> k.getFitResult().isPresent())
					.filter(k -> k.getFitResult().get().getAvgDistance() > 0.0)
					.min(Comparator.comparingDouble(k -> k.getFitResult().get().getAvgDistance()));

			if (bestEstimator.isPresent())
			{
				IKickEstimator est = bestEstimator.get();
				boolean noLastBestEstimator = (lastBestEstimator == null) || !estimators.contains(lastBestEstimator);
				if (noLastBestEstimator || lastBestEstimator.getFitResult().isEmpty() || ((est != lastBestEstimator) && (
						est.getFitResult().orElseThrow()
						.getAvgDistance() < (lastBestEstimator.getFitResult().orElseThrow().getAvgDistance()
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
				KickFitResult bestKickFitResult = lastBestEstimator.getFitResult().orElseThrow();
				if (((timestamp - lastKickTimestamp) * 1e-9) > 0.5)
				{
					// only keep the best estimator 0.5s after kick
					estimators
							.removeIf(k -> k.getFitResult()
									.orElse(new KickFitResult(null, 0,
											Geometry.getBallFactory()
													.createTrajectoryFromBallAtRest(Vector2f.ZERO_VECTOR), timestamp, ""))
									.getAvgDistance() > bestKickFitResult.getAvgDistance());
				}

				return bestKickFitResult;
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
						.toList();

				Optional<ILineSegment> line = Lines.regressionLineFromPointsList(points);
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
			kickEventHistory.clear();
			filteredBallHistory.clear();
			lastBestEstimator = null;
		}
	}
}
