/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.moduli.AModule;


/**
 * The base class for every implementation of a skill system
 * 
 * @author Gero
 * 
 */
public abstract class ASkillSystem extends AModule
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
	public abstract void execute(BotID botId, AMoveSkill skill);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void deinitModule()
	{
		observers.clear();
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(ISkillSystemObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(ISkillSystemObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected void notifySkillStarted(AMoveSkill skill, BotID botId)
	{
		for (final ISkillSystemObserver observer : observers)
		{
			observer.onSkillStarted(skill, botId);
		}
	}
	
	
	protected void notifySkillCompleted(AMoveSkill skill, BotID botId)
	{
		for (final ISkillSystemObserver observer : observers)
		{
			observer.onSkillCompleted(skill, botId);
		}
	}
}
