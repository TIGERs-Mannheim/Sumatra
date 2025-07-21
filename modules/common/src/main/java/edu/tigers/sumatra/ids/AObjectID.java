/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ids;

/**
 * Object identifier.
 * 
 * @author Oliver Steinbrecher
 */
public abstract class AObjectID implements Comparable<AObjectID>
{
	/** needs to be 255 thus the bot firmware can perform a deinitialization of the network interface */
	public static final int UNINITIALIZED_ID = 255;
	
	/** */
	protected static final int BALL_ID = -1;
	
	/** */
	public static final int BOT_ID_MIN = 0;
	/** */
	public static final int BOT_ID_MAX = 15;
	public static final int BOT_ID_MIDDLE_BS = 15;
	/** */
	public static final int BOT_ID_MAX_BS = 31;
	
	
	private int number;
	
	
	/**
	 * Creates an uninitialized id
	 */
	public AObjectID()
	{
		number = UNINITIALIZED_ID;
	}
	
	
	/**
	 * @param number
	 */
	public AObjectID(final int number)
	{
		if (number == UNINITIALIZED_ID)
		{
			this.number = number;
		} else
		{
			if ((number == BALL_ID) || ((number >= BOT_ID_MIN) && (number <= BOT_ID_MAX)))
			{
				this.number = number;
			} else
			{
				throw new IllegalArgumentException(" This is not a valid ID/number: " + number);
			}
		}
	}
	
	
	@Override
	public int compareTo(final AObjectID o)
	{
		return Integer.compare(getNumber(), o.getNumber());
	}
	
	
	@Override
	public String toString()
	{
		return "ObjectID[ " + getNumber() + "]";
	}
	
	
	public String getSaveableString()
	{
		return String.valueOf(getNumber());
	}
	
	
	/**
	 * @return the number
	 */
	public int getNumber()
	{
		return number;
	}
	
	
	public boolean isUninitializedID()
	{
		return number == UNINITIALIZED_ID;
	}
	
	
	/**
	 * @return true when this is a {@link #BALL_ID}
	 */
	public boolean isBall()
	{
		return number == BALL_ID;
	}
	
	
	/**
	 * @return true when this is a BotId ( checks valid range {@link #BOT_ID_MIN} - {@link #BOT_ID_MAX} )
	 */
	public boolean isBot()
	{
		return (number >= BOT_ID_MIN) && (number <= BOT_ID_MAX);
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + number;
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AObjectID other = (AObjectID) obj;
		return number == other.number;
	}
}
