/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statemachine;

import org.apache.log4j.Logger;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * State machine. Primary used for roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <T>
 */
public class StateMachine<T extends IState> implements IStateMachine<T>
{
	private static final Logger					log								= Logger.getLogger(StateMachine.class
			.getName());
	
	private T											currentState					= null;
	private T											initialState					= null;
	private int											queueSize						= 1;
	private final Map<IEvent, Map<IState, T>>	transitions						= new HashMap<>();
	private final Deque<IEvent>					eventQueue						= new LinkedList<>();
	private boolean									initialized						= false;
	private boolean									doEntryActionsFirstState	= true;
	private boolean									extendedLogging				= false;
	
	
	/**
	 * @param initialState first to be active
	 */
	public StateMachine(final T initialState)
	{
		setInitialState(initialState);
	}
	
	
	/**
	 * Default
	 */
	public StateMachine()
	{
		// nothing to do
	}
	
	
	private T getNextState(IEvent event, IState currentState)
	{
		Map<IState, T> subTransitions = transitions.computeIfAbsent(event, k -> new HashMap<>());
		T nextState = subTransitions.get(currentState);
		if (nextState == null)
		{
			nextState = subTransitions.get(null);
			if (currentState != null && currentState.equals(nextState))
			{
				// ignore wildcard transition to itself
				return null;
			}
		}
		if (nextState == null)
		{
			// no transition for the event
			String stateName = currentState == null ? "null" : currentState.getIdentifier();
			extendedLogging(
					() -> log.warn("No transition found for " + event + " in state " + stateName + ". Keep state"));
		}
		return nextState;
	}
	
	
	private void extendedLogging(Runnable run)
	{
		if (extendedLogging)
		{
			run.run();
		}
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
			IEvent newEvent = eventQueue.removeLast();
			T newState = getNextState(newEvent, currentState);
			if (newState != null)
			{
				if (currentState.equals(newState))
				{
					extendedLogging(() -> log.warn("Transition to itself. This doesn't sound reasonable?!"));
				} else
				{
					changeState(newState);
					// note: changeState may add events to eventQueue again!
					stateTransitionsLeft--;
					stateChanged = true;
				}
			}
			if (stateTransitionsLeft <= 0)
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
	
	
	@SuppressWarnings("squid:S2583")
	private void changeState(final T newState)
	{
		currentState.doExitActions();
		if (newState == null)
		{
			// done
			currentState = null;
			return;
		}
		extendedLogging(() -> log.trace("Switch state from " + currentState + " to " + newState));
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
	
	
	@Override
	public void stop()
	{
		if (currentState != null)
		{
			currentState.doExitActions();
		}
		currentState = null;
	}
	
	
	@Override
	public void triggerEvent(final IEvent event)
	{
		if (event == null)
		{
			log.warn("trigger event is null!");
			return;
		}
		extendedLogging(() -> log.trace("Event enqueued: " + event));
		eventQueue.add(event);
		while (eventQueue.size() > queueSize)
		{
			IEvent ev = eventQueue.removeFirst();
			extendedLogging(() -> log.warn("Queue full. Event " + ev + " removed."));
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
	public final T getCurrentState()
	{
		return currentState;
	}
	
	
	@Override
	public final void addTransition(final IState currentState, final IEvent event, final T state)
	{
		assert event != null;
		assert state != null;
		Map<IState, T> subTransitions = transitions.computeIfAbsent(event, k -> new HashMap<>());
		T preState = subTransitions.put(currentState, state);
		if (preState != null)
		{
			log.warn("Overwriting transition for event: " + event + " and state " + currentState + ". Change state from "
					+ preState + " to "
					+ state);
		}
	}
	
	
	@Override
	public final void setInitialState(final T currentState)
	{
		if (initialized)
		{
			throw new IllegalStateException("Already initialized");
		}
		this.currentState = currentState;
		initialState = currentState;
	}
	
	
	@Override
	public void setExtendedLogging(final boolean extendedLogging)
	{
		this.extendedLogging = extendedLogging;
	}
	
	
	@Override
	public void setQueueSize(int queueSize)
	{
		this.queueSize = queueSize;
	}
}
