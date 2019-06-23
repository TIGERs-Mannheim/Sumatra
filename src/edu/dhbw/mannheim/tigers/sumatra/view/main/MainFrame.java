/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: Sumatra
 * Date: Jul 27, 2010
 * Authors:
 * Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.view.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DeveloperUtil;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;


/**
 * The Sumatra-main view with all available JComponents as dockable views :).
 */
public class MainFrame extends JFrame implements IMainFrame
{
	// ---------------------------------------------------------------
	// --- instance vars ---------------------------------------------
	// ---------------------------------------------------------------
	/**  */
	private static final long						serialVersionUID		= -6858464942004450029L;
	

	private ArrayList<IMainFrameObserver>		observers				= new ArrayList<IMainFrameObserver>();
	
	private ArrayList<JRadioButtonMenuItem>	layoutItems				= new ArrayList<JRadioButtonMenuItem>();
	

	// --- infonode-framework variables ---
	/**
	 * The one and only root window
	 */
	private RootWindow								rootWindow;
	
	/**
	 * Contains all the static views
	 */
	private ViewMap									viewMap					= new ViewMap();
	
	/**
	 * The currently applied docking windows theme
	 */
	private DockingWindowsTheme					currentTheme			= new ShapedGradientDockingTheme();
	
	/**
	 * In this properties object the modified property values for close buttons etc. are stored. This object is cleared
	 * when the theme is changed.
	 */
	private RootWindowProperties					properties				= new RootWindowProperties();
	
	// --- logger ---
	private Logger										log						= Logger.getLogger(getClass());
	
	// --- menu stuff ---
	private JMenuBar									menuBar					= null;
	private JMenu										viewsMenu				= null;
	private JMenu										layoutMenu				= null;
	private JMenu										fileMenu					= null;
	private JMenu										moduliMenu				= null;
	private JMenu										lookAndFeelMenu		= null;
	
	private List<ISumatraView>						views						= new ArrayList<ISumatraView>();
	private Map<ISumatraView, List<JMenu>>		customMenuMap			= new Hashtable<ISumatraView, List<JMenu>>();
	
	// --- toolbar ---
	private JToolBar									toolBar					= null;
	private JButton									startStopButton		= null;
	private JButton									emergencyStopButton	= null;
	
	
	// ---------------------------------------------------------------
	// --- constructor(s) --------------------------------------------
	// ---------------------------------------------------------------
	
	/**
	 * Constructor of GuiView.
	 */
	public MainFrame()
	{
		// --- create the main window with the default layout (file: default.ly) ---
		createRootWindow();
		
		// --- usual Swing initialization of the view ---
		showFrame();
	}
	

	// ---------------------------------------------------------------
	// --- important-methods for every day use -----------------------
	// ---------------------------------------------------------------
	
