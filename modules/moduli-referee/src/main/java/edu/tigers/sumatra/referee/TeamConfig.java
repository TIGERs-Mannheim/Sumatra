/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Contain information about teams and play direction
 * 
 * @author Gero
 */
public final class TeamConfig
{
	@Configurable
	private static int keeperIdBlue = 0;
	@Configurable
	private static int keeperIdYellow = 0;
	@Configurable
	private static ETeamColor leftTeam = ETeamColor.BLUE;
	
	
	static
	{
		ConfigRegistration.registerClass("team", TeamConfig.class);
	}
	
	
	private TeamConfig()
	{
	}
	
	
	/**
	 * @return the keeperIdBlue
	 */
	public static int getKeeperIdBlue()
	{
		return keeperIdBlue;
	}
	
	
	/**
	 * @return the keeperIdYellow
	 */
	public static int getKeeperIdYellow()
	{
		return keeperIdYellow;
	}
	
	
	/**
	 * @param teamColor
	 * @return the keeper id of given team
	 */
	public static int getKeeperId(final ETeamColor teamColor)
	{
		switch (teamColor)
		{
			case BLUE:
				return keeperIdBlue;
			case YELLOW:
				return keeperIdYellow;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	
	/**
	 * @return the leftTeam
	 */
	public static ETeamColor getLeftTeam()
	{
		return leftTeam;
	}
	
	
	public static void setLeftTeam(final ETeamColor team)
	{
		leftTeam = team;
	}
	
	
	/**
	 * @param keeperIdBlue the keeperIdBlue to set
	 */
	public static void setKeeperIdBlue(final int keeperIdBlue)
	{
		TeamConfig.keeperIdBlue = keeperIdBlue;
	}
	
	
	/**
	 * @param keeperIdYellow the keeperIdYellow to set
	 */
	public static void setKeeperIdYellow(final int keeperIdYellow)
	{
		TeamConfig.keeperIdYellow = keeperIdYellow;
	}
}
