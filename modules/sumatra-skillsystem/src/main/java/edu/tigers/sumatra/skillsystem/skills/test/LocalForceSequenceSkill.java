/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalForce;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.DefaultEvents;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;

import java.util.stream.IntStream;


/**
 * Execute a time-based sequence of local forces and torques, no controller at work.
 * Used to experiment with physical behavior of bot.
 */
public class LocalForceSequenceSkill extends AMoveSkill
{
	private final IState finalState = new FinalState();


	/**
	 * UI constructor
	 *
	 * @param durations  durations in [s]
	 * @param forces     forces in [N]
	 * @param directions direction in [rad], bot local system
	 * @param torques    torques in [Nm]
	 */
	public LocalForceSequenceSkill(
			final Double[] durations,
			final Double[] forces,
			final Double[] directions,
			final Double[] torques
	)
	{
		int minParams = IntStream.of(durations.length, forces.length, directions.length, torques.length)
				.min()
				.orElse(0);

		IState entryState = new InitState();
		setInitialState(entryState);

		IState lastState = entryState;

		for (int i = 0; i < minParams; i++)
		{
			IState state = new ExecuteSequenceState(Vector2.fromAngleLength(directions[i], forces[i]), torques[i],
					durations[i]);

			addTransition(lastState, DefaultEvents.DONE, state);

			lastState = state;
		}

		addTransition(lastState, DefaultEvents.DONE, finalState);
	}


	public boolean isDone()
	{
		return getCurrentState() == finalState;
	}


	private class InitState extends AState
	{
		@Override
		public void doEntryActions()
		{
			triggerEvent(DefaultEvents.DONE);
		}
	}

	private class ExecuteSequenceState extends AState
	{
		private ABotSkill localForceSkill;
		private TimestampTimer timer;


		public ExecuteSequenceState(IVector2 xy, double w, double duration)
		{
			this.localForceSkill = new BotSkillLocalForce(xy, w);
			this.timer = new TimestampTimer(duration);
		}


		@Override
		public void doEntryActions()
		{
			timer.start(getWorldFrame().getTimestamp());
			getMatchCtrl().setSkill(localForceSkill);
		}


		@Override
		public void doUpdate()
		{
			if (timer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				triggerEvent(DefaultEvents.DONE);
			}
		}
	}

	private class FinalState extends AState
	{
		@Override
		public void doEntryActions()
		{
			setMotorsOff();
		}
	}
}
