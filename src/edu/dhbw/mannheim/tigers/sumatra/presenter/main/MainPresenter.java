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

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import net.infonode.gui.laf.InfoNodeLookAndFeel;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
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
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.LoadModulesException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.exceptions.StartModuleException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * The main presenter for Sumatra.
 * It is the brain of Sumatra.
 * It loads the modules, controls them and interact with
 * the view (GUI).
 * @author BernhardP, AndreR
 * 
 */
public class MainPresenter implements IMainFrameObserver, IModuliStateObserver, ILookAndFeelStateObserver,
		AWTEventListener
{
	// --------------------------------------------------------------------------
	// --- instance-variable(s) -------------------------------------------------
	// --------------------------------------------------------------------------
	protected final Logger			log						= Logger.getLogger(getClass());
	
	// --- model and view ---
	private final SumatraModel		model;
	private GenericSkillSystem		skillSystem				= null;
	
	private IMainFrame				mainFrame;
	private MainFrame					mainFrameGUI			= null;
	
	private AICenterPresenter		aiPresenter				= null;
	
	// --- noGuiFlag ---
	private boolean					noguiFlag				= false;
	
	/**
	 * Path where all layouts are located.
	 */
	public final static String		LAYOUT_CONFIG_PATH	= "./config/guiLayouts/";
	public final static String		LAYOUT_DEFAULT			= "default.ly";
	
	/** path to the configuration for pos. and size of the sumatra gui. */
	private static final String	GUI_SIZE_POS_FILE		= "guiposition.props";
	private static final String	GUI_SIZE_POS_PATH		= LAYOUT_CONFIG_PATH + GUI_SIZE_POS_FILE;
	
	private final static String	CONFIG_SETTINGS_PATH	= "./config/userSettings/";
	
	private String						currentLayout			= LAYOUT_DEFAULT;
	
	private GraphicsDevice			graphicsDevice			= null;
	
	private String						currentLafName			= "";
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Initializes Sumatra.
	 * (Constructor of the Presenter)
	 */
	public MainPresenter(String[] args)
	{
		// --- set model ---
		this.model = SumatraModel.getInstance();
		
		// --- configure log4j-logger ---
		BasicConfigurator.configure();
		
		// Configure IP4-stack
		Properties props = System.getProperties();
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
			// Remove console-logger
			Logger root = Logger.getRootLogger();
			// TODO log4j-"workaround": BasicConfigurator introduces only one Logger for the console. But this way to get
			// it is not very... you know... not a nice way
			Enumeration<?> enumm = root.getAllAppenders();
			Appender consoleAppender = (Appender) enumm.nextElement();
			root.removeAppender(consoleAppender);
			

			// --- start GUI ---
			mainFrameGUI = new MainFrame();
			mainFrame = mainFrameGUI;
			
			// --- necessary for saving gui size and position ---
			Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK);
		}
	}
	

	public void start()
	{
		refreshLayoutItems();
		refreshModuliItems();
		setDefaultLayoutAndModuliConfig();
		
		mainFrame.addObserver(this);
		
		if (noguiFlag)
		{
			onStartStopModules();
		}
	}
	

	public void addView(ISumatraView view)
	{
		mainFrame.addView(view);
	}
	

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
			
			GraphicsConfiguration gc = graphicsDevice.getDefaultConfiguration();
			log.debug("Display size: " + gc.getBounds().width + " x " + gc.getBounds().height);
			
			try
			{
				graphicsDevice.setFullScreenWindow(mainFrameGUI);
			} catch (Throwable e)
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
		ArrayList<String> filenames = new ArrayList<String>();
		
		// --- read all available layouts from guiLayout-folder ---
		File dir = new File(LAYOUT_CONFIG_PATH);
		File[] fileList = dir.listFiles();
		
		for (File f : fileList)
		{
			if (!f.isHidden() && !f.getName().equals(".svn") && !f.getName().equals(GUI_SIZE_POS_FILE))
			{
				filenames.add(f.getName());
			}
		}
		
		mainFrame.setMenuLayoutItems(filenames);
		mainFrame.selectLayoutItem(currentLayout);
	}
	

	private void refreshModuliItems()
	{
		ArrayList<String> filenames = new ArrayList<String>();
		
		// --- read all config-files from config-folder ---
		File dir = new File(SumatraModel.MODULI_CONFIG_PATH);
		File[] fileList = dir.listFiles();
		for (File f : fileList)
		{
			if (!f.isHidden())
			{
				filenames.add(f.getName());
			}
		}
		
		mainFrame.setMenuModuliItems(filenames);
		mainFrame.selectModuliItem(model.getCurrentModuliConfig());
	}
	

	/**
	 * Sets default value for currentLayout and currentModuliConfig.
	 */
	private void setDefaultLayoutAndModuliConfig()
	{
		// --- get pc-name and user-name ---
		String pcName = null;
		String moduliConfig = null;
		Properties props = new Properties();
		FileInputStream in;
		String lookAndFeelName = "";
		
		try
		{
			pcName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException err)
		{
			log.warn("Could not resolve local PC name, loading default configs");
			
			init(currentLayout, moduliConfig, "");
			return;
		}
		
		String userName = System.getProperty("user.name");
		String filename = CONFIG_SETTINGS_PATH + pcName + "_" + userName + ".gui";
		
		// --- load properties ---
		try
		{
			in = new FileInputStream(filename);
			props.load(in);
			in.close();
		} catch (FileNotFoundException err)
		{
			log.warn("Config: " + filename + " not found, loading default configs");
			moduliConfig = SumatraModel.MODULI_DEFAULT_CONFIG;
			init(currentLayout, moduliConfig, "");
			return;
		} catch (IOException err)
		{
			log.warn("Config: " + filename + " cannot be read, loading default configs");
			moduliConfig = SumatraModel.MODULI_DEFAULT_CONFIG;
			init(currentLayout, moduliConfig, "");
			return;
		}
		
		// --- set vars ---
		currentLayout = props.getProperty("currentLayout", currentLayout);
		lookAndFeelName = props.getProperty("currentLookAndFeel", "");
		moduliConfig = props.getProperty("moduli", SumatraModel.MODULI_DEFAULT_CONFIG);
		AAgent.currentConfig = props.getProperty("ai", AAgent.AI_DEFAULT_CONFIG);
		ABotManager.setSelectedPersistentConfig(props.getProperty("botmanager", ABotManager.BOTMANAGER_DEFAULT_CONFIG));
		
		init(currentLayout, moduliConfig, lookAndFeelName);
	}
	

	private void init(String layout, String moduliConfig, String lookAndFeelName)
	{
		log.debug("Loading layout: " + layout);
		onLoadLayout(layout);
		
		log.debug("Loading moduli config: " + moduliConfig);
		onLoadConfig(moduliConfig);
		mainFrame.selectModuliItem(moduliConfig);
		
		LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo info : lafs)
		{
			if (info.getName().equals(lookAndFeelName))
			{
				log.debug("Loading look and feel: " + info.getName());
				onSelectLookAndFeel(info);
				break;
			}
			
			if (lookAndFeelName.equals("") && info.getClassName().equals(UIManager.getSystemLookAndFeelClassName()))
			{
				log.debug("Loading look and feel: " + info.getName());
				onSelectLookAndFeel(info);
				break;
			}
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
			} catch (IllegalArgumentException err)
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
					if (args.length - 1 >= i + 1)
					{
						String configFile = args[i + 1];
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
		// --- set control modules, graphical buttons, etc. ---
		switch (state)
		{
			case NOT_LOADED:
				mainFrame.setStartStopButtonState(false);
				break;
			
			case RESOLVED:
				mainFrame.setStartStopButtonState(true, new ImageIcon(ClassLoader.getSystemResource("start.png")));
				break;
			
			case ACTIVE:
				mainFrame.setStartStopButtonState(true, new ImageIcon(ClassLoader.getSystemResource("stop.png")));
				break;
		}
	}
	

	@Override
	public void onStartStopModules()
	{
		switch (model.getModulesState().get())
		{
			case RESOLVED:
				try
				{
					model.startModules();
				} catch (InitModuleException err)
				{
					log.error("Cannot init modules: " + err.getMessage());
				} catch (StartModuleException err)
				{
					log.error("Cannot start modules: " + err.getMessage());
				}
				model.getModulesState().set(ModulesState.ACTIVE);
				break;
			case ACTIVE:
				model.stopModules();
				model.getModulesState().set(ModulesState.RESOLVED);
				break;
		}
	}
	

	@Override
	public void onSaveLayout()
	{
		// --- Ask for the filename ---
		String filename = JOptionPane.showInputDialog(null, "Please specify the name of the layout file:", "Layout name",
				JOptionPane.QUESTION_MESSAGE);
		
		if (filename == null)
		{
			return;
		}
		
		// --- add .ly if necessary ---
		if (!filename.endsWith(".ly"))
		{
			filename += ".ly";
		}
		
		String filenameWithPath = LAYOUT_CONFIG_PATH + filename;
		
		mainFrame.saveLayout(filenameWithPath);
		
		// --- debug msg ---
		log.debug("Saved layout to: " + filename);
		
		refreshLayoutItems();
	}
	

	@Override
	public void onDeleteLayout()
	{
		// --- delete current layout ---
		File file = new File(LAYOUT_CONFIG_PATH + currentLayout);
		
		// --- if file exists -> delete ---
		if (file.exists())
		{
			file.delete();
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
		// --- store properties of moduli- and layout-configuration ---
		Properties props = new Properties();
		
		// --- get and store properties ---
		
		// layout
		if (currentLayout != null)
		{
			props.put("currentLayout", currentLayout);
		}
		
		// laf
		props.put("currentLookAndFeel", UIManager.getLookAndFeel().getName());
		
		// moduli
		props.put("moduli", SumatraModel.getInstance().getCurrentModuliConfig());
		
		// ai
		if (AAgent.currentConfig != null)
		{
			props.put("ai", AAgent.currentConfig);
		}
		
		// botmanager
		if (ABotManager.getSelectedPersistentConfig() != null)
		{
			props.put("botmanager", ABotManager.getSelectedPersistentConfig());
		}
		

		// --- get pc-name and user-name ---
		String pcName = null;
		try
		{
			pcName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException err)
		{
			log.warn("Could not resolve pc name, configuration is not saved");
			if (model.getModulesState().get() == ModulesState.ACTIVE)
			{
				model.stopModules();
			}
			System.exit(0);
			return;
		}
		
		String userName = System.getProperty("user.name");
		
		// --- save properties in file ---
		String filename = CONFIG_SETTINGS_PATH + pcName + "_" + userName + ".gui";
		
		try
		{
			FileOutputStream out = new FileOutputStream(filename);
			props.store(out, null);
			out.close();
		} catch (IOException err)
		{
			log.warn("Could not write to " + filename + ", configuration is not saved");
			if (model.getModulesState().get() == ModulesState.ACTIVE)
			{
				model.stopModules();
			}
			System.exit(0);
			return;
		}
		
		log.debug("Saved configuration to: " + filename);
		
		if (model.getModulesState().get() == ModulesState.ACTIVE)
		{
			model.stopModules();
		}
		

		// --- save gui position ---
		savePosition(mainFrameGUI);
		

		// --- exit application ---
		System.exit(0);
	}
	

	@Override
	public void onLoadLayout(String filename)
	{
		String path = LAYOUT_CONFIG_PATH + filename;
		currentLayout = filename;
		
		if (!new File(path).exists())
		{
			log.warn("Layout file: " + path + " does not exist, falling back to " + LAYOUT_DEFAULT);
			path = LAYOUT_CONFIG_PATH + LAYOUT_DEFAULT;
			currentLayout = LAYOUT_DEFAULT;
		}
		
		mainFrame.loadLayout(path);
		
		// --- debug msg ---
		log.debug("Loaded layout: " + path);
	}
	

	@Override
	public void onLoadConfig(String filename)
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
		} catch (LoadModulesException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + model.getCurrentModuliConfig() + "') ");
		} catch (DependencyException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + model.getCurrentModuliConfig() + "') ");
		}
		
		try
		{
			skillSystem = (GenericSkillSystem) model.getModule(ASkillSystem.MODULE_ID);
			
		} catch (ModuleNotFoundException err)
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
			currentLafName = info.getClassName();
			UIManager.setLookAndFeel(info.getClassName());
		} catch (ClassNotFoundException err)
		{
		} catch (InstantiationException err)
		{
		} catch (IllegalAccessException err)
		{
		} catch (UnsupportedLookAndFeelException err)
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
		mainFrame.setLookAndFeel(currentLafName);
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
	

	public void addAIPresenter(AICenterPresenter presenter)
	{
		aiPresenter = presenter;
	}
	

	// ---------------------------------------------------------------
	// -------------- Save Gui Position and size ---------------------
	// ---------------------------------------------------------------
	@Override
	public void eventDispatched(AWTEvent event)
	{
		WindowEvent wev = (WindowEvent) event;
		
		if (wev.getSource() == mainFrameGUI)
		{
			JFrame frame = (JFrame) wev.getComponent();
			switch (event.getID())
			{
				// save method will be called in this.exit()
				case WindowEvent.WINDOW_OPENED:
					loadPosition(frame);
					break;
			}
		}
		
	}
	

	/**
	 * Load JFrame size and position.
	 * 
	 * @param window
	 */
	private void loadPosition(JFrame frame)
	{
		Properties settings = new Properties();
		
		try
		{
			File f = new File(GUI_SIZE_POS_PATH);
			f.createNewFile();
			
			settings.load(new FileInputStream(f));
			String name = this.getClass().getName();
			
			int displayCount = getInt(settings, name + ".disyplayCount", 1);
			int x = 0;
			int y = 0;
			
			if (displayCount == getNumberOfDisplays())
			{
				/*
				 * no changes of available displays thus load window
				 * position from config file.
				 */
				x = getInt(settings, name + ".x", 0);
				y = getInt(settings, name + ".y", 0);
			}
			
			int w = getInt(settings, name + ".w", 1456);
			int h = getInt(settings, name + ".h", 886);
			
			frame.setLocation(x, y);
			frame.setSize(new Dimension(w, h));
			frame.validate();
			
		} catch (FileNotFoundException err)
		{
			err.printStackTrace();
		} catch (IOException err)
		{
			err.printStackTrace();
		}
	}
	

	/**
	 * Save JFrame size and position.
	 * 
	 * @param window
	 */
	private void savePosition(JFrame frame)
	{
		Properties settings = new Properties();
		try
		{
			settings.load(new FileInputStream(GUI_SIZE_POS_PATH));
			String name = this.getClass().getName();
			
			settings.setProperty(name + ".disyplayCount", "" + getNumberOfDisplays());
			settings.setProperty(name + ".x", "" + frame.getX());
			settings.setProperty(name + ".y", "" + frame.getY());
			settings.setProperty(name + ".w", "" + frame.getWidth());
			settings.setProperty(name + ".h", "" + frame.getHeight());
			
			settings.store(new FileOutputStream(GUI_SIZE_POS_PATH), null);
			
		} catch (FileNotFoundException err)
		{
			err.printStackTrace();
		} catch (IOException err)
		{
			err.printStackTrace();
		}
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
		String v = props.getProperty(name);
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
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			
			return env.getScreenDevices().length;
			
		} catch (HeadlessException e)
		{
			e.printStackTrace();
		}
		
		return 1;
	}
	

	public boolean hasGUI()
	{
		return !noguiFlag;
	}
	
}
