package edu.tigers.sumatra.referee.control;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Data structure for stage changes from game controller
 */
public class EventStage
{
	private Operation stageOperation;
	
	
	public EventStage(Operation stageOperation)
	{
		this.stageOperation = stageOperation;
	}
	
	public enum Operation
	{
		@JsonProperty("next")
		NEXT,
		@JsonProperty("previous")
		PREVIOUS,
		@JsonProperty("endGame")
		END_GAME
	}
	
	
	public Operation getStageOperation()
	{
		return stageOperation;
	}
	
	
	public void setStageOperation(final Operation stageOperation)
	{
		this.stageOperation = stageOperation;
	}
}
