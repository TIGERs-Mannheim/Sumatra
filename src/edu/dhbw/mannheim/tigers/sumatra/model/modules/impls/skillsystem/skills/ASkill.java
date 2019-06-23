/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * Andre Ryll,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillEventObserver;


/**
 * This is the base class for every skill, which provides subclasses with the newest data and handles their lifecycle
 * 
 * @author Ryan, Gero
 * 
 */
public abstract class ASkill
{
	/** [ns] */
	private long									period;
	
	private boolean								completed	= false;
	
	private final ESkillName					skillName;
	
	private WorldFrame							worldFrame	= null;
	
	private TrackedTigerBot						bot			= null;
	
	private final Set<ISkillEventObserver>	observers	= new HashSet<ISkillEventObserver>();
	
	
	/**
	 * @param skill skillName
	 */
	public ASkill(ESkillName skill)
	{
		skillName = skill;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ESkillName getSkillName()
	{
		return skillName;
	}
	
	
	protected WorldFrame getWorldFrame()
	{
		return worldFrame;
	}
	
	
	/**
	 * @param newWorldFrame
	 */
	public void setWorldFrame(WorldFrame newWorldFrame)
	{
		worldFrame = newWorldFrame;
	}
	
	
	/**
	 * @param period [ns]
	 */
	public void setPeriod(long period)
	{
		this.period = period;
	}
	
	
	/**
	 * @return period [ns]
	 */
	public long getPeriod()
	{
		return period;
	}
	
	
	/**
	 * Called from derived user classes to signal the skill system to remove this skill
	 */
	protected void complete()
	{
		completed = true;
	}
	
	
	/**
	 * @return
	 */
	public final boolean isComplete()
	{
		return completed;
	}
	
	
	/**
	 * 
	 * @param bot
	 * @return
	 */
	public final List<ACommand> calcActions(TrackedTigerBot bot)
	{
		if (worldFrame == null)
		{
			return new ArrayList<ACommand>();
		}
		this.bot = bot;
		
		return calcActions(bot, new ArrayList<ACommand>());
	}
	
	
	/**
	 * 
	 * @param bot
	 * @param cmds
	 * @return
	 */
	public abstract List<ACommand> calcActions(TrackedTigerBot bot, List<ACommand> cmds);
	
	
	/**
	 * 
	 * @param bot
	 * @return
	 */
	public final List<ACommand> calcExitActions(TrackedTigerBot bot)
	{
		if (worldFrame == null)
		{
			return new ArrayList<ACommand>();
		}
		this.bot = bot;
		
		List<ACommand> cmds = calcExitActions(bot, new ArrayList<ACommand>());
		observers.clear();
		
		return cmds;
	}
	
	
	/**
	 * Should be overridden by subclasses!!!
	 * @param bot
	 * @param cmds
	 * @return
	 */
	public List<ACommand> calcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		return cmds;
	}
	
	
	/**
	 * 
	 * @param bot
	 * @return
	 */
	public final List<ACommand> calcEntryActions(TrackedTigerBot bot)
	{
		if (worldFrame == null)
		{
			return new ArrayList<ACommand>();
		}
		this.bot = bot;
		
		return calcEntryActions(bot, new ArrayList<ACommand>());
	}
	
	
	/**
	 * Should be overridden by subclasses!!!
	 * @param bot
	 * @param cmds
	 * @return
	 */
	public List<ACommand> calcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		return cmds;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(ISkillEventObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(ISkillEventObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected void notifyEvent(Enum<? extends Enum<?>> event)
	{
		synchronized (observers)
		{
			for (ISkillEventObserver observer : observers)
			{
				observer.onNewEvent(event);
			}
		}
	}
	
	
	@Override
	public String toString()
	{
		return getSkillName().toString();
	}
	
	
	/**
	 * @return the bot
	 */
	public final TrackedTigerBot getBot()
	{
		return bot;
	}
}
