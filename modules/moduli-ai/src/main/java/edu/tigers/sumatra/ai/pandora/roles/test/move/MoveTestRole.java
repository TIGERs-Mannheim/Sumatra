/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.botmanager.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.test.MoveBangBangSkill;
import edu.tigers.sumatra.skillsystem.tracking.RobotTrajectoryTracker;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * A generic move test role that has different modes and can capture data
 */
public class MoveTestRole extends ARole
{
	private static final Logger log = LogManager.getLogger(MoveTestRole.class.getName());

	private final List<MotionResult> results = new ArrayList<>();
	private BotWatcher botWatcher = null;
	private final EMoveMode mode;
	private final CollectorObserver collectorObserver = new CollectorObserver();
	private final boolean continuousCapture;

	private final IVector2 primaryDir;
	private final boolean fastMove;
	private final boolean rollOut;

	private enum EEvent implements IEvent
	{
		DONE,
	}


	/**
	 * Possible move modes
	 */
	public enum EMoveMode
	{
		TRAJ_WHEEL_VEL,
		TRAJ_VEL,
		TRAJ_POS,
		TRAJ_GLOBAL_VEL,
		MOVE_TO,
	}


	@SuppressWarnings("squid:S00107") // number of parameters required for UI
	public MoveTestRole(final EMoveMode mode,
			final IVector2 initPos,
			final IVector2 finalPos,
			final IVector2 primaryDir,
			final double startAngle,
			final double stopAngle,
			final double angleStepDeg,
			final double angleTurnDeg,
			final boolean fastMove,
			final int iterations,
			final boolean rollOut,
			final boolean continuousCapture)
	{
		super(ERole.MOVE_TEST);
		this.mode = mode;
		this.continuousCapture = continuousCapture;
		this.primaryDir = primaryDir;
		this.fastMove = fastMove;
		this.rollOut = rollOut;

		double orientation = finalPos.subtractNew(initPos).getAngle(0);
		double scale = finalPos.distanceTo(initPos);

		collectorObserver.parameters.put("mode", mode.name());
		collectorObserver.parameters.put("initPos", initPos.getSaveableString());
		collectorObserver.parameters.put("finalPos", finalPos.getSaveableString());
		collectorObserver.parameters.put("primaryDir", primaryDir.getSaveableString());
		collectorObserver.parameters.put("orientation", String.format("%.3f", orientation));
		collectorObserver.parameters.put("scale", String.format("%.0f", scale));
		collectorObserver.parameters.put("startAngle", String.format("%.2f", startAngle));
		collectorObserver.parameters.put("stopAngle", String.format("%.2f", stopAngle));
		collectorObserver.parameters.put("angleStepDeg", String.format("%.0f", angleStepDeg));
		collectorObserver.parameters.put("angleTurnDeg", String.format("%.0f", angleTurnDeg));
		collectorObserver.parameters.put("fastMove", String.valueOf(fastMove));
		collectorObserver.parameters.put("iterations", String.valueOf(iterations));
		collectorObserver.parameters.put("rollOut", String.valueOf(rollOut));

		List<double[]> relTargets = new ArrayList<>();
		for (double a = AngleMath.deg2rad(startAngle); a < (AngleMath.deg2rad(stopAngle) - 1e-4); a += AngleMath
				.deg2rad(angleStepDeg))
		{
			IVector2 dir = Vector2.fromAngle(orientation);
			relTargets.add(new double[] { dir.x(), dir.y(), a, a + AngleMath.deg2rad(angleTurnDeg) });
		}

		IState lastState = new InitState();
		setInitialState(lastState);
		for (int i = 0; i < iterations; i++)
		{
			for (double[] target : relTargets)
			{
				IVector2 dest = initPos.addNew(Vector2.fromXY(target[0], target[1]).scaleToNew(scale));
				double initOrient = orientation + target[2];
				double finalOrient = orientation + target[3];
				IState waitState1 = new WaitState(0, ECapture.STOP);
				IState prepareState = new PrepareState(initPos, initOrient);
				IState waitState2 = new WaitState(500, ECapture.START);
				IState moveState = new MoveToState(dest, finalOrient, i);
				IState waitState3 = new WaitState(0, ECapture.STOP);
				IState prepare2State = new PrepareState(dest, finalOrient);
				IState waitState4 = new WaitState(500, ECapture.START);
				IState moveBackState = new MoveToState(initPos, initOrient, i);

				addTransition(lastState, EEvent.DONE, waitState1);
				addTransition(waitState1, EEvent.DONE, prepareState);
				addTransition(prepareState, EEvent.DONE, waitState2);
				addTransition(waitState2, EEvent.DONE, moveState);
				addTransition(moveState, EEvent.DONE, waitState3);
				addTransition(waitState3, EEvent.DONE, prepare2State);
				addTransition(prepare2State, EEvent.DONE, waitState4);
				addTransition(waitState4, EEvent.DONE, moveBackState);
				lastState = moveBackState;
			}
			IState evalState = new EvaluationState();
			addTransition(lastState, EEvent.DONE, evalState);
			lastState = evalState;
		}
		addTransition(lastState, EEvent.DONE, new CleanUpState());
	}


