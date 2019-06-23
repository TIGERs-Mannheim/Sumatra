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

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	private float				angle					= 0;
	private DynamicPosition	target				= null;
	private IVector2			destPos				= null;
	
	@Configurable(comment = "Tol [rad/s] - when below, rotation is considered to be done")
	private static float		rotSpeedTolerance	= 0.1f;
	
	@Configurable(comment = "Tol [rad] - this tolerance value is used to check if the viewing direction of the bot is correct.")
	private static float		angleTolerance		= 0.17f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * This ctor creates a {@link ViewAngleCondition} which does not care about the bot angle. It
	 * will be initialized with the actual bot angle by the skill on the first execution. Until the
	 * Skill is completed this angle keeps constant.
	 */
	public ViewAngleCondition()
	{
		super(ECondition.VIEW_ANGLE);
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
	protected EConditionState doCheckCondition(SimpleWorldFrame worldFrame, BotID botID)
	{
		if ((destPos != null) && (target != null))
		{
			target.update(worldFrame);
			updateLookAtTarget(destPos, target);
		}
		
		final TrackedTigerBot bot = worldFrame.getBot(botID);
		
		if (!(Math.abs(AngleMath.getShortestRotation(bot.getAngle(), getTargetAngle())) < angleTolerance))
		{
			setCondition(String.format("Rot. %.1f > %.1f",
					Math.abs(AngleMath.getShortestRotation(bot.getAngle(), getTargetAngle())), angleTolerance));
		} else if (!(Math.abs(bot.getaVel()) < rotSpeedTolerance))
		{
			setCondition(String.format("Vel. %.1f > %.1f", Math.abs(bot.getaVel()), rotSpeedTolerance));
		} else
		{
			setCondition("Orient. reached.");
			return EConditionState.FULFILLED;
		}
		
		return EConditionState.PENDING;
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * @param destPos
	 * @param lookAtTarget
	 */
	private void updateLookAtTarget(IVector2 destPos, DynamicPosition lookAtTarget)
	{
		final IVector2 tmp = lookAtTarget.subtractNew(destPos);
		if (!tmp.isZeroVector())
		{
			final float angle = tmp.getAngle();
			updateTargetAngle(angle);
			resetCache();
		}
		target = lookAtTarget;
		this.destPos = destPos;
		// lookAtTarget and destination are equal => can not calc angle.
		// as this happens quite frequently, just keep the old angle and do not complain
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * @param lookAtTarget
	 */
	public void updateLookAtTarget(DynamicPosition lookAtTarget)
	{
		target = lookAtTarget;
	}
	
	
	/**
	 * Update dynamic positions and recalc angle occording to destination
	 * 
	 * @param destination
	 * @param swf
	 */
	public void update(IVector2 destination, SimpleWorldFrame swf)
	{
		if (target != null)
		{
			target.update(swf);
			updateLookAtTarget(destination, target);
		}
	}
	
	
	@Override
	public void update(SimpleWorldFrame swf, BotID botId)
	{
		super.update(swf, botId);
		if (!isActive())
		{
			updateTargetAngle(swf.getBot(botId).getAngle());
		}
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
	
	
	@Override
	protected boolean compareContent(ACondition condition)
	{
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
	
	
	/**
	 * @return the target
	 */
	public final DynamicPosition getTarget()
	{
		return target;
	}
}
