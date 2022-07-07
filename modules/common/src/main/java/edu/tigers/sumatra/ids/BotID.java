/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ids;

import com.sleepycat.persist.model.Persistent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Identifier for bots
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public final class BotID extends AObjectID
{
	private final ETeamColor teamColor;

	private static final BotID NO_BOT_ID = new BotID();
	private static final Map<Integer, BotID> YELLOW_BOT_IDS = new ConcurrentHashMap<>();
	private static final Map<Integer, BotID> BLUE_BOT_IDS = new ConcurrentHashMap<>();
	private static final Map<Integer, BotID> UNKNOWN_BOT_IDS = new ConcurrentHashMap<>();


	static
	{
		for (int i = 0; i <= AObjectID.BOT_ID_MAX; i++)
		{
			YELLOW_BOT_IDS.put(i, new BotID(i, ETeamColor.YELLOW));
			BLUE_BOT_IDS.put(i, new BotID(i, ETeamColor.BLUE));
		}
	}


	/**
	 *
	 */
	private BotID()
	{
		super();
		teamColor = ETeamColor.NEUTRAL;
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
	public static BotID noBot()
	{
		return NO_BOT_ID;
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
			return NO_BOT_ID;
		}
		return switch (color)
				{
					case BLUE -> createBotId(BLUE_BOT_IDS, number, color);
					case NEUTRAL -> createBotId(UNKNOWN_BOT_IDS, number, color);
					case YELLOW -> createBotId(YELLOW_BOT_IDS, number, color);
				};
	}


	/**
	 * Create botid from a number with color offset based BS format
	 *
	 * @param number
	 * @return
	 */
	public static BotID createBotIdFromIdWithColorOffsetBS(final int number)
	{
		if (number > BOT_ID_MAX_BS)
		{
			return NO_BOT_ID;
		}

		ETeamColor color = ETeamColor.YELLOW;
		int colorDependentNumber = number;
		if (number > BOT_ID_MIDDLE_BS)
		{
			colorDependentNumber -= BOT_ID_MIDDLE_BS + 1;
			color = ETeamColor.BLUE;
		}
		return createBotId(colorDependentNumber, color);
	}


	private static BotID createBotId(final Map<Integer, BotID> bots, final int number, final ETeamColor color)
	{
		return bots.computeIfAbsent(number, k -> new BotID(number, color));
	}


	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------


	@Override
	public String toString()
	{
		return "BotID " + getNumber() + " " + teamColor.name().charAt(0);
	}


	@Override
	public String getSaveableString()
	{
		return String.format("%d %s", getNumber(), getTeamColor().name().charAt(0));
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
		return getNumber() + (teamColor == ETeamColor.YELLOW ? 0 : AObjectID.BOT_ID_MIDDLE_BS + 1);
	}


	/**
	 * @return the teamColor
	 */
	public ETeamColor getTeamColor()
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
		return teamColor == other.teamColor;
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
		return new BotIdComparator();
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


	private static class BotIdComparator implements Comparator<BotID>
	{

		@Override
		public int compare(final BotID o1, final BotID o2)
		{
			return Integer.compare(o1.getNumberWithColorOffset(), o2.getNumberWithColorOffset());
		}
	}
}
