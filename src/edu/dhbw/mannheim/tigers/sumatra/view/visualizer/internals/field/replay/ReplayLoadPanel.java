/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.record.RecordPersistance;


/**
 * This panel contains primary the record button for capturing
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ReplayLoadPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long		serialVersionUID	= 1L;
	private static final String	BASE_PATH			= "logs/record/";
	
	private ODBFileFilter			odbFileFilter		= new ODBFileFilter();
	private List<File>				persistanceFiles;
	private File						persistancePath	= new File(BASE_PATH);
	
	private JComboBox<File>			persistanceCombo;
	private ActionListener			comboListener		= new ComboxListener();
	
	
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
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		if (!(persistancePath.isDirectory()))
		{
			persistancePath.mkdir();
		}
		persistanceFiles = Arrays.asList(persistancePath.listFiles(odbFileFilter));
		Collections.sort(persistanceFiles);
		persistanceCombo = new JComboBox<File>(persistanceFiles.toArray(new File[persistanceFiles.size()]));
		persistanceCombo.addActionListener(comboListener);
		
		add(persistanceCombo);
		add(Box.createGlue());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Update Records in Combobox
	 * 
	 */
	public void doUpdate()
	{
		persistanceCombo.removeActionListener(comboListener);
		persistanceCombo.removeAllItems();
		
		persistanceFiles = Arrays.asList(persistancePath.listFiles(odbFileFilter));
		for (File file : persistanceFiles)
		{
			persistanceCombo.addItem(file);
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
	
	
	private class ODBFileFilter implements FileFilter
	{
		
		
		@Override
		public boolean accept(File pathname)
		{
			return pathname.getName().toLowerCase().endsWith(".odb");
		}
	}
	
	
	private class ComboxListener implements ActionListener, Runnable
	{
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Thread loadThread = new Thread(this);
			loadThread.start();
		}
		
		
		@Override
		public void run()
		{
			String dbname = ((File) persistanceCombo.getSelectedItem()).getName();
			RecordPersistance persistance = new RecordPersistance(dbname.substring(0, dbname.indexOf(".")));
			persistance.load();
			
			List<IRecordFrame> aiFrameBuffer = persistance.getRecordFrames();
			ReplayWindow replaceWindow = new ReplayWindow(aiFrameBuffer);
			replaceWindow.activate();
			
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
		}
		
	}
	
}
