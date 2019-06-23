/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ISkillWorldInfoProvider;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;


/**
 * The base class for every implementation of a skill system
 * 
 * @author Gero
 */
public abstract class ASkillSystem extends AModule implements IEmergencyStop, ISkillWorldInfoProvider
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** */
	public static final String						MODULE_TYPE	= "AMoveSystem";
	/** */
	public static final String						MODULE_ID	= "skillsystem";
	
	private final Set<ISkillSystemObserver>	observers	= Collections
																					.newSetFromMap(new ConcurrentHashMap<ISkillSystemObserver, Boolean>());
	
	
	// --------------------------------------------------------------------------
	// --- interface ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 * @param skill
	 */
	public abstract void execute(BotID botId, ISkill skill);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void deinitModule()
	{
		observers.clear();
	}
	
	
	/**
	 * Reset skill system aka delete roles from given ai color.
	 * Used after crash in AI
	 * 
	 * @param color
	 */
	public void reset(final ETeamColor color)
	{
		List<ISkillSystemObserver> copy = new ArrayList<ISkillSystemObserver>(observers);
		for (ISkillSystemObserver o : copy)
		{
			if (o instanceof ARole)
			{
				ARole role = (ARole) o;
				if (role.getBotID().getTeamColor() == color)
				{
					observers.remove(o);
				}
			}
		}
	}
	
	
	/**
	 */
	protected void checkObserversCleared()
	{
		observers.clear();
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISkillSystemObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISkillSystemObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected void notifySkillStarted(final ISkill skill, final BotID botId)
	{
		synchronized (observers)
		{
			for (final ISkillSystemObserver observer : observers)
			{
				observer.onSkillStarted(skill, botId);
			}
		}
	}
	
	
	protected void notifySkillCompleted(final ISkill skill, final BotID botId)
	{
		synchronized (observers)
		{
			for (final ISkillSystemObserver observer : observers)
			{
				observer.onSkillCompleted(skill, botId);
			}
		}
	}
}
