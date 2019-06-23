/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


/**
 * 
 * Condition that checks whether the bot has reached a given destination.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class DestinationCondition extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2	destination	= Vector2.ZERO_VECTOR;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param dest If <code>null</code>, condition will be inactive
	 */
	public DestinationCondition(IVector2 dest)
	{
		super(ECondition.DESTINATION);
		updateDestination(dest);
	}
	
	
	/**
	 * New inactive destination condition.
	 * You have to update the destination to active this condition
	 */
	public DestinationCondition()
	{
		this(null);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Algorithm which is used within skills to check position.
	 * 
	 * @param worldFrame
	 * @param botID
	 * @return true when bot has reached its position
	 */
	@Override
	protected EConditionState doCheckCondition(WorldFrame worldFrame, BotID botID)
	{
		final float posTolerance = getBotConfig().getTolerances().getPositioning();
		final float moveSpeedThreshold = getBotConfig().getSkills().getMoveSpeedThreshold();
		final TrackedTigerBot bot = worldFrame.tigerBotsVisible.get(botID);
		
		final float dist = GeoMath.distancePP(bot, destination);
		
		final String conditionStr = String.format(
				"dist(%.2f) < posTolerance (%.2f) && botVel(%.2f) < moveSpeedThreshold(%.2f)", dist, posTolerance, bot
						.getVel().getLength2(), moveSpeedThreshold);
		setCondition(conditionStr);
		
		if ((dist < posTolerance) && (bot.getVel().getLength2() < moveSpeedThreshold))
		{
			return EConditionState.FULFILLED;
		}
		return EConditionState.PENDING;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return destination
	 * @throws IllegalStateException if condition is inactive
	 */
	public final IVector2 getDestination()
	{
		if (!isActive())
		{
			throw new IllegalStateException("Condition is inactive, destination may be invalid!");
		}
		return destination;
	}
	
	
	/**
	 * 
	 * Updates the destination of this condition.
	 * 
	 * @param dest if null, condition will be set inactive
	 */
	public final void updateDestination(IVector2 dest)
	{
		if (dest == null)
		{
			setActive(false);
			destination = AVector2.ZERO_VECTOR;
		} else
		{
			setActive(true);
			destination = dest;
		}
		resetCache();
	}
	
	
	@Override
	protected boolean compareContent(ACondition condition)
	{
		final float posTolerance = getBotConfig().getTolerances().getPositioning();
		final DestinationCondition con = (DestinationCondition) condition;
		return getDestination().equals(con.getDestination(), posTolerance);
	}
}
