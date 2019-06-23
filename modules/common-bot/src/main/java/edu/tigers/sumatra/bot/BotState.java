/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;


@Persistent
public class BotState extends State
{
	private final BotID botID;
	
	
	@SuppressWarnings("unused") // berkeley
	private BotState()
	{
		super();
		botID = BotID.noBot();
	}
	
	
	private BotState(final BotID botID, final State state)
	{
		super(state.getPose(), state.getVel3());
		this.botID = botID;
	}
	
	
	public static BotState of(final BotID botID, final State state)
	{
		return new BotState(botID, state);
	}
	
	
	@Override
	public BotState mirrored()
	{
		return BotState.of(botID, super.mirrored());
	}
	
	
	public BotState interpolate(final BotState state, double percentage)
	{
		return BotState.of(botID, super.interpolate(state, percentage));
	}
	
	
	public BotID getBotID()
	{
		return botID;
	}
}
