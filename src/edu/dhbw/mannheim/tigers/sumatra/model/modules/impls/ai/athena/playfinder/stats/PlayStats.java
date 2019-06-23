/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlayState;


/**
 * Data holder for statistics of a single play
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Entity
public class PlayStats
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Enumerated(EnumType.STRING)
	private EPlay					play;
	@Enumerated(EnumType.STRING)
	private EPlayState			playResult;
	private int						numberOfRoles;
	@Enumerated(EnumType.STRING)
	private ESelectionReason	selectionReason;
	private long					startTime;
	private long					endTime;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param play
	 * @param result
	 * @param numberOfRoles
	 * @param selectionReason
	 * @param startTime
	 * @param endTime
	 */
	public PlayStats(EPlay play, EPlayState result, int numberOfRoles, ESelectionReason selectionReason, long startTime,
			long endTime)
	{
		this.play = play;
		this.playResult = result;
		this.numberOfRoles = numberOfRoles;
		this.selectionReason = selectionReason;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + numberOfRoles;
		result = (prime * result) + ((play == null) ? 0 : play.hashCode());
		result = (prime * result) + ((this.playResult == null) ? 0 : this.playResult.hashCode());
		result = (prime * result) + ((selectionReason == null) ? 0 : selectionReason.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj)
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
		PlayStats other = (PlayStats) obj;
		if (numberOfRoles != other.numberOfRoles)
		{
			return false;
		}
		if (play != other.play)
		{
			return false;
		}
		if (playResult != other.playResult)
		{
			return false;
		}
		if (selectionReason != other.selectionReason)
		{
			return false;
		}
		return true;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("PlayStats [play=");
		builder.append(play);
		builder.append(", result=");
		builder.append(playResult);
		builder.append(", numberOfRoles=");
		builder.append(numberOfRoles);
		builder.append(", selectionReason=");
		builder.append(selectionReason);
		builder.append("]");
		return builder.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the play
	 */
	public final EPlay getPlay()
	{
		return play;
	}
	
	
	/**
	 * @return the result
	 */
	public final EPlayState getResult()
	{
		return playResult;
	}
	
	
	/**
	 * @return the numberOfRoles
	 */
	public final int getNumberOfRoles()
	{
		return numberOfRoles;
	}
	
	
	/**
	 * @return the selectionReason
	 */
	public final ESelectionReason getSelectionReason()
	{
		return selectionReason;
	}
	
	
	/**
	 * @return the startTime
	 */
	public final long getStartTime()
	{
		return startTime;
	}
	
	
	/**
	 * @return the endTime
	 */
	public final long getEndTime()
	{
		return endTime;
	}
}
