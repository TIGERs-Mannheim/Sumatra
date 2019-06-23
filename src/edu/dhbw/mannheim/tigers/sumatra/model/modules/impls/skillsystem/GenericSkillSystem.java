/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ImmediateStopSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;


/**
 * Generic skill system.
 * 
 * @author AndreR
 */
public class GenericSkillSystem extends ASkillSystem implements IBotManagerObserver

{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log				= Logger.getLogger(GenericSkillSystem.class.getName());
	
	private ABotManager									botManager		= null;
	private AWorldPredictor								worldpredictor	= null;
	
	private final Map<BotID, SkillExecutorInfo>	skillExecutors	= new HashMap<BotID, SkillExecutorInfo>();
	/** [us] */
	private final long									minPeriod;
	
	private final Sisyphus								sisyphus			= new Sisyphus();
	
	private static class SkillExecutorInfo
	{
		/** */
		public SkillExecutor					executor;
		/** */
		public ExecutorHandler				handler;
		/** */
		public ScheduledExecutorService	service;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public GenericSkillSystem()
	{
		minPeriod = 20000;
	}
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public GenericSkillSystem(final SubnodeConfiguration subnodeConfiguration)
	{
		minPeriod = subnodeConfiguration.getLong("minProcessingPeriod");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void startModule()
	{
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			worldpredictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ABotManager.MODULE_ID + "'!");
			return;
		}
		
		botManager.addObserver(this);
		
		// Finish initialization
		final Map<BotID, ABot> botList = botManager.getAllBots();
		
		// create bot skill executors
		for (final BotID botId : botList.keySet())
		{
			addExecutor(botId);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		if (botManager != null)
		{
			botManager.removeObserver(this);
		}
		
		emergencyStop();
		
		for (final SkillExecutorInfo info : skillExecutors.values())
		{
			removeExecutor(info.handler.getBotID());
		}
		
		sisyphus.stopAllPathPlanning();
		
		skillExecutors.clear();
	}
	
	
	@Override
	public void deinitModule()
	{
		super.deinitModule();
		
		botManager = null;
		worldpredictor = null;
	}
	
	
	@Override
	public void execute(final BotID botId, final ISkill skill)
	{
		final SkillExecutorInfo info = skillExecutors.get(botId);
		if ((info == null) || (info.executor == null))
		{
			log.warn("invalid botID: " + botId);
			return;
		}
		
		info.executor.setSkill(skill);
		
	}
	
	
	@Override
	public void onBotIdChanged(final BotID oldId, final BotID newId)
	{
		final SkillExecutorInfo info = skillExecutors.get(oldId);
		info.handler.setBotId(newId);
		
		skillExecutors.put(newId, info);
		skillExecutors.remove(oldId);
	}
	
	
	@Override
	public void onBotAdded(final ABot bot)
	{
		addExecutor(bot.getBotID());
	}
	
	
	@Override
	public void onBotRemoved(final ABot bot)
	{
		removeExecutor(bot.getBotID());
	}
	
	
	private void addExecutor(final BotID botID)
	{
		final ABot bot = botManager.getAllBots().get(botID);
		final SkillExecutor executor = new SkillExecutor(bot, minPeriod, this);
		final ExecutorHandler handler = new ExecutorHandler(botID);
		
		executor.addObserver(handler);
		worldpredictor.addWorldFrameConsumer(executor);
		
		final SkillExecutorInfo info = new SkillExecutorInfo();
		info.executor = executor;
		info.handler = handler;
		info.service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SkillExecutor bot "
				+ botID.getNumber() + " " + botID.getTeamColor()));
		skillExecutors.put(botID, info);
		
		info.service.scheduleAtFixedRate(executor, 0, minPeriod, TimeUnit.MICROSECONDS);
	}
	
	
	private void removeExecutor(final BotID botID)
	{
		final SkillExecutorInfo info = skillExecutors.get(botID);
		if (info == null)
		{
			log.warn("Invalid executor id: " + botID);
			return;
		}
		
		worldpredictor.removeWorldFrameConsumer(info.executor);
		info.service.shutdown();
		try
		{
			info.service.awaitTermination(minPeriod, TimeUnit.MICROSECONDS);
		} catch (InterruptedException err)
		{
			log.error("Interrupted.", err);
		}
		info.executor.removeObserver(info.handler);
	}
	
	private class ExecutorHandler implements ISkillExecutorObserver
	{
		private BotID	botID;
		
		
		/**
		 * @param id
		 */
		public ExecutorHandler(final BotID id)
		{
			botID = id;
		}
		
		
		/**
		 * @param botID
		 */
		public void setBotId(final BotID botID)
		{
			this.botID = botID;
		}
		
		
		/**
		 * @return
		 */
		public BotID getBotID()
		{
			return botID;
		}
		
		
		@Override
		public void onNewCommand(final ACommand command)
		{
			botManager.execute(botID, command);
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill)
		{
			log.trace("Started Skill " + skill.getSkillName() + " on Bot " + botID);
			notifySkillStarted(skill, botID);
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill)
		{
			log.trace("Completed Skill " + skill.getSkillName() + " on Bot " + botID);
			notifySkillCompleted(skill, botID);
		}
	}
	
	
	@Override
	public void emergencyStop()
	{
		if (botManager != null)
		{
			for (final ABot bot : botManager.getAllBots().values())
			{
				execute(bot.getBotID(), new ImmediateStopSkill());
			}
		}
		checkObserversCleared();
	}
	
	
	@Override
	public void onBotConnectionChanged(final ABot bot)
	{
	}
	
	
	@Override
	public Sisyphus getSisyphus()
	{
		return sisyphus;
	}
}
