/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.lachesis;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;


/**
 * Info about role numbers, etc.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class RoleFinderInfo
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log						= Logger.getLogger(RoleFinderInfo.class.getName());
	private final int					minRoles;
	private final int					maxRoles;
	private final int					desiredRoles;
	private final List<BotID>		desiredBots				= new ArrayList<BotID>(3);
	private int							forceNumDesiredBots	= 0;
	
	
	@SuppressWarnings("unused")
	private RoleFinderInfo()
	{
		minRoles = 0;
		maxRoles = 0;
		desiredRoles = 0;
	}
	
	
	/**
	 * @param minRoles
	 * @param maxRoles
	 * @param desiredRoles
	 */
	public RoleFinderInfo(final int minRoles, final int maxRoles, final int desiredRoles)
	{
		super();
		this.minRoles = minRoles;
		this.maxRoles = maxRoles;
		this.desiredRoles = desiredRoles;
	}
	
	
	/**
	 * @param value
	 */
	public RoleFinderInfo(final RoleFinderInfo value)
	{
		this(value.minRoles, value.maxRoles, value.desiredRoles);
		desiredBots.addAll(value.desiredBots);
	}
	
	
	/**
	 * @return the minRoles
	 */
	public int getMinRoles()
	{
		return minRoles;
	}
	
	
	/**
	 * @return the maxRoles
	 */
	public int getMaxRoles()
	{
		return maxRoles;
	}
	
	
	/**
	 * @return the desiredRoles
	 */
	public int getDesiredRoles()
	{
		return desiredRoles;
	}
	
	
	/**
	 * @return the desiredBots
	 */
	public List<BotID> getDesiredBots()
	{
		return desiredBots;
	}
	
	
	/**
	 * @return the forceNumDesiredBots
	 */
	public int getForceNumDesiredBots()
	{
		return forceNumDesiredBots;
	}
	
	
	/**
	 * Force the first forceNumDesiredBots number of bots in the desiredBots list, regardless of their usefulness
	 * 
	 * @param forceNumDesiredBots the forceNumDesiredBots to set
	 */
	public void setForceNumDesiredBots(final int forceNumDesiredBots)
	{
		if (forceNumDesiredBots > desiredBots.size())
		{
			log.warn("Tried to set forceNumDesiredBots=" + forceNumDesiredBots + ", but only " + desiredBots.size()
					+ " desired bots available!");
			this.forceNumDesiredBots = desiredBots.size();
		} else
		{
			this.forceNumDesiredBots = forceNumDesiredBots;
		}
	}
	
	
	@Override
	public String toString()
	{
		return "RoleFinderInfo [minRoles=" + minRoles + ", maxRoles=" + maxRoles + ", desiredRoles=" + desiredRoles
				+ ", desiredBots=" + desiredBots + ", forceNumDesiredBots=" + forceNumDesiredBots + "]";
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((desiredBots == null) ? 0 : desiredBots.hashCode());
		result = (prime * result) + desiredRoles;
		result = (prime * result) + forceNumDesiredBots;
		result = (prime * result) + maxRoles;
		result = (prime * result) + minRoles;
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
		RoleFinderInfo other = (RoleFinderInfo) obj;
		if (desiredBots == null)
		{
			if (other.desiredBots != null)
			{
				return false;
			}
		} else if (!desiredBots.equals(other.desiredBots))
		{
			return false;
		}
		if (desiredRoles != other.desiredRoles)
		{
			return false;
		}
		if (forceNumDesiredBots != other.forceNumDesiredBots)
		{
			return false;
		}
		if (maxRoles != other.maxRoles)
		{
			return false;
		}
		if (minRoles != other.minRoles)
		{
			return false;
		}
		return true;
	}
}
