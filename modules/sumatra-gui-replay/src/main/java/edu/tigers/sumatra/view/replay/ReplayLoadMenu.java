/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.replay;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
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
				
				subMenu.addMenuListener(new MenuListener()
				{
					private boolean itemsAdded = false;
					
					
					@Override
					public void menuSelected(final MenuEvent menuEvent)
					{
						if (!itemsAdded)
						{
							dirs.forEach(d -> addFileToMenu(d, subMenu));
							itemsAdded = true;
						}
					}
					
					
					@Override
					public void menuDeselected(final MenuEvent menuEvent)
					{
						// no action
					}
					
					
					@Override
					public void menuCanceled(final MenuEvent menuEvent)
					{
						// no action
					}
				});
				return;
			}
		}
		JMenu subsubMenu = new JMenu(file.getName());
		menu.add(subsubMenu);
		
		JMenuItem rename = new JMenuItem("rename");
		JMenuItem run = new JMenuItem("run");
		JMenuItem delete = new JMenuItem("delete");
		JMenuItem zip = new JMenuItem("zip");
		
		subsubMenu.add(run);
		subsubMenu.add(rename);
		subsubMenu.add(zip);
		subsubMenu.add(delete);
		
		if (file.getName().endsWith(".zip"))
		{
			zip.setEnabled(false);
		}
		
		
		run.addActionListener(new RunActionListener(file.getAbsolutePath()));
		rename.addActionListener(new RenameActionListener(file.getAbsolutePath()));
		delete.addActionListener(new DeleteActionListener(file.getAbsolutePath()));
		zip.addActionListener(new ZipActionListener(file.getAbsolutePath()));
	}
	
	
	/**
	 * Observer
	 */
	public interface IReplayLoadMenuObserver
	{
		void onOpenReplay(BerkeleyDb db);
		
		
		void onCompressReplay(Path path);
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
	
	private class RunActionListener implements ActionListener
	{
		
		private final String filename;
		
		
		public RunActionListener(String filename)
		{
			this.filename = filename;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			Thread loadThread = new Thread(new LoadDatabase(filename), "LoadDatabase");
			loadThread.start();
		}
	}
	
	private class RenameActionListener implements ActionListener
	{
		
		private final String filename;
		
		
		public RenameActionListener(String filename)
		{
			this.filename = filename;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			File file = new File(filename);
			String newName = (String) JOptionPane.showInputDialog(null,
					"Change file name. Rename it to: ",
					"Rename File", JOptionPane.PLAIN_MESSAGE, null, null,
					file.getName());
			if (newName != null && !(file.renameTo(new File(file.getParent() + File.separator + newName))))
			{
				log.error("Renaming file failed.");
			}
		}
	}
	
	private class DeleteActionListener implements ActionListener
	{
		
		final String filename;
		
		
		public DeleteActionListener(final String absolutePath)
		{
			this.filename = absolutePath;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			
			if (JOptionPane.showConfirmDialog(null, "Do you want to delete '" + filename + "'?", "Confirm deletion",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				log.info("Deleting '" + filename + "'...");
				try
				{
					FileUtils.deleteDirectory(new File(filename));
				} catch (IOException exception)
				{
					log.error("Replay deletion not successful!", exception);
				}
			}
		}
	}
	
	private class ZipActionListener implements ActionListener
	{
		
		private final String filename;
		
		
		public ZipActionListener(String filename)
		{
			this.filename = filename;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent event)
		{
			for (IReplayLoadMenuObserver observer : observers)
			{
				observer.onCompressReplay(Paths.get(filename));
			}
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
			
			Optional<RecordManager> recordManager = SumatraModel.getInstance().getModuleOpt(RecordManager.class);
			if (recordManager.isPresent())
			{
				try
				{
					@SuppressWarnings("squid:S2095") // DB will be closed automatically later
					BerkeleyDb db = recordManager.get().newBerkeleyDb(Paths.get(filename));
					db.open();
					observers.forEach(o -> o.onOpenReplay(db));
				} catch (Exception e)
				{
					log.error("An exception occurred while loading database. See stacktrace.", e);
					JOptionPane.showMessageDialog(ReplayLoadMenu.this,
							"An exception occurred while loading database:\n"
									+ ExceptionUtils.getStackTrace(e),
							"Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else
			{
				JOptionPane.showMessageDialog(ReplayLoadMenu.this,
						"To watch a replay, you need to start the moduli system at least once " +
								"with a config that includes a record manager module, " +
								"because the record manager depends on the config (autoRef vs. AI).",
						"Missing RecordManager module",
						JOptionPane.ERROR_MESSAGE);
			}
			
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
