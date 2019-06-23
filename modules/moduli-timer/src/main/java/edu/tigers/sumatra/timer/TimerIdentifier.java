/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.timer;

import java.util.Comparator;


/**
 * Identifier consisting of {@link String} and frame id
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TimerIdentifier
{
	private final String	timable;
	private final long		id;
	private final int			customId;
	
	
	/**
	 * @param timable
	 * @param id
	 */
	public TimerIdentifier(final String timable, final long id)
	{
		this.timable = timable;
		this.id = id;
		customId = 0;
	}
	
	
	/**
	 * @param timable
	 * @param id
	 * @param customId
	 */
	public TimerIdentifier(final String timable, final long id, final int customId)
	{
		this.timable = timable;
		this.id = id;
		this.customId = customId;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (id ^ (id >>> 32));
		result = (prime * result) + customId;
		result = (prime * result) + ((timable == null) ? 0 : timable.hashCode());
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
		TimerIdentifier other = (TimerIdentifier) obj;
		return id == other.id && timable.equals(other.timable) && customId == other.customId;
	}
	
	
	/**
	 * @return
	 */
	public static Comparator<TimerIdentifier> getComparator()
	{
		return (o1, o2) -> {
			int cmpid = Long.compare(o1.id, o2.id);
			if (cmpid == 0)
			{
				return o1.timable.compareTo(o2.timable);
			}
			return cmpid;
		};
	}
	
	
	/**
	 * @return the timable
	 */
	public final String getTimable()
	{
		return timable;
	}
	
	
	/**
	 * @return the id
	 */
	public final long getId()
	{
		return id;
	}
	
	
	/**
	 * @return the customId
	 */
	public final int getCustomId()
	{
		return customId;
	}
}
