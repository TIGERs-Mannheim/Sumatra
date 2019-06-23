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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillEventObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;
import edu.dhbw.mannheim.tigers.sumatra.util.config.EConfigurableCat;


/**
 * This is the base class for every skill, which provides subclasses with the newest data and handles their lifecycle
 * 
 * @author Ryan, Gero
 */
public abstract class ASkill implements ISkill
{
	private static final Logger				log				= Logger.getLogger(ASkill.class.getName());
	
	private boolean								completed		= false;
	
	private final ESkillName					skillName;
	
	private ASkillSystem							skillSystem		= null;
	private WorldFrame							worldFrame		= null;
	
	private TrackedTigerBot						tBot				= null;
	private ABot									bot				= null;
	
	private final Set<ISkillEventObserver>	observers		= new HashSet<ISkillEventObserver>();
	
	private long									timeoutStart	= Long.MAX_VALUE;
	private long									timeout			= 0;
	
	private long									lastUpdate		= SumatraClock.nanoTime();
	private float									dt					= 1;
	private float									minDt				= 0.008f;
	
	
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
	public final ESkillName getSkillName()
	{
		return skillName;
	}
	
	
	protected final WorldFrame getWorldFrame()
	{
		return worldFrame;
	}
	
	
	/**
	 * @param newWorldFrame
	 */
	@Override
	public final void update(final WorldFrame newWorldFrame)
	{
		if (newWorldFrame != null)
		{
			worldFrame = newWorldFrame;
			TrackedTigerBot newTbot = worldFrame.getBot(bot.getBotID());
			if (newTbot != null)
			{
				tBot = newTbot;
			}
		} else
		{
			if (needsVision())
			{
				complete();
			}
		}
	}
	
	
	/**
	 * Called from derived user classes to signal the skill system to remove this skill
	 */
	public final void complete()
	{
		completed = true;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final boolean isComplete()
	{
		return completed;
	}
	
	
	/**
	 */
	@Override
	public final void calcActions(final List<ACommand> cmds)
	{
		if ((SumatraClock.nanoTime() - timeoutStart) > timeout)
		{
			log.trace("Skill " + getSkillName().name() + " timed out");
			complete();
			return;
		}
		dt = (SumatraClock.nanoTime() - lastUpdate) * 1e-9f;
		// skip update if we get too many frames
		if (dt >= getMinDt())
		{
			doCalcActions(cmds);
			lastUpdate = SumatraClock.nanoTime();
		}
	}
	
	
	@Override
	public final void calcExitActions(final List<ACommand> cmds)
	{
		doCalcExitActions(cmds);
		getDevices().disarm(cmds);
		getDevices().dribble(cmds, false);
		observers.clear();
	}
	
	
	@Override
	public final void calcEntryActions(final List<ACommand> cmds)
	{
		ConfigRegistration.applySpezis(this, EConfigurableCat.SKILLS, getBotType().name());
		doCalcEntryActions(cmds);
	}
	
	
	protected void doCalcActions(final List<ACommand> cmds)
	{
	}
	
	
	protected void doCalcExitActions(final List<ACommand> cmds)
	{
	}
	
	
	protected void doCalcEntryActions(final List<ACommand> cmds)
	{
	}
	
	
	/**
	 * @param observer
	 */
	@Override
	public final void addObserver(final ISkillEventObserver observer)
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
	public final void removeObserver(final ISkillEventObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected final void notifyEvent(final Enum<? extends Enum<?>> event)
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
	
	
	protected final IVector2 getPos()
	{
		if (tBot != null)
		{
			return tBot.getPos();
		}
		return AVector2.ZERO_VECTOR;
	}
	
	
	protected final float getAngle()
	{
		if (tBot != null)
		{
			return tBot.getAngle();
		}
		return 0;
	}
	
	
	protected final IVector2 getVel()
	{
		if (tBot != null)
		{
			return tBot.getVel();
		}
		return AVector2.ZERO_VECTOR;
	}
	
	
	protected final float getaVel()
	{
		if (tBot != null)
		{
			return tBot.getaVel();
		}
		return 0;
	}
	
	
	protected final EBotType getBotType()
	{
		return bot.getType();
	}
	
	
	protected final boolean hasBallContact()
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
	 * This must only be called by SkillExecuter!!
	 * 
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
		// tBot = TrackedTigerBot.defaultBot(bot.getBotID(), bot);
	}
	
	
	/**
	 * Careful, may be null if no vision
	 * 
	 * @return
	 */
	protected final TrackedTigerBot getTBot()
	{
		return tBot;
	}
	
	
	/**
	 * @return
	 */
	protected final TigerDevices getDevices()
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
		timeoutStart = SumatraClock.nanoTime();
		timeout = TimeUnit.MILLISECONDS.toNanos(ms);
	}
	
	
	/**
	 * @return
	 */
	protected final boolean isTimeoutRunning()
	{
		return timeout != 0;
	}
	
	
	/**
	 * @return dt since last update in [s]
	 */
	protected final float getDt()
	{
		return dt;
	}
	
	
	/**
	 * @return the minDt
	 */
	protected final float getMinDt()
	{
		return minDt;
	}
	
	
	/**
	 * @param minDt the minDt to set
	 */
	@Override
	public final void setMinDt(final float minDt)
	{
		this.minDt = minDt;
	}
	
	
	/**
	 * @return the skillSystem
	 */
	protected final ASkillSystem getSkillSystem()
	{
		return skillSystem;
	}
	
	
	/**
	 * @param skillSystem the skillSystem to set
	 */
	@Override
	public final void setSkillSystem(final ASkillSystem skillSystem)
	{
		this.skillSystem = skillSystem;
	}
}
