/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.teamclient;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.util.Safe;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class TeamClientModule extends AModule implements IAIObserver
{
	private static final int TIMEOUT = 2;

	@Configurable(comment = "Our Team Name", defValue = "TIGERs Mannheim")
	private static String tigersTeamName = "TIGERs Mannheim";

	@Configurable(comment = "Override simulation detection to always connect to any available team, even if the name does not match", defValue = "false")
	private static boolean connectToAnyTeamName = false;

	private static final Logger log = LogManager.getLogger(TeamClientModule.class);
	private ExecutorService threadPool = null;
	private List<TeamClientTask> tasks = new ArrayList<>();

	static
	{
		ConfigRegistration.registerClass("user", TeamClientModule.class);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AAgent.class).removeObserver(this);
		tasks.forEach(task -> task.setActive(false));
		tasks.clear();
		threadPool.shutdownNow();
		try
		{
			Validate.isTrue(threadPool.awaitTermination(TIMEOUT, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
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
		threadPool.submit(() -> Safe.run(task));
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
					log.debug("Restarting TeamClient for " + lastAiInfoFrame.getTeamColor());
					addTask(new TeamClientTask(lastAiInfoFrame.getTeamColor()));
					tasks.remove(task);
					return;
				}
				task.submitNewAiFrame(lastAiInfoFrame);
				return;
			}
		}
		// The team does not yet have a client -> create new
		// But only do this in Simulation or if we are TIGERs
		if (newTaskShouldBeCreated(lastAiInfoFrame))
		{
			log.debug("Creating new TeamClient for " + lastAiInfoFrame.getTeamColor());
			addTask(new TeamClientTask(lastAiInfoFrame.getTeamColor()));
		}
	}


	private boolean newTaskShouldBeCreated(final AIInfoFrame lastAiInfoFrame)
	{
		return SumatraModel.getInstance().isSimulation()
				|| lastAiInfoFrame.getRefereeMsg().getTeamInfo(lastAiInfoFrame.getTeamColor()).getName()
						.equals(tigersTeamName)
				|| connectToAnyTeamName;
	}
}