	private class InitState extends AState
	{
		@Override
		public void doEntryActions()
		{
			collectorObserver.parameters.put("velMax",
					String.format("%.1f", getBot().getRobotInfo().getBotParams().getMovementLimits().getVelMax()));
			collectorObserver.parameters.put("accMax",
					String.format("%.1f", getBot().getRobotInfo().getBotParams().getMovementLimits().getAccMax()));
			collectorObserver.parameters.put("velMaxW",
					String.format("%.1f", getBot().getRobotInfo().getBotParams().getMovementLimits().getVelMaxW()));
			collectorObserver.parameters.put("accMaxW",
					String.format("%.1f", getBot().getRobotInfo().getBotParams().getMovementLimits().getAccMaxW()));

			triggerEvent(EEvent.DONE);
		}
	}


	private class EvaluationState extends AState
	{
		@Override
		public void doEntryActions()
		{
			reportAverageValues();

			try (CSVExporter exp = new CSVExporter("data/movetest/", "", CSVExporter.EMode.PREPEND_DATE))
			{
				exp.setHeader(MotionResult.getHeaders());
				for (MotionResult r : results)
				{
					exp.addValues(r.getNumberList());
				}
			}
			results.clear();
			triggerEvent(EEvent.DONE);
		}


		private void reportAverageValues()
		{
			double avgDist2Line = results.stream().flatMap(r -> r.dists2Line.stream()).mapToDouble(a -> a).average()
					.orElse(0.0);
			double avgOffset = results.stream().mapToDouble(r -> r.dest.subtractNew(r.finalPos).getLength2()).average()
					.orElse(0.0);
			log.info("Overall: avgDist2Line={}, avgOffset={}", avgDist2Line, avgOffset);
		}
	}

	private enum ECapture
	{
		START,
		STOP,
	}

	private class WaitState extends AState
	{
		private long tStart;
		private final long waitNs;
		private final ECapture capture;


		public WaitState(final long waitMs, ECapture capture)
		{
			waitNs = (long) (waitMs * 1e6);
			this.capture = capture;
		}


		@Override
		public void doEntryActions()
		{
			tStart = getWFrame().getTimestamp();

			if (capture == ECapture.START && (!continuousCapture || botWatcher == null))
			{
				startWatcher();
			}
		}


		@Override
		public void doExitActions()
		{
			if (!continuousCapture && capture == ECapture.STOP)
			{
				stopWatcher();
			}
		}


		@Override
		public void doUpdate()
		{
			if ((getWFrame().getTimestamp() - tStart) > waitNs)
			{
				triggerEvent(EEvent.DONE);
			}
		}


		private void startWatcher()
		{
			botWatcher = new BotWatcher(getBotID(), EDataAcquisitionMode.NONE, "move-test");
			botWatcher.setTimeSeriesDataCollectorObserver(collectorObserver);
			botWatcher.start();
		}
	}

	private class PrepareState extends AState
	{
		protected IVector2 dest;
		private long tLastStill = 0;

		protected IVector2 initPos;
		protected double initOrientation;
		protected final double destOrientation;


		private PrepareState(final IVector2 dest, final double orientation)
		{
			this.dest = dest;
			this.destOrientation = orientation;
		}


