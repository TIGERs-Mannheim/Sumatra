/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Gunther Berthold
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;


/**
 * used to check that a bot has a certain view angle,
 * which is passed in via corresponding update method
 * 
 * @author DanielW
 * 
 */
public class ViewAngleCon extends ACondition
{
	private float				targetViewAngle	= 0;
	protected float	tolerance;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param tolerance in radiant
	 */
	public ViewAngleCon(float tolerance)
	{
		super(ECondition.VIEW_ANGLE);
		this.tolerance = tolerance;
	}
	

	public ViewAngleCon()
	{
		this(ECondition.VIEW_ANGLE);
	}
	

	protected ViewAngleCon(ECondition type)
	{
		super(type);
		tolerance = AIConfig.getTolerances().getViewAngle();
	}
	

	protected ViewAngleCon(ECondition type, float tolerance)
	{
		super(type);
		this.tolerance = tolerance;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean doCheckCondition(WorldFrame worldFrame, int botID)
	{
		float currentBotViewAngle = worldFrame.tigerBots.get(botID).angle;
		return tolerance > Math.abs(currentBotViewAngle - this.targetViewAngle);
	}
	

	/**
	 * updates the intended view angle
	 * @param viewAngle the absolute angle the bot should look at (-pi,pi]
	 *           <b>radiant measure</b>
	 */
	public void updateTargetViewAngle(float viewAngle)
	{
		this.targetViewAngle = viewAngle;
		resetCache();
	}
	

	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public float getTargetViewAngle()
	{
		return targetViewAngle;
	}


	public float getTolerance()
	{
		return tolerance;
	}


	public void setTolerance(float tolerance)
	{
		this.tolerance = tolerance;
	}
	
	
	
}
