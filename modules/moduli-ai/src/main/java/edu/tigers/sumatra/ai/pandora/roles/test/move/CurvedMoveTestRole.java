/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * A generic move test role that has different modes and can capture data
 */
@Log4j2
public class CurvedMoveTestRole extends ARole
{
	private BotWatcher botWatcher = null;
	private final CollectorObserver collectorObserver = new CollectorObserver();
	private final boolean continuousCapture;
	private final IVector2 primaryDir;

	private enum EEvent implements IEvent
	{
		DONE,
	}


	@SuppressWarnings("squid:S00107") // number of parameters required for UI
	public CurvedMoveTestRole(
			final IVector2 initPos,
			final IVector2 intermediatePos,
			final double intermediateDuration,
			final IVector2 finalPos,
			final double initOrientation,
			final double intermediateOrientation,
			final double finalOrientation,
			final IVector2 primaryDir,
			final int iterations,
			final boolean continuousCapture)
	{
		super(ERole.CURVED_MOVE_TEST);
		this.continuousCapture = continuousCapture;
		this.primaryDir = primaryDir;

		collectorObserver.parameters.put("initPos", initPos.getSaveableString());
		collectorObserver.parameters.put("intermediatePos", intermediatePos.getSaveableString());
		collectorObserver.parameters.put("intermediateDuration", String.format("%.1f", intermediateDuration));
		collectorObserver.parameters.put("finalPos", finalPos.getSaveableString());
		collectorObserver.parameters.put("initOrientation", String.format("%.1f", initOrientation));
		collectorObserver.parameters.put("intermediateOrientation", String.format("%.1f", intermediateOrientation));
		collectorObserver.parameters.put("finalOrientation", String.format("%.1f", finalOrientation));
		collectorObserver.parameters.put("primaryDir", primaryDir.getSaveableString());
		collectorObserver.parameters.put("iterations", String.valueOf(iterations));

		IState lastState = new InitState();
		setInitialState(lastState);
		for (int i = 0; i < iterations; i++)
		{
			IState waitState1 = new WaitState(0, ECapture.STOP);
			IState prepareState = new PrepareState(initPos, initOrientation);
			IState waitState2 = new WaitState(500, ECapture.START);
			IState moveState1 = new MoveToState(intermediatePos, intermediateOrientation, intermediateDuration, i);
			IState moveState2 = new MoveToState(finalPos, finalOrientation, Double.MAX_VALUE, i);
			IState waitState3 = new WaitState(0, ECapture.STOP);

			addTransition(lastState, EEvent.DONE, waitState1);
			addTransition(waitState1, EEvent.DONE, prepareState);
			addTransition(prepareState, EEvent.DONE, waitState2);
			addTransition(waitState2, EEvent.DONE, moveState1);
			addTransition(moveState1, EEvent.DONE, moveState2);
			addTransition(moveState2, EEvent.DONE, waitState3);

			lastState = waitState3;
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

			if (continuousCapture)
			{
				startWatcher();
			}

			triggerEvent(EEvent.DONE);
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

			if (!continuousCapture && capture == ECapture.START)
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
	}

	private class PrepareState extends AState
	{
		protected IVector2 dest;
		private long tLastStill = 0;

		protected IVector2 initPos;
		protected double initOrientation;
		protected final double destOrientation;
		protected MoveToSkill skill;


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

			MoveToSkill move = MoveToSkill.createMoveToSkill();
			move.updateDestination(dest);
			move.updateTargetAngle(destOrientation);
			skill.getMoveCon().physicalObstaclesOnly();
			move.getMoveCon().setBallObstacle(false);
			setNewSkill(move);
		}


		@Override
		public void doUpdate()
		{
			double dist2Dest = VectorMath.distancePP(dest, getPos());

			if ((getBot().getRobotInfo().getInternalState().map(BotState::getVel2).orElse(getBot().getVel()).getLength2()
					< 0.2)
					&& (Math.abs(
					getBot().getRobotInfo().getInternalState().map(BotState::getAngularVel).orElse(getBot().getAngularVel()))
					< 0.5)
					&& (dist2Dest < 2000))
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
		int iteration;
		double maxDuration;
		long tStart;


		private MoveToState(final IVector2 dest, final double orientation, final double maxDuration, final int iteration)
		{
			super(dest, orientation);
			this.iteration = iteration;
			this.maxDuration = maxDuration;
		}


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();

			collectorObserver.parameters.put("dest", dest.getSaveableString());
			collectorObserver.parameters.put("destOrientation", String.format("%.3f", destOrientation));
			collectorObserver.parameters.put("iteration", String.valueOf(iteration));

			MoveToSkill skill = MoveToSkill.createMoveToSkill();
			skill.updateDestination(dest);
			skill.updateTargetAngle(destOrientation);
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveConstraints().setPrimaryDirection(primaryDir);
			setNewSkill(skill);

			tStart = getWFrame().getTimestamp();
		}


		@Override
		public void doUpdate()
		{
			double duration = (getWFrame().getTimestamp() - tStart) / 1e9;
			if (duration >= maxDuration)
			{
				triggerEvent(EEvent.DONE);
				return;
			}

			super.doUpdate();
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


	private void startWatcher()
	{
		botWatcher = new BotWatcher(getBotID(), EDataAcquisitionMode.NONE, "curved-move-test");
		botWatcher.setTimeSeriesDataCollectorObserver(collectorObserver);
		botWatcher.start();
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