		@Override
		public void doEntryActions()
		{
			initPos = getPos();
			initOrientation = getBot().getOrientation();
			dest = initPos.addNew(dest.subtractNew(getPos()));
			tLastStill = 0;

			MoveToSkill skill = MoveToSkill.createMoveToSkill();
			skill.updateDestination(dest);
			skill.updateTargetAngle(destOrientation);
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			if ((getBot().getRobotInfo().getInternalState().map(BotState::getVel2).orElse(getBot().getVel()).getLength2()
					< 0.2)
					&& (Math.abs(
					getBot().getRobotInfo().getInternalState().map(BotState::getAngularVel).orElse(getBot().getAngularVel()))
					< 0.5))
			{
				if (tLastStill == 0)
				{
					tLastStill = getWFrame().getTimestamp();
				}
				if ((getWFrame().getTimestamp() - tLastStill) > 5e8)
				{
					onDone();
					triggerEvent(EEvent.DONE);
				}
			} else
			{
				tLastStill = 0;
			}
		}


		protected void onDone()
		{
			// can be overwritten
		}
	}

	private class MoveToState extends PrepareState
	{
		MotionResult result;
		int iteration;
		RobotTrajectoryTracker trackerInternal;
		RobotTrajectoryTracker trackerVision;


		private MoveToState(final IVector2 dest, final double orientation, final int iteration)
		{
			super(dest, orientation);
			this.iteration = iteration;
		}


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();

			collectorObserver.parameters.put("dest", dest.getSaveableString());
			collectorObserver.parameters.put("destOrientation", String.format("%.3f", destOrientation));
			collectorObserver.parameters.put("iteration", String.valueOf(iteration));

			switch (mode)
			{
				case MOVE_TO -> setNewSkill(getMoveToSkill());
				case TRAJ_POS ->
						setNewSkill(new MoveBangBangSkill(dest, destOrientation, EBotSkill.GLOBAL_POSITION, rollOut));
				case TRAJ_VEL ->
						setNewSkill(new MoveBangBangSkill(dest, destOrientation, EBotSkill.LOCAL_VELOCITY, rollOut));
				case TRAJ_GLOBAL_VEL ->
						setNewSkill(new MoveBangBangSkill(dest, destOrientation, EBotSkill.GLOBAL_VELOCITY, rollOut));
				case TRAJ_WHEEL_VEL ->
						setNewSkill(new MoveBangBangSkill(dest, destOrientation, EBotSkill.WHEEL_VELOCITY, rollOut));
				default -> throw new IllegalArgumentException("Invalid mode: " + mode);
			}

			result = new MotionResult();
			trackerInternal = new RobotTrajectoryTracker(5.0);
			trackerVision = new RobotTrajectoryTracker(5.0);

			var trajectory = TrajectoryGenerator.generatePositionTrajectory(getBot(), dest);

			var trajW = TrajectoryGenerator.generateRotationTrajectory(getBot(), destOrientation);
			var trajectoryXyw = new TrajectoryXyw(trajectory, trajW);

			for (double t = 0; t < trajectoryXyw.getTotalTime(); t += 0.01)
			{
				long timestamp = getWFrame().getTimestamp() + (long) (t * 1e9);
				State state = State.of(Pose.from(trajectoryXyw.getPositionMM(t)), trajectoryXyw.getVelocity(t));
				trackerInternal.addTarget(timestamp, state);
				trackerVision.addTarget(timestamp, state);
			}
		}


		private MoveToSkill getMoveToSkill()
		{
			MoveToSkill skill = MoveToSkill.createMoveToSkill();
			skill.updateDestination(dest);
			skill.updateTargetAngle(destOrientation);
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveConstraints().setPrimaryDirection(primaryDir);
			skill.getMoveConstraints().setFastMove(fastMove);
			return skill;
		}


		@Override
		public void doUpdate()
		{
			super.doUpdate();
			double dist2Line = Lines.lineFromPoints(initPos, dest).distanceTo(getPos());
			result.dists2Line.add(dist2Line);

			getBot().getFilteredState().ifPresent(s -> trackerVision.addMeasure(getWFrame().getTimestamp(), s));
			getBot().getRobotInfo().getInternalState()
					.ifPresent(s -> trackerInternal.addMeasure(getWFrame().getTimestamp(), s));
		}


		@Override
		protected void onDone()
		{
			result.initPos = initPos;
			result.initOrientation = initOrientation;
			result.finalPos = getPos();
			result.finalOrientation = getBot().getOrientation();
			result.dest = dest;
			result.destOrientation = destOrientation;
			results.add(result);

			summary(trackerInternal, "internal");
			summary(trackerVision, "vision");
		}


