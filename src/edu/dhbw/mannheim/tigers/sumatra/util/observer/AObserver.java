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
 * <p>
 * An implementation of the Observer-Pattern.
 * </p>
 * 
 * @param <S> Observable type (Subject)
 * @param <O> Observer type
 * @param <E> Event type
 * 
 * @see Observable
 * 
 * @author Gero
 */
public abstract class AObserver<S extends IObservable<S, O, E>, O extends IObserver<S, O, E>, E> implements
		IObserver<S, O, E>
{
	
	/**
	 * 
	 * @param observable
	 * @param event
	 */
	protected abstract void onUpdate(S observable, E event);
}
