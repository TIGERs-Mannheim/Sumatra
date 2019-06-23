/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 30, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine;


/**
 * Pair of state and event
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class EventStatePair
{
	private final Enum<? extends Enum<?>>	event;
	private final Enum<? extends Enum<?>>	state;
	
	private EValid									valid	= EValid.UNKNOWN;
	
	/**
	 */
	public enum EValid
	{
		/**  */
		UNKNOWN,
		/**  */
		CHECKING,
		/**  */
		YES,
		/**  */
		NO;
	}
	
	
	/**
	 * @param event
	 * @param state
	 */
	public EventStatePair(Enum<? extends Enum<?>> event, Enum<? extends Enum<?>> state)
	{
		this.event = event;
		this.state = state;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((event == null) ? 0 : event.hashCode());
		result = (prime * result) + ((state == null) ? 0 : state.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		@SuppressWarnings("unchecked")
		EventStatePair other = (EventStatePair) obj;
		if (event == null)
		{
			if (other.event != null)
			{
				return false;
			}
		} else if (!event.equals(other.event))
		{
			return false;
		}
		if (state == null)
		{
			if (other.state != null)
			{
				return false;
			}
		} else if (!state.equals(other.state))
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * @return the event
	 */
	public final Enum<? extends Enum<?>> getEvent()
	{
		return event;
	}
	
	
	/**
	 * @return the state
	 */
	public final Enum<? extends Enum<?>> getState()
	{
		return state;
	}
	
	
	/**
	 * @return the valid
	 */
	public final EValid getValid()
	{
		return valid;
	}
	
	
	/**
	 * @param valid the valid to set
	 */
	public final void setValid(EValid valid)
	{
		this.valid = valid;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("EventStatePair [event=");
		builder.append(event);
		builder.append(", state=");
		builder.append(state);
		builder.append("]");
		return builder.toString();
	}
	
}
