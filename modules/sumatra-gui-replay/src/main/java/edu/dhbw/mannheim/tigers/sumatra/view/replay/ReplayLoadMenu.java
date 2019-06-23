/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.ABerkeleyPersistence;


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
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger
			.getLogger(
					ReplayLoadMenu.class.getName());
	
	private transient FileFilter fileFilter;
	private List<File> persistanceFiles;
	
	private final transient List<IReplayLoadMenuObserver> observers = new CopyOnWriteArrayList<>();
	private final transient Function<String, ABerkeleyPersistence> persistenceCreator;
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@FunctionalInterface
	public interface IReplayLoadMenuObserver
	{
		/**
		 * @param p
		 */
		void onLoadPersistance(ABerkeleyPersistence p);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create a reploy load menu
	 *
	 * @param persistenceCreator function for creating the persistence class
	 */
	public ReplayLoadMenu(Function<String, ABerkeleyPersistence> persistenceCreator)
	{
		super("Replay");
		this.persistenceCreator = persistenceCreator;
		File persistancePath = new File(getDefaultBasePath());
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
	 * @return
	 */
	public static String getDefaultBasePath()
	{
		return SumatraModel.getInstance()
				.getUserProperty("edu.tigers.sumatra.persistence.basePath", "data/record");
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
		
		File persistancePath = new File(getDefaultBasePath());
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
			addFileToMenu(file, this);
		}
	}
	
	
	private void addFileToMenu(final File file, final JMenu menu)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles(fileFilter);
			List<File> dirs;
			if (files == null)
			{
				dirs = new ArrayList<>();
			} else
			{
				dirs = Arrays.stream(files).sorted().collect(Collectors.toList());
			}
			if (!dirs.isEmpty())
			{
				JMenu subMenu = new JMenu(file.getName());
				menu.add(subMenu);
				for (File d : dirs)
				{
					addFileToMenu(d, subMenu);
				}
				return;
			}
		}
		JMenuItem item = new JMenuItem(file.getName());
		item.addActionListener(new ComboxListener(file.getAbsolutePath()));
		menu.add(item);
	}
	
	
	private static class RecordDbFilter implements FileFilter
	{
		@Override
		public boolean accept(final File pathname)
		{
			return pathname.isDirectory() || pathname.getName().endsWith(".zip");
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
			// ignore
		}
		
		
		@Override
		public void menuCanceled(final MenuEvent e)
		{
			// ignore
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
			ABerkeleyPersistence persistance = null;
			try
			{
				persistance = persistenceCreator.apply(fileName);
				persistance.open();
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
				SumatraModel.getInstance().setUserProperty("edu.tigers.sumatra.persistence.basePath",
						fc.getSelectedFile().getAbsolutePath());
			}
		}
		
	}
}
