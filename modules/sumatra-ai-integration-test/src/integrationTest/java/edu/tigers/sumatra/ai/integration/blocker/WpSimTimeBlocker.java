/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.blocker;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@Log4j2
public class WpSimTimeBlocker extends SimTimeBlocker implements IWorldFrameObserver
{
	private final CountDownLatch latch = new CountDownLatch(1);
	private final List<IStopCondition> stopConditions = new ArrayList<>();


	public WpSimTimeBlocker(final double maxDuration)
	{
		super(maxDuration);
	}


	public WpSimTimeBlocker addStopCondition(IStopCondition condition)
	{
		stopConditions.add(condition);
		return this;
	}


	private void start()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
		SimulationHelper.startSimulation();
	}


	private void stop()
	{
		SimulationHelper.pauseSimulation();
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
	}


	@Override
	@SuppressWarnings("squid:S899") // Return value is not relevant
	public WpSimTimeBlocker await()
	{
		start();
		try
		{
			latch.await(5, TimeUnit.MINUTES);
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} finally
		{
			stop();
		}
		return this;
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		updateTime(wFrameWrapper.getTimestamp());
		var matchingStopConditions = stopConditions.stream().filter(c -> c.stopSimulation(wFrameWrapper)).toList();
		if (!matchingStopConditions.isEmpty())
		{
			log.info("Stop condition(s) match: {}. Stopping...", matchingStopConditions);
			latch.countDown();
		} else if (isTimeUp())
		{
			log.info("Time is up. Stopping...");
			latch.countDown();
		}
	}


	@FunctionalInterface
	public interface IStopCondition
	{
		boolean stopSimulation(final WorldFrameWrapper wfw);
	}
}
