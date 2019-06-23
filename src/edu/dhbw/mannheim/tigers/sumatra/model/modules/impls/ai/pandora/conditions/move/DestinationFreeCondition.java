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

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;


/**
 * Checks, if destination is free of bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class DestinationFreeCondition extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	VELOCITY_TOL		= 0.1f;
	private final Vector2		destination			= new Vector2();
	private boolean				considerFoeBots	= true;
	private float					destFreeTol			= AIConfig.getTolerances(EBotType.UNKNOWN).getDestEqualRadius();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * New inactive condition
	 */
	public DestinationFreeCondition()
	{
		this(null, false);
	}
	
	
	/**
	 * 
	 * @param dest
	 * @param considerFoeBots
	 */
	public DestinationFreeCondition(IVector2 dest, boolean considerFoeBots)
	{
		super(ECondition.DEST_FREE);
		updateDestination(dest);
		setConsiderFoeBots(considerFoeBots);
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
		if (!isDestinationFreeOfTigers(worldFrame, botID))
		{
			setCondition("Not free of tigers");
			return EConditionState.BLOCKED;
		}
		if (considerFoeBots && !isDestinationFreeOfFoes(worldFrame))
		{
			setCondition("Not free of foes");
			return EConditionState.BLOCKED;
		}
		setCondition("Free");
		return EConditionState.FULFILLED;
	}
	
	
	@Override
	protected boolean compareContent(ACondition condition)
	{
		DestinationFreeCondition con = (DestinationFreeCondition) condition;
		
		if (getDestination().equals(con.getDestination()) && (considerFoeBots == con.considerFoeBots))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Loop over all tiger bots and check if they have pos equal to dest
	 * 
	 * @param worldFrame
	 * @param botId
	 * @return
	 */
	private boolean isDestinationFreeOfTigers(WorldFrame worldFrame, BotID botId)
	{
		for (Map.Entry<BotID, TrackedTigerBot> entry : worldFrame.tigerBotsVisible)
		{
			TrackedTigerBot bot = entry.getValue();
			if (entry.getKey().equals(botId))
			{
				continue;
			}
			if ((bot.getVel().getLength2() < VELOCITY_TOL) && bot.getPos().similar(getDestination(), getDestFreeTol()))
			{
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Loop over all foe bots and check if they have pos equal to dest
	 * 
	 * @param worldFrame
	 * @return
	 */
	private boolean isDestinationFreeOfFoes(WorldFrame worldFrame)
	{
		for (Map.Entry<BotID, TrackedBot> entry : worldFrame.foeBots)
		{
			if ((entry.getValue().getVel().getLength2() < VELOCITY_TOL)
					&& entry.getValue().getPos().similar(getDestination(), getDestFreeTol()))
			{
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * @param dest the destination to set
	 */
	public final void updateDestination(final IVector2 dest)
	{
		if (dest == null)
		{
			setActive(false);
			destination.set(Vector2.ZERO_VECTOR);
		} else
		{
			setActive(true);
			destination.set(dest);
		}
		resetCache();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the dest
	 */
	public final IVector2 getDestination()
	{
		if (!isActive())
		{
			throw new IllegalStateException("Condition is inactive");
		}
		return destination;
	}
	
	
	/**
	 * @return the considerFoeBots
	 */
	public boolean isConsiderFoeBots()
	{
		if (!isActive())
		{
			throw new IllegalStateException("Condition is inactive");
		}
		return considerFoeBots;
	}
	
	
	/**
	 * @param considerFoeBots the considerFoeBots to set
	 */
	public final void setConsiderFoeBots(final boolean considerFoeBots)
	{
		this.considerFoeBots = considerFoeBots;
		resetCache();
	}
	
	
	/**
	 * @return the destFreeTol
	 */
	public final float getDestFreeTol()
	{
		return destFreeTol;
	}
	
	
	/**
	 * @param destFreeTol the destFreeTol to set
	 */
	public final void setDestFreeTol(float destFreeTol)
	{
		this.destFreeTol = destFreeTol;
	}
	
	
}
