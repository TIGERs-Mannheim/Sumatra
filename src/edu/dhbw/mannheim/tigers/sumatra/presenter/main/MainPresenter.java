/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): bernhard, AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.main;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import net.infonode.gui.laf.InfoNodeLookAndFeel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.view.main.AboutDialog;
import edu.dhbw.mannheim.tigers.sumatra.view.main.IMainFrameObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.MainFrame;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.AICenterView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.BotCenterView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.BotOverviewView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.ConfigEditorView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.LogView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.RcmView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.RefereeView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.StatisticsView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.TimerView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.VisualizerView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.WPCenterView;


/**
 * The main presenter for Sumatra.
 * It is the brain of Sumatra.
 * It loads the modules, controls them and interact with
 * the view (GUI).
 * 
 * @author BernhardP, AndreR
 */
public class MainPresenter implements IMainFrameObserver, IModuliStateObserver, ILookAndFeelStateObserver
{
	// --------------------------------------------------------------------------
	// --- instance-variable(s) -------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log						= Logger.getLogger(MainPresenter.class.getName());
	
	// --- model and view ---
	private MainFrame					mainFrameGUI			= null;
	
	
	// Layout
	/** The key the currently selected layout is stored in the application properties */
	public static final String		KEY_LAYOUT_PROP		= MainPresenter.class.getName() + ".layout";
	/** */
	public static final String		LAYOUT_CONFIG_PATH	= "./config/gui/";
	/** */
	public static final String		LAYOUT_DEFAULT			= "default.ly";
	
	/** */
	public static final String		KEY_LAF_PROP			= MainPresenter.class.getName() + ".lookAndFeel";
	
	private static final String	LAST_LAYOUT_FILENAME	= "last.ly";
	
	private GraphicsDevice			graphicsDevice			= null;
	
