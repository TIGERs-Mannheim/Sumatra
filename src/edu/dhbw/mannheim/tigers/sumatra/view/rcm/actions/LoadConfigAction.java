/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * 
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.AControllerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.ControllerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.utils.ConfFileFilter;


/**
 * This action loads config from selected file.
 * 
 * @author Lukas
 * 
 */


public class LoadConfigAction implements ActionListener
{
	// --------------------------------------------------------------------------
	// --- class variables ------------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log	= Logger.getLogger(LoadConfigAction.class.getName());
	
	private final ControllerPanel			panel;
	private final AControllerPresenter	cPresenter;
	private final JFileChooser				fc		= new JFileChooser();
	private final File						dir	= new File("config/rcm");
	
	
	// -----------------------------------------------------------------
	// ----- Constructor -----------------------------------------------
	// -----------------------------------------------------------------
	/**
	 * @param panel
	 * @param cPresenter
	 */
	public LoadConfigAction(ControllerPanel panel, AControllerPresenter cPresenter)
	{
		this.panel = panel;
		this.cPresenter = cPresenter;
		fc.setFileFilter(new ConfFileFilter());
		fc.setCurrentDirectory(dir);
	}
	
	
	// -----------------------------------------------------------------
	// ----- Methods ---------------------------------------------------
	// -----------------------------------------------------------------
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		final int returnVal = fc.showOpenDialog(panel);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			final File file = fc.getSelectedFile();
			log.info("Opening: " + file.getName());
			cPresenter.loadCurrentConfig(file);
		}
	}
}