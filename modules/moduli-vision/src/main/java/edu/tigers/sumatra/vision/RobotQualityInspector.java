/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Track the overall quality/visibility of each robot by counting the number of detection over a fixed time horizon.
 */
public class RobotQualityInspector
{
	@Configurable(defValue = "20.0", comment = "The time horizon [s] back into past to measure the quality")
	private static double trackingTimeHorizon = 20.0;

	@Configurable(defValue = "0.05", comment = "If the quality of a robot is below this, it will not be passed out")
	private static double robotQualityThreshold = 0.05;

	static
	{
		ConfigRegistration.registerClass("vision", RobotQualityInspector.class);
	}

	private final Map<BotID, List<Long>> measurements = new HashMap<>();

	private long initialTimestamp;
	private double maxPossibleDetectionsPerCam;
	private double avgDt;


	public RobotQualityInspector()
	{
		reset();
	}


	public void reset()
	{
		initialTimestamp = -1;
		maxPossibleDetectionsPerCam = 0;
		avgDt = 0.01;
		for (BotID botID : BotID.getAll())
		{
			measurements.put(botID, new ArrayList<>());
		}
	}


	public synchronized void addDetection(CamRobot camRobot)
	{
		measurements.get(camRobot.getBotId()).add(camRobot.getTimestamp());

		if (initialTimestamp < 0)
		{
			initialTimestamp = camRobot.getTimestamp();
		}

		double trackingTime = (camRobot.getTimestamp() - initialTimestamp) / 1e9;
		double time = Math.min(trackingTime, trackingTimeHorizon);
		maxPossibleDetectionsPerCam = time / avgDt;
	}


	public synchronized void prune(long currentTimestamp)
	{
		long timestamp = currentTimestamp - (long) (trackingTimeHorizon * 1e9);
		for (List<Long> timestamps : measurements.values())
		{
			while (!timestamps.isEmpty())
			{
				if (timestamps.get(0) < timestamp)
				{
					timestamps.remove(0);
				} else
				{
					break;
				}
			}
		}
	}


	public synchronized void updateAverageDt(double averageDt)
	{
		avgDt = averageDt;
	}


	private double getQuality(final BotID botID)
	{
		return measurements.get(botID).size() / maxPossibleDetectionsPerCam;
	}


	public synchronized long getNumDetections(final BotID botID)
	{
		return measurements.get(botID).size();
	}


	public synchronized double getPossibleDetections()
	{
		return maxPossibleDetectionsPerCam;
	}


	public synchronized boolean passesQualityInspection(final BotID botID)
	{
		return getQuality(botID) > robotQualityThreshold;
	}
}
