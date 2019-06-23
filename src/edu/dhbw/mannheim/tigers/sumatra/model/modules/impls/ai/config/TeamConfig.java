/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Contain information about teams and play direction
 * 
 * @author Gero
 */
public final class TeamConfig
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable
	private static int			keeperIdBlue	= 0;
	@Configurable
	private static int			keeperIdYellow	= 0;
	@Configurable
	private static ETeamColor	leftTeam			= ETeamColor.BLUE;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private TeamConfig()
	{
	}
	
	
	/**
	 * @return the keeperIdBlue
	 */
	public static final int getKeeperIdBlue()
	{
		return keeperIdBlue;
	}
	
	
	/**
	 * @return the keeperIdYellow
	 */
	public static final int getKeeperIdYellow()
	{
		return keeperIdYellow;
	}
	
	
	/**
	 * @param tc
	 * @return
	 */
	public static final int getKeeperId(final ETeamColor tc)
	{
		if (tc == ETeamColor.BLUE)
		{
			return keeperIdBlue;
		} else if (tc == ETeamColor.YELLOW)
		{
			return keeperIdYellow;
		}
		throw new IllegalArgumentException();
	}
	
	
	/**
	 * @return the leftTeam
	 */
	public static final ETeamColor getLeftTeam()
	{
		return leftTeam;
	}
	
	
	/**
	 * @param keeperIdBlue the keeperIdBlue to set
	 */
	public static final void setKeeperIdBlue(final int keeperIdBlue)
	{
		TeamConfig.keeperIdBlue = keeperIdBlue;
	}
	
	
	/**
	 * @param keeperIdYellow the keeperIdYellow to set
	 */
	public static final void setKeeperIdYellow(final int keeperIdYellow)
	{
		TeamConfig.keeperIdYellow = keeperIdYellow;
	}
}
