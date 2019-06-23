/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.observer;


/**
 * @param <S> Observable type (Subject)
 * @param <O> Observer type
 * @param <E> Event type
 * 
 * @see Observable
 * 
 * @author Gero
 */
public interface IObservable<S extends IObservable<S, O, E>, O extends IObserver<S, O, E>, E>
{
	
	/**
	 * Notifies all registered observers, that there is a new event - the given.
	 * 
	 * @param event: The {@link Event} that changed the model
	 */
	public abstract void notifyObservers(E event);
	

	/**
	 * Notifies all registered observers, using the last given event.
	 */
	public abstract void notifyObservers();
	

	public abstract boolean addObservers(O... observers);
	

	public abstract boolean removeObserver(O observer);
	
	
	public abstract void removeAllObservers();
}