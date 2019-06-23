/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.observer;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @param <S> Observable type (Subject)
 * @param <O> Observer type
 * @param <E> Event type
 * @see IObserver
 * @author Gero
 */
public class Observable<S extends IObservable<S, O, E>, O extends IObserver<S, O, E>, E> implements
		IObservable<S, O, E>
{
	
	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Global declarations
	 */
	private final List<O>	registeredObservers	= new CopyOnWriteArrayList<O>();
	
	private E					lastEvent;
	
	
	/**
	 * @param initEvent
	 */
	public Observable(final E initEvent)
	{
		this.lastEvent = initEvent;
	}
	
	
	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Notify
	 */
	/**
	 * Notifies all registered observers.
	 * 
	 * @param event The event (e) that changed the model
	 */
	@Override
	public synchronized void notifyObservers(final E event)
	{
		lastEvent = event;
		
		doNotify(event);
	}
	
	
	@Override
	public synchronized void notifyObservers()
	{
		doNotify(lastEvent);
	}
	
	
	@SuppressWarnings("unchecked")
	private void doNotify(final E event)
	{
		for (final O observer : registeredObservers)
		{
			observer.update((S) this, event);
		}
	}
	
	
	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Observer management
	 */
	/**
	 * @return Whether all observers has been added without problems
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean addObservers(final O... observers)
	{
		if (observers == null)
		{
			return false;
		}
		
		boolean result = true;
		
		synchronized (this)
		{
			for (final O obs : observers)
			{
				if (obs != null)
				{
					registeredObservers.add(obs);
					if (lastEvent != null)
					{
						obs.update((S) this, lastEvent);
					}
					// obs.onAdd();
				} else
				{
					result = false;
				}
			}
		}
		
		return result;
	}
	
	
	@Override
	public synchronized boolean removeObserver(final O observer)
	{
		final boolean containedObs = registeredObservers.remove(observer);
		
		// if (containedObs)
		// {
		// observer.onRemove();
		// }
		
		return containedObs;
	}
	
	
	@Override
	public synchronized void removeAllObservers()
	{
		registeredObservers.clear();
	}
}
