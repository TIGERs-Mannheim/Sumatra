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
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillEventObserver;


/**
 * This is the base class for every skill, which provides subclasses with the newest data and handles their lifecycle
 * 
 * @author Ryan, Gero
 */
public abstract class ASkill implements ISkill
{
	private static final Logger				log				= Logger.getLogger(ASkill.class.getName());
	
	/** [ns] */
	private long									dt;
	
	private boolean								completed		= false;
	
	private final ESkillName					skillName;
	
	private WorldFrame							worldFrame		= null;
	
	private TrackedTigerBot						tBot				= null;
	private ABot									bot				= null;
	
	private final Set<ISkillEventObserver>	observers		= new HashSet<ISkillEventObserver>();
	
	private Sisyphus								sisyphus			= null;
	
	private long									timeoutStart	= Long.MAX_VALUE;
	private long									timeout			= 0;
	
	
	/**
	 * @param skill skillName
	 */
	public ASkill(final ESkillName skill)
	{
		skillName = skill;
	}
	
	
	/**
	 * @return
	 */
	@Override
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
	@Override
	public void setWorldFrame(final WorldFrame newWorldFrame)
	{
		worldFrame = newWorldFrame;
	}
	
	
	/**
	 * @param period [ns]
	 */
	@Override
	public void setDt(final long period)
	{
		dt = period;
	}
	
	
	/**
	 * @return period [s]
	 */
	public float getDt()
	{
		return dt / 1e9f;
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
	@Override
	public boolean isComplete()
	{
		return completed;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final List<ACommand> calcActions()
	{
		if ((System.nanoTime() - timeoutStart) > timeout)
		{
			log.trace("Skill " + getSkillName().name() + " timed out");
			complete();
		}
		return calcActions(new ArrayList<ACommand>());
	}
	
	
	/**
	 * @param cmds
	 * @return
	 */
	@Override
	public abstract List<ACommand> calcActions(List<ACommand> cmds);
	
	
	/**
	 * @return
	 */
	@Override
	public final List<ACommand> calcExitActions()
	{
		List<ACommand> cmds = calcExitActions(new ArrayList<ACommand>());
		observers.clear();
		
		return cmds;
	}
	
	
	/**
	 * Should be overridden by subclasses!!!
	 * 
	 * @param cmds
	 * @return
	 */
	@Override
	public List<ACommand> calcExitActions(final List<ACommand> cmds)
	{
		return cmds;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final List<ACommand> calcEntryActions()
	{
		AIConfig.getSkillsClient().applyConfigToObject(this, getBotType().name());
		return calcEntryActions(new ArrayList<ACommand>());
	}
	
	
	/**
	 * Should be overridden by subclasses!!!
	 * 
	 * @param cmds
	 * @return
	 */
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		return cmds;
	}
	
	
	/**
	 * @param observer
	 */
	@Override
	public void addObserver(final ISkillEventObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	@Override
	public void removeObserver(final ISkillEventObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected void notifyEvent(final Enum<? extends Enum<?>> event)
	{
		synchronized (observers)
		{
			for (ISkillEventObserver observer : observers)
			{
				observer.onNewEvent(event);
			}
		}
	}
	
	
	/**
	 * Does this skill need vision?
	 * 
	 * @return
	 */
	@Override
	public boolean needsVision()
	{
		return true;
	}
	
	
	protected IVector2 getPos()
	{
		if (tBot != null)
		{
			return tBot.getPos();
		}
		return AVector2.ZERO_VECTOR;
	}
	
	
	protected float getAngle()
	{
		if (tBot != null)
		{
			return tBot.getAngle();
		}
		return 0;
	}
	
	
	protected IVector2 getVel()
	{
		if (tBot != null)
		{
			return tBot.getVel();
		}
		return AVector2.ZERO_VECTOR;
	}
	
	
	protected float getaVel()
	{
		if (tBot != null)
		{
			return tBot.getaVel();
		}
		return 0;
	}
	
	
	protected EBotType getBotType()
	{
		return bot.getType();
	}
	
	
	protected boolean hasBallContact()
	{
		if (tBot != null)
		{
			return tBot.hasBallContact();
		}
		return false;
	}
	
	
	@Override
	public String toString()
	{
		return getSkillName().toString();
	}
	
	
	/**
	 * @return the bot
	 */
	@Override
	public final ABot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @param bot the bot to set
	 */
	@Override
	public final void setBot(final ABot bot)
	{
		if (this.bot != null)
		{
			log.warn("Bot already set!");
		}
		this.bot = bot;
	}
	
	
	/**
	 * @param tBot the tBot to set
	 */
	@Override
	public final void settBot(final TrackedTigerBot tBot)
	{
		this.tBot = tBot;
	}
	
	
	/**
	 * @param sisyphus
	 */
	@Override
	public final void setSisyphus(final Sisyphus sisyphus)
	{
		this.sisyphus = sisyphus;
	}
	
	
	/**
	 * @return
	 */
	protected final Sisyphus getSisyphus()
	{
		return sisyphus;
	}
	
	
	/**
	 * @return
	 */
	public final TigerDevices getDevices()
	{
		return getBot().getDevices();
	}
	
	
	/**
	 * Complete this skill after given time.
	 * 
	 * @param ms
	 */
	public final void startTimeout(final int ms)
	{
		timeoutStart = System.nanoTime();
		timeout = TimeUnit.MILLISECONDS.toNanos(ms);
	}
	
	
	/**
	 * @return
	 */
	public final boolean isTimeoutRunning()
	{
		return timeout != 0;
	}
}
