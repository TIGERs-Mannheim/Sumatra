/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.08.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Checks, if destination is free of bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DestinationFreeCondition extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	VELOCITY_TOL		= 0.1f;
	private final Vector2		destination			= new Vector2();
	private boolean				considerFoeBots	= true;
	
	@Configurable(comment = "Dist [mm] - Distance between two bots up to which they are considered to conflict each other in position")
	private static float			destFreeTol			= 180;
	
	/** tolerance for this instance */
	private float					destFreeTolerance	= destFreeTol;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * New inactive condition
	 * 
	 * @param botId
	 */
	public DestinationFreeCondition(final BotID botId)
	{
		this(botId, null, false);
	}
	
	
	/**
	 * @param botId
	 * @param dest
	 * @param considerFoeBots
	 */
	public DestinationFreeCondition(final BotID botId, final IVector2 dest, final boolean considerFoeBots)
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
	protected EConditionState doCheckCondition(final SimpleWorldFrame worldFrame, final BotID botID)
	{
		for (Map.Entry<BotID, TrackedTigerBot> entry : worldFrame.getBots().entrySet())
		{
			TrackedTigerBot bot = entry.getValue();
			if (entry.getKey().equals(botID))
			{
				continue;
			}
			if ((bot.getVel().getLength2() < VELOCITY_TOL) && bot.getPos().similar(getDestination(), destFreeTol))
			{
				if (bot.getId().getTeamColor().equals(botID.getTeamColor()))
				{
					// tiger bot
					setCondition("Blocking Tigers");
					return EConditionState.BLOCKED;
				}
				setCondition("Blocking Foes");
				return EConditionState.BLOCKED;
			}
		}
		
		setCondition("Free");
		return EConditionState.FULFILLED;
	}
	
	
	@Override
	protected boolean compareContent(final ACondition condition)
	{
		DestinationFreeCondition con = (DestinationFreeCondition) condition;
		
		if (getDestination().equals(con.getDestination()) && (considerFoeBots == con.considerFoeBots))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param dest the destination to set
	 */
	public final void updateDestination(final IVector2 dest)
	{
		if (dest == null)
		{
			setActive(false);
			destination.set(AVector2.ZERO_VECTOR);
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
	 * @return the destFreeTolerance
	 */
	public final float getDestFreeTolerance()
	{
		return destFreeTolerance;
	}
	
	
	/**
	 * @param destFreeTolerance the destFreeTolerance to set
	 */
	public final void setDestFreeTolerance(final float destFreeTolerance)
	{
		this.destFreeTolerance = destFreeTolerance;
	}
}
