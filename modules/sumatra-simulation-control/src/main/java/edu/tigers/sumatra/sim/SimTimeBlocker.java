/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class SimTimeBlocker implements IWorldFrameObserver
{
	private final CountDownLatch latch = new CountDownLatch(1);
	private final double maxDuration;
	private Long latestTime;
	private Long startTime;
	private final List<IStopCondition> stopConditions = new ArrayList<>();
	
	
	public SimTimeBlocker(final double maxDuration)
	{
		this.maxDuration = maxDuration;
	}
	
	
	public SimTimeBlocker addStopCondition(IStopCondition condition)
	{
		stopConditions.add(condition);
		return this;
	}
	
	
	private void start()
	{
		try
		{
			AWorldPredictor wic = SumatraModel.getInstance()
					.getModule(AWorldPredictor.class);
			wic.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			throw new IllegalStateException("WP module must be present", e);
		}
	}
	
	
	private void stop()
	{
		try
		{
			AWorldPredictor wic = SumatraModel.getInstance()
					.getModule(AWorldPredictor.class);
			wic.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			throw new IllegalStateException("WP module must be present", e);
		}
	}
	
	
	@SuppressWarnings("squid:S899") // Return value is not relevant
	public void await()
	{
		start();
		try
		{
			latch.await(20, TimeUnit.MINUTES);
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} finally
		{
			stop();
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		latestTime = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
		if (startTime == null)
		{
			startTime = latestTime;
		}
		boolean stop = stopConditions.stream().anyMatch(c -> c.stopSimulation(wFrameWrapper));
		if (stop || getDuration() > maxDuration)
		{
			latch.countDown();
			stop();
		}
	}
	
	
	public double getDuration()
	{
		return (latestTime - startTime) / 1e9;
	}
	
	@FunctionalInterface
	public interface IStopCondition
	{
		boolean stopSimulation(final WorldFrameWrapper wfw);
	}
}
