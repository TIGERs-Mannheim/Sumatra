/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.10.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;


/**
 * This interface should be implemeted by all sub-panels which are
 * integrated in the {@link ModuleControlPanel} to control their behavior.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public interface IControlPanel
{
	/**
	 * This function clears the view of a control panel.
	 */
	public abstract void clearView();
	

	/**
	 * This function enables or disables buttons of a control panel.
	 */
	public abstract void setAthenaOverride(boolean status);
}
