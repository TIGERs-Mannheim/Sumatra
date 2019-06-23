/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.replay;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.RecordManager;


/**
 * This panel contains primary the record button for capturing
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance should be used carefully
public class ReplayLoadMenu extends JMenu
{
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ReplayLoadMenu.class.getName());
	
	private transient FileFilter fileFilter;
	
	private final transient List<IReplayLoadMenuObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * Create a replay load menu
	 */
	public ReplayLoadMenu()
	{
		super("Replay");
		File path = new File(getDefaultBasePath());
		if (!(path.isDirectory()))
		{
			boolean dirCreated = path.mkdirs();
			if (dirCreated)
			{
				log.debug("Created folders for persistencePath " + path);
			}
		}
		fileFilter = new RecordDbFilter();
		addMenuListener(new MyMenuListener());
	}
	
	
	/**
	 * @return
	 */
	private static String getDefaultBasePath()
	{
		return SumatraModel.getInstance()
				.getUserProperty("edu.tigers.sumatra.persistence.basePath", "data/record");
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
		item.addActionListener(new ComboBoxListener(file.getAbsolutePath()));
		menu.add(item);
	}
	
	
	/**
	 * Observer
	 */
	@FunctionalInterface
	public interface IReplayLoadMenuObserver
	{
		void onOpenReplay(BerkeleyDb db);
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
			removeAll();
			
			JMenuItem mit = new JMenuItem("Set default path");
			mit.addActionListener(new SetDefaultPathListener());
			add(mit);
			
			File path = new File(getDefaultBasePath());
			File[] files = path.listFiles(fileFilter);
			final List<File> fileList;
			if (files == null)
			{
				fileList = new ArrayList<>(0);
			} else
			{
				fileList = Arrays.asList(files);
			}
			Collections.sort(fileList);
			for (File file : fileList)
			{
				addFileToMenu(file, ReplayLoadMenu.this);
			}
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
	
	private class ComboBoxListener implements ActionListener
	{
		private final String fileName;
		
		
		/**
		 * 
		 */
		private ComboBoxListener(final String fileName)
		{
			this.fileName = fileName;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			Thread loadThread = new Thread(new LoadDatabase(fileName), "LoadDatabase");
			loadThread.start();
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
	
	private final class LoadDatabase implements Runnable
	{
		final String filename;
		
		
		private LoadDatabase(final String filename)
		{
			this.filename = filename;
		}
		
		
		@Override
		public void run()
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			BerkeleyDb db = null;
			try
			{
				RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
				db = recordManager.newBerkeleyDb(Paths.get(filename));
				db.open();
				BerkeleyDb dbOut = db;
				observers.forEach(o -> o.onOpenReplay(dbOut));
			} catch (Exception e)
			{
				log.error(
						"An exception occurred while loading database.\nTo watch a replay, you need to have started the simulation at least once.",
						e);
				JOptionPane.showMessageDialog(ReplayLoadMenu.this,
						"An exception occurred while loading database.\nTo watch a replay, you need to have started the simulation at least once.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				if (db != null)
				{
					db.close();
				}
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
