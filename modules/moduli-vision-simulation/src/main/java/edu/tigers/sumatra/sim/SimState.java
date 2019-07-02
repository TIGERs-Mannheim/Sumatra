package edu.tigers.sumatra.sim;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * Data holder for simulation per frame state
 */
public class SimState
{
	private final Map<BotID, SimulatedBot> simulatedBots = new ConcurrentSkipListMap<>();
	private final SimulatedBall simulatedBall = new SimulatedBall(simulatedBots.values());
	private long simTime;
	private long frameId = 0;
	private IKickEvent lastKickEvent = null;
	private Referee.SSL_Referee latestRefereeMessage = null;
	
	
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
	
	
	public IKickEvent getLastKickEvent()
	{
		return lastKickEvent;
	}
	
	
	public void setLastKickEvent(final IKickEvent lastKickEvent)
	{
		this.lastKickEvent = lastKickEvent;
	}
	
	
	public Referee.SSL_Referee getLatestRefereeMessage()
	{
		return latestRefereeMessage;
	}
	
	
	public void setLatestRefereeMessage(final Referee.SSL_Referee latestRefereeMessage)
	{
		this.latestRefereeMessage = latestRefereeMessage;
	}
}
