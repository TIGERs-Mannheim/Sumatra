/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Check if a bot is mal functioning by not moving to its destination (and rotating around itself constantly)
 */
@Log4j2
public class MalFunctioningBotCalculator
{
	@Configurable(defValue = "5", comment = "[s] Min time without movement to be considered mal functioning")
	private static int minTimeNotMoved = 5;
	@Configurable(defValue = "0.1", comment = "[rad] Min orientation change to consider as rotation")
	private static double minOrientationChange = 0.1;
	@Configurable(defValue = "100", comment = "[mm] Min position distance to consider as movement")
	private static double minPosDiff = 100;
	@Configurable(defValue = "true", comment = "Consider constant rotation as additional condition for malfunction")
	private static boolean useRotationCriteria = true;

	static
	{
		ConfigRegistration.registerClass("wp", MalFunctioningBotCalculator.class);
	}

	private final Map<BotID, LastBotState> lastBotStates = new HashMap<>();


	public void reset()
	{
		lastBotStates.clear();
	}


	public boolean isMalFunctioning(RobotInfo info, Pose pose, Map<BotID, BotState> botStates)
	{
		if (info.getRobotMode() != ERobotMode.READY)
		{
			lastBotStates.remove(info.getBotId());
			return false;
		}
		boolean rotating = false;
		boolean notMoving = false;
		if (lastBotStates.containsKey(info.getBotId()))
		{
			IVector2 pos = pose.getPos();
			ITrajectory<IVector3> trajectory = info.getTrajectory().orElse(null);
			long timestamp = info.getTimestamp();
			IVector2 dest = trajectory != null ? trajectory.getFinalDestination().getXYVector() : null;
			boolean hasMovementIntent = dest != null && dest.distanceTo(pos) > minPosDiff;
			double orientation = pose.getOrientation();
			LastBotState lastState = lastBotStates.get(info.getBotId());
			lastState.updateState(pos, orientation, timestamp);

			if (timestamp - lastState.lastMoved > minTimeNotMoved * 1e9 && hasMovementIntent)
			{
				List<IVector2> closeBotPositions = botStates.values().stream()
						.map(State::getPos)
						.filter(botPos -> botPos.distanceTo(pos) < 2.1 * Geometry.getBotRadius())
						.toList();
				boolean isBlocked = closeBotPositions.stream()
						.anyMatch(botPos -> Lines.segmentFromPoints(pos, dest).distanceTo(botPos) < minPosDiff);

				notMoving = closeBotPositions.size() < 2 && !isBlocked;
				if (notMoving)
				{
					log.warn("{} has not moved in the last {} seconds. It might be mal functioning.", info.getBotId(),
							(timestamp - lastState.lastMoved) / 1e9);
				}
			}
			rotating = timestamp - lastState.lastFixedOrientation > minTimeNotMoved * 1e9;
		}
		lastBotStates.computeIfAbsent(info.getBotId(),
				botID -> new LastBotState(pose.getPos(), pose.getOrientation(), info.getTimestamp()));
		return notMoving && (!useRotationCriteria || rotating);
	}


	private static class LastBotState
	{
		private IVector2 position;
		private double orientation;
		private long lastMoved;
		private long lastFixedOrientation;


		public LastBotState(IVector2 pos, double orientation, long timestamp)
		{
			position = pos;
			this.orientation = orientation;
			lastMoved = timestamp;
			lastFixedOrientation = timestamp;
		}


		public void updateState(IVector2 pos, double orientation, long timestamp)
		{
			boolean moved = pos.distanceTo(position) > minPosDiff;
			boolean rotated = Math.abs(orientation - this.orientation) > minOrientationChange;

			if (moved)
			{
				position = pos;
				lastMoved = timestamp;
				lastFixedOrientation = timestamp;
			} else if (!rotated)
			{
				lastFixedOrientation = timestamp;
			}
			this.orientation = orientation;
		}
	}
}
