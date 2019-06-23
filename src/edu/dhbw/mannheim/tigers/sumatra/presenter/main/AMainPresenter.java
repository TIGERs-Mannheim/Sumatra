/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.main;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.view.main.AMainFrame;
import edu.dhbw.mannheim.tigers.sumatra.view.main.IMainFrameObserver;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;


/**
 * Base for MainPresenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AMainPresenter implements IMainFrameObserver
{
	private static final Logger	log						= Logger.getLogger(AMainPresenter.class.getName());
	private AMainFrame				mainFrameGUI;
	
	/** */
	protected static final String	LAYOUT_CONFIG_PATH	= "./config/gui/";
	
	/** */
	protected static final String	KEY_LAF_PROP			= MainPresenter.class.getName() + ".lookAndFeel";
	
	
	protected final void init(final AMainFrame mainFrame)
	{
		mainFrameGUI = mainFrame;
		
		final Properties userSettings = SumatraModel.getInstance().getUserSettings();
		// ## Load position
		loadPosition(mainFrameGUI, userSettings);
		mainFrameGUI.setVisible(true);
		
		// ## Init Layout
		final String layout = userSettings.getProperty(getLayoutKey());
		onLoadLayout(layout);
		
		refreshLayoutItems();
		
		mainFrameGUI.addObserver(this);
		
		
		EventQueue.invokeLater(() -> {
			// initialize all views that are currently visible
				for (ASumatraView view : mainFrameGUI.getViews())
				{
					if (view.getView().isShowing())
					{
						view.ensureInitialized();
					}
				}
				mainFrameGUI.updateViewMenu();
			});
	}
	
	
	/**
	 * @param view
	 */
	public final void addView(final ASumatraView view)
	{
		mainFrameGUI.addView(view);
	}
	
	
	private void refreshLayoutItems()
	{
		final ArrayList<String> filenames = new ArrayList<String>();
		
		// --- read all available layouts from guiLayout-folder ---
		final File dir = new File(LAYOUT_CONFIG_PATH);
		final File[] fileList = dir.listFiles();
		
		if (fileList != null)
		{
			for (final File file : fileList)
			{
				if (!file.isHidden() && !file.getName().startsWith("."))
				{
					filenames.add(file.getName());
				}
			}
		}
		
		mainFrameGUI.setMenuLayoutItems(filenames);
		mainFrameGUI.selectLayoutItem(getCurrentLayout());
	}
	
	
	@Override
	public void onSaveLayout()
	{
		// --- Ask for the filename ---
		final String initial = SumatraModel.getInstance().getUserProperty(getLayoutKey());
		String filename = JOptionPane.showInputDialog(null, "Please specify the name of the layout file:", initial);
		
		if (filename == null)
		{
			return;
		}
		
		// --- add .ly if necessary ---
		if (!filename.endsWith(".ly"))
		{
			filename += ".ly";
		}
		
		final String filenameWithPath = LAYOUT_CONFIG_PATH + filename;
		
		mainFrameGUI.saveLayout(filenameWithPath);
		
		// --- DEBUG msg ---
		log.debug("Saved layout to: " + filename);
		
		refreshLayoutItems();
	}
	
	
	@Override
	public void onDeleteLayout()
	{
		// --- delete current layout ---
		final String currentLayout = getCurrentLayout();
		final File file = new File(LAYOUT_CONFIG_PATH + currentLayout);
		
		// --- if file exists -> delete ---
		if (file.exists())
		{
			boolean deleted = file.delete();
			if (!deleted)
			{
				log.error("Could not delete file:" + file);
			}
		}
		
		refreshLayoutItems();
		
		log.debug("Deleted layout: " + currentLayout);
	}
	
	
	@Override
	public void onRefreshLayoutItems()
	{
		refreshLayoutItems();
	}
	
	
	@Override
	public void onLoadLayout(final String filename)
	{
		String path = LAYOUT_CONFIG_PATH + filename;
		setCurrentLayout(filename);
		
		if (!new File(path).exists())
		{
			log.warn("Layout file: " + path + " does not exist, falling back to " + getDefaultLayout());
			path = LAYOUT_CONFIG_PATH + getDefaultLayout();
			setCurrentLayout(getDefaultLayout());
		}
		
		mainFrameGUI.loadLayout(path);
		
		// --- DEBUG msg ---
		log.debug("Loaded layout: " + path);
	}
	
	
	@Override
	public void onExit()
	{
		// ### Persist user settings
		final Properties appProps = SumatraModel.getInstance().getUserSettings();
		
		// --- save gui position ---
		savePosition(mainFrameGUI, appProps);
		
		// save current layout to separat file
		saveCurrentLayout();
		// save last layout for next usage
		SumatraModel.getInstance().setUserProperty(getLayoutKey(), getLastLayoutFile());
	}
	
	
	private void saveCurrentLayout()
	{
		final String filenameWithPath = LAYOUT_CONFIG_PATH + getLastLayoutFile();
		
		mainFrameGUI.saveLayout(filenameWithPath);
	}
	
	
	/**
	 * @return
	 */
	public abstract String getLastLayoutFile();
	
	
	/**
	 * Load JFrame size and position.
	 */
	private void loadPosition(final JFrame frame, final Properties appProps)
	{
		final String prefix = this.getClass().getName();
		
		final int displayCount = getInt(appProps, prefix + ".disyplayCount", 1);
		int x = 0;
		int y = 0;
		
		if (displayCount == getNumberOfDisplays())
		{
			/*
			 * no changes of available displays thus load window
			 * position from config file.
			 */
			x = getInt(appProps, prefix + ".x", 0);
			y = getInt(appProps, prefix + ".y", 0);
		}
		frame.setLocation(x, y);
		
		final String strMaximized = appProps.getProperty(prefix + ".maximized", "true");
		final boolean maximized = Boolean.valueOf(strMaximized);
		
		if (maximized)
		{
			frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
		} else
		{
			final int w = getInt(appProps, prefix + ".w", 1456);
			final int h = getInt(appProps, prefix + ".h", 886);
			
			frame.setSize(new Dimension(w, h));
		}
	}
	
	
	/**
	 * Save JFrame size and position.
	 */
	private void savePosition(final JFrame frame, final Properties appProps)
	{
		final String prefix = this.getClass().getName();
		appProps.setProperty(prefix + ".disyplayCount", "" + getNumberOfDisplays());
		appProps.setProperty(prefix + ".x", "" + frame.getX());
		appProps.setProperty(prefix + ".y", "" + frame.getY());
		appProps.setProperty(prefix + ".w", "" + frame.getWidth());
		appProps.setProperty(prefix + ".h", "" + frame.getHeight());
		appProps.setProperty(prefix + ".maximized",
				String.valueOf((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH));
	}
	
	
	/**
	 * Read parameter from property file.
	 * 
	 * @param props , the property file
	 * @param name , value to read
	 * @param defaultValue
	 */
	private int getInt(final Properties props, final String name, final int defaultValue)
	{
		final String v = props.getProperty(name);
		if (v == null)
		{
			return defaultValue;
		}
		return Integer.parseInt(v);
		
	}
	
	
	/**
	 * Returns the number of displays.
	 * 
	 * @return the number of available display devices (default return is 1)
	 */
	private int getNumberOfDisplays()
	{
		try
		{
			// Get local graphics environment
			final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			
			return env.getScreenDevices().length;
			
		} catch (final HeadlessException e)
		{
			log.fatal("HeadlessException", e);
		}
		
		return 1;
	}
	
	
	private String getCurrentLayout()
	{
		return SumatraModel.getInstance().getUserProperty(getLayoutKey(), getDefaultLayout());
	}
	
	
	private void setCurrentLayout(final String newLayout)
	{
		SumatraModel.getInstance().setUserProperty(getLayoutKey(), newLayout);
	}
	
	
	protected abstract String getLayoutKey();
	
	
	protected abstract String getDefaultLayout();
}
