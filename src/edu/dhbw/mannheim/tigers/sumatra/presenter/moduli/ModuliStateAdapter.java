/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.moduli;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.moduli.listenerVariables.ModulesState;


/**
 * Little helper singleton to observer the moduli state.
 * Useful in setting up a presenter.
 * 
 * @author AndreR
 * 
 */
public final class ModuliStateAdapter implements PropertyChangeListener
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private SumatraModel								model			= null;
	private static ModuliStateAdapter			instance		= null;
	private final List<IModuliStateObserver>	observers	= new CopyOnWriteArrayList<IModuliStateObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private ModuliStateAdapter()
	{
		model = SumatraModel.getInstance();
		model.getModulesState().addChangeListener(this);
	}
	
	
	/**
	 * @return
	 */
	public static synchronized ModuliStateAdapter getInstance()
	{
		if (instance == null)
		{
			instance = new ModuliStateAdapter();
		}
		
		return instance;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param o
	 */
	public void addObserver(IModuliStateObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IModuliStateObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * Receive an event if property "modulesState" will be changed.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		// --- property "stateApplication" ---
		if (evt.getSource() == model.getModulesState())
		{
			
			final ModulesState newState = (ModulesState) evt.getNewValue();
			
			synchronized (observers)
			{
				for (IModuliStateObserver o : observers)
				{
					o.onModuliStateChanged(newState);
				}
			}
		}
	}
}
