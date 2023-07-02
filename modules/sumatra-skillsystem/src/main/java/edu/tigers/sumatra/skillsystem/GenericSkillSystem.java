/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Generic skill system.
 */
public class GenericSkillSystem extends ASkillSystem
		implements IBotManagerObserver, IWorldFrameObserver, ISkillExecutorPostHook
{
	private static final ShapeMapSource SKILL_SHAPE_MAP_SOURCE = ShapeMapSource.of("Skills");

	static
	{
		for (ESkill ec : ESkill.values())
		{
			ConfigRegistration.registerClass("skills", ec.getInstanceableClass().getImpl());
		}
	}

	private final Map<BotID, SkillExecutor> executors = new ConcurrentHashMap<>();
	private final List<ISkillExecutorPostHook> skillExecutorPostHooks = new CopyOnWriteArrayList<>();

	private AWorldPredictor wp;


	private GenericSkillSystem()
	{
		// hidden, but instantiatable by moduli
	}


	/**
	 * Create a dedicated skill system with dummy bots for simulation
	 *
	 * @return a new instance
	 */
	public static GenericSkillSystem forAnalysis()
	{
		final GenericSkillSystem skillSystem = new GenericSkillSystem();
		BotID.getAll().forEach(skillSystem::addSkillExecutor);
		return skillSystem;
	}


	private void addSkillExecutor(final BotID botID)
	{
		SkillExecutor se = new SkillExecutor(botID);
		se.addPostHook(this);
		executors.put(botID, se);
	}


	@Override
	public void initModule()
	{
		SumatraModel.getInstance().getModuleOpt(ABotManager.class).ifPresent(o -> o.addObserver(this));
		BotID.getAll().forEach(this::addSkillExecutor);
	}


	@Override
	public void deinitModule()
	{
		executors.clear();
	}


	@Override
	public void startModule()
	{
		super.startModule();
		executors.values().forEach(e -> e.start(getExecutorService()));

		wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addConsumer(this);
	}


	@Override
	public void stopModule()
	{
		emergencyStop();

		SumatraModel.getInstance().getModuleOpt(ABotManager.class).ifPresent(o -> o.removeObserver(this));

		if (wp != null)
		{
			wp.removeConsumer(this);
		}

		executors.values().forEach(SkillExecutor::stop);
		super.stopModule();
	}


	@Override
	public void execute(final BotID botId, final ISkill skill)
	{
		executors.get(botId).setNewSkill(skill);
	}


	@Override
	public void reset(final BotID botId)
	{
		execute(botId, new IdleSkill());
	}


	@Override
	public void reset(final ETeamColor color)
	{
		executors.keySet().stream()
				.filter(botID -> botID.getTeamColor() == color)
				.forEach(this::reset);
	}


	@Override
	public void emergencyStop()
	{
		resetAll();
	}


	@Override
	public void emergencyStop(ETeamColor teamColor)
	{
		reset(teamColor);
	}


	@Override
	public void onBotAdded(final ABot bot)
	{
		executors.get(bot.getBotId()).setNewBot(bot);
	}


	@Override
	public void onBotRemoved(final ABot bot)
	{
		reset(bot.getBotId());
		executors.get(bot.getBotId()).setNewBot(null);
	}


	private void resetAll()
	{
		executors.keySet().forEach(this::reset);
	}


	@Override
	public List<ISkill> getCurrentSkills(final ETeamColor teamColor)
	{
		return executors.values().stream()
				.filter(e -> e.getBotID().getTeamColor() == teamColor)
				.map(SkillExecutor::getCurrentSkill)
				.filter(ISkill::isAssigned)
				.toList();
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		executors.values().forEach(e -> e.onNewWorldFrame(wFrameWrapper));
		if (SumatraModel.getInstance().isSimulation())
		{
			executors.values().forEach(SkillExecutor::waitUntilWorldFrameProcessed);
		}
	}


	@Override
	public Map<BotID, ShapeMap> process(final WorldFrameWrapper wfw, final ETeamColor teamColor)
	{
		final Map<BotID, ShapeMap> shapeMaps = new HashMap<>();

		executors.entrySet().stream()
				.filter(e -> e.getKey().getTeamColor() == teamColor)
				.forEach(e -> e.getValue().update(wfw, shapeMaps.compute(e.getKey(), (b, v) -> new ShapeMap())));

		return shapeMaps;
	}


	@Override
	public void onClearWorldFrame()
	{
		executors.values().forEach(SkillExecutor::onClearWorldFrame);
		if (wp != null)
		{
			wp.notifyRemoveSourceFromShapeMap(SKILL_SHAPE_MAP_SOURCE);
		}
	}


	@Override
	public void onSkillUpdated(final ABot bot, final long timestamp, final ShapeMap shapeMap)
	{
		notifyCommandSent(bot, timestamp);
		if (wp != null)
		{
			wp.notifyNewShapeMap(timestamp, shapeMap,
					ShapeMapSource.of(bot.getBotId().toString(), SKILL_SHAPE_MAP_SOURCE));
		}
		skillExecutorPostHooks.forEach(hook -> hook.onSkillUpdated(bot, timestamp, shapeMap));
	}


	@Override
	public void onRobotRemoved(final BotID botID)
	{
		if (wp != null)
		{
			wp.notifyRemoveSourceFromShapeMap(ShapeMapSource.of(botID.toString(), SKILL_SHAPE_MAP_SOURCE));
		}
		skillExecutorPostHooks.forEach(hook -> hook.onRobotRemoved(botID));
	}


	@Override
	public void addSkillExecutorPostHook(ISkillExecutorPostHook hook)
	{
		skillExecutorPostHooks.add(hook);
	}


	@Override
	public void removeSkillExecutorPostHook(ISkillExecutorPostHook hook)
	{
		skillExecutorPostHooks.remove(hook);
	}
}
