/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.08.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


/**
 * General super condition for all angle conditions. This stores the
 * tolerance, threshold values and algorithms for checking the angle within
 * the skills.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class ViewAngleCondition extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * If {@link ViewAngleCondition#angle} is null the bot keeps its actual initial orientation.
	 * The {@link Float} class is used here to underline an uninitialized {@link ViewAngleCondition}.
	 * [rad]
	 */
	private float	angle;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param angle [rad]
	 */
	public ViewAngleCondition(float angle)
	{
		super(ECondition.VIEW_ANGLE);
		updateTargetAngle(angle);
	}
	
	
	/**
	 * This ctor creates a {@link ViewAngleCondition} which does not care about the bot angle. It
	 * will be initialized with the actual bot angle by the skill on the first execution. Until the
	 * Skill is completed this angle keeps constant.
	 */
	public ViewAngleCondition()
	{
		this(0);
		setActive(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Algorithm which is used within skills to check the angle.
	 * 
	 * @param worldFrame
	 * @param botID
	 * @return true when bot has reached its destination angle
	 */
	@Override
	protected EConditionState doCheckCondition(WorldFrame worldFrame, BotID botID)
	{
		final float angleTolerance = getBotConfig().getTolerances().getViewAngle();
		final float rotSpeedThreshold = getBotConfig().getSkills().getRotationSpeedThreshold();
		
		final TrackedTigerBot bot = worldFrame.tigerBotsVisible.get(botID);
		
		final String conditionStr = String.format(
				"rotation(%.2f) < angleTol (%.2f) && botVel(%.2f) < rotSpeedThreshold(%.2f)",
				Math.abs(AngleMath.getShortestRotation(bot.getAngle(), getTargetAngle())), angleTolerance,
				Math.abs(bot.getaVel()), rotSpeedThreshold);
		setCondition(conditionStr);
		
		if ((Math.abs(AngleMath.getShortestRotation(bot.getAngle(), getTargetAngle())) < angleTolerance)
				&& (Math.abs(bot.getaVel()) < rotSpeedThreshold))
		{
			return EConditionState.FULFILLED;
		}
		return EConditionState.PENDING;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return target angle [rad]
	 * @throws IllegalStateException if you call this method while condition is inactive
	 */
	public float getTargetAngle()
	{
		if (!isActive())
		{
			throw new IllegalStateException("Condition is inactive, targetAngle may be invalid!");
		}
		return angle;
	}
	
	
	/**
	 * updates the intended view angle.
	 * condition will be activated, if it was not.
	 * 
	 * Use {@link ACondition#setActive(boolean)} to keep current angle
	 * 
	 * @param angle the absolute angle the bot should look at (-pi,pi)
	 *           <b>[rad]</b>
	 */
	public final void updateTargetAngle(float angle)
	{
		this.angle = angle;
		setActive(true);
		resetCache();
	}
	
	
	@Override
	protected boolean compareContent(ACondition condition)
	{
		final float angleTolerance = getBotConfig().getTolerances().getViewAngle();
		
		if (condition == null)
		{
			return false;
		}
		
		ViewAngleCondition con = (ViewAngleCondition) condition;
		
		if (!isActive() && !con.isActive())
		{
			return true;
		}
		if (isActive() != con.isActive())
		{
			return false;
		}
		
		if (Math.abs(getTargetAngle() - con.getTargetAngle()) < angleTolerance)
		{
			return true;
		}
		return false;
	}
}
