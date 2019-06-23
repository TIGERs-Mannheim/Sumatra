/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.lookandfeel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.UIManager;


/**
 * This class acts as a simple observable and informs all observers about
 * Look and Feel changes.
 * Observers usually need this to repaint all their child components
 * (including currently invisible ones).
 * 
 * @author AndreR
 */
public final class LookAndFeelStateAdapter implements PropertyChangeListener
{
	private static LookAndFeelStateAdapter instance = null;
	private final List<ILookAndFeelStateObserver> observers = new CopyOnWriteArrayList<>();
	
	
	private LookAndFeelStateAdapter()
	{
		UIManager.addPropertyChangeListener(this);
	}
	
	
	/**
	 * @return
	 */
	public static synchronized LookAndFeelStateAdapter getInstance()
	{
		if (instance == null)
		{
			instance = new LookAndFeelStateAdapter();
		}
		return instance;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ILookAndFeelStateObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ILookAndFeelStateObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyLookAndFeelChanged()
	{
		for (final ILookAndFeelStateObserver observer : observers)
		{
			observer.onLookAndFeelChanged();
		}
	}
	
	
	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{
		if ("lookAndFeel".equals(e.getPropertyName()))
		{
			notifyLookAndFeelChanged();
		}
	}
}
