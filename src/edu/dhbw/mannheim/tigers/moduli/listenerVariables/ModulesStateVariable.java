/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-centralSoftware
 * Date: 06.10.2009
 * Authors: Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.moduli.listenerVariables;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Create a listener on central stateApplication - variable at main-model.
 */
public class ModulesStateVariable
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private PropertyChangeSupport	support;
	private ModulesState				stateModules	= ModulesState.NOT_LOADED;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Initialize support for property-change-listener.
	 */
	public ModulesStateVariable()
	{
		support = new PropertyChangeSupport(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Getter-method.
	 * @return
	 */
	public ModulesState get()
	{
		return stateModules;
	}
	
	
	/**
	 * Setter-method.
	 * @param stateApplicationNew new state
	 */
	public void set(ModulesState stateApplicationNew)
	{
		ModulesState oldValue = stateModules;
		stateModules = stateApplicationNew;
		support.firePropertyChange("stateModules", oldValue, stateApplicationNew);
	}
	
	
	/**
	 * -method.
	 * @param moduleState
	 * @return
	 */
	public boolean equals(ModulesState moduleState)
	{
		if (stateModules.equals(moduleState))
		{
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Add a change-listener.
	 * @param listener PropertyChangeListener
	 */
	public void addChangeListener(PropertyChangeListener listener)
	{
		support.addPropertyChangeListener(listener);
	}
	
	
	/**
	 * Remove a change-listener.
	 * @param listener PropertyChangeListener
	 */
	public void removeChangeListener(PropertyChangeListener listener)
	{
		support.removePropertyChangeListener(listener);
	}
	
}
