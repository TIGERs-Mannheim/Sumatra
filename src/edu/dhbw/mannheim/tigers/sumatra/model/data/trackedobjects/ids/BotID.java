/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.09.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;


/**
 * Identifier for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot}.
 * 
 * @author Oliver Steinbrecher
 * 
 */
@Embeddable
public class BotID extends AObjectID
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6393895441923435492L;
	
	private ETeam					team					= ETeam.UNKNOWN;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Creates an uninitialized {@link BotID}.
	 */
	public BotID()
	{
		super();
	}
	
	
	/**
	 * Creates a tiger {@link BotID} concerning the team color in the moduli configuration file.
	 * 
	 * @param number id between BOT_ID_MIN and BOT_ID_MAX
	 */
	public BotID(int number)
	{
		super(number);
		if (!((number >= AObjectID.BOT_ID_MIN) && (number <= AObjectID.BOT_ID_MAX))
				&& (number != AObjectID.UNINITIALIZED_ID))
		{
			throw new IllegalArgumentException("You tried to initialize a BotId with an invalid id: " + number);
		}
		team = ETeam.TIGERS;
	}
	
	
	/**
	 * Creates a BotID with a team of your choice
	 * @param number id between BOT_ID_MIN and BOT_ID_MAX
	 * @param team TIGERS or OPPONENTS
	 */
	public BotID(int number, ETeam team)
	{
		this(number);
		if ((team == ETeam.TIGERS) || (team == ETeam.OPPONENTS))
		{
			this.team = team;
		} else
		{
			throw new IllegalArgumentException("Team must be TIGERS or OPPONENTS!");
		}
	}
	
	
	/**
	 * Deep copy
	 * @param botID
	 */
	public BotID(BotID botID)
	{
		this(botID.getNumber(), botID.team);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		return "BotID [team=" + team + ", number=" + getNumber() + "]";
	}
	
	
	/**
	 * @return the team
	 */
	public ETeam getTeam()
	{
		return team;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((team == null) ? 0 : team.hashCode());
		result = (prime * result) + getNumber();
		return result;
	}
	
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof BotID))
		{
			return false;
		}
		if ((getNumber() == ((BotID) o).getNumber()) && (getTeam() == ((BotID) o).getTeam()))
		{
			return true;
		}
		return false;
	}
}
