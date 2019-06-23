/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statemachine;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * State machine. Primary used for roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <STATETYPE>
 */
public class StateMachine<STATETYPE extends IState> implements IStateMachine<STATETYPE>
{
	private static final Logger						log								= Logger.getLogger(StateMachine.class
																											.getName());
	
	private STATETYPE										currentState					= null;
	private STATETYPE										initialState					= null;
	private final int										queueSize						= 1;
	private final Map<EventStatePair, STATETYPE>	transititions					= new HashMap<EventStatePair, STATETYPE>();
	private final Deque<Enum<? extends Enum<?>>>	eventQueue						= new LinkedList<Enum<? extends Enum<?>>>();
	private boolean										initialized						= false;
	private boolean										doEntryActionsFirstState	= true;
	
	
	/**
	 * @param initialState
	 */
	public StateMachine(final STATETYPE initialState)
	{
		setInitialState(initialState);
	}
	
	
	/**
	 */
	public StateMachine()
	{
	}
	
	
	@Override
	public void update()
	{
		if (currentState == null)
		{
			// no initial state or done
			return;
		}
		initialized = true;
		boolean stateChanged = false;
		int stateTransitionsLeft = 10;
		while (!stateChanged && !eventQueue.isEmpty())
		{
			Enum<? extends Enum<?>> newEvent = eventQueue.removeLast();
			STATETYPE newState = transititions
					.get(new EventStatePair(newEvent, currentState.getIdentifier(), currentState));
			// if (newState == null)
			// {
			// // event for wildcard states
			// newState = transititions.get(new EventStatePair(newEvent));
			// }
			if (newState != null)
			{
				changeState(newState);
				// note: changeState may add events to eventQueue again!
				stateTransitionsLeft--;
				stateChanged = true;
			} else
			{
				// no transition for the event
				log.trace("No transition found for " + newEvent + " in state " + currentState.getIdentifier()
						+ ". Keep state");
			}
			if ((stateTransitionsLeft <= 0))
			{
				log.warn("Possible endless loop detected! Too many transitions in one update.");
				break;
			}
		}
		if (!stateChanged)
		{
			if (doEntryActionsFirstState)
			{
				currentState.doEntryActions();
			}
			// no new events, simply update current
			currentState.doUpdate();
		}
		doEntryActionsFirstState = false;
	}
	
	
	private void changeState(final STATETYPE newState)
	{
		currentState.doExitActions();
		if (newState.getIdentifier() == DoneState.EStateId.DONE)
		{
			// done
			currentState = null;
			return;
		}
		log.trace("Switch state from " + enumToString(currentState.getIdentifier()) + " to "
				+ enumToString(newState.getIdentifier()));
		currentState = newState;
		currentState.doEntryActions();
		// currentState may got null, if the role was set to completed... :D
		if (currentState != null)
		{
			currentState.doUpdate();
		}
	}
	
	
	@Override
	public void restart()
	{
		if (initialState != null)
		{
			changeState(initialState);
		}
	}
	
	
	/**
	 * 
	 */
	@Override
	public void stop()
	{
		if (currentState != null)
		{
			currentState.doExitActions();
		}
		currentState = null;
	}
	
	
	private String enumToString(final Enum<?> e)
	{
		if (e == null)
		{
			return "null";
		}
		String[] canName = e.getClass().getName().split("\\.");
		return canName[canName.length - 1] + "." + e.name();
	}
	
	
	@Override
	public void triggerEvent(final Enum<? extends Enum<?>> event)
	{
		log.trace("Event enqueued: " + enumToString(event));
		eventQueue.add(event);
		while (eventQueue.size() > queueSize)
		{
			Enum<?> ev = eventQueue.removeFirst();
			log.trace("Queue full. Event " + ev + " removed.");
		}
	}
	
	
	@Override
	public boolean valid()
	{
		if (currentState == null)
		{
			log.warn("StateMachine has no initial state!");
			return false;
		}
		return true;
	}
	
	
	@Override
	public final STATETYPE getCurrentStateId()
	{
		return currentState;
	}
	
	
	/**
	 * @param esp
	 * @param state
	 */
	@Override
	public final void addTransition(final EventStatePair esp, final STATETYPE state)
	{
		STATETYPE preState = transititions.put(esp, state);
		if (preState != null)
		{
			log.warn("Overwriting transition for EventStatePair: " + esp + ". Change state from " + preState + " to "
					+ state);
		}
	}
	
	
	@Override
	public final void setInitialState(final STATETYPE currentState)
	{
		if (initialized)
		{
			throw new IllegalStateException("Alread initialized");
		}
		this.currentState = currentState;
		initialState = currentState;
	}
}
