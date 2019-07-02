package edu.tigers.sumatra.referee.control;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Data structure for manipulating cards from game controller
 */
public class EventCard
{
	private Type cardType;
	private EventTeam forTeam;
	private Operation operation;
	private Modification modification;
	
	
	public EventCard(final Type cardType, final EventTeam forTeam, final Operation operation)
	{
		this.cardType = cardType;
		this.forTeam = forTeam;
		this.operation = operation;
	}
	
	
	public EventTeam getForTeam()
	{
		return forTeam;
	}
	
	
	public void setForTeam(final EventTeam forTeam)
	{
		this.forTeam = forTeam;
	}
	
	
	public Type getCardType()
	{
		return cardType;
	}
	
	
	public void setCardType(final Type cardType)
	{
		this.cardType = cardType;
	}
	
	
	public Operation getOperation()
	{
		return operation;
	}
	
	
	public void setOperation(final Operation operation)
	{
		this.operation = operation;
	}
	
	
	public Modification getModification()
	{
		return modification;
	}
	
	
	public void setModification(final Modification modification)
	{
		this.modification = modification;
	}
	
	
	public enum Type
	{
		@JsonProperty("yellow")
		YELLOW,
		@JsonProperty("red")
		RED
	}
	
	public enum Operation
	{
		@JsonProperty("add")
		ADD,
		@JsonProperty("revoke")
		REVOKE,
		@JsonProperty("modify")
		MODIFY
	}
	
	public static class Modification
	{
		private int cardId;
		private long timeLeft;
		
		
		public int getCardId()
		{
			return cardId;
		}
		
		
		public void setCardId(final int cardId)
		{
			this.cardId = cardId;
		}
		
		
		public long getTimeLeft()
		{
			return timeLeft;
		}
		
		
		public void setTimeLeft(final long timeLeft)
		{
			this.timeLeft = timeLeft;
		}
	}
	
}
