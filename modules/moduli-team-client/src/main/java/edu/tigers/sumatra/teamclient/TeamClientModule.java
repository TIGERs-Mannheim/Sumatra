/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.teamclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.model.SumatraModel;

public class TeamClientModule extends AModule implements IAIObserver
{
	private static final int TIMEOUT = 2;
	private static final Logger log = Logger.getLogger(TeamClientModule.class);
	private ExecutorService threadPool = null;
	private List<TeamClientTask> tasks = new ArrayList<>();
	
	
	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AAgent.class).removeObserver(this);
		tasks.forEach(task -> task.setActive(false));
		tasks.clear();
		threadPool.shutdownNow();
		try {
			Validate.isTrue(threadPool.awaitTermination(TIMEOUT, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			log.warn("Interrupted while waiting for termination", e);
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public void startModule()
	{
		threadPool = Executors.newFixedThreadPool(2);
		SumatraModel.getInstance().getModule(AAgent.class).addObserver(this);
	}
	
	
	private void addTask(final TeamClientTask task)
	{
		tasks.add(task);
		threadPool.submit(task);
	}
	
	
	@Override
	public synchronized void onNewAIInfoFrame(final AIInfoFrame lastAiInfoFrame)
	{
		for (TeamClientTask task : tasks)
		{
			if (task.getTeamColor() == lastAiInfoFrame.getTeamColor())
			{
				if (!task.getActive())
				{
					addTask(new TeamClientTask(lastAiInfoFrame.getTeamColor()));
					tasks.remove(task);
					return;
				}
				task.submitNewAiFrame(lastAiInfoFrame);
				return;
			}
		}
		// The team does not yet have a client -> create new
		addTask(new TeamClientTask(lastAiInfoFrame.getTeamColor()));
	}
}
