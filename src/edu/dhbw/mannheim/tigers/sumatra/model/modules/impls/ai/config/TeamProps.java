/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import java.io.Serializable;

import org.apache.commons.configuration.Configuration;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;


/**
 * The data-holder for team-specific things like our color, the direction of play and the keeper-id.
 * 
 * @author Gero
 */
@Persistent(version = 1)
public class TeamProps implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 4029974009206769185L;
	
	private int						keeperIdBlue;
	private int						keeperIdYellow;
	
	/** */
	private ETeamColor			leftTeam;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public TeamProps()
	{
		keeperIdYellow = 0;
		keeperIdBlue = 0;
		leftTeam = ETeamColor.YELLOW;
	}
	
	
	/**
	 * @param configFile
	 */
	public TeamProps(final Configuration configFile)
	{
		keeperIdYellow = configFile.getInt("keeperIdYellow");
		keeperIdBlue = configFile.getInt("keeperIdBlue");
		String strLeftTeam = configFile.getString("leftTeam", ETeamColor.YELLOW.name());
		try
		{
			leftTeam = ETeamColor.valueOf(strLeftTeam);
		} catch (IllegalArgumentException err)
		{
			leftTeam = ETeamColor.YELLOW;
		}
	}
	
	
	/**
	 * @param original
	 */
	public TeamProps(final TeamProps original)
	{
		keeperIdYellow = original.keeperIdYellow;
		keeperIdBlue = original.keeperIdBlue;
		leftTeam = original.leftTeam;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the keeperId
	 */
	public int getKeeperIdYellow()
	{
		return keeperIdYellow;
	}
	
	
	/**
	 * @return the keeperId
	 */
	public int getKeeperIdBlue()
	{
		return keeperIdBlue;
	}
	
	
	/**
	 * @param keeperId the keeperId to set
	 */
	public final void setKeeperIdBlue(final int keeperId)
	{
		keeperIdBlue = keeperId;
	}
	
	
	/**
	 * @param keeperId the keeperId to set
	 */
	public final void setKeeperIdYellow(final int keeperId)
	{
		keeperIdYellow = keeperId;
	}
	
	
	/**
	 * @return the leftTeam
	 */
	public final ETeamColor getLeftTeam()
	{
		return leftTeam;
	}
	
	
	/**
	 * @param leftTeam the leftTeam to set
	 */
	public final void setLeftTeam(final ETeamColor leftTeam)
	{
		this.leftTeam = leftTeam;
	}
	
}