	private ToolbarPresenter		toolbar;
	private ASkillSystem				skillSystem				= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Initializes Sumatra.
	 * (Constructor of the Presenter)
	 */
	public MainPresenter()
	{
		log.trace("load");
		
		InfoNodeLookAndFeel.install();
		log.trace("info node look and feel installed");
		
		// --- add variable - Listener (stateModules) ---
		ModuliStateAdapter.getInstance().addObserver(this);
		log.trace("ModuliStateAdapter created");
		
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		log.trace("LookAndFeelStateAdapter created");
		
		// --- start GUI ---
		mainFrameGUI = new MainFrame();
		toolbar = new ToolbarPresenter(mainFrameGUI.getToolbar());
		mainFrameGUI.getToolbar().addObserver(toolbar);
		mainFrameGUI.getToolbar().addObserver(this);
		
		addView(new AICenterView(ETeamColor.YELLOW));
		addView(new AICenterView(ETeamColor.BLUE));
		addView(new BotCenterView());
		addView(new ConfigEditorView());
		addView(new LogView());
		addView(new RcmView());
		addView(new RefereeView());
		addView(new TimerView());
		addView(new VisualizerView());
		addView(new WPCenterView());
		addView(new BotOverviewView());
		addView(new StatisticsView());
		
		
		loadLayoutAndConfig();
		refreshLayoutItems();
		refreshModuliItems();
		
		mainFrameGUI.addObserver(this);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// initialize all views that are currently visible
				for (ASumatraView view : mainFrameGUI.getViews())
				{
					if (view.getView().isShowing())
					{
						view.ensureInitialized();
					}
				}
				mainFrameGUI.updateViewMenu();
			}
		});
		
		GlobalShortcuts.register(EShortcut.MATCH_LAYOUT, new Runnable()
		{
			@Override
			public void run()
			{
				onLoadLayout("match.ly");
			}
		});
		
		GlobalShortcuts.register(EShortcut.TIMEOUT_LAYOUT, new Runnable()
		{
			@Override
			public void run()
			{
				onLoadLayout("timeout.ly");
			}
		});
		
		GlobalShortcuts.register(EShortcut.DEFAULT_LAYOUT, new Runnable()
		{
			@Override
			public void run()
			{
				onLoadLayout("default.ly");
			}
		});
		
		log.trace("loaded");
	}
	
	
	/**
	 * @param view
	 */
	public final void addView(final ASumatraView view)
	{
		mainFrameGUI.addView(view);
	}
	
	
	/**
	 * @param gd
	 */
	public void setFullscreen(final GraphicsDevice gd)
	{
		if (mainFrameGUI == null)
		{
			return;
		}
		
		if (gd != null)
		{
			if (graphicsDevice == null)
			{
				graphicsDevice = gd;
				
				mainFrameGUI.dispose();
				mainFrameGUI.setResizable(false);
				mainFrameGUI.setUndecorated(true);
			} else
			{
				graphicsDevice.setFullScreenWindow(null);
				
				graphicsDevice = gd;
			}
			
			final GraphicsConfiguration gc = graphicsDevice.getDefaultConfiguration();
			log.debug("Display size: " + gc.getBounds().width + " x " + gc.getBounds().height);
			
			try
			{
				graphicsDevice.setFullScreenWindow(mainFrameGUI);
			} catch (final Exception e)
			{
				log.error("Fullscreen failed" + e.getMessage(), e);
				
				setFullscreen(null);
			}
			
			if (graphicsDevice.getFullScreenWindow() == null)
			{
				log.error("Fullscreen failed");
				
				setFullscreen(null);
			}
		} else
		{
			if (graphicsDevice != null)
			{
				graphicsDevice.setFullScreenWindow(null);
				
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						mainFrameGUI.setVisible(false);
						mainFrameGUI.dispose();
						mainFrameGUI.setResizable(true);
						mainFrameGUI.setIgnoreRepaint(false);
						mainFrameGUI.setUndecorated(false);
						mainFrameGUI.pack();
						mainFrameGUI.setVisible(true);
					}
				});
				
				graphicsDevice = null;
			}
		}
	}
	
	
	private void refreshLayoutItems()
	{
		final ArrayList<String> filenames = new ArrayList<String>();
		
		// --- read all available layouts from guiLayout-folder ---
		final File dir = new File(LAYOUT_CONFIG_PATH);
		final File[] fileList = dir.listFiles();
		
		for (final File file : fileList)
		{
			if (!file.isHidden() && !file.getName().startsWith("."))
			{
				filenames.add(file.getName());
			}
		}
		
		mainFrameGUI.setMenuLayoutItems(filenames);
		mainFrameGUI.selectLayoutItem(getCurrentLayout());
	}
	
	
	private void refreshModuliItems()
	{
		final ArrayList<String> filenames = new ArrayList<String>();
		
		// --- read all config-files from config-folder ---
		final File dir = new File(SumatraModel.MODULI_CONFIG_PATH);
		final File[] fileList = dir.listFiles();
		for (final File file : fileList)
		{
			if (!file.isHidden() && !file.getName().startsWith("."))
			{
				filenames.add(file.getName());
			}
		}
		
		mainFrameGUI.setMenuModuliItems(filenames);
		mainFrameGUI.selectModuliItem(SumatraModel.getInstance().getCurrentModuliConfig());
	}
	
	
	/**
	 * Sets default value for currentLayout and currentModuliConfig.
	 */
	private void loadLayoutAndConfig()
	{
		final Properties userSettings = SumatraModel.getInstance().getUserSettings();
		// ## Load position
		loadPosition(mainFrameGUI, userSettings);
		mainFrameGUI.setVisible(true);
		
		// ## Init moduli config
		final String moduliConfig = SumatraModel.getInstance().getCurrentModuliConfig();
		mainFrameGUI.selectModuliItem(moduliConfig);
		
		// ## Init Layout
		final String layout = userSettings.getProperty(MainPresenter.KEY_LAYOUT_PROP);
		log.debug("Loading layout: " + layout);
		onLoadLayout(layout);
		
		
		// ## Init Look-and-Feel
		final LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		String lookAndFeel = userSettings.getProperty(MainPresenter.KEY_LAF_PROP);
		if (lookAndFeel == null)
		{
			log.debug("Unproper Lool-and-Feel, taking System default.");
			lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		}
		
		boolean found = false;
		for (final LookAndFeelInfo info : lafs)
		{
			if (info.getClassName().equals(lookAndFeel))
			{
				log.debug("Loading look and feel: " + info.getName());
				onSelectLookAndFeel(info);
				mainFrameGUI.selectLookAndFeelItem(info.getName());
				found = true;
				break;
			}
		}
		
		if (!found)
		{
			// Causes it to select system default
			onSelectLookAndFeel(null);
		}
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				try
				{
					skillSystem = (ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get skillsystem module");
				}
				mainFrameGUI.setModuliMenuEnabled(false);
				break;
			case NOT_LOADED:
			case RESOLVED:
				mainFrameGUI.setModuliMenuEnabled(true);
				break;
		}
	}
	
	
	@Override
	public void onSaveLayout()
	{
		// --- Ask for the filename ---
		final String initial = SumatraModel.getInstance().getUserProperty(KEY_LAYOUT_PROP);
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
	public void onAbout()
	{
		new AboutDialog().setVisible(true);
	}
	
	
	@Override
	public void onExit()
	{
		// save current layout to separat file
		saveCurrentLayout();
		// save last layout for next usage
		SumatraModel.getInstance().setUserProperty(MainPresenter.KEY_LAYOUT_PROP, LAST_LAYOUT_FILENAME);
		
		// ### Persist user settings
		final Properties appProps = SumatraModel.getInstance().getUserSettings();
		
		
		// --- save gui position ---
		savePosition(mainFrameGUI, appProps);
		
		
		// --- persist application properties ---
		final File uf = SumatraModel.getInstance().getUserPropertiesFile();
		
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(uf);
			appProps.store(out, null);
		} catch (final IOException err)
		{
			log.warn("Could not write to " + uf.getPath() + ", configuration is not saved");
		}
		
		if (out != null)
		{
			try
			{
				out.close();
				log.debug("Saved configuration to: " + uf.getPath());
			} catch (IOException e)
			{
				log.warn("Could not close " + uf.getPath() + ", configuration is not saved");
			}
		}
		
		if (SumatraModel.getInstance().getModulesState().get() == ModulesState.ACTIVE)
		{
			SumatraModel.getInstance().stopModules();
		}
		
		// --- exit application ---
		System.exit(0);
	}
	
	
	@Override
	public void onLoadLayout(final String filename)
	{
		String path = LAYOUT_CONFIG_PATH + filename;
		setCurrentLayout(filename);
		
		if (!new File(path).exists())
		{
			log.warn("Layout file: " + path + " does not exist, falling back to " + LAYOUT_DEFAULT);
			path = LAYOUT_CONFIG_PATH + LAYOUT_DEFAULT;
			setCurrentLayout(LAYOUT_DEFAULT);
		}
		
		mainFrameGUI.loadLayout(path);
		
		// --- DEBUG msg ---
		log.debug("Loaded layout: " + path);
	}
	
	
	@Override
	public void onRefreshLayoutItems()
	{
		refreshLayoutItems();
	}
	
	
	@Override
	public void onSelectLookAndFeel(final LookAndFeelInfo info)
	{
		try
		{
			String currentLafName = null;
			if (info == null)
			{
				currentLafName = UIManager.getSystemLookAndFeelClassName();
			} else
			{
				currentLafName = info.getClassName();
			}
			
			setCurrentLookAndFeel(currentLafName);
			UIManager.setLookAndFeel(currentLafName);
		} catch (final ClassNotFoundException err)
		{
		} catch (final InstantiationException err)
		{
		} catch (final IllegalAccessException err)
		{
		} catch (final UnsupportedLookAndFeelException err)
		{
		}
	}
	
	
	@Override
	public void onSetFullscreen(final GraphicsDevice gd)
	{
		if (mainFrameGUI == null)
		{
			return;
		}
		
		setFullscreen(gd);
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
		mainFrameGUI.setLookAndFeel(getCurrentLookAndFeel());
	}
	
	
	@Override
	public void onStartStopModules()
	{
		// this is done in ToolbarPresenter
	}
	
	
	@Override
	public void onEmergencyStop()
	{
		for (ASumatraView sumatraView : mainFrameGUI.getViews())
		{
			if (sumatraView.isInitialized())
			{
				sumatraView.getPresenter().onEmergencyStop();
			}
		}
		if (skillSystem != null)
		{
			skillSystem.emergencyStop();
		}
	}
	
	
	// ---------------------------------------------------------------
	// -------------- Save Gui Position and size ---------------------
	// ---------------------------------------------------------------
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
	
	
	private void saveCurrentLayout()
	{
		final String filenameWithPath = LAYOUT_CONFIG_PATH + LAST_LAYOUT_FILENAME;
		
		mainFrameGUI.saveLayout(filenameWithPath);
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
	
	
	// --------------------------------------------------------------------------
	// --- property getter ------------------------------------------------------
	// --------------------------------------------------------------------------
	private String getCurrentLookAndFeel()
	{
		return SumatraModel.getInstance().getUserProperty(MainPresenter.KEY_LAF_PROP);
	}
	
	
	private void setCurrentLookAndFeel(final String newLookAndFeel)
	{
		SumatraModel.getInstance().setUserProperty(MainPresenter.KEY_LAF_PROP, newLookAndFeel);
	}
	
	
	private String getCurrentLayout()
	{
		return SumatraModel.getInstance().getUserProperty(MainPresenter.KEY_LAYOUT_PROP);
	}
	
	
	private void setCurrentLayout(final String newLayout)
	{
		SumatraModel.getInstance().setUserProperty(MainPresenter.KEY_LAYOUT_PROP, newLayout);
	}
}
