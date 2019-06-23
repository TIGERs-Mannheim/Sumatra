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
public interface IObserver<S extends IObservable<S, O, E>, O extends IObserver<S, O, E>, E>
{
	
	/**
	 * Calls the Observer with the observable and the event <strong>(May be <code>null</code>!)</strong>
	 * 
	 * @param observable
	 * @param event
	 */
	void update(S observable, E event);
	
	
	// void onAdd();
	
	
	// void onRemove();
	
}