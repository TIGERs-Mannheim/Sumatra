/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Condition that checks whether the bot has reached a given destination.
 * 
 * @author Oliver Steinbrecher
 */
public class DestinationCondition extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2		destination				= Vector2.ZERO_VECTOR;
	
	@Configurable(comment = "Dist [mm] - distance between destination and current pos when positioning is considered done.")
	private static float	posTolerance			= 20;
	@Configurable(comment = "Vel [m/s] - max velocity of bot when positining is considered done")
	private static float	moveSpeedThreshold	= 0.5f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * New inactive destination condition.
	 * You have to update the destination to active this condition
	 */
	public DestinationCondition()
	{
		super(ECondition.DESTINATION);
		setActive(false);
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
	protected EConditionState doCheckCondition(final SimpleWorldFrame worldFrame, final BotID botID)
	{
		final TrackedTigerBot bot = worldFrame.getBot(botID);
		
		final float dist = GeoMath.distancePP(bot, destination);
		
		boolean position = (dist < posTolerance);
		boolean movement = (bot.getVel().getLength2() < moveSpeedThreshold);
		
		if (!position)
		{
			setCondition(String.format("Dist. %.1f > %.1f", dist, posTolerance));
		} else if (!movement)
		{
			setCondition(String.format("Vel. %.1f > %.1f", bot.getVel().getLength2(), moveSpeedThreshold));
		} else
		{
			setCondition("Dest. reached");
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
	 * Updates the destination of this condition.
	 * 
	 * @param dest if null, condition will be set inactive
	 */
	public final void updateDestination(final IVector2 dest)
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
	protected boolean compareContent(final ACondition condition)
	{
		final DestinationCondition con = (DestinationCondition) condition;
		return getDestination().equals(con.getDestination(), posTolerance);
	}
	
	
	/**
	 * @param swf
	 */
	@Override
	public void update(final SimpleWorldFrame swf, final BotID botId)
	{
		super.update(swf, botId);
		if (!isActive())
		{
			updateDestination(swf.getBot(botId).getPos());
		}
	}
}
