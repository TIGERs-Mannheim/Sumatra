/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;


public class BotStateFromTrajectoryCalculator
{
	private static final int BUFFER_SIZE = 30;
	
	private final Map<BotID, DataSync<BotState>> botStateBuffer = new HashMap<>();
	
	
	public Optional<BotState> getState(final RobotInfo robotInfo)
	{
		Optional<BotState> trajState = botToState(robotInfo);
		if (trajState.isPresent())
		{
			DataSync<BotState> stateBuffer = getStateBuffer(robotInfo.getBotId());
			stateBuffer.add(robotInfo.getTimestamp(), trajState.get());
			
			long timestamp = robotInfo.getTimestamp() - (long) (robotInfo.getBotParams().getFeedbackDelay() * 1e9);
			return stateBuffer.get(timestamp).map(p -> interpolate(p, timestamp));
		}
		return Optional.empty();
	}
	
	
	public void reset(BotID botID)
	{
		Optional.ofNullable(botStateBuffer.get(botID)).ifPresent(DataSync::reset);
	}
	
	
	public Optional<BotState> getLatestState(BotID botID)
	{
		return getStateBuffer(botID).getLatest().map(DataSync.DataStore::getData);
	}
	
	
	private BotState interpolate(DataSync<BotState>.DataPair p, long timestamp)
	{
		DataSync<BotState>.DataStore first = p.getFirst();
		DataSync<BotState>.DataStore second = p.getSecond();
		assert first.getTimestamp() <= timestamp : first.getTimestamp() + " " + timestamp;
		assert second.getTimestamp() >= timestamp : second.getTimestamp() + " " + timestamp;
		long timeDiff = second.getTimestamp() - first.getTimestamp();
		if (timeDiff == 0)
		{
			return first.getData();
		}
		assert timeDiff >= 0 : timeDiff;
		long targetToSecond = second.getTimestamp() - timestamp;
		assert targetToSecond >= 0 : targetToSecond;
		double percentageOfSecond = (double) targetToSecond / timeDiff;
		assert percentageOfSecond >= 0.0 : percentageOfSecond;
		assert percentageOfSecond <= 1.0 : percentageOfSecond;
		return first.getData().interpolate(second.getData(), percentageOfSecond);
	}
	
	
	private DataSync<BotState> getStateBuffer(final BotID botId)
	{
		return botStateBuffer.computeIfAbsent(botId, b -> new DataSync<>(BUFFER_SIZE));
	}
	
	
	private Optional<BotState> botToState(final RobotInfo bot)
	{
		return bot.getTrajectory().map(this::trajectoryToState).map(s -> BotState.of(bot.getBotId(), s));
	}
	
	
	private State trajectoryToState(final ITrajectory<IVector3> traj)
	{
		IVector3 pose = traj.getPositionMM(0.0);
		return State.of(Pose.from(pose.getXYVector(), pose.z()), traj.getVelocity(0.0));
	}
}
