/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.laf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

/**
 * This class acts as a simple observable and informs all observers about
 * Look and Feel changes.
 * Observers usually need this to repaint all their child components
 * (including currently invisible ones).
 * 
 * @author AndreR
 * 
 */
public class LookAndFeelStateAdapter implements PropertyChangeListener
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static LookAndFeelStateAdapter instance = null;
	private final List<ILookAndFeelStateObserver> observers = new ArrayList<ILookAndFeelStateObserver>();
	
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
	public static synchronized LookAndFeelStateAdapter getInstance()
	{
		if (instance == null)
		{
			instance = new LookAndFeelStateAdapter();
		}
		return instance;
	}
	
	public void addObserver(ILookAndFeelStateObserver observer)
	{
		synchronized(observers)
		{
			observers.add(observer);
		}
	}
	
	
	public void removeObserver(ILookAndFeelStateObserver observer)
	{
		synchronized(observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyLookAndFeelChanged()
	{
		synchronized(observers)
		{
			for (ILookAndFeelStateObserver observer : observers)
			{
				observer.onLookAndFeelChanged();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		if(e.getPropertyName().equals("lookAndFeel"))
		{
			notifyLookAndFeelChanged();
		}
	}
}
