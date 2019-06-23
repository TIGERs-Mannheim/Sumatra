/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.lookandfeel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

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
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static LookAndFeelStateAdapter instance = null;
	private final Set<ILookAndFeelStateObserver> observers = new HashSet<>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private LookAndFeelStateAdapter()
	{
		UIManager.addPropertyChangeListener(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
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
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ILookAndFeelStateObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyLookAndFeelChanged()
	{
		synchronized (observers)
		{
			for (final ILookAndFeelStateObserver observer : observers)
			{
				observer.onLookAndFeelChanged();
			}
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
