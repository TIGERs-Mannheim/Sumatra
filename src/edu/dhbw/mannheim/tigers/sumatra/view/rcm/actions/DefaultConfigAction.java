/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-robotControlUtility
 * Date: 19.11.2010
 * Authors: Clemens Teichmann <clteich@gmx.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.AControllerPresenter;


/**
 * @author Sven Frank
 * 
 */
public class DefaultConfigAction implements ActionListener
{
	// --------------------------------------------------------------------------
	// --- class variables ------------------------------------------------------
	// --------------------------------------------------------------------------
	private final AControllerPresenter	cPresenter;
	
	
	// -----------------------------------------------------------------
	// ----- Constructor -----------------------------------------------
	// -----------------------------------------------------------------
	/**
	 * @param cPresenter
	 */
	public DefaultConfigAction(AControllerPresenter cPresenter)
	{
		this.cPresenter = cPresenter;
	}
	
	
	// -----------------------------------------------------------------
	// ----- Methods ---------------------------------------------------
	// -----------------------------------------------------------------
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		// --- load default configuration ---
		cPresenter.loadDefaultConfig();
	}
}
