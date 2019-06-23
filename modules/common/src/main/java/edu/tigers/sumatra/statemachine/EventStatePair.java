/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 30, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statemachine;

import java.util.Optional;


/**
 * Pair of state/stateInstance and event.
 * Primary method here is equals.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class EventStatePair
{
	private final Enum<? extends Enum<?>>				event;
	private final Optional<Enum<? extends Enum<?>>>	state;
	private final Optional<IState>						stateInstance;
	
	
	/**
	 * @param event
	 */
	public EventStatePair(final Enum<? extends Enum<?>> event)
	{
		this.event = event;
		state = Optional.empty();
		stateInstance = Optional.empty();
	}
	
	
	/**
	 * @param event
	 * @param state
	 */
	public EventStatePair(final Enum<? extends Enum<?>> event, final Enum<? extends Enum<?>> state)
	{
		this.event = event;
		this.state = Optional.of(state);
		stateInstance = Optional.empty();
	}
	
	
	/**
	 * @param event
	 * @param stateInstance
	 */
	public EventStatePair(final Enum<? extends Enum<?>> event, final IState stateInstance)
	{
		this.event = event;
		this.stateInstance = Optional.of(stateInstance);
		state = Optional.empty();
	}
	
	
	/**
	 * @param event
	 * @param state
	 * @param stateInstance
	 */
	public EventStatePair(final Enum<? extends Enum<?>> event, final Enum<? extends Enum<?>> state,
			final IState stateInstance)
	{
		this.event = event;
		this.stateInstance = Optional.of(stateInstance);
		this.state = Optional.ofNullable(state);
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((event == null) ? 0 : event.hashCode());
		// note: we do not include state or stateInstance, because that may violate the contract
		// of hashCode(). We have to return the same hashCode, if equals returns true, but we may
		// return the same hash code for unequal objects.
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
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
		if (state.isPresent() && other.state.isPresent())
		{
			if (!state.equals(other.state))
			{
				return false;
			}
		}
		if (stateInstance.isPresent() && other.stateInstance.isPresent())
		{
			if (!stateInstance.equals(other.stateInstance))
			{
				return false;
			}
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
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("EventStatePair [event=");
		builder.append(event);
		if (state.isPresent())
		{
			builder.append(", state=");
			builder.append(state);
		}
		if (stateInstance.isPresent())
		{
			builder.append(", stateInstance=");
			builder.append(stateInstance.getClass().getName());
		}
		builder.append("]");
		return builder.toString();
	}
	
}
