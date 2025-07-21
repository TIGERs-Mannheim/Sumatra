/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.botmanager.BotManager;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.timer.ATimer;
import edu.tigers.sumatra.timer.ITimer;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


/**
 * The AI manager is responsible for executing the AI in its own thread in the desired speed
 */
public class AiManager
{
	private static final Logger log = LogManager.getLogger(AiManager.class.getName());

	private final BlockingDeque<WorldFrameWrapper> freshWorldFrames = new LinkedBlockingDeque<>(1);
	private final AiProcessor aiProcessor = new AiProcessor();
	private final AAgent agent;
	private final Ai ai;
	private final String aiName;
	private final ShapeMapSource shapeMapSource;

	private ExecutorService executor;
	private AWorldPredictor wp;
	private ITimer timer;


	/**
	 * @param agent
	 * @param aiTeam
	 * @param skillSystem
	 */
	public AiManager(final AAgent agent, final EAiTeam aiTeam, final ASkillSystem skillSystem)
	{
		this.agent = agent;
		ai = new Ai(aiTeam, skillSystem);
		aiName = "AI_" + ai.getAiTeam();
		shapeMapSource = ShapeMapSource.of(aiTeam.getTeamColor().name(), ShapeMapSource.of("AI"));
	}


	/**
	 * Start the AI
	 */
	public void start()
	{
		timer = SumatraModel.getInstance().getModuleOpt(ATimer.class).orElse(null);
		wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);

		SumatraModel.getInstance().getModuleOpt(BotManager.class).ifPresent(
				b -> b.addAllocatedBots(getClass().getCanonicalName(), BotID.getAll(ai.getAiTeam().getTeamColor()))
		);

		assert executor == null;
		executor = Executors.newSingleThreadExecutor();
		executor.execute(aiProcessor);
	}


	/**
	 * Stop the AI
	 */
	public void stop()
	{
		SumatraModel.getInstance().getModuleOpt(BotManager.class).ifPresent(
				b -> b.removeAllocatedBots(getClass().getCanonicalName(), BotID.getAll(ai.getAiTeam().getTeamColor()))
		);

		aiProcessor.running = false;
		executor.shutdown();

		try
		{
			Validate.isTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}

		freshWorldFrames.clear();
		executor = null;
		wp = null;
		timer = null;
	}


	/**
	 * True if this AI is running.
	 *
	 * @return
	 */
	public boolean isRunning()
	{
		return executor != null;
	}


	/**
	 * @param mode
	 */
	public void changeMode(final EAIControlState mode)
	{
		ai.changeMode(mode);
	}


	public BlockingDeque<WorldFrameWrapper> getFreshWorldFrames()
	{
		return freshWorldFrames;
	}


	public Ai getAi()
	{
		return ai;
	}


	private class AiProcessor implements Runnable
	{
		private static final int RUN_EVERY_N_TH_FRAME = 1;

		boolean running = true;
		private int skipCounter = 0;


		@Override
		public void run()
		{
			Thread.currentThread().setName(aiName);
			ai.start();
			while (running)
			{
				try
				{
					process();
				} catch (InterruptedException err)
				{
					Thread.currentThread().interrupt();
				} catch (Throwable err)
				{
					log.error("Serious error occurred in AI processor.", err);
				}
			}
			ai.stop();
			agent.notifyAIStopped(ai.getAiTeam());
			wp.notifyRemoveSourceFromShapeMap(shapeMapSource);
			ThreadContext.remove("wfTs");
			ThreadContext.remove("wfId");
		}


		private void process() throws InterruptedException
		{
			WorldFrameWrapper wfw = freshWorldFrames.pollLast(15, TimeUnit.MILLISECONDS);
			if (wfw == null)
			{
				return;
			}

			long tNow = wfw.getSimpleWorldFrame().getTimestamp();
			long id = wfw.getSimpleWorldFrame().getFrameNumber();
			ThreadContext.put("wfTs", String.valueOf(tNow));
			ThreadContext.put("wfId", String.valueOf(id));

			skipCounter++;
			if (skipCounter < RUN_EVERY_N_TH_FRAME)
			{
				return;
			}
			skipCounter = 0;

			if (timer != null)
			{
				timer.start(aiName, id);
			}

			AIInfoFrame frame = ai.processWorldFrame(wfw);
			if (frame != null)
			{
				try
				{
					agent.notifyNewAIInfoFrame(frame);
					VisualizationFrame visFrame = new VisualizationFrame(frame);
					agent.notifyNewAIInfoFrameVisualize(visFrame);
					frame.getShapeMap().setInverted(wfw.getWorldFrame(ai.getAiTeam()).isInverted());
					wp.notifyNewShapeMap(tNow, frame.getShapeMap(), shapeMapSource);
				} catch (Throwable err)
				{
					log.error("Error during AI frame publishing", err);
				}
			}

			if (timer != null)
			{
				timer.stop(aiName, id);
			}
		}
	}
}
