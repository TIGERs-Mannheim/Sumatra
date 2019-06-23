/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.04.2014
 * Author(s): Simon
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects;

import java.io.Serializable;
import java.util.Comparator;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * A Bot with a Value
 * 
 * @author Simon
 * 
 */
@Persistent
public class ValueBot
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private BotID													bot						= null;
	private float													value						= 0;
	
	/**  */
	public static final Comparator<? super ValueBot>	VALUEHIGHCOMPARATOR	= new ValueHighComparator();
	/**  */
	public static final Comparator<? super ValueBot>	VALUELOWCOMPARATOR	= new ValueLowComparator();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private ValueBot()
	{
	}
	
	
	/**
	 * @param bot
	 * @param value
	 */
	public ValueBot(BotID bot, float value)
	{
		this.bot = bot;
		this.value = value;
	}
	
	
	/**
	 * @param bot
	 */
	public ValueBot(BotID bot)
	{
		this.bot = bot;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		return Float.floatToIntBits(value);
	}
	
	
	@Override
	public String toString()
	{
		return "(Bot=" + bot.toString() + ",val=" + value + ")";
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		final ValueBot other = (ValueBot) obj;
		if (!SumatraMath.isEqual(value, other.value))
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * 
	 * Sort {@link ValuePoint} after Value, highest value first.
	 * 
	 */
	private static class ValueHighComparator implements Comparator<ValueBot>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(ValueBot v1, ValueBot v2)
		{
			if (v1.value < v2.value)
			{
				return 1;
			} else if (v1.value > v2.value)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	
	/**
	 * 
	 * Sort {@link ValuePoint} after Value, lowest value first.
	 * 
	 */
	private static class ValueLowComparator implements Comparator<ValueBot>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(ValueBot v1, ValueBot v2)
		{
			if (v1.value > v2.value)
			{
				return 1;
			} else if (v1.value < v2.value)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * @return
	 */
	public float getValue()
	{
		return value;
	}
	
	
	/**
	 * 
	 * @param value
	 */
	public void setValue(float value)
	{
		this.value = value;
	}
	
	
	/**
	 * @return
	 */
	public BotID getBotID()
	{
		return bot;
	}
	
	
}
