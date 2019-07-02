/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.components;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.JPanel;


/**
 * Generic panel base class which groups commonly needed functionality like an observer mechanism as well as a method to
 * enabled/disable the panel and all of its components
 * 
 * @param <T> the observer type
 */
public abstract class BasePanel<T> extends JPanel
{
	private final List<T> observer = new CopyOnWriteArrayList<>();
	
	
	public void addObserver(final T observer)
	{
		this.observer.add(observer);
	}
	
	
	public void removeObserver(final T observer)
	{
		this.observer.remove(observer);
	}
	
	
	protected void informObserver(final Consumer<T> consumer)
	{
		observer.forEach(consumer);
	}
}
