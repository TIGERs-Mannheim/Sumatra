/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.referee;

import java.util.HashSet;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Contain information about teams and play direction
 * 
 * @author Gero
 */
public final class TeamConfig
{
	@Configurable
	private static int			keeperIdBlue	= 0;
	@Configurable
	private static int			keeperIdYellow	= 0;
	@Configurable
	private static ETeamColor	leftTeam			= ETeamColor.BLUE;
	
	
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
	 * @return
	 */
	public static final BotID getKeeperBotIDBlue()
	{
		return BotID.createBotId(keeperIdBlue, ETeamColor.BLUE);
	}
	
	
	/**
	 * @return
	 */
	public static final BotID getKeeperBotIDYellow()
	{
		return BotID.createBotId(keeperIdYellow, ETeamColor.YELLOW);
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static final BotID getKeeperBotID(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return getKeeperBotIDBlue();
			case YELLOW:
				return getKeeperBotIDYellow();
			default:
				throw new IllegalArgumentException();
		}
	}
	
	
	/**
	 * Returns the keeper IDs as set
	 * 
	 * @return modifiable set of the keeper ids
	 */
	public static Set<BotID> getKeeperIDs()
	{
		Set<BotID> keeperSet = new HashSet<>();
		keeperSet.add(getKeeperBotIDBlue());
		keeperSet.add(getKeeperBotIDYellow());
		return keeperSet;
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
