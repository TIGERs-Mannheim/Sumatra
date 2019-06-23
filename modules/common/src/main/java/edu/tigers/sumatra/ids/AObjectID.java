/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

import java.io.Serializable;

import com.sleepycat.persist.model.Persistent;


/**
 * Object identifier.
 * 
 * @author Oliver Steinbrecher
 */
@Persistent
public abstract class AObjectID implements Comparable<AObjectID>, Serializable
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= -1210556807036502590L;
	
	/** needs to be 255 thus the bot firmware can perform a deinitialization of the network interface */
	public static final int		UNINITIALIZED_ID	= 255;
	
	/** */
	protected static final int	BALL_ID				= -1;
	
	/** */
	public static final int		BOT_ID_MIN			= 0;
	/** */
	public static final int		BOT_ID_MAX			= 11;
	/** */
	public static final int		BOT_ID_MAX_BS		= 23;
	
	
	// --------------------------------------------------------------------------
	
	private int						number;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Creates an uninitialized {@link AObjectID}.
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
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the number
	 */
	public int getNumber()
	{
		return number;
	}
	
	
	/**
	 * @return if {@link AObjectID} is not initialized.
	 */
	public boolean isUninitializedID()
	{
		return number == UNINITIALIZED_ID;
	}
	
	
	/**
	 * @return true when this is a {@link AObjectID}{@link #BALL_ID}
	 */
	public boolean isBall()
	{
		if (number == BALL_ID)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @return true when this is a BotId ( checks valid range {@link AObjectID}{@link #BOT_ID_MIN} - {@link AObjectID}
	 *         {@link #BOT_ID_MAX} )
	 */
	public boolean isBot()
	{
		if ((number >= BOT_ID_MIN) && (number <= BOT_ID_MAX))
		{
			return true;
		}
		return false;
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
		if (number != other.number)
		{
			return false;
		}
		return true;
	}
}
