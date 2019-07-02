package edu.tigers.sumatra.referee.control;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tigers.sumatra.ids.ETeamColor;


public enum EventTeam
{
	@JsonProperty("Yellow")
	YELLOW,
	@JsonProperty("Blue")
	BLUE,
	/** Both or neutral team **/
	@JsonProperty("Both")
	BOTH,
	
	;
	
	/**
	 * Map a team color to event team
	 * 
	 * @param teamColor
	 * @return
	 */
	public static EventTeam fromTeamColor(ETeamColor teamColor)
	{
		switch (teamColor)
		{
			case YELLOW:
				return YELLOW;
			case BLUE:
				return BLUE;
			case NEUTRAL:
				return BOTH;
			default:
				throw new IllegalStateException("Unsupported team color: " + teamColor);
		}
	}
}
