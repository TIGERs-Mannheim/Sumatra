/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * Dataholder for bot information from AI
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class BotAiInformation
{
	private String		play				= "";
	private String		role				= "";
	private String		skill				= "";
	private String		roleState		= "";
	private String		skillState		= "";
	private String		skillDriver		= "";
	private boolean	ballContact		= false;
	private double		battery			= 0;
	private double		kickerCharge	= 0;
	private IVector2	vel				= Vector2.ZERO_VECTOR;
	private IVector2	pos				= Vector2.ZERO_VECTOR;
	private String		brokenFeatures	= "";
	private double		maxVel			= 0;
	private String		limits			= "";
	private double		dribbleSpeed	= 0;
	private double		kickSpeed		= 0;
	private String		device			= "";
	
	
	/**
	  * 
	  */
	public BotAiInformation()
	{
		
	}
	
	
	/**
	 * @return the play
	 */
	public final String getPlay()
	{
		return play;
	}
	
	
	/**
	 * @param play the play to set
	 */
	public final void setPlay(final String play)
	{
		this.play = play;
	}
	
	
	/**
	 * @return the role
	 */
	public final String getRole()
	{
		return role;
	}
	
	
	/**
	 * @param role the role to set
	 */
	public final void setRole(final String role)
	{
		this.role = role;
	}
	
	
	/**
	 * @return the skill
	 */
	public final String getSkill()
	{
		return skill;
	}
	
	
	/**
	 * @param skill the skill to set
	 */
	public final void setSkill(final String skill)
	{
		this.skill = skill;
	}
	
	
	/**
	 * @return the ballContact
	 */
	public final boolean isBallContact()
	{
		return ballContact;
	}
	
	
	/**
	 * @param ballContact the ballContact to set
	 */
	public final void setBallContact(final boolean ballContact)
	{
		this.ballContact = ballContact;
	}
	
	
	/**
	 * @return the battery
	 */
	public final double getBattery()
	{
		return battery;
	}
	
	
	/**
	 * @param battery the battery to set
	 */
	public final void setBattery(final double battery)
	{
		this.battery = battery;
	}
	
	
	/**
	 * @return the kickerCharge
	 */
	public final double getKickerCharge()
	{
		return kickerCharge;
	}
	
	
	/**
	 * @param kickerCharge the kickerCharge to set
	 */
	public final void setKickerCharge(final double kickerCharge)
	{
		this.kickerCharge = kickerCharge;
	}
	
	
	/**
	 * @return the roleState
	 */
	public final String getRoleState()
	{
		return roleState;
	}
	
	
	/**
	 * @param roleState the roleState to set
	 */
	public final void setRoleState(final String roleState)
	{
		this.roleState = roleState;
	}
	
	
	/**
	 * @return the vel
	 */
	public final IVector2 getVel()
	{
		return vel;
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	public final void setVel(final IVector2 vel)
	{
		// avoid numerical problems for zero-vel
		this.vel = vel.addNew(new Vector2(1e-5, 1e-5));
	}
	
	
	/**
	 * @return the pos
	 */
	public final IVector2 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public final void setPos(final IVector2 pos)
	{
		this.pos = pos;
	}
	
	
	/**
	 * @return the brokenFeatures
	 */
	public final String getBrokenFeatures()
	{
		return brokenFeatures;
	}
	
	
	/**
	 * @param brokenFeatures the brokenFeatures to set
	 */
	public final void setBrokenFeatures(final String brokenFeatures)
	{
		this.brokenFeatures = brokenFeatures;
	}
	
	
	/**
	 * @return the maxVel
	 */
	public final double getMaxVel()
	{
		return maxVel;
	}
	
	
	/**
	 * @param maxVel the maxVel to set
	 */
	public final void setMaxVel(final double maxVel)
	{
		this.maxVel = maxVel;
	}
	
	
	/**
	 * @return the skillState
	 */
	public final String getSkillState()
	{
		return skillState;
	}
	
	
	/**
	 * @param skillState the skillState to set
	 */
	public final void setSkillState(final String skillState)
	{
		this.skillState = skillState;
	}
	
	
	/**
	 * @return the skillDriver
	 */
	public final String getSkillDriver()
	{
		return skillDriver;
	}
	
	
	/**
	 * @param skillDriver the skillDriver to set
	 */
	public final void setSkillDriver(final String skillDriver)
	{
		this.skillDriver = skillDriver;
	}
	
	
	/**
	 * @return the limits
	 */
	public String getLimits()
	{
		return limits;
	}
	
	
	/**
	 * @param limits the limits to set
	 */
	public void setLimits(final String limits)
	{
		this.limits = limits;
	}
	
	
	/**
	 * @return the dribbleSpeed
	 */
	public double getDribbleSpeed()
	{
		return dribbleSpeed;
	}
	
	
	/**
	 * @param dribbleSpeed the dribbleSpeed to set
	 */
	public void setDribbleSpeed(final double dribbleSpeed)
	{
		this.dribbleSpeed = dribbleSpeed;
	}
	
	
	/**
	 * @return the kickSpeed
	 */
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * @param kickSpeed the kickSpeed to set
	 */
	public void setKickSpeed(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
	}
	
	
	/**
	 * @return the device
	 */
	public String getDevice()
	{
		return device;
	}
	
	
	/**
	 * @param device the device to set
	 */
	public void setDevice(final String device)
	{
		this.device = device;
	}
}
