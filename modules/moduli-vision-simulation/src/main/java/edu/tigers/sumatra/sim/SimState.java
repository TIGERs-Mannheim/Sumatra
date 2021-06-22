/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * Data holder for simulation per frame state
 */
public class SimState
{
	private final Map<BotID, SimulatedBot> simulatedBots = new ConcurrentSkipListMap<>();
	private final SimulatedBall simulatedBall = new SimulatedBall(simulatedBots.values());
	private long simTime;
	private long frameId = 0;
	private SimKickEvent lastKickEvent = null;
	private SslGcRefereeMessage.Referee latestRefereeMessage = null;


	public Map<BotID, SimulatedBot> getSimulatedBots()
	{
		return simulatedBots;
	}


	public SimulatedBall getSimulatedBall()
	{
		return simulatedBall;
	}


	public long getSimTime()
	{
		return simTime;
	}


	public void setSimTime(final long simTime)
	{
		this.simTime = simTime;
	}


	public void incSimTime(final long dt)
	{
		simTime += dt;
	}


	public long getFrameId()
	{
		return frameId;
	}


	public void incFrameId()
	{
		frameId += 1;
	}


	public SimKickEvent getLastKickEvent()
	{
		return lastKickEvent;
	}


	public void setLastKickEvent(final SimKickEvent lastKickEvent)
	{
		this.lastKickEvent = lastKickEvent;
	}


	public SslGcRefereeMessage.Referee getLatestRefereeMessage()
	{
		return latestRefereeMessage;
	}


	public void setLatestRefereeMessage(final SslGcRefereeMessage.Referee latestRefereeMessage)
	{
		this.latestRefereeMessage = latestRefereeMessage;
	}
}
