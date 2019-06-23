/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.DummyBot;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Generic skill system.
 *
 * @author AndreR
 */
public class GenericSkillSystem extends ASkillSystem
		implements IBotManagerObserver, IWorldFrameObserver, ISkillExecuterPostHook
{
	private static final Logger log = Logger
			.getLogger(GenericSkillSystem.class.getName());
	
	static
	{
		for (ESkill ec : ESkill.values())
		{
			ConfigRegistration.registerClass("skills", ec.getInstanceableClass().getImpl());
		}
	}
	
	private final Map<BotID, SkillExecutor> executors = new ConcurrentHashMap<>(
			12);
	private final Object sync = new Object();
	private ExecutorService service = null;
	
	
	/**
	 * Create a dedicated skill system with dummy bots for simulation
	 */
	public GenericSkillSystem()
	{
		for (int i = 0; i < AObjectID.BOT_ID_MAX; i++)
		{
			addSkillExecutor(new DummyBot(BotID.createBotId(i, ETeamColor.YELLOW)));
			addSkillExecutor(new DummyBot(BotID.createBotId(i, ETeamColor.BLUE)));
		}
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
		// empty
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		service = Executors.newCachedThreadPool(new NamedThreadFactory("SkillExecutor"));
		
		try
		{
			ABotManager botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			botManager.addObserver(this);
			for (Map.Entry<BotID, ABot> entry : botManager.getAllBots().entrySet())
			{
				ABot bot = entry.getValue();
				addSkillExecutor(bot);
			}
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ABotManager.MODULE_ID + "'!", err);
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find worldpredictor", err);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		emergencyStop();
		
		try
		{
			ABotManager botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			botManager.removeObserver(this);
			for (BotID botId : botManager.getAllBots().keySet())
			{
				removeSkillExecutor(botId);
			}
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ABotManager.MODULE_ID + "'!", err);
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find worldpredictor", err);
		}
		service.shutdown();
	}
	
	
	@Override
	public void execute(final BotID botId, final ISkill skill)
	{
		SkillExecutor se = executors.get(botId);
		if (se != null)
		{
			se.setNewSkill(skill);
		}
	}
	
	
	@Override
	public void reset(final BotID botId)
	{
		execute(botId, new IdleSkill());
	}
	
	
	@Override
	public void reset(final ETeamColor color)
	{
		for (SkillExecutor se : executors.values())
		{
			if (se.getBot().getBotId().getTeamColor() == color)
			{
				se.setNewSkill(new IdleSkill());
			}
		}
	}
	
	
	@Override
	public void emergencyStop()
	{
		resetAll();
	}
	
	
	@Override
	public void emergencyStop(ETeamColor teamColor)
	{
		resetAll(teamColor);
	}
	
	
	@Override
	public void onBotAdded(final ABot bot)
	{
		addSkillExecutor(bot);
	}
	
	
	@Override
	public void onBotRemoved(final ABot bot)
	{
		removeSkillExecutor(bot.getBotId());
	}
	
	
	/**
	 * Reset all bots to idle skill
	 */
	private void resetAll()
	{
		for (SkillExecutor se : executors.values())
		{
			se.setNewSkill(new IdleSkill());
		}
	}
	
	
	/**
	 * Reset all bots of certain color to idle skill
	 */
	private void resetAll(ETeamColor teamColor)
	{
		for (SkillExecutor se : executors.values())
		{
			if (se.getBot().getBotId().getTeamColor() == teamColor)
			{
				se.setNewSkill(new IdleSkill());
			}
		}
	}
	
	
	@Override
	public List<ISkill> getCurrentSkills(final ETeamColor teamColor)
	{
		List<ISkill> skills = new ArrayList<>(executors.size());
		for (SkillExecutor se : executors.values())
		{
			if (se.getBot().getBotId().getTeamColor() == teamColor)
			{
				skills.add(se.getCurrentSkill());
			}
		}
		return skills;
	}
	
	
	/**
	 * @param bot
	 */
	private void addSkillExecutor(final ABot bot)
	{
		SkillExecutor se = new SkillExecutor(bot);
		se.addPostHook(this);
		synchronized (sync)
		{
			SkillExecutor oldSe = executors.put(bot.getBotId(), se);
			if (oldSe != null)
			{
				log.warn("Added new skill excutor, but there was one already registered for bot " + bot, new Exception());
				oldSe.stop();
			}
		}
		
		if (service != null)
		{
			se.start(service);
		}
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	private SkillExecutor removeSkillExecutor(final BotID botId)
	{
		synchronized (sync)
		{
			SkillExecutor se = executors.remove(botId);
			se.removePostHook(this);
			se.stop();
			return se;
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		for (SkillExecutor se : executors.values())
		{
			se.onNewWorldFrame(wFrameWrapper);
		}
	}
	
	
	@Override
	public void process(final WorldFrameWrapper wfw)
	{
		for (SkillExecutor se : executors.values())
		{
			se.update(wfw, wfw.getSimpleWorldFrame().getTimestamp());
		}
	}
	
	
	@Override
	public void onClearWorldFrame()
	{
		for (SkillExecutor se : executors.values())
		{
			se.onClearWorldFrame();
		}
	}
	
	
	@Override
	public void onCommandSent(final ABot bot, final long timestamp)
	{
		notifyCommandSent(bot, timestamp);
	}
	
	
	@Override
	public void setProcessAllWorldFrames(final boolean processAllWorldFrames)
	{
		for (SkillExecutor se : executors.values())
		{
			se.setProcessAllWorldFrames(processAllWorldFrames);
		}
	}
}