/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlResetCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IdleSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Execute skills for a single bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SkillExecutor
{
	private static final Logger						log									= Logger.getLogger(SkillExecutor.class
																												.getName());
	private static final int							MAX_SKILL_PROCESSING_TIME_US	= 1000;
	private ISkill											currentSkill						= new IdleSkill();
	private ISkill											newSkill								= null;
	private final ABot									bot;
	private final ASkillSystem							skillSystem;
	private final List<ISkillExecutorObserver>	observers							= new CopyOnWriteArrayList<ISkillExecutorObserver>();
	
	private final Object									newSkillSync						= new Object();
	
	private int												dtPeaks								= 0;
	
	private boolean										resetSent							= false;
	
	@Configurable(comment = "Pause skills if the ball is outside of field")
	private static boolean								pauseSkillsIfBallOutside		= false;
	
	
	/**
	 * @param bot
	 * @param skillSystem
	 */
	public SkillExecutor(final ABot bot, final ASkillSystem skillSystem)
	{
		this.bot = bot;
		this.skillSystem = skillSystem;
		currentSkill.setBot(bot);
	}
	
	
	/**
	 * @param wf
	 */
	public void update(final WorldFrameWrapper wf)
	{
		long start = SumatraClock.nanoTime();
		
		if (bot.getNetworkState() != ENetworkState.ONLINE)
		{
			return;
		}
		List<ACommand> cmds = new ArrayList<ACommand>(5);
		ISkill nextSkill;
		synchronized (newSkillSync)
		{
			nextSkill = newSkill;
			newSkill = null;
		}
		processNewSkill(nextSkill, wf, cmds);
		executeSave(() -> currentSkill.update(wf == null ? null : wf.getWorldFrame(bot.getColor())));
		processSkillCompleted(cmds);
		executeSave(() -> currentSkill.calcActions(cmds));
		notifySkillProcessed(currentSkill);
		if (bot.getType() == EBotType.TIGER_V3)
		{
			if (pauseSkillsIfBallOutside)
			{
				IVector2 ballPos = wf.getSimpleWorldFrame().getBall().getPos();
				if (!AIConfig.getGeometry().getField().isPointInShape(ballPos, 0))
				{
					cmds.clear();
					if (!resetSent)
					{
						cmds.add(new TigerCtrlResetCommand());
						log.info("Skill " + currentSkill.getSkillName() + " paused, because ball outside field.");
						resetSent = true;
					}
				} else
				{
					resetSent = false;
				}
			}
			executeSave(() -> notifyNewMatchCommands(cmds));
		} else
		{
			enqueueCmds(cmds);
		}
		
		if (!SumatraModel.getInstance().isProductive() && (bot.getType() != EBotType.SUMATRA))
		{
			long diff = SumatraClock.nanoTime() - start;
			if (diff > (MAX_SKILL_PROCESSING_TIME_US * 1000))
			{
				dtPeaks++;
				if (dtPeaks > 5)
				{
					log.warn("Skill " + currentSkill.getSkillName().name() + " needed " + (diff / 1000)
							+ "us! Max allowed is " + MAX_SKILL_PROCESSING_TIME_US + "us");
					dtPeaks = 0;
				}
			} else
			{
				dtPeaks = 0;
			}
		}
	}
	
	
	/**
	 * @param skill
	 */
	public void setNewSkill(final ISkill skill)
	{
		synchronized (newSkillSync)
		{
			newSkill = skill;
		}
	}
	
	
	private void processNewSkill(final ISkill skill, final WorldFrameWrapper wf, final List<ACommand> cmds)
	{
		if (skill == null)
		{
			return;
		}
		
		executeSave(() -> currentSkill.calcExitActions(cmds));
		executeSave(() -> notifySkillCompleted(currentSkill));
		final ISkill nextSkill;
		if (skill.needsVision() && ((wf == null) || (wf.getSimpleWorldFrame().getBot(bot.getBotID()) == null)))
		{
			nextSkill = new IdleSkill();
		} else
		{
			nextSkill = skill;
		}
		nextSkill.setBot(bot);
		nextSkill.setSkillSystem(skillSystem);
		executeSave(() -> nextSkill.update(wf == null ? null : wf.getWorldFrame(bot.getColor())));
		executeSave(() -> nextSkill.calcEntryActions(cmds));
		executeSave(() -> notifySkillStarted(nextSkill));
		currentSkill = nextSkill;
	}
	
	
	private void processSkillCompleted(final List<ACommand> cmds)
	{
		if (currentSkill.isComplete())
		{
			notifySkillCompletedItself(currentSkill);
			processNewSkill(new IdleSkill(), null, cmds);
		}
	}
	
	
	private void executeSave(final Runnable run)
	{
		try
		{
			run.run();
		} catch (Throwable err)
		{
			log.error("Exception in SkillExecutor " + currentSkill.getBot().getBotID(), err);
		}
	}
	
	
	private void enqueueCmds(final List<ACommand> cmds)
	{
		for (ACommand cmd : cmds)
		{
			notifyNewCommand(cmd);
		}
	}
	
	
	private void notifyNewCommand(final ACommand cmd)
	{
		for (final ISkillExecutorObserver observer : observers)
		{
			observer.onNewCommand(cmd);
		}
	}
	
	
	private void notifyNewMatchCommands(final List<ACommand> cmds)
	{
		for (final ISkillExecutorObserver observer : observers)
		{
			observer.onNewMatchCommand(cmds);
		}
	}
	
	
	private void notifySkillCompleted(final ISkill skill)
	{
		for (final ISkillExecutorObserver observer : observers)
		{
			observer.onSkillCompleted(skill);
		}
	}
	
	
	private void notifySkillCompletedItself(final ISkill skill)
	{
		for (final ISkillExecutorObserver observer : observers)
		{
			observer.onSkillCompletedItself(skill);
		}
	}
	
	
	private void notifySkillStarted(final ISkill skill)
	{
		for (final ISkillExecutorObserver observer : observers)
		{
			observer.onSkillStarted(skill);
		}
	}
	
	
	private void notifySkillProcessed(final ISkill skill)
	{
		for (final ISkillExecutorObserver observer : observers)
		{
			observer.onSkillProcessed(skill);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISkillExecutorObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISkillExecutorObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * 
	 */
	public void clearObservers()
	{
		observers.clear();
	}
	
	
	/**
	 * @return the currentSkill
	 */
	public ISkill getCurrentSkill()
	{
		return currentSkill;
	}
	
	
	/**
	 * @return the bot
	 */
	public ABot getBot()
	{
		return bot;
	}
	
}
