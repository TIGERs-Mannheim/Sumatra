/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ids;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EAiTeam
{
	/** */
	YELLOW(ETeamColor.YELLOW, true),
	/** */
	BLUE(ETeamColor.BLUE, true),
	
	;
	
	private ETeamColor teamColor;
	private boolean activeByDefault;
	
	
	/**
	 * @param teamColor the associated team color
	 * @param activeByDefault active by default
	 */
	EAiTeam(ETeamColor teamColor, boolean activeByDefault)
	{
		this.teamColor = teamColor;
		this.activeByDefault = activeByDefault;
	}
	
	
	public ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	public boolean isActiveByDefault()
	{
		return activeByDefault;
	}
	
	
	/**
	 * Get the primary team of the given team color
	 * 
	 * @param teamColor
	 * @return
	 */
	public static EAiTeam primary(ETeamColor teamColor)
	{
		if (teamColor == ETeamColor.BLUE)
		{
			return BLUE;
		} else if (teamColor == ETeamColor.YELLOW)
		{
			return YELLOW;
		}
		throw new IllegalArgumentException("Can not map team color: " + teamColor);
	}
	
	
	/**
	 * Check if color of aiteam matches color
	 *
	 * @param color
	 * @return
	 */
	public boolean matchesColor(ETeamColor color)
	{
		return color == teamColor;
	}
}
