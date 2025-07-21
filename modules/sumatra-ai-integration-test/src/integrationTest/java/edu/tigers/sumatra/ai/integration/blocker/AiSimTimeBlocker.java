/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.blocker;

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


@Log4j2
public class AiSimTimeBlocker extends SimTimeBlocker implements IAIObserver
{
	private final BlockingDeque<AIInfoFrame> queue = new LinkedBlockingDeque<>(2);
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
		log.trace("Starting");
		queue.clear();
		SumatraModel.getInstance().getModule(AAgent.class).addObserver(this);
		SimulationHelper.startSimulation();
		log.trace("Started");
	}


	private void stop()
	{
		log.trace("Stopping");
		SumatraModel.getInstance().getModule(AAgent.class).removeObserver(this);
		queue.clear();
		SimulationHelper.pauseSimulation();
		log.trace("Stopped");
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

				if (isTimeUp())
				{
					log.info("Time is up");
					return this;
				}

				var matchingStopConditions = stopConditions.stream()
						.filter(c -> c.stopSimulation(frame))
						.toList();
				if (!matchingStopConditions.isEmpty())
				{
					var names = matchingStopConditions.stream().map(IStopCondition::name).toList();
					log.info("Stop condition(s) match: {}", names);
					for (var c : matchingStopConditions)
					{
						var hint = c.hint(frame);
						if (!hint.isEmpty())
						{
							log.info("Hint for {}: {}", c.name(), hint);
						}
					}
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

		default String name()
		{
			return getClass().getSimpleName();
		}

		default String hint(final AIInfoFrame frame)
		{
			return "";
		}
	}

	@FunctionalInterface
	public interface IAiFrameHook
	{
		void process(final AIInfoFrame frame);
	}
}
