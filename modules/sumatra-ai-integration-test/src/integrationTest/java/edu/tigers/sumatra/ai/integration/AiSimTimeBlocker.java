/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SimulationHelper;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Log4j2
public class AiSimTimeBlocker extends SimTimeBlocker implements IAIObserver
{
	private final BlockingDeque<AIInfoFrame> queue = new LinkedBlockingDeque<>(1);
	private final List<IStopCondition> stopConditions = new ArrayList<>();
	private final List<IAiFrameHook> hooks = new ArrayList<>();


	public AiSimTimeBlocker(final double maxDuration)
	{
		super(maxDuration);
	}


	public AiSimTimeBlocker addStopCondition(IStopCondition condition)
	{
		stopConditions.add(condition);
		return this;
	}


	public AiSimTimeBlocker addHook(IAiFrameHook hook)
	{
		hooks.add(hook);
		return this;
	}


	private void start()
	{
		queue.clear();
		SumatraModel.getInstance().getModule(AAgent.class).addObserver(this);
		SimulationHelper.startSimulation();
	}


	private void stop()
	{
		SumatraModel.getInstance().getModule(AAgent.class).removeObserver(this);
		queue.clear();
		SimulationHelper.pauseSimulation();
	}


	@Override
	public AiSimTimeBlocker await()
	{
		start();
		try
		{
			while (true)
			{
				AIInfoFrame frame = queue.pollLast(30, TimeUnit.SECONDS);
				if (frame == null)
				{
					throw new IllegalStateException("Timed out polling for AI frames");
				}
				updateTime(frame.getWorldFrame().getTimestamp());
				
				if (isTimeUp()) {
					log.info("Time is up. Stopping...");
					return this;
				}
				
				var matchingStopConditions = stopConditions.stream().filter(c -> c.stopSimulation(frame)).collect(Collectors.toList());
				if (!matchingStopConditions.isEmpty())
				{
					log.info("Stop condition(s) match: {}. Stopping...", matchingStopConditions);
					return this;
				}

				hooks.forEach(c -> c.process(frame));
			}
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
	public void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		try
		{
			queue.putFirst(lastAIInfoframe);
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}


	@FunctionalInterface
	public interface IStopCondition
	{
		boolean stopSimulation(final AIInfoFrame frame);
	}

	@FunctionalInterface
	public interface IAiFrameHook
	{
		void process(final AIInfoFrame frame);
	}
}
