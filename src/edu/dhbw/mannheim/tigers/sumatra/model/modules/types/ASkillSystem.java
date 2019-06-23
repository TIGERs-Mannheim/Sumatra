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

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;
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
	public static final String							MODULE_TYPE	= "ASkillSystem";
	public static final String							MODULE_ID	= "skillsystem";
	
	protected final List<ISkillSystemObserver>	observers	= new ArrayList<ISkillSystemObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- interface ------------------------------------------------------------
	// --------------------------------------------------------------------------
	public abstract void execute(int botId, ASkill skill);
	public abstract void execute(int botId, SkillFacade facade);
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ISkillSystemObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(ISkillSystemObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	protected void notifySkillStarted(ASkill skill, int botId)
	{
		synchronized(observers)
		{
			for (ISkillSystemObserver observer : observers)
			{
				observer.onSkillStarted(skill, botId);
			}
		}
	}
	
	protected void notifySkillCompleted(ASkill skill, int botId)
	{
		synchronized(observers)
		{
			for (ISkillSystemObserver observer : observers)
			{
				observer.onSkillCompleted(skill, botId);
			}
		}
	}
}
