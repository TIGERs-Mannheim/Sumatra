/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.replay;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.persistance.IRecordPersistence;
import edu.tigers.sumatra.persistance.RecordBerkeleyPersistence;


/**
 * This panel contains primary the record button for capturing
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayLoadMenu extends JMenu
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long							serialVersionUID	= 1L;
	private static final Logger						log					= Logger
			.getLogger(
					ReplayLoadMenu.class.getName());
	
	private transient FileFilter						fileFilter;
	private List<File>									persistanceFiles;
	
	private final List<IReplayLoadMenuObserver>	observers			= new CopyOnWriteArrayList<>();
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public interface IReplayLoadMenuObserver
	{
		/**
		 * @param p
		 */
		void onLoadPersistance(IRecordPersistence p);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public ReplayLoadMenu()
	{
		super("Replay");
		File persistancePath = new File(
				RecordBerkeleyPersistence.getDefaultBasePath());
		if (!(persistancePath.isDirectory()))
		{
			boolean dirCreated = persistancePath.mkdirs();
			if (dirCreated)
			{
				log.debug("Created folders for persistancePath");
			}
		}
		fileFilter = new RecordDbFilter();
		addMenuListener(new MyMenuListener());
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IReplayLoadMenuObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IReplayLoadMenuObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * Update Records in Combobox
	 */
	public final void doUpdate()
	{
		removeAll();
		
		JMenuItem mit = new JMenuItem("Set default path");
		mit.addActionListener(new SetDefaultPathListener());
		add(mit);
		
		File persistancePath = new File(
				RecordBerkeleyPersistence.getDefaultBasePath());
		File[] files = persistancePath.listFiles(fileFilter);
		if (files == null)
		{
			persistanceFiles = new ArrayList<>(0);
		} else
		{
			persistanceFiles = Arrays.asList(files);
		}
		Collections.sort(persistanceFiles);
		for (File file : persistanceFiles)
		{
			JMenuItem item = new JMenuItem(file.getName());
			item.addActionListener(new ComboxListener(file.getAbsolutePath()));
			add(item);
		}
	}
	
	
	private static class RecordDbFilter implements FileFilter
	{
		@Override
		public boolean accept(final File pathname)
		{
			if (pathname.isDirectory())
			{
				return true;
			}
			if (pathname.getName().endsWith(".zip"))
			{
				return true;
			}
			return false;
		}
	}
	
	private class MyMenuListener implements MenuListener
	{
		@Override
		public void menuSelected(final MenuEvent e)
		{
			doUpdate();
		}
		
		
		@Override
		public void menuDeselected(final MenuEvent e)
		{
		}
		
		
		@Override
		public void menuCanceled(final MenuEvent e)
		{
		}
	}
	
	private class ComboxListener implements ActionListener, Runnable
	{
		private final String fileName;
		
		
		/**
		 * 
		 */
		private ComboxListener(final String fileName)
		{
			this.fileName = fileName;
		}
		
		
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
				persistance = new RecordBerkeleyPersistence(fileName, false);
				for (IReplayLoadMenuObserver o : observers)
				{
					o.onLoadPersistance(persistance);
				}
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
	
	private class SetDefaultPathListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fc.showOpenDialog(ReplayLoadMenu.this);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				RecordBerkeleyPersistence.setDefaultBasePath(fc.getSelectedFile().getAbsolutePath());
			}
		}
		
	}
}
