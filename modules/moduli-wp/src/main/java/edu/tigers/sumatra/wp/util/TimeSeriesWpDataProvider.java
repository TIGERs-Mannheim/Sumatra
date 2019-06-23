/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Data provider for data from world predictor (filtered data)
 */
public class TimeSeriesWpDataProvider implements ITimeSeriesDataProvider, IWorldFrameObserver
{
	private static final Logger log = Logger.getLogger(TimeSeriesWpDataProvider.class.getName());
	
	private final Map<String, Collection<IExportable>> dataBuffers = new HashMap<>();
	
	private IVector2 initBallPos = null;
	private int numFramesBallStopped = 0;
	private SimpleWorldFrame currentFrame = null;
	
	
	/**
	 * Default constructor
	 */
	public TimeSeriesWpDataProvider()
	{
		dataBuffers.put("nearestBot", new ConcurrentLinkedQueue<>());
	}
	
	
	@Override
	public void stop()
	{
		try
		{
			AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			wp.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
	}
	
	
	@Override
	public void start()
	{
		try
		{
			AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			wp.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
	}
	
	
	@Override
	public boolean isDone()
	{
		if (currentFrame == null)
		{
			return false;
		}
		
		if ((!initBallPos.isCloseTo(currentFrame.getBall().getPos().getXYVector(), 50))
				&& (currentFrame.getBall().getVel().getLength2() < 0.1))
		{
			numFramesBallStopped++;
			if (numFramesBallStopped > 10)
			{
				log.debug("ball stopped, data size: "
						+ dataBuffers.values().stream().map(Collection::size).collect(Collectors.toList()));
				return true;
			}
		} else
		{
			numFramesBallStopped = 0;
		}
		return false;
	}
	
	
	@Override
	public Map<String, Collection<IExportable>> getExportableData()
	{
		return dataBuffers;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		currentFrame = wfWrapper.getSimpleWorldFrame();
		if (initBallPos == null)
		{
			initBallPos = currentFrame.getBall().getPos();
		}
		processWorldFrame(currentFrame);
	}
	
	
	private void processWorldFrame(final SimpleWorldFrame currentFrame)
	{
		dataBuffers.computeIfAbsent("wpBall", key -> new ConcurrentLinkedQueue<>())
				.add(currentFrame.getBall());
		dataBuffers.computeIfAbsent("wpBots", key -> new ConcurrentLinkedQueue<>())
				.addAll(currentFrame.getBots().values());
		
		ITrackedBot nearestBot = getBotNearestToBall(currentFrame);
		if (nearestBot != null)
		{
			dataBuffers.get("nearestBot").add(nearestBot);
		}
	}
	
	
	private ITrackedBot getBotNearestToBall(final SimpleWorldFrame frame)
	{
		IVector2 ballPos = frame.getBall().getPos().getXYVector();
		double minDist = Double.MAX_VALUE;
		ITrackedBot nearest = null;
		for (ITrackedBot bot : frame.getBots().values())
		{
			double dist = VectorMath.distancePP(ballPos, bot.getPos());
			if (dist < minDist)
			{
				nearest = bot;
				minDist = dist;
			}
		}
		return nearest;
	}
}
