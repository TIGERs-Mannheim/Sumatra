/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): bernhard, AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.main;

import java.awt.Dimension;
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

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.AICenterPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.AboutDialog;
import edu.dhbw.mannheim.tigers.sumatra.view.main.EArgument;
import edu.dhbw.mannheim.tigers.sumatra.view.main.IMainFrame;
import edu.dhbw.mannheim.tigers.sumatra.view.main.IMainFrameObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.main.MainFrame;
import edu.dhbw.mannheim.tigers.sumatra.view.main.MainFrameNoGui;
import edu.moduli.exceptions.DependencyException;
import edu.moduli.exceptions.LoadModulesException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * The main presenter for Sumatra.
 * It is the brain of Sumatra.
 * It loads the modules, controls them and interact with
 * the view (GUI).
 * @author BernhardP, AndreR
 * 
 */
public class MainPresenter implements IMainFrameObserver, IModuliStateObserver, ILookAndFeelStateObserver
{
	// --------------------------------------------------------------------------
	// --- instance-variable(s) -------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log						= Logger.getLogger(MainPresenter.class.getName());
	
	// --- model and view ---
	private final SumatraModel		model;
	private GenericSkillSystem		skillSystem				= null;
	
	private IMainFrame				mainFrame;
	private MainFrame					mainFrameGUI			= null;
	
	private AICenterPresenter		aiPresenter				= null;
	
	// --- noGuiFlag ---
	private boolean					noguiFlag				= false;
	
	// Layout
	/** The key the currently selected layout is stored in the application properties */
	public static final String		KEY_LAYOUT_PROP		= MainPresenter.class.getName() + ".layout";
	/** */
	public static final String		LAYOUT_CONFIG_PATH	= "./config/gui/";
	/** */
	public static final String		LAYOUT_DEFAULT			= "default.ly";
	
	/** */
	public static final String		KEY_LAF_PROP			= MainPresenter.class.getName() + ".lookAndFeel";
	
	private GraphicsDevice			graphicsDevice			= null;
	
	private ToolbarPresenter		toolbar;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Initializes Sumatra.
	 * (Constructor of the Presenter)
	 * @param args
	 */
	public MainPresenter(String[] args)
	{
		log.trace("load");
		// --- set model ---
		model = SumatraModel.getInstance();
		
		// Configure IP4-stack
		final Properties props = System.getProperties();
		props.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperties(props);
		
		
		InfoNodeLookAndFeel.install();
		
		// --- parse arguments ---
		parseParameters(args);
		
		// --- add variable - Listener (stateModules) ---
		ModuliStateAdapter.getInstance().addObserver(this);
		
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		if (noguiFlag)
		{
			mainFrame = new MainFrameNoGui();
		} else
		{
			// --- start GUI ---
			mainFrameGUI = new MainFrame();
			mainFrame = mainFrameGUI;
			toolbar = new ToolbarPresenter(mainFrameGUI.getToolbar());
			mainFrameGUI.getToolbar().addObserver(toolbar);
			mainFrameGUI.getToolbar().addObserver(this);
		}
		log.trace("loaded");
	}
	
	
	/**
	 */
	public void start()
	{
		log.trace("start");
		loadLayoutAndConfig();
		refreshLayoutItems();
		refreshModuliItems();
		
		mainFrame.addObserver(this);
		
		if (noguiFlag)
		{
			toolbar.onStartStopModules();
		}
		log.trace("finished start");
	}
	
	
	/**
	 * @param view
	 */
	public void addView(ISumatraView view)
	{
		mainFrame.addView(view);
	}
	
	
	/**
	 * @param gd
	 */
	public void setFullscreen(GraphicsDevice gd)
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
		
		mainFrame.setMenuLayoutItems(filenames);
		mainFrame.selectLayoutItem(getCurrentLayout());
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
		
