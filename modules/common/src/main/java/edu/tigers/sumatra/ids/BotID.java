/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 14, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sleepycat.persist.model.Persistent;


/**
 * Identifier for bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public final class BotID extends AObjectID
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long						serialVersionUID	= 3900802414469252613L;
	private final ETeamColor						teamColor;
															
	private static final BotID						UNINITIALIZED_ID	= new BotID();
	private static final Map<Integer, BotID>	YELLOW_BOT_IDS		= new ConcurrentHashMap<Integer, BotID>();
	private static final Map<Integer, BotID>	BLUE_BOT_IDS		= new ConcurrentHashMap<Integer, BotID>();
	private static final Map<Integer, BotID>	UNKNOWN_BOT_IDS	= new ConcurrentHashMap<Integer, BotID>();
																					
																					
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	private BotID()
	{
		super();
		teamColor = ETeamColor.UNINITIALIZED;
	}
	
	
	/**
	 * Creates a BotID with a team color of your choice
	 * 
	 * @param number id between BOT_ID_MIN and BOT_ID_MAX
	 * @param color
	 */
	private BotID(final int number, final ETeamColor color)
	{
		super(number);
		if (!((number >= AObjectID.BOT_ID_MIN) && (number <= AObjectID.BOT_ID_MAX))
				&& (number != AObjectID.UNINITIALIZED_ID))
		{
			throw new IllegalArgumentException("You tried to initialize a BotId with an invalid id: " + number);
		}
		teamColor = color;
	}
	
	
	/**
	 * @return
	 */
	public static BotID get()
	{
		return UNINITIALIZED_ID;
	}
	
	
	/**
	 * @param number
	 * @param color
	 * @return
	 */
	public static BotID createBotId(final int number, final ETeamColor color)
	{
		if (number == -1)
		{
			return UNINITIALIZED_ID;
		}
		switch (color)
		{
			case BLUE:
				return createBotId(BLUE_BOT_IDS, number, color);
			case UNINITIALIZED:
				return createBotId(UNKNOWN_BOT_IDS, number, color);
			case YELLOW:
				return createBotId(YELLOW_BOT_IDS, number, color);
			default:
				throw new IllegalStateException();
		}
	}
	
	
	/**
	 * Create botid from a number with color offset based BS format
	 * 
	 * @param number
	 * @return
	 */
	public static BotID createBotIdFromIdWithColorOffsetBS(int number)
	{
		if (number > BOT_ID_MAX_BS)
		{
			return UNINITIALIZED_ID;
		}
		
		ETeamColor color = ETeamColor.YELLOW;
		if (number > BOT_ID_MAX)
		{
			number -= BOT_ID_MAX + 1;
			color = ETeamColor.BLUE;
		}
		return createBotId(number, color);
	}
	
	
	private static BotID createBotId(final Map<Integer, BotID> bots, final int number, final ETeamColor color)
	{
		BotID botId = bots.get(number);
		if (botId == null)
		{
			botId = new BotID(number, color);
			bots.put(number, botId);
		}
		return botId;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		return "BotID " + getNumber() + " " + teamColor;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * For Yellow color, ids are normal, for blue color, id + 100 is returned
	 * 
	 * @return
	 */
	public int getNumberWithColorOffset()
	{
		return getNumber() + (teamColor == ETeamColor.YELLOW ? 0 : 100);
	}
	
	
	/**
	 * Get number with color offset in basestation format
	 * 
	 * @return
	 */
	public int getNumberWithColorOffsetBS()
	{
		return getNumber() + (teamColor == ETeamColor.YELLOW ? 0 : AObjectID.BOT_ID_MAX + 1);
	}
	
	
	/**
	 * @return the teamColor
	 */
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((teamColor == null) ? 0 : teamColor.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		BotID other = (BotID) obj;
		if (teamColor != other.teamColor)
		{
			return false;
		}
		return true;
	}
	
	
	@Override
	public int compareTo(final AObjectID o)
	{
		if (o.getClass().equals(BotID.class))
		{
			return Integer.compare(getNumberWithColorOffset(), ((BotID) o).getNumberWithColorOffset());
		}
		return super.compareTo(o);
	}
	
	
	/**
	 * @return
	 */
	public static Comparator<BotID> getComparator()
	{
		return new Comparator<BotID>()
		{
			
			@Override
			public int compare(final BotID o1, final BotID o2)
			{
				return Integer.compare(o1.getNumberWithColorOffset(), o2.getNumberWithColorOffset());
			}
		};
	}
	
	
	/**
	 * @return
	 */
	public static Collection<BotID> getAllYellow()
	{
		return new ArrayList<>(YELLOW_BOT_IDS.values());
	}
	
	
	/**
	 * @return
	 */
	public static Collection<BotID> getAllBlue()
	{
		return new ArrayList<>(BLUE_BOT_IDS.values());
	}
	
	
	/**
	 * @return new list with all known bot ids
	 */
	public static Collection<BotID> getAll()
	{
		Collection<BotID> all = getAllBlue();
		all.addAll(getAllYellow());
		return all;
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static Collection<BotID> getAll(final ETeamColor color)
	{
		if (color == ETeamColor.BLUE)
		{
			return getAllBlue();
		} else if (color == ETeamColor.YELLOW)
		{
			return getAllYellow();
		}
		throw new IllegalArgumentException("Only blue or yellow is allowed");
	}
}
