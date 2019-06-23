/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * The base class for every implementation of a skill system
 * 
 * @author Gero
 */
public abstract class ASkillSystem extends AModule
{
	/** */
	public static final String						MODULE_TYPE	= "AMoveSystem";
	/** */
	public static final String						MODULE_ID	= "skillsystem";
	
	private final List<ISkillSystemObserver>	observers	= new CopyOnWriteArrayList<>();
	
	
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
	 * @return
	 * @param teamColor
	 */
	public abstract List<ISkill> getCurrentSkills(final ETeamColor teamColor);
	
	
	/**
	 * @param wfw
	 */
	public abstract void process(final WorldFrameWrapper wfw);
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
	}
	
	
	@Override
	public void stopModule()
	{
	}
	
	
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
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISkillSystemObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISkillSystemObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * @param processAllWorldFrames
	 */
	public abstract void setProcessAllWorldFrames(final boolean processAllWorldFrames);
}
