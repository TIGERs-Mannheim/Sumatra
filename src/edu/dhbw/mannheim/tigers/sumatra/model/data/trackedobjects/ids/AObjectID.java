/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids;

import java.io.Serializable;

import javax.persistence.Embeddable;


/**
 * Object identifier.
 * 
 * @author Oliver Steinbrecher
 */
@Embeddable
public abstract class AObjectID implements Comparable<AObjectID>, Serializable
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long		serialVersionUID	= -1210556807036502590L;
	
	/** needs to be 255 thus the bot firmware can perform a deinitialization of the network interface */
	public static final int			UNINITIALIZED_ID	= 255;
	
	/** */
	protected static final int		BALL_ID				= -1;
	
	/** */
	public static final int			BOT_ID_MIN			= 0;
	/** */
	public static final int			BOT_ID_MAX			= 13;
	
	/**  */
	public static final AObjectID	UNINITIALIZED_OID	= new UninitializedID();
	
	
	// --------------------------------------------------------------------------
	
	private int							number;
	
	
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
	public AObjectID(int number)
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
	public int hashCode()
	{
		return number;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if ((obj == null) || (getClass() != obj.getClass()))
		{
			return false;
		}
		
		final AObjectID other = (AObjectID) obj;
		if (number != other.number)
		{
			return false;
		}
		return true;
	}
	
	
	@Override
	public int compareTo(AObjectID o)
	{
		int compareResult = 0;
		if (getNumber() < o.getNumber())
		{
			compareResult = -1;
		} else
		{
			if (getNumber() > o.getNumber())
			{
				compareResult = 1;
			}
		}
		return compareResult;
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
}
