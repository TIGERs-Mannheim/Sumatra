/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;


/**
 * Skill executer with nano second precision.
 * 
 * @author AndreR
 * 
 */
public class SkillExecutor extends Thread implements IWorldPredictorObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ASkill									slots[]				= new ASkill[ESkillGroup.count()];
	private final ASkill									newSkills[]			= new ASkill[ESkillGroup.count()];
	
	/** [us] */
	private final static int							DEFAULT_PERIOD		= 20000;
	
	/** [ns] */
	private final long									period;
	/** Used to cancel {@link ThreadUtil#parkNanosSafe(long, AtomicBoolean)} */
	private final AtomicBoolean						sleepCancelSwitch	= new AtomicBoolean(false);
	
	/** Volatile because this is altered in {@link #terminate()} from extern threads */
	private volatile boolean							active				= true;
	
	/** Volatile because it is written from the outside */
	private volatile WorldFrame						latestWorldFrame	= null;
	
	private final List<ISkillExecutorObserver>	observers			= new ArrayList<ISkillExecutorObserver>();
	
	private final Logger									log					= Logger.getLogger(getClass());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Calls {@link #SkillExecutor(long)} with {@link #DEFAULT_PERIOD}({@value #DEFAULT_PERIOD})
	 */
	public SkillExecutor()
	{
		this(DEFAULT_PERIOD);
	}
	

	/**
	 * @param period [us]
	 */
	public SkillExecutor(long period)
	{
		this.period = TimeUnit.MICROSECONDS.toNanos(period);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ISkillExecutorObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(ISkillExecutorObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	

	@Override
	public void run()
	{
		while (active)
		{
			long loopStarted = 0;
			long loopFinished = 0;
			
			synchronized (slots)
			{
				loopStarted = System.nanoTime();
				
				for (ASkill skill : slots)
				{
					if (skill == null)
					{
						continue;
					}
					
					skill.setWorldFrame(latestWorldFrame);
					
					if (skill.isComplete())
					{
						List<ACommand> cmds = skill.calcExitActions();
						
						for (ACommand cmd : cmds)
						{
							notifyNewCommand(cmd);
						}
						
						notifySkillCompleted(skill);
						
						// replace by new skill (if available) or remove
						if (newSkills[skill.getGroupID()] != null)
						{
							ASkill newSkill = newSkills[skill.getGroupID()];
							slots[skill.getGroupID()] = newSkill;
							newSkills[skill.getGroupID()] = null;
							
							newSkill.setPeriod(period);
							
							skill = newSkill;
							
							skill.setWorldFrame(latestWorldFrame);
							
							cmds = skill.calcEntryActions();
							
							for (ACommand cmd : cmds)
							{
								notifyNewCommand(cmd);
							}
							
							notifySkillStarted(newSkill);
						} else
						{
							slots[skill.getGroupID()] = null;
							
							skill = null;
						}
					}
					
					if (skill == null)
					{
						continue;
					}
					
					// process
					List<ACommand> cmds = skill.calcActions();
					
					for (ACommand cmd : cmds)
					{
						notifyNewCommand(cmd);
					}
				}
				
				loopFinished = System.nanoTime(); // This line _inside_ synchronized...
			}
			
			long duration = loopFinished - loopStarted;
			long sleep = period - duration;

			sleepCancelSwitch.set(false);
			ThreadUtil.parkNanosSafe(sleep, sleepCancelSwitch);
		}
	}
	

	void terminate()
	{
		active = false;
		this.interrupt();
		
		try
		{
			this.join();
		} catch (InterruptedException e)
		{
			log.error("Could not join SkillExecutor");
		}
	}
	

	private void notifyNewCommand(ACommand cmd)
	{
		synchronized (observers)
		{
			for (ISkillExecutorObserver observer : observers)
			{
				observer.onNewCommand(cmd);
			}
		}
	}
	

	private void notifySkillCompleted(ASkill skill)
	{
		synchronized (observers)
		{
			for (ISkillExecutorObserver observer : observers)
			{
				observer.onSkillCompleted(skill);
			}
		}
	}
	

	private void notifySkillStarted(ASkill skill)
	{
		synchronized (observers)
		{
			for (ISkillExecutorObserver observer : observers)
			{
				observer.onSkillStarted(skill);
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	void setSkill(int botId, ASkill newSkill, ISkillWorldInfoProvider provider)
	{
		synchronized (slots)
		{
			newSkill.setBotId(botId);
			newSkill.setSisyphus(provider.getSisyphus());
			doSetSkill(newSkill);
		}
	}
	

	void setSkills(int botId, SkillFacade facade, ISkillWorldInfoProvider provider)
	{
		synchronized (slots)
		{
			for (ASkill newSkill : facade.skills)
			{
				if (newSkill != null)
				{
					newSkill.setBotId(botId);
					newSkill.setSisyphus(provider.getSisyphus());
					doSetSkill(newSkill);
				}
			}
		}
	}
	

	private void doSetSkill(ASkill newSkill)
	{
		ASkill oldSkill = slots[newSkill.getGroupID()];
		
		if (oldSkill != null)
		{
			if (oldSkill.compare(newSkill))
			{
				// Drop new skill (skills are equal)
				
				// clear eventually existent new skill (which is now outdated)
				newSkills[oldSkill.getGroupID()] = null;
			} else
			{
				// schedule skill as replacement
				newSkills[oldSkill.getGroupID()] = newSkill;
				oldSkill.complete();
				
				// AMoveSkills? Move PID-state from old to new.
				if (newSkill.getGroup() == ESkillGroup.MOVE)
				{
					// Ok, both are Move-skills. But are they really AMoveSkills?
					if (newSkill instanceof AMoveSkillV2 && oldSkill instanceof AMoveSkillV2)
					{
						AMoveSkillV2 newMove = (AMoveSkillV2) newSkill;
						AMoveSkillV2 oldMove = (AMoveSkillV2) oldSkill;
						newMove.setPidStateLen(oldMove.getPidStateLen());
						newMove.setPidStateW(oldMove.getPidStateW());
					}
				}
				
				// Fire!
				sleepCancelSwitch.set(true);
				LockSupport.unpark(this); // fire immediate update of skills
			}
		} else
		{
			slots[newSkill.getGroupID()] = newSkill;
		}
	}
	

	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		latestWorldFrame = wf;
	}
}
