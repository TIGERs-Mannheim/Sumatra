package edu.tigers.sumatra.referee.control;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Data structure for triggers from game controller
 */
public class EventTrigger
{
	private Type triggerType;
	
	
	public EventTrigger(Type triggerType)
	{
		this.triggerType = triggerType;
	}
	
	public enum Type
	{
		@JsonProperty("resetMatch")
		RESET_MATCH,
		@JsonProperty("switchColor")
		SWITCH_COLOR,
		@JsonProperty("switchSides")
		SWITCH_SIDES,
		@JsonProperty("undo")
		UNDO,
		@JsonProperty("continue")
		CONTINUE
	}
	
	
	public Type getTriggerType()
	{
		return triggerType;
	}
	
	
	public void setTriggerType(final Type triggerType)
	{
		this.triggerType = triggerType;
	}
}