		mainFrame.setMenuModuliItems(filenames);
		mainFrame.selectModuliItem(model.getCurrentModuliConfig());
	}
	
	
	/**
	 * Sets default value for currentLayout and currentModuliConfig.
	 */
	private void loadLayoutAndConfig()
	{
		final Properties userSettings = SumatraModel.getInstance().getUserSettings();
		// ## Load position
		loadPosition(mainFrameGUI, userSettings);
		
		// ## Init moduli config
		final String moduliConfig = model.getCurrentModuliConfig();
		log.debug("Loading moduli config: " + moduliConfig);
		onLoadModuliConfig(moduliConfig);
		mainFrame.selectModuliItem(moduliConfig);
		
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
	
	
	private void parseParameters(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			EArgument arg;
			try
			{
				arg = EArgument.valueOf(args[i].toUpperCase());
			} catch (final IllegalArgumentException err)
			{
				log.warn("'" + args[i] + "' is no valid argument!");
				continue;
			}
			
			switch (arg)
			{
				case NOGUI:
					noguiFlag = true;
					break;
				
				case MODULICONFIG:
					if ((args.length - 1) >= (i + 1))
					{
						final String configFile = args[i + 1];
						model.setCurrentModuliConfig(configFile);
						log.debug("Using passed '" + configFile + "' as moduli config-file!");
						i++;
					} else
					{
						log.debug("'moduliconfig' expects a filename as parameter!");
					}
					break;
				
				default:
					log.debug("No action defined for '" + arg + "'.");
			}
		}
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
	}
	
	
	@Override
	public void onSaveLayout()
	{
		// --- Ask for the filename ---
		final String initial = model.getUserProperty(KEY_LAYOUT_PROP);
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
		
		mainFrame.saveLayout(filenameWithPath);
		
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
		// ### Persist user settings
		final Properties appProps = model.getUserSettings();
		
		
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
		
		if (model.getModulesState().get() == ModulesState.ACTIVE)
		{
			model.stopModules();
		}
		
		// --- exit application ---
		System.exit(0);
	}
	
	
	@Override
	public void onLoadLayout(String filename)
	{
		String path = LAYOUT_CONFIG_PATH + filename;
		setCurrentLayout(filename);
		
		if (!new File(path).exists())
		{
			log.warn("Layout file: " + path + " does not exist, falling back to " + LAYOUT_DEFAULT);
			path = LAYOUT_CONFIG_PATH + LAYOUT_DEFAULT;
			setCurrentLayout(LAYOUT_DEFAULT);
		}
		
		mainFrame.loadLayout(path);
		
		// --- DEBUG msg ---
		log.debug("Loaded layout: " + path);
	}
	
	
	@Override
	public void onLoadModuliConfig(String filename)
	{
		// --- if module-system = active -> stop modules ---
		if (model.getModulesState().get() == ModulesState.ACTIVE)
		{
			model.stopModules();
			model.getModulesState().set(ModulesState.RESOLVED);
		}
		
		// --- set new config-file and load it ---
		model.setCurrentModuliConfig(filename);
		
		// --- load modules into Sumatra ---
		// --- module-handle ---
		try
		{
			// --- get modules from configuration-file ---
			model.loadModules(SumatraModel.MODULI_CONFIG_PATH + model.getCurrentModuliConfig());
			log.debug("Loaded config: " + filename);
		} catch (final LoadModulesException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + model.getCurrentModuliConfig() + "') ");
		} catch (final DependencyException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + model.getCurrentModuliConfig() + "') ");
		}
		
		// Get SkillSystem for emergency-stop!
		try
		{
			skillSystem = (GenericSkillSystem) model.getModule(ASkillSystem.MODULE_ID);
			
		} catch (final ModuleNotFoundException err)
		{
			log.fatal("Skillsystem not found");
		}
	}
	
	
	@Override
	public void onRefreshLayoutItems()
	{
		refreshLayoutItems();
	}
	
	
	@Override
	public void onSelectLookAndFeel(LookAndFeelInfo info)
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
	public void onSetFullscreen(GraphicsDevice gd)
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
		mainFrame.setLookAndFeel(getCurrentLookAndFeel());
	}
	
	
	@Override
	public void onStartStopModules()
	{
		// this is done in ToolbarPresenter
	}
	
	
	@Override
	public void onEmergencyStop()
	{
		skillSystem.emergencyStop();
		if (aiPresenter != null)
		{
			aiPresenter.onEmergencyMode();
		}
	}
	
	
	/**
	 * @param presenter
	 */
	public void addAIPresenter(AICenterPresenter presenter)
	{
		aiPresenter = presenter;
	}
	
	
	// ---------------------------------------------------------------
	// -------------- Save Gui Position and size ---------------------
	// ---------------------------------------------------------------
	/**
	 * Load JFrame size and position.
	 */
	private void loadPosition(JFrame frame, Properties appProps)
	{
		final String name = this.getClass().getName();
		
		final int displayCount = getInt(appProps, name + ".disyplayCount", 1);
		int x = 0;
		int y = 0;
		
		if (displayCount == getNumberOfDisplays())
		{
			/*
			 * no changes of available displays thus load window
			 * position from config file.
			 */
			x = getInt(appProps, name + ".x", 0);
			y = getInt(appProps, name + ".y", 0);
		}
		
		final int w = getInt(appProps, name + ".w", 1456);
		final int h = getInt(appProps, name + ".h", 886);
		
		frame.setLocation(x, y);
		frame.setSize(new Dimension(w, h));
	}
	
	
	/**
	 * Save JFrame size and position.
	 */
	private void savePosition(JFrame frame, Properties appProps)
	{
		final String prefix = this.getClass().getName();
		appProps.setProperty(prefix + ".disyplayCount", "" + getNumberOfDisplays());
		appProps.setProperty(prefix + ".x", "" + frame.getX());
		appProps.setProperty(prefix + ".y", "" + frame.getY());
		appProps.setProperty(prefix + ".w", "" + frame.getWidth());
		appProps.setProperty(prefix + ".h", "" + frame.getHeight());
	}
	
	
	/**
	 * Read parameter from property file.
	 * 
	 * @param props , the property file
	 * @param name , value to read
	 * @param defaultValue
	 */
	private int getInt(Properties props, String name, int defaultValue)
	{
		final String v = props.getProperty(name);
		if (v == null)
		{
			return defaultValue;
		}
		return Integer.parseInt(v);
		
	}
	
	
	/**
	 * 
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
	
	
	/**
	 * @return
	 */
	public boolean hasGUI()
	{
		return !noguiFlag;
	}
	
	
	// --------------------------------------------------------------------------
	// --- property getter ------------------------------------------------------
	// --------------------------------------------------------------------------
	private String getCurrentLookAndFeel()
	{
		return model.getUserProperty(MainPresenter.KEY_LAF_PROP);
	}
	
	
	private void setCurrentLookAndFeel(String newLookAndFeel)
	{
		model.setUserProperty(MainPresenter.KEY_LAF_PROP, newLookAndFeel);
	}
	
	
	private String getCurrentLayout()
	{
		return model.getUserProperty(MainPresenter.KEY_LAYOUT_PROP);
	}
	
	
	private void setCurrentLayout(String newLayout)
	{
		model.setUserProperty(MainPresenter.KEY_LAYOUT_PROP, newLayout);
	}
}
