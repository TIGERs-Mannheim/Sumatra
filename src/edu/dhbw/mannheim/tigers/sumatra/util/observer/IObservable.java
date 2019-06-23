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
	 * @param event The event that changed the model
	 */
	void notifyObservers(E event);
	
	
	/**
	 * Notifies all registered observers, using the last given event.
	 */
	void notifyObservers();
	
	
	/**
	 * 
	 * @param observers
	 * @return
	 */
	@SuppressWarnings("unchecked")
	boolean addObservers(O... observers);
	
	
	/**
	 * 
	 * @param observer
	 * @return
	 */
	boolean removeObserver(O observer);
	
	
	/**
	 * Remove all observers
	 */
	void removeAllObservers();
}