	public void addObserver(IMainFrameObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	

	public void removeObserver(IMainFrameObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	

	public void setStartStopButtonState(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				startStopButton.setEnabled(enable);
			}
		});
	}
	

	public void setStartStopButtonState(final boolean enable, final ImageIcon icon)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				startStopButton.setEnabled(enable);
				startStopButton.setIcon(icon);
			}
		});
	}
	

	public void loadLayout(final String path)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				File f = new File(path);
				String filename = f.getName();
				
				try
				{
					// --- Load the layout ---
					ObjectInputStream in1 = new ObjectInputStream(new FileInputStream(path));
					
					try
					{
						rootWindow.read(in1, true);
					} catch (NullPointerException npe)
					{
						log.warn("Seems as if a view stored in the config is not available!");
					} finally
					{
						if (in1 != null)
						{
							in1.close();
						}
					}
					
					// select RadioButton in layoutMenu
					for (JRadioButtonMenuItem item : layoutItems)
					{
						String itemName = item.getText();
						if (itemName.equals(filename))
						{
							item.setSelected(true);
						}
					}
				} catch (IOException e1)
				{
					log.warn("Can't load layout: " + e1.getMessage());
				}
			}
		});
	}
	

	public void saveLayout(String filename)
	{
		// --- save layout to file ---
		try
		{
			FileOutputStream fileStream = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileStream);
			rootWindow.write(out, false);
			out.close();
			
		} catch (FileNotFoundException err)
		{
			log.error("Can't save layout:" + err.getMessage());
		} catch (IOException err)
		{
			log.error("Can't save layout:" + err.getMessage());
		}
	}
	

	public void setMenuLayoutItems(final ArrayList<String> names)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// remove all layout items from menu
				for (JRadioButtonMenuItem item : layoutItems)
				{
					layoutMenu.remove(item);
				}
				
				layoutItems.clear();
				
				// --- buttonGroup for layout-files ---
				ButtonGroup group = new ButtonGroup();
				for (String name : names)
				{
					JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
					group.add(item);
					item.addActionListener(new LoadLayout(name));
					layoutItems.add(item);
				}
				
				int pos = 3;
				for (JRadioButtonMenuItem item : layoutItems)
				{
					layoutMenu.insert(item, pos++);
				}
			}
		});
	}
	

	public void setMenuModuliItems(final ArrayList<String> names)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				moduliMenu.removeAll();
				
				ButtonGroup group = new ButtonGroup();
				
				for (String name : names)
				{
					JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
					item.addActionListener(new LoadConfig(name));
					group.add(item);
					moduliMenu.add(item);
				}
			}
		});
	}
	

	public void selectModuliItem(final String name)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				File f = new File(name);
				String filename = f.getName();
				
				// select RadioButton in moduliMenu
				for (int i = 0; i < moduliMenu.getItemCount(); i++)
				{
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) moduliMenu.getItem(i);
					if (item.getText().equals(filename))
					{
						item.setSelected(true);
					}
				}
			}
		});
	}
	

	public void selectLayoutItem(final String name)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				File f = new File(name);
				String filename = f.getName();
				
				for (JRadioButtonMenuItem item : layoutItems)
				{
					if (item.getText().equals(filename))
					{
						item.setSelected(true);
					}
				}
			}
		});
	}
	

	public void addView(ISumatraView view)
	{
		viewMap.addView(view.getID(), new View(view.getTitle(), Icons.VIEW_ICON, view.getViewComponent()));
		
		views.add(view);
		
		updateViewMenu();
	}
	

	public void setLookAndFeel(final String lafName)
	{
		final JFrame frame = this;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// update visible components
				int state = getExtendedState();
				SwingUtilities.updateComponentTreeUI(frame);
				setExtendedState(state);
				
				// update menu
				for (int i = 0; i < lookAndFeelMenu.getItemCount(); i++)
				{
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) lookAndFeelMenu.getItem(i);
					if (item.getText().equals(lafName))
					{
						item.setSelected(true);
					}
				}
				
				// update all views (including non-visible)
				for (ISumatraView view : views)
				{
					SwingUtilities.updateComponentTreeUI(view.getViewComponent());
				}
			}
		});
	}
	

	private void removeFromCustomMenu(final List<JMenu> menus)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (JMenu menu : menus)
				{
					menuBar.remove(menu);
				}
				
				paintAll(getGraphics());
				
				// customMenus.removeAll(menu);
			}
		});
	}
	

	private void addToCustomMenu(final List<JMenu> menus)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (JMenu menu : menus)
				{
					menuBar.add(menu);
				}
				
				// customMenus.removeAll(menu);
			}
		});
		
	}
	

	/**
	 * Initializes the frame and shows it.
	 */
	private void showFrame()
	{
		// --- init windowlistener ---
		addWindowListener(new Exit());
		
		// --- some adjustments ---
		this.setLayout(new BorderLayout());
		this.setLocation(0, 0);
		this.setSize(new Dimension(800, 600));
		this.setTitle("Tigers Mannheim - Sumatra V " + SumatraModel.VERSION);
		this.setIconImage(new ImageIcon(ClassLoader.getSystemResource("kralle-icon.png")).getImage());
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// --- add/set components ---
		this.add(createToolBar(), BorderLayout.NORTH);
		this.add(rootWindow, BorderLayout.CENTER);
		this.setJMenuBar(createMenuBar());
		
		// --- maximize ---
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		// --- set visible ---
		this.setVisible(true);
	}
	

	// ---------------------------------------------------------------
	// --- unimportant-methods for every day use (framework-things) --
	// ---------------------------------------------------------------
	
	/**
	 * Creates the root window and the views.
	 */
	private void createRootWindow()
	{
		// --- create the RootWindow without MixedHandler ---
		rootWindow = DockingUtil.createRootWindow(viewMap, true);
		
		// --- add a listener which updates the menus when a window is closing or closed.
		rootWindow.addListener(new ViewUpdater());
		
		// --- set gradient theme. The theme properties object is the super object of our properties object, which
		// means our property value settings will override the theme values ---
		properties.addSuperObject(currentTheme.getRootWindowProperties());
		
		// --- our properties object is the super object of the root window properties object, so all property values of
		// the
		// theme and in our property object will be used by the root window ---
		rootWindow.getRootWindowProperties().addSuperObject(properties);
		
		// --- enable the bottom window bar ---
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
	}
	

	/**
	 * Creates the frame tool bar.
	 * 
	 * @return the frame tool bar
	 */
	private JToolBar createToolBar()
	{
		// --- configure buttons ---
		startStopButton = new JButton();
		startStopButton.addActionListener(new StartStopModules());
		startStopButton.setBorder(null);
		
		emergencyStopButton = new JButton("Emergency Stop");
		emergencyStopButton.setForeground(Color.red);
		emergencyStopButton.addActionListener(new EmergencyStopListener());
		

		// --- configure toolbar ---
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		// --- add buttons ---
		toolBar.addSeparator();
		toolBar.add(startStopButton);
		toolBar.addSeparator();
		toolBar.add(emergencyStopButton);
		toolBar.addSeparator();
		return toolBar;
	}
	

	/**
	 * Creates the frame menu bar.
	 * 
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar()
	{
		// Views menu
		viewsMenu = new JMenu("Views");
		
		// File Menu
		fileMenu = new JMenu("File");
		
		JMenuItem aboutMenuItem;
		JMenuItem exitMenuItem;
		
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new AboutBox());
		aboutMenuItem.setToolTipText("Information about Sumatra");
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new Exit());
		exitMenuItem.setToolTipText("Exits the application");
		
		// fullscreen stuff
		JMenu fsMenu = new JMenu("Fullscreen");
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gds = ge.getScreenDevices();
		for (GraphicsDevice gd : gds)
		{
			JMenuItem item = new JMenuItem(gd.toString());
			item.addActionListener(new SetFullscreen(gd));
			fsMenu.add(item);
		}
		
		JMenuItem windowMode = new JMenuItem("Window mode");
		windowMode.addActionListener(new SetFullscreen(null));
		fsMenu.add(windowMode);
		
		fileMenu.add(fsMenu);
		fileMenu.add(aboutMenuItem);
		fileMenu.add(exitMenuItem);
		

		// layout menu
		layoutMenu = new JMenu("Layout");
		
		JMenuItem saveLayoutItem;
		JMenuItem deleteLayoutItem;
		
		saveLayoutItem = new JMenuItem("Save layout");
		saveLayoutItem.addActionListener(new SaveLayout());
		saveLayoutItem.setToolTipText("Saves current layout to file");
		deleteLayoutItem = new JMenuItem("Delete layout");
		deleteLayoutItem.addActionListener(new DeleteLayout());
		deleteLayoutItem.setToolTipText("Deletes current layout");
		
		layoutMenu.add(saveLayoutItem);
		layoutMenu.add(deleteLayoutItem);
		
		layoutMenu.addSeparator();
		layoutMenu.addSeparator();
		
		layoutMenu.add(new JMenuItem("Refresh layouts")).addActionListener(new RefreshLayoutItems());
		
		layoutMenu.add("Show Window Layout Frame").addActionListener(new ShowWindowLayout());
		
		// moduli menu
		moduliMenu = new JMenu("Moduli");
		
		// look and feel menu
		ButtonGroup group = new ButtonGroup();
		lookAndFeelMenu = new JMenu("Look & Feel");
		LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo info : lafs)
		{
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(info.getName());
			item.addActionListener(new SetLookAndFeel(info));
			group.add(item);
			lookAndFeelMenu.add(item);
			if (info.getClassName().equals(UIManager.getSystemLookAndFeelClassName()))
			{
				item.setSelected(true);
			}
		}
		
		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(moduliMenu);
		menuBar.add(layoutMenu);
		menuBar.add(viewsMenu);
		menuBar.add(lookAndFeelMenu);
		menuBar.add(Box.createHorizontalStrut(50));
		
		return menuBar;
	}
	

	private void updateViewMenu()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (int i = 0; i < viewsMenu.getItemCount(); i++)
				{
					ActionListener[] listener = viewsMenu.getItem(i).getActionListeners();
					for (ActionListener l : listener)
					{
						viewsMenu.getItem(i).removeActionListener(l);
					}
				}
				
				viewsMenu.removeAll();
				
				for (int i = 0; i < viewMap.getViewCount(); i++)
				{
					View view = viewMap.getViewAtIndex(i);
					
					JMenuItem item = new JMenuItem(view.getTitle());
					item.setEnabled(view.getRootWindow() == null);
					item.addActionListener(new RestoreView(view));
					viewsMenu.add(item);
				}
			}
		});
	}
	
	
	// -------------------------
	// --- ActionListener ------
	// -------------------------
	
	protected class StartStopModules implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onStartStopModules();
				}
			}
		}
	}
	
	protected class SaveLayout implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onSaveLayout();
				}
			}
		}
	}
	
	protected class DeleteLayout implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onDeleteLayout();
				}
			}
		}
	}
	
	protected class LoadLayout implements ActionListener
	{
		private String	layoutName;
		
		
		public LoadLayout(String name)
		{
			this.layoutName = name;
		}
		

		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onLoadLayout(layoutName);
				}
			}
		}
	}
	
	protected class AboutBox implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onAbout();
				}
			}
		}
	}
	
	protected class ShowWindowLayout implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			DeveloperUtil.createWindowLayoutFrame("Root Window Layout as Java-like Pseudo-Code", rootWindow).setVisible(
					true);
		}
	}
	
	protected class RestoreView implements ActionListener
	{
		View	view;
		
		
		public RestoreView(View v)
		{
			view = v;
		}
		

		@Override
		public void actionPerformed(ActionEvent e)
		{
			JMenuItem item = (JMenuItem) e.getSource();
			
			view.restoreFocus();
			DockingUtil.addWindow(view, rootWindow);
			item.setEnabled(false);
		}
	}
	
	protected class LoadConfig implements ActionListener
	{
		private String	configName;
		
		
		public LoadConfig(String c)
		{
			configName = c;
		}
		

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onLoadConfig(configName);
				}
			}
		}
	}
	
	protected class RefreshLayoutItems implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onRefreshLayoutItems();
				}
			}
		}
	}
	
	protected class SetLookAndFeel implements ActionListener
	{
		private LookAndFeelInfo	info;
		
		
		public SetLookAndFeel(LookAndFeelInfo i)
		{
			info = i;
		}
		

		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onSelectLookAndFeel(info);
				}
			}
		}
	}
	
	protected class SetFullscreen implements ActionListener
	{
		private GraphicsDevice	gd;
		
		
		public SetFullscreen(GraphicsDevice gd)
		{
			this.gd = gd;
		}
		

		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onSetFullscreen(gd);
				}
			}
		}
	}
	
	protected class Exit extends WindowAdapter implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			exit();
		}
		

		public void windowClosing(WindowEvent w)
		{
			exit();
		}
		

		private void exit()
		{
			for (IMainFrameObserver o : observers)
			{
				o.onExit();
			}
		}
	}
	
	protected class ViewUpdater extends DockingWindowAdapter
	{
		public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow)
		{
			updateViewMenu();
		}
		

		public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow)
		{
			updateViewMenu();
		}
		

		public void windowShown(DockingWindow window)
		{
			/*
			 * It is a completely undocumented feature(?) that the
			 * window title consists of a comma separated list if multiple
			 * windows get shown at once. This usually happens when loading
			 * a layout.
			 */
			String titles[] = window.getTitle().split(",");
			for (String title : titles)
			{
				title = title.trim();
				
				for (ISumatraView view : views)
				{
					if (view.getTitle().equals(title))
					{
						List<JMenu> menu = view.getCustomMenus();
						
						if (menu != null)
						{
							if (customMenuMap.containsKey(view))
							{
								removeFromCustomMenu(customMenuMap.get(view));
							}
							
							customMenuMap.put(view, menu);
							
							addToCustomMenu(menu);
						}
						
						view.onShown();
					}
				}
			}
		}
		

		public void windowHidden(DockingWindow window)
		{
			String titles[] = window.getTitle().split(",");
			for (String title : titles)
			{
				title = title.trim();
				
				for (ISumatraView view : views)
				{
					if (view.getTitle().equals(title))
					{
						List<JMenu> menu = customMenuMap.remove(view);
						if (menu != null)
						{
							removeFromCustomMenu(menu);
						}
						
						view.onHidden();
					}
				}
			}
		}
		

		public void viewFocusChanged(View previous, View focused)
		{
			if (previous != null)
			{
				for (ISumatraView view : views)
				{
					if (view.getTitle().equals(previous.getTitle()))
					{
						view.onFocusLost();
					}
				}
			}
			
			if (focused != null)
			{
				for (ISumatraView view : views)
				{
					if (view.getTitle().equals(focused.getTitle()))
					{
						view.onFocused();
					}
				}
			}
		}
		
	}
	
	private class EmergencyStopListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IMainFrameObserver o : observers)
				{
					o.onEmergencyStop();
				}
			}
		}
	}
}
