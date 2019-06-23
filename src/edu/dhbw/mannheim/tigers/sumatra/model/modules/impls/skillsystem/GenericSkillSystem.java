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

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.DribbleBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ImmediateDisarm;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ImmediateStop;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IEmergencyStop;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * Generic skill system.
 * 
 * @author AndreR
 * 
 */
public class GenericSkillSystem extends ASkillSystem implements IWorldPredictorObserver, ISkillWorldInfoProvider,
		IBotManagerObserver, IEmergencyStop
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger										log					= Logger.getLogger(getClass());
	
	private final SumatraModel								model					= SumatraModel.getInstance();
	
	protected ABotManager									botManager			= null;
	private AWorldPredictor									wp						= null;
	protected ITimer											timer					= null;
	
	private final Map<Integer, SkillExecutorInfo>	skillExecutors		= new HashMap<Integer, SkillExecutorInfo>();
	/** [us] */
	private final long										minPeriod;
	
	private volatile WorldFrame							currentSituation	= null;
	private volatile Sisyphus								sisyphus				= null;
	
	private static class SkillExecutorInfo
	{
		public SkillExecutor		executor;
		public ExecutorHandler	handler;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public GenericSkillSystem(SubnodeConfiguration subnodeConfiguration)
	{
		this.minPeriod = subnodeConfiguration.getLong("minProcessingPeriod");
		// this.numThreads = subnodeConfiguration.getInt("numThreads", 1);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		log.info("Initialized.");
	}
	

	@Override
	public void startModule()
	{
		try
		{
			botManager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
			
			wp = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
			wp.addFunctionalObserver(this);
			
		} catch (ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ABotManager.MODULE_ID + "'!");
			return;
		}
		
		botManager.addObserver(this);
		
		// Finish initialization
		Map<Integer, ABot> botList = botManager.getAllBots();
		
		// create bot skill executors
		for (int botId : botList.keySet())
		{
			addExecutor(botId);
			
			log.debug("Created skill executor for bot: " + botId);
		}
		
		log.info("Started.");
	}
	

	@Override
	public void stopModule()
	{
		if (botManager != null)
		{
			botManager.removeObserver(this);
		}
		
		for (SkillExecutorInfo info : skillExecutors.values())
		{
			removeExecutor(info.handler.getBotId());
		}
		
		if (wp != null)
		{
			wp.removeFunctionalObserver(this);
		}
		
		skillExecutors.clear();
		
		log.info("Stopped.");
	}
	

	@Override
	public void deinitModule()
	{
		botManager = null;
		wp = null;
		
		synchronized (observers)
		{
			observers.clear();
		}
		
		log.info("Deinitialized.");
	}
	

	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		currentSituation = wf;
	}
	

	@Override
	public WorldFrame getCurrentWorldFrame()
	{
		return currentSituation;
	}
	

	@Override
	public void execute(int botId, ASkill skill)
	{
		SkillExecutorInfo info = skillExecutors.get(botId);
		if (info == null || info.executor == null)
		{
			log.warn("invalid botID: " + botId);
			return;
		}
		
		info.executor.setSkill(botId, skill, this);
		
		time(currentSituation);
	}
	

	@Override
	public void execute(int botId, SkillFacade facade)
	{
		SkillExecutorInfo info = skillExecutors.get(botId);
		if (info == null || info.executor == null)
		{
			log.warn("invalid botID: " + botId);
			return;
		}
		info.executor.setSkills(botId, facade, this);
		
		time(currentSituation);
	}
	

	private void time(WorldFrame wFrame)
	{
		if (timer != null && wFrame != null)
		{
			timer.time(wFrame.id);
		}
	}
	

	@Override
	public void onBotIdChanged(int oldId, int newId)
	{
		SkillExecutorInfo info = skillExecutors.get(oldId);
		info.handler.setBotId(newId);
		
		skillExecutors.put(newId, info);
		skillExecutors.remove(oldId);
	}
	

	@Override
	public void onBotAdded(ABot bot)
	{
		addExecutor(bot.getBotId());
		
		log.debug("Created skill executor for bot: " + bot.getName());
	}
	

	@Override
	public void onBotRemoved(ABot bot)
	{
		removeExecutor(bot.getBotId());
		
		log.debug("Removed skill executor for bot: " + bot.getName());
	}
	

	private void addExecutor(int botId)
	{
		SkillExecutor executor = new SkillExecutor(minPeriod);
		executor.setName("SkillExecutor " + botId);
		ExecutorHandler handler = new ExecutorHandler(botId);
		
		executor.addObserver(handler);
		wp.addFunctionalObserver(executor);
		
		SkillExecutorInfo info = new SkillExecutorInfo();
		info.executor = executor;
		info.handler = handler;
		skillExecutors.put(botId, info);
		
		executor.start();
	}
	

	private void removeExecutor(int botId)
	{
		SkillExecutorInfo info = skillExecutors.get(botId);
		if (info == null)
		{
			log.warn("Invalid executor id: " + botId);
			return;
		}
		
		wp.removeFunctionalObserver(info.executor);
		info.executor.terminate();
		info.executor.removeObserver(info.handler);
	}
	
	private class ExecutorHandler implements ISkillExecutorObserver
	{
		private int	botId;
		
		
		public ExecutorHandler(int id)
		{
			botId = id;
		}
		

		public void setBotId(int botId)
		{
			this.botId = botId;
		}
		

		public int getBotId()
		{
			return botId;
		}
		

		@Override
		public void onNewCommand(ACommand command)
		{
			botManager.execute(botId, command);
		}
		

		@Override
		public void onSkillStarted(ASkill skill)
		{
			notifySkillStarted(skill, botId);
			
			log.debug("Started Skill " + skill.getSkillName() + " on Bot " + botId);
		}
		

		@Override
		public void onSkillCompleted(ASkill skill)
		{
			notifySkillCompleted(skill, botId);
			
			log.debug("Completed Skill " + skill.getSkillName() + " on Bot " + botId);
		}
	}
	
	
	/**
	 * Sets the default {@link Sisyphus}-instance all skills may access
	 */
	public void setSisyphus(Sisyphus sisyphus)
	{
		this.sisyphus = sisyphus;
	}
	

	@Override
	public Sisyphus getSisyphus()
	{
		return sisyphus;
	}
	

	@Override
	public void emergencyStop()
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			execute(bot.getBotId(), new ImmediateStop());
			execute(bot.getBotId(), new ImmediateDisarm());
			execute(bot.getBotId(), new DribbleBall(0));
		}
	}
}
