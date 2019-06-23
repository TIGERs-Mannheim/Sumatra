/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.persistance.IRecordPersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordBerkeleyPersistence;


/**
 * This panel contains primary the record button for capturing
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayLoadPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long		serialVersionUID		= 1L;
	private static final String	BASE_PATH				= "data/record/";
	private static final Logger	log						= Logger.getLogger(ReplayLoadPanel.class.getName());
	
	private FileFilter				fileFilter				= new RecordDbFilter();
	private List<File>				persistanceFiles;
	private File						persistancePath		= new File(BASE_PATH);
	
	private JComboBox<String>		persistanceCombo;
	private ActionListener			comboListener			= new ComboxListener();
	private PopupMenuListener		ComboxPopupListener	= new ComboxPopupListener();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public ReplayLoadPanel()
	{
		// --- border ---
		final TitledBorder border = BorderFactory.createTitledBorder("load Replay");
		setBorder(border);
		setLayout(new FlowLayout());
		
		if (!(persistancePath.isDirectory()))
		{
			persistancePath.mkdirs();
		}
		persistanceFiles = Arrays.asList(persistancePath.listFiles(fileFilter));
		Collections.sort(persistanceFiles);
		persistanceCombo = new JComboBox<String>();
		for (File file : persistanceFiles)
		{
			persistanceCombo.addItem(file.getName());
		}
		persistanceCombo.addActionListener(comboListener);
		persistanceCombo.addPopupMenuListener(ComboxPopupListener);
		
		add(persistanceCombo);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Update Records in Combobox
	 */
	public void doUpdate()
	{
		persistanceCombo.removeActionListener(comboListener);
		persistanceCombo.removeAllItems();
		
		persistanceFiles = Arrays.asList(persistancePath.listFiles(fileFilter));
		Collections.sort(persistanceFiles);
		for (File file : persistanceFiles)
		{
			persistanceCombo.addItem(file.getName());
		}
		comboListener = new ComboxListener();
		persistanceCombo.addActionListener(comboListener);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- inner class --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private class RecordDbFilter implements FileFilter
	{
		
		
		@Override
		public boolean accept(final File pathname)
		{
			return true;
		}
	}
	
	private class ComboxPopupListener implements PopupMenuListener
	{
		
		@Override
		public void popupMenuCanceled(final PopupMenuEvent e)
		{
			
		}
		
		
		@Override
		public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
		{
			
		}
		
		
		@Override
		public void popupMenuWillBecomeVisible(final PopupMenuEvent e)
		{
			doUpdate();
			
		}
		
	}
	
	private class ComboxListener implements ActionListener, Runnable
	{
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Thread loadThread = new Thread(this, "LoadRecording");
			loadThread.start();
		}
		
		
		@Override
		public void run()
		{
			IRecordPersistence persistance = null;
			try
			{
				String dbname = ((String) persistanceCombo.getSelectedItem());
				
				persistance = new RecordBerkeleyPersistence(dbname);
				
				ReplayWindow replaceWindow = new ReplayWindow(persistance);
				replaceWindow.activate();
			} catch (Exception e)
			{
				log.error("An exception ocurred on load. See log.", e);
				JOptionPane.showMessageDialog(null, "An exception ocurred on load. See log.", "Error",
						JOptionPane.ERROR_MESSAGE);
				if (persistance != null)
				{
					persistance.close();
				}
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
