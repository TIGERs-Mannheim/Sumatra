/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * Data holder for simulation per frame state
 */
@Data
public class SimState
{
	private final Map<BotID, SimulatedBot> simulatedBots = new ConcurrentSkipListMap<>();
	private final SimulatedBall simulatedBall = new SimulatedBall(simulatedBots.values());
	private long simTime;
	private long frameId;
	private SimKickEvent lastKickEvent;
	private SslGcRefereeMessage.Referee latestRefereeMessage;


	public void incSimTime(final long dt)
	{
		simTime += dt;
	}


	public void incFrameId()
	{
		frameId += 1;
	}
}