		private void summary(RobotTrajectoryTracker tracker, String name)
		{
			final double feedbackDelayWithAvg = tracker.estimateTimeDifferenceWithAvg();
			final double feedbackDelayWithMedian = tracker.estimateTimeDifferenceWithMedian();
			if (log.isInfoEnabled())
			{
				log.info("{}: Estimated feedback delay: {} (avg), {} (median)", name,
						String.format("%.3f, ", feedbackDelayWithAvg),
						String.format("%.3f, ", feedbackDelayWithMedian));
			}
			double feedbackDelay = feedbackDelayWithAvg;

			Optional<RobotTrajectoryTracker.StateDifference> stateDifferenceAvg = tracker
					.differenceAverage(feedbackDelay);
			Optional<RobotTrajectoryTracker.StateDifference> stateDifferenceMin = tracker
					.differencePercentile(feedbackDelay, 0.0);
			Optional<RobotTrajectoryTracker.StateDifference> stateDifferenceMax = tracker
					.differencePercentile(feedbackDelay, 1.0);
			Optional<RobotTrajectoryTracker.StateDifference> stateDifferenceMedian = tracker
					.differencePercentile(feedbackDelay, 0.5);

			stateDifferenceAvg.ifPresent(difference -> log.info("{}: Tracker diff avg: {}", name, difference));
			stateDifferenceMin.ifPresent(difference -> log.info("{}: Tracker diff min: {}", name, difference));
			stateDifferenceMax.ifPresent(difference -> log.info("{}: Tracker diff max: {}", name, difference));
			stateDifferenceMedian.ifPresent(difference -> log.info("{}: Tracker diff median: {}", name, difference));

			String tempDir = System.getProperty("java.io.tmpdir");
			trackerInternal.export(tempDir + "/tracker_" + name + ".csv", feedbackDelay);
		}
	}

	private class CleanUpState extends AState
	{
		@Override
		public void doEntryActions()
		{
			setCompleted();
		}
	}


	private static class MotionResult
	{
		private IVector2 initPos;
		private double initOrientation;
		private IVector2 finalPos;
		private double finalOrientation;
		private IVector2 dest;
		private double destOrientation;
		private final List<Double> dists2Line = new ArrayList<>();


		static List<String> getHeaders()
		{
			return Arrays.asList(
					"initPos.x",
					"initPos.y",
					"initOrientation",
					"finalPos.x",
					"finalPos.y",
					"finalOrientation",
					"dest.x",
					"dest.y",
					"destOrientation",
					"diff.x",
					"diff.y",
					"offset",
					"aDiff",
					"avgDist2Line");
		}


		public List<Number> getNumberList()
		{
			double offset = VectorMath.distancePP(finalPos, dest);
			IVector2 diff = finalPos.subtractNew(dest);
			double avgDist2Line = dists2Line.stream().mapToDouble(a -> a).average().orElse(0.0);
			double aDiff = AngleMath.difference(finalOrientation, destOrientation);

			List<Number> nbrs = new ArrayList<>(initPos.getNumberList());
			nbrs.add(initOrientation);
			nbrs.addAll(finalPos.getNumberList());
			nbrs.add(finalOrientation);
			nbrs.addAll(dest.getNumberList());
			nbrs.add(destOrientation);
			nbrs.addAll(diff.getNumberList());
			nbrs.add(offset);
			nbrs.add(aDiff);
			nbrs.add(avgDist2Line);
			return nbrs;
		}


		@Override
		public String toString()
		{
			List<Number> nbrs = getNumberList();
			StringBuilder sb = new StringBuilder();
			for (Number nbr : nbrs)
			{
				sb.append(nbr);
				sb.append(' ');
			}
			return sb.toString();
		}
	}


	private void stopWatcher()
	{
		if (botWatcher != null)
		{
			botWatcher.stop();
		}
	}


	@Override
	protected void onCompleted()
	{
		super.onCompleted();
		stopWatcher();
	}


	private static class CollectorObserver implements ITimeSeriesDataCollectorObserver
	{
		final Map<String, String> parameters = new HashMap<>();


		@Override
		public void onAddMetadata(final Map<String, Object> jsonMapping)
		{
			jsonMapping.putAll(parameters);
		}
	}
}
