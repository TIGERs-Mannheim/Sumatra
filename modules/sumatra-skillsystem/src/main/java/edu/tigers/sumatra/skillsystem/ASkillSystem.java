package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The base class for every implementation of a skill system
 */
public abstract class ASkillSystem extends AModule
{
	protected final Map<BotID, SkillExecutor> executors = new ConcurrentHashMap<>();
	private final List<ISkillSystemObserver> observers = new CopyOnWriteArrayList<>();


	@Override
	public void initModule()
	{
		BotID.getAll().forEach(botID -> executors.put(botID, new SkillExecutor(botID)));
	}


	@Override
	public void deinitModule()
	{
		executors.clear();
	}


	/**
	 * @param botId the bot id
	 * @return the skill executor for the given bot
	 */
	public SkillExecutor getExecutor(BotID botId)
	{
		return executors.get(botId);
	}


	/**
	 * @param botId
	 * @param skill
	 */
	public abstract void execute(BotID botId, ISkill skill);


	/**
	 * @param botId
	 */
	public abstract void reset(final BotID botId);


	/**
	 * @param color
	 */
	public abstract void reset(ETeamColor color);


	/**
	 * @param teamColor
	 * @return
	 */
	public abstract List<ISkill> getCurrentSkills(final ETeamColor teamColor);


	/**
	 * Process all skill executors synchronously.
	 * This is used for replay recalc only.
	 *
	 * @param wfw
	 * @param teamColor
	 * @return
	 */
	public abstract Map<BotID, ShapeMap> process(final WorldFrameWrapper wfw, final ETeamColor teamColor);


	/**
	 * Stop all bots as fast as possible
	 */
	public abstract void emergencyStop();


	/**
	 * Stop all bots of certain color as fast as possible
	 *
	 * @param teamColor the color of the team to send the emergency to
	 */
	public abstract void emergencyStop(ETeamColor teamColor);


	protected void notifyCommandSent(final ABot bot, final long timestamp)
	{
		for (ISkillSystemObserver observer : observers)
		{
			observer.onCommandSent(bot, timestamp);
		}
	}


	public void addObserver(final ISkillSystemObserver observer)
	{
		observers.add(observer);
	}


	public void removeObserver(final ISkillSystemObserver observer)
	{
		observers.remove(observer);
	}
}
