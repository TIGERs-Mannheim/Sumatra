/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.EqualsAndHashCode;
import lombok.Value;


/**
 * A bot state with a delay.
 */
@Persistent
@Value
@EqualsAndHashCode(callSuper = true)
public class DelayedBotState extends BotState
{
	double delay;


	@SuppressWarnings("unused") // berkeley
	private DelayedBotState()
	{
		super();
		delay = 0;
	}


	private DelayedBotState(BotID botID, State state, double delay)
	{
		super(botID, state);
		this.delay = delay;
	}


	public static DelayedBotState fromBotState(BotState botState, double delay)
	{
		return new DelayedBotState(botState.getBotID(), botState, delay);
	}


	public static DelayedBotState of(BotID botID, State state, double delay)
	{
		return new DelayedBotState(botID, state, delay);
	}


	public static DelayedBotState zero()
	{
		return DelayedBotState.of(BotID.noBot(), State.of(Pose.zero(), Vector3f.zero()), 0);
	}


	@Override
	public DelayedBotState mirrored()
	{
		return DelayedBotState.fromBotState(super.mirrored(), delay);
	}
}
