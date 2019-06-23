/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Dataholder for bot information from AI
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 5)
public class BotAiInformation
{
	private String			play				= "";
	private String			role				= "";
	private String			skill				= "";
	private boolean		ballContact		= false;
	private float			battery			= 0;
	private float			kickerCharge	= 0;
	private String			roleState		= "";
	private boolean		pathPlanning	= false;
	private List<String>	conditions		= new ArrayList<String>();
	private int				numPaths			= 0;
	private IVector2		vel				= Vector2.ZERO_VECTOR;
	private IVector2		pos				= Vector2.ZERO_VECTOR;
	private String			brokenFeatures	= "";
	private float			maxVel			= 0;
	
	
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
	public final float getBattery()
	{
		return battery;
	}
	
	
	/**
	 * @param battery the battery to set
	 */
	public final void setBattery(final float battery)
	{
		this.battery = battery;
	}
	
	
	/**
	 * @return the kickerCharge
	 */
	public final float getKickerCharge()
	{
		return kickerCharge;
	}
	
	
	/**
	 * @param kickerCharge the kickerCharge to set
	 */
	public final void setKickerCharge(final float kickerCharge)
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
	 * @return the conditions
	 */
	public final List<String> getConditions()
	{
		return conditions;
	}
	
	
	/**
	 * @param condition the conditions to add
	 */
	public final void addCondition(final String condition)
	{
		conditions.add(condition);
	}
	
	
	/**
	 * @return the pathPlanning
	 */
	public final boolean isPathPlanning()
	{
		return pathPlanning;
	}
	
	
	/**
	 * @param pathPlanning the pathPlanning to set
	 */
	public final void setPathPlanning(final boolean pathPlanning)
	{
		this.pathPlanning = pathPlanning;
	}
	
	
	/**
	 * @return the numPaths
	 */
	public final int getNumPaths()
	{
		return numPaths;
	}
	
	
	/**
	 * @param numPaths the numPaths to set
	 */
	public final void setNumPaths(final int numPaths)
	{
		this.numPaths = numPaths;
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
	public final float getMaxVel()
	{
		return maxVel;
	}
	
	
	/**
	 * @param maxVel the maxVel to set
	 */
	public final void setMaxVel(final float maxVel)
	{
		this.maxVel = maxVel;
	}
}
