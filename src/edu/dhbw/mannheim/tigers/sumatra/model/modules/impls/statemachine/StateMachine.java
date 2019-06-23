/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.EventStatePair.EValid;


/**
 * State machine. Primary used for roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <STATE_TYPE>
 * 
 */
public class StateMachine<STATE_TYPE extends IState> implements IStateMachine<STATE_TYPE>
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger					log								= Logger
																										.getLogger(StateMachine.class.getName());
	private static final int						MAX_STATE_TRANSITIONS		= 50000;
	
	/** count transitions to avoid endless transitions */
	private int											stateTransitionsLeft			= MAX_STATE_TRANSITIONS;
	private STATE_TYPE								currentState					= null;
	private Map<EventStatePair, STATE_TYPE>	transititions					= new HashMap<EventStatePair, STATE_TYPE>();
	private Queue<Enum<? extends Enum<?>>>		eventQueue						= new LinkedBlockingQueue<Enum<? extends Enum<?>>>();
	private boolean									initialized						= true;
	private boolean									doEntryActionsFirstState	= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param initialState
	 */
	public StateMachine(STATE_TYPE initialState)
	{
		if (initialState == null)
		{
			throw new IllegalArgumentException("initial state must not be null");
		}
		currentState = initialState;
	}
	
	
	/**
	 */
	public StateMachine()
	{
		initialized = false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update()
	{
		if (currentState == null)
		{
			// no initial state or done
			return;
		}
		if (stateTransitionsLeft <= 0)
		{
			currentState.doExitActions();
			currentState = null;
			log.warn("StateMachine canceled due to too many transitions, max allowed: " + MAX_STATE_TRANSITIONS);
			return;
		}
		Enum<? extends Enum<?>> newEvent = eventQueue.poll();
		if (newEvent == null)
		{
			if (doEntryActionsFirstState)
			{
				currentState.doEntryActions();
			}
			// no new events, simply update current
			currentState.doUpdate();
		} else
		{
			STATE_TYPE newState = transititions.get(new EventStatePair(newEvent, currentState.getIdentifier()));
			if (newState != null)
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
				currentState.doUpdate();
				stateTransitionsLeft--;
			} else
			{
				// no transition for the event
				log.trace("No transition found for " + newEvent + " in state " + currentState.getIdentifier()
						+ ". Keep state");
			}
		}
		doEntryActionsFirstState = false;
	}
	
	
	private String enumToString(Enum<?> e)
	{
		String[] canName = e.getClass().getName().split("\\.");
		return canName[canName.length - 1] + "." + e.name();
	}
	
	
	@Override
	public void nextState(Enum<? extends Enum<?>> event)
	{
		log.trace("Event enqueued: " + enumToString(event));
		eventQueue.add(event);
	}
	
	
	@Override
	public boolean valid()
	{
		for (Map.Entry<EventStatePair, STATE_TYPE> entry : transititions.entrySet())
		{
			entry.getKey().setValid(EValid.UNKNOWN);
		}
		if (currentState == null)
		{
			log.warn("StateMachine has no initial state!");
			return false;
		}
		return valid(currentState);
	}
	
	
	private boolean valid(STATE_TYPE state)
	{
		if (state == null)
		{
			log.warn("StateMachine has a null state!");
			return false;
		}
		if (state.getIdentifier() == DoneState.EStateId.DONE)
		{
			return true;
		}
		log.trace("Checking " + state.getIdentifier() + " for validaty");
		for (Map.Entry<EventStatePair, STATE_TYPE> entry : transititions.entrySet())
		{
			if ((entry.getKey().getState() == state.getIdentifier()))
			{
				switch (entry.getKey().getValid())
				{
					case UNKNOWN:
						entry.getKey().setValid(EValid.CHECKING);
						if (valid(entry.getValue()))
						{
							entry.getKey().setValid(EValid.YES);
							log.trace(entry.getKey() + " is valid");
						} else
						{
							log.warn("Invalid state detected: " + entry.getKey() + " -> " + entry.getValue());
							entry.getKey().setValid(EValid.NO);
							return false;
						}
						break;
					case CHECKING:
						continue;
					case NO:
						return false;
					case YES:
						return true;
				}
				
			}
		}
		return true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public final STATE_TYPE getCurrentState()
	{
		return currentState;
	}
	
	
	@Override
	public final Map<EventStatePair, STATE_TYPE> getTransititions()
	{
		return transititions;
	}
	
	
	@Override
	public final void setInitialState(STATE_TYPE currentState)
	{
		if (initialized)
		{
			throw new IllegalStateException("Alread initialized");
		}
		initialized = true;
		this.currentState = currentState;
	}
}
