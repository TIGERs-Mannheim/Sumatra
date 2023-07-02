/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3f;


@Persistent
public class BotState extends State
{
	private final BotID botID;


	@SuppressWarnings("unused") // berkeley
	protected BotState()
	{
		super();
		botID = BotID.noBot();
	}


	protected BotState(final BotID botID, final State state)
	{
		super(state.getPose(), state.getVel3());
		this.botID = botID;
	}


	public static BotState of(final BotID botID, final State state)
	{
		return new BotState(botID, state);
	}


	public static BotState zero()
	{
		return BotState.of(BotID.noBot(), State.of(Pose.zero(), Vector3f.zero()));
	}


	public static BotState nan()
	{
		return BotState.of(BotID.noBot(), State.of(Pose.nan(), Vector3f.nan()));
	}


	@Override
	public BotState mirrored()
	{
		return BotState.of(botID, super.mirrored());
	}


	public BotID getBotId()
	{
		return botID;
	}
}
