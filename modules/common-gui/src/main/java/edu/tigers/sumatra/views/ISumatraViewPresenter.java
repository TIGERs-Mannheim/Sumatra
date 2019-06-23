/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.views;

import java.awt.Component;

import edu.tigers.moduli.IModuliStateObserver;


/**
 * All presenters of SumatraViews must implement this
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISumatraViewPresenter extends IModuliStateObserver
{
	/**
	 * Get the component, namely the main panel of the view associated with this presenter
	 * 
	 * @return
	 */
	Component getComponent();
	
	
	/**
	 * Get the {@link ISumatraView} associated with this presenter
	 * 
	 * @return
	 */
	ISumatraView getSumatraView();
}
