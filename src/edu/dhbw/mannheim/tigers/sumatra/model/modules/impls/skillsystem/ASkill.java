/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * André Ryll,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * This is the base class for every skill, which provides subclasses with the newest data and handles their lifecycle
 * 
 * @author Ryan, Gero
 * 
 */
public abstract class ASkill
{
	private static final int				UNINITIALIZED_BOTID	= -2;
	
	/** id of the bot this skill is running on */
	private int									botId						= UNINITIALIZED_BOTID;
	
	/** [ns] */
	private long								period;
	
	private boolean							completed				= false;
	
	private final ESkillName				skill;
	private final ESkillGroup				group;
	
	// To be set by SkillExecutor!
	private Sisyphus							sisyphus					= null;
	private WorldFrame						currentSituation		= null;
	
	
	/**
	  * @param skill
	  * @param group
	  */
	public ASkill(ESkillName skill, ESkillGroup group)
	{
		this.skill = skill;
		this.group = group;
	}
	

	/**
	 * <p>
	 * <strong>NOTE:</strong> Set by SkillSystem only!!!
	 * </p>
	 * @param botId
	 */
	void setBotId(int botId)
	{
		this.botId = botId;
	}
	

	protected Sisyphus getSisyphus()
	{
		return sisyphus;
	}
	

	/**
	 * <p>
	 * <strong>NOTE:</strong> Used by SkillSystem only!!!
	 * </p>
	 * @param botId
	 */
	int getBotId()
	{
		return botId;
	}
	

	public ESkillName getSkillName()
	{
		return skill;
	}
	

	protected TrackedTigerBot getBot()
	{
		return currentSituation.tigerBots.get(botId);
	}
	

	protected TrackedBall getBall()
	{
		return currentSituation.ball;
	}
	

	protected long getTime()
	{
		return currentSituation.time;
	}
	

	protected WorldFrame getWorldFrame()
	{
		return currentSituation;
	}
	
	
	void setWorldFrame(WorldFrame newWorldFrame)
	{
		this.currentSituation = newWorldFrame;
	}
	

	void setSisyphus(Sisyphus sisyphus)
	{
		this.sisyphus = sisyphus;
	}
	

	/**
	 * @param period [ns]
	 */
	void setPeriod(long period)
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
	

	boolean isComplete()
	{
		return completed;
	}
	

	public final ArrayList<ACommand> calcActions()
	{
		if (currentSituation == null || getBot() == null)
		{
			return new ArrayList<ACommand>();
		}
		
		return calcActions(new ArrayList<ACommand>());
	}
	

	public abstract ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds);
	

	public final ArrayList<ACommand> calcExitActions()
	{
		if (currentSituation == null || getBot() == null)
		{
			return new ArrayList<ACommand>();
		}
		
		return calcExitActions(new ArrayList<ACommand>());
	}
	

	/**
	 * Should be overridden by subclasses!!!
	 * 
	 * @param cmds
	 * @return
	 */
	public ArrayList<ACommand> calcExitActions(ArrayList<ACommand> cmds)
	{
		return cmds;
	}
	

	public final ArrayList<ACommand> calcEntryActions()
	{
		if (currentSituation == null)
			return new ArrayList<ACommand>();
		
		return calcEntryActions(new ArrayList<ACommand>());
	}
	

	/**
	 * Should be overridden by subclasses!!!
	 * 
	 * @param cmds
	 * @return
	 */
	public ArrayList<ACommand> calcEntryActions(ArrayList<ACommand> cmds)
	{
		return cmds;
	}
	

	/**
	 * Compares to skills. Does some basic checking on group and name.
	 * 
	 * @param newSkill Right hand side skill to compare.
	 * @return true if the skills are equal, false otherwise
	 */
	boolean compare(ASkill newSkill)
	{
		if (newSkill == null || newSkill.getGroup() != getGroup() || newSkill.getSkillName() != getSkillName())
		{
			return false;
		}
		return compareContent(newSkill);
	}
	

	/**
	 * Compare the skill contents.
	 * This can be for example target destination(s) in a move skill.
	 * 
	 * @param newSkill Right hand side skill to compare.
	 * @return true if the skills are equal, false otherwise
	 */
	protected abstract boolean compareContent(ASkill newSkill);
	

	public ESkillGroup getGroup()
	{
		return group;
	}
	

	public int getGroupID()
	{
		return group.ordinal();
	}
	

	@Override
	public String toString()
	{
		return getSkillName().toString();
	}
}
