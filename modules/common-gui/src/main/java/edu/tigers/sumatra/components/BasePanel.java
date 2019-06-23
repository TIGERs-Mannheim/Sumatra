/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.sumatra.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;


/**
 * Generic panel base class which groups commonly needed functionality like an observer mechanism as well as a method to
 * enabled/disable the panel and all of its components
 * 
 * @author Lukas Magel
 * @param <T>
 */
public abstract class BasePanel<T> extends JPanel
{
	
	/**  */
	private static final long	serialVersionUID	= 1L;
	
	private List<T>				observer				= new ArrayList<>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final T observer)
	{
		this.observer.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final T observer)
	{
		this.observer.remove(observer);
	}
	
	
	protected List<T> getObserver()
	{
		return observer;
	}
	
	
	protected void informObserver(final Consumer<T> consumer)
	{
		observer.forEach(observer -> consumer.accept(observer));
	}
	
	
	/**
	 * Enable/disable the panel and all of its components
	 * 
	 * @param enabled
	 */
	public abstract void setPanelEnabled(boolean enabled);
	
}
