package edu.tigers.sumatra.referee.control;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Data structure for commands from game controller
 */
public class EventCommand
{
	private Command commandType;
	private EventTeam forTeam;
	private EventLocation location;
	
	
	public EventCommand(final Command commandType)
	{
		this.commandType = commandType;
	}
	
	
	public EventCommand(final Command commandType, final EventTeam forTeam)
	{
		this.forTeam = forTeam;
		this.commandType = commandType;
	}
	
	
	public EventCommand(final Command commandType, final EventTeam forTeam, final EventLocation location)
	{
		this.commandType = commandType;
		this.forTeam = forTeam;
		this.location = location;
	}
	
	
	public EventTeam getForTeam()
	{
		return forTeam;
	}
	
	
	public void setForTeam(final EventTeam forTeam)
	{
		this.forTeam = forTeam;
	}
	
	
	public Command getCommandType()
	{
		return commandType;
	}
	
	
	public void setCommandType(final Command commandType)
	{
		this.commandType = commandType;
	}
	
	
	public EventLocation getLocation()
	{
		return location;
	}
	
	
	public void setLocation(final EventLocation location)
	{
		this.location = location;
	}
	
	public enum Command
	{
		@JsonProperty("halt")
		HALT,
		@JsonProperty("stop")
		STOP,
		@JsonProperty("normalStart")
		NORMAL_START,
		@JsonProperty("forceStart")
		FORCE_START,
		@JsonProperty("direct")
		DIRECT,
		@JsonProperty("indirect")
		INDIRECT,
		@JsonProperty("kickoff")
		KICKOFF,
		@JsonProperty("penalty")
		PENALTY,
		@JsonProperty("timeout")
		TIMEOUT,
		@JsonProperty("ballPlacement")
		BALL_PLACEMENT,
	}
}
