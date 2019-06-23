/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.data;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * Additional bot skill input data.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillInput
{
	private final ABotSkill	skill;
	private final IVector3	curPos;		// [mm, rad]
	private final IVector3	curVelLocal;
	private final IVector3	curAccLocal;
	private final long		tNow;
	
	
	/**
	 * @param skill
	 * @param curPos
	 * @param curVelLocal
	 * @param curAccLocal
	 * @param tNow
	 */
	public BotSkillInput(final ABotSkill skill, final IVector3 curPos, final IVector3 curVelLocal,
			final IVector3 curAccLocal,
			final long tNow)
	{
		this.skill = skill;
		this.curPos = curPos;
		this.curVelLocal = curVelLocal;
		this.curAccLocal = curAccLocal;
		this.tNow = tNow;
	}
	
	
	public IVector3 getCurPos()
	{
		return curPos;
	}
	
	
	public IVector3 getCurVelLocal()
	{
		return curVelLocal;
	}
	
	
	public IVector3 getCurAccLocal()
	{
		return curAccLocal;
	}
	
	
	/**
	 * @return
	 */
	public long gettNow()
	{
		return tNow;
	}
	
	
	public ABotSkill getSkill()
	{
		return skill;
	}
}
