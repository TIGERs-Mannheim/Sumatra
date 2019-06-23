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
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.AControllerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.ControllerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.utils.ConfFileFilter;


/**
 * This action saves the current config in the selected file.
 * 
 * @author Lukas
 * 
 */

public class SaveConfigAction implements ActionListener
{
	// --------------------------------------------------------------------------
	// --- class variables ------------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log			= Logger.getLogger(SaveConfigAction.class.getName());
	
	private final ControllerPanel			panel;
	private final AControllerPresenter	cPresenter;
	private final JFileChooser				fc				= new JFileChooser();
	private final ConfFileFilter			confFilter	= new ConfFileFilter();
	private final File						dir			= new File("config/rcm");
	
	
	// -----------------------------------------------------------------
	// ----- Constructor -----------------------------------------------
	// -----------------------------------------------------------------
	/**
	 * @param panel
	 * @param cPresenter
	 */
	public SaveConfigAction(ControllerPanel panel, AControllerPresenter cPresenter)
	{
		this.panel = panel;
		this.cPresenter = cPresenter;
		fc.setFileFilter(confFilter);
		fc.setCurrentDirectory(dir);
	}
	
	
	// -----------------------------------------------------------------
	// ----- Methods ---------------------------------------------------
	// -----------------------------------------------------------------
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		final int returnVal = fc.showSaveDialog(panel);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			if (!file.getName().endsWith(confFilter.getFileSuffix()))
			{
				file = new File(file.getAbsolutePath() + confFilter.getFileSuffix());
			}
			if (!file.exists())
			{
				log.info("Config saved in \"" + file.getAbsolutePath() + "\"");
				cPresenter.saveCurrentConfig(file);
			} else
			{
				final int answer = JOptionPane.showConfirmDialog(panel, "Overwrite " + file.getName() + "?");
				if (answer == JOptionPane.YES_OPTION)
				{
					log.info("Config saved in \"" + file.getAbsolutePath() + "\"");
					cPresenter.saveCurrentConfig(file);
				}
				if (answer == JOptionPane.NO_OPTION)
				{
					actionPerformed(arg0);
				}
			}
		}
	}
}
