/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderThread;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IdleSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;


/**
 * Generic skill system.
 * 
 * @author AndreR
 */
public class GenericSkillSystem extends ASkillSystem
{
	private static final Logger				log							= Logger
																								.getLogger(GenericSkillSystem.class.getName());
	
	private final BotManagerObserver			botManagerObserver		= new BotManagerObserver();
	private final SkillExecutorScheduler	skillExecutorScheduler	= new SkillExecutorScheduler();
	private ABotManager							botManager;
	private final PathFinderThread			pathFinderScheduler		= new PathFinderThread();
	
	
	/**
	  * 
	  */
	public GenericSkillSystem()
	{
	}
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public GenericSkillSystem(final SubnodeConfiguration subnodeConfiguration)
	{
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	/**
	 * initialize skill system without starting threads
	 */
	public void init()
	{
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			botManager.addObserver(botManagerObserver);
			for (Map.Entry<BotID, ABot> entry : botManager.getAllBots().entrySet())
			{
				BotID botId = entry.getKey();
				ABot bot = entry.getValue();
				skillExecutorScheduler.addSkillExecutor(botId, new SkillExecutor(bot, this));
				skillExecutorScheduler.addObserver(new ExecutorHandler(botId), botId);
			}
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ABotManager.MODULE_ID + "'!");
		}
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		super.startModule();
		init();
		skillExecutorScheduler.start();
		pathFinderScheduler.startScheduler();
	}
	
	
	/**
	 */
	public void deinit()
	{
		if (botManager != null)
		{
			botManager.removeObserver(botManagerObserver);
			for (BotID botId : botManager.getAllBots().keySet())
			{
				SkillExecutor skillExecutor = skillExecutorScheduler.removeSkillExecutor(botId);
				if (skillExecutor != null)
				{
					skillExecutor.clearObservers();
				}
			}
		}
	}
	
	
	@Override
	public void stopModule()
	{
		super.stopModule();
		emergencyStop();
		
		pathFinderScheduler.stopScheduler();
		
		deinit();
		
		skillExecutorScheduler.stop();
	}
	
	
	@Override
	public void execute(final BotID botId, final ISkill skill)
	{
		skillExecutorScheduler.execute(skill, botId);
		
	}
	
	
	@Override
	public void reset(final BotID botId)
	{
		skillExecutorScheduler.execute(new IdleSkill(), botId);
	}
	
	
	@Override
	public void emergencyStop()
	{
		skillExecutorScheduler.resetAll();
		checkObserversCleared();
	}
	
	
	/**
	 * @return the latestAresData
	 */
	@Override
	public AresData getLatestAresData()
	{
		AresData aresData = new AresData();
		for (ISkill skill : skillExecutorScheduler.getCurrentSkills())
		{
			BotID botID = skill.getBot().getBotID();
			aresData.getPaths().put(botID, skill.getDrawablePath());
			aresData.getLatestPaths().put(botID, skill.getLatestDrawablePath());
			aresData.getNumPaths().put(botID, skill.getNewPathCounter());
			aresData.getSkills().put(botID, skill.getSkillName().name());
		}
		return new AresData(aresData);
	}
	
	
	/**
	 * @return the pathFinderScheduler
	 */
	@Override
	public final PathFinderThread getPathFinderScheduler()
	{
		return pathFinderScheduler;
	}
	
	private class ExecutorHandler implements ISkillExecutorObserver
	{
		private final BotID	botID;
		
		
		/**
		 * @param id
		 */
		public ExecutorHandler(final BotID id)
		{
			botID = id;
		}
		
		
		@Override
		public void onNewCommand(final ACommand command)
		{
			botManager.execute(botID, command);
		}
		
		
		@Override
		public void onNewMatchCommand(final List<ACommand> commands)
		{
			ABot bot = botManager.getAllBots().get(botID);
			if ((bot != null) && (bot.getType() == EBotType.TIGER_V3))
			{
				((TigerBotV3) bot).executeMatchCmd(commands);
			}
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill)
		{
			log.trace("Started Skill " + skill.getSkillName() + " on Bot " + botID);
			notifySkillStarted(skill, botID);
		}
		
		
		@Override
		public void onSkillCompletedItself(final ISkill skill)
		{
			notifySkillCompleted(skill, botID);
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill)
		{
			log.trace("Completed Skill " + skill.getSkillName() + " on Bot " + botID);
		}
		
		
		@Override
		public void onSkillProcessed(final ISkill skill)
		{
		}
	}
	
	private class BotManagerObserver implements IBotManagerObserver
	{
		
		
		@Override
		public void onBotConnectionChanged(final ABot bot)
		{
		}
		
		
		@Override
		public void onBotAdded(final ABot bot)
		{
			SkillExecutor skillExecutor = new SkillExecutor(bot, GenericSkillSystem.this);
			skillExecutor.addObserver(new ExecutorHandler(bot.getBotID()));
			skillExecutorScheduler.addSkillExecutor(bot.getBotID(), skillExecutor);
		}
		
		
		@Override
		public void onBotRemoved(final ABot bot)
		{
			SkillExecutor skillExecutor = skillExecutorScheduler.removeSkillExecutor(bot.getBotID());
			if (skillExecutor != null)
			{
				skillExecutor.clearObservers();
			}
		}
		
		
		@Override
		public void onBotIdChanged(final BotID oldId, final BotID newId)
		{
			SkillExecutor skillExecutor = skillExecutorScheduler.removeSkillExecutor(oldId);
			skillExecutor.clearObservers();
			SkillExecutor newSkillExecutor = new SkillExecutor(skillExecutor.getBot(), GenericSkillSystem.this);
			newSkillExecutor.addObserver(new ExecutorHandler(newId));
			skillExecutorScheduler.addSkillExecutor(newId, newSkillExecutor);
		}
	}
	
	
	/**
	 * @return the skillExecutorScheduler
	 */
	public final SkillExecutorScheduler getSkillExecutorScheduler()
	{
		return skillExecutorScheduler;
	}
}
