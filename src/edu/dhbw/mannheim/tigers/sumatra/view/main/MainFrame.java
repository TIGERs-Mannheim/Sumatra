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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.ConfigControlMenu;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.ToolBar;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.ReplayLoadMenu;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;


/**
 * The Sumatra-main view with all available JComponents as dockable views :).
 */
public class MainFrame extends AMainFrame
{
	protected static final long		serialVersionUID		= -6858464942004450029L;
	
	// --- menu stuff ---
	private JMenu							moduliMenu				= null;
	private JMenu							lookAndFeelMenu		= null;
	private JMenu							replayMenu				= new ReplayLoadMenu();
	private final ConfigControlMenu	botManagerConfigMenu	= new ConfigControlMenu("Botmanager",
																					ABotManager.KEY_BOTMANAGER_CONFIG);
	private final ConfigControlMenu	geomConfigMenu			= new ConfigControlMenu("Geometry",
																					AAgent.KEY_GEOMETRY_CONFIG);
	
	private ToolBar						toolBar;
	
	
	/**
	 * Constructor of GuiView.
	 */
	public MainFrame()
	{
		super();
		
		// --- usual Swing initialization of the view ---
		showFrame();
	}
	
	
	// ---------------------------------------------------------------
	// --- important-methods for every day use -----------------------
	// ---------------------------------------------------------------
	
	
	/**
	 * @param names
	 */
	public void setMenuModuliItems(final List<String> names)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				moduliMenu.removeAll();
				
				final ButtonGroup group = new ButtonGroup();
				
				for (final String name : names)
				{
					final JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
					item.addActionListener(new LoadConfig(name));
					group.add(item);
					moduliMenu.add(item);
				}
			}
		});
	}
	
	
	/**
	 * @param enabled
	 */
	public void setModuliMenuEnabled(final boolean enabled)
	{
		for (int i = 0; i < moduliMenu.getItemCount(); i++)
		{
			moduliMenu.getItem(i).setEnabled(enabled);
		}
		for (int i = 0; i < botManagerConfigMenu.getConfigMenu().getItemCount(); i++)
		{
			JMenuItem item = botManagerConfigMenu.getConfigMenu().getItem(i);
			if (item != null)
			{
				item.setEnabled(enabled);
			}
		}
		for (int i = 0; i < geomConfigMenu.getConfigMenu().getItemCount(); i++)
		{
			JMenuItem item = geomConfigMenu.getConfigMenu().getItem(i);
			if (item != null)
			{
				item.setEnabled(enabled);
			}
		}
	}
	
	
	/**
	 * @param name
	 */
	public void selectModuliItem(final String name)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// select RadioButton in moduliMenu
				for (int i = 0; i < moduliMenu.getItemCount(); i++)
				{
					final JMenuItem item = moduliMenu.getItem(i);
					if (item.getText().equals(name))
					{
						item.setSelected(true);
					}
				}
			}
		});
	}
	
	
	/**
	 * @param name
	 */
	public void selectLookAndFeelItem(final String name)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < lookAndFeelMenu.getItemCount(); i++)
				{
					final JMenuItem item = lookAndFeelMenu.getItem(i);
					if (item.getText().equals(name))
					{
						item.setSelected(true);
					}
				}
			}
		});
	}
	
	
	/**
	 * @param lafName
	 */
	public void setLookAndFeel(final String lafName)
	{
		final JFrame frame = this;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// update visible components
				final int state = getExtendedState();
				SwingUtilities.updateComponentTreeUI(frame);
				setExtendedState(state);
				
				// update menu
				for (int i = 0; i < lookAndFeelMenu.getItemCount(); i++)
				{
					final JMenuItem item = lookAndFeelMenu.getItem(i);
					if (item.getText().equals(lafName))
					{
						item.setSelected(true);
					}
				}
				
				// update all views (including non-visible)
				for (final ASumatraView view : views)
				{
					if (view.isInitialized())
					{
						SwingUtilities.updateComponentTreeUI(view.getComponent());
					}
				}
			}
		});
	}
	
	
	/**
	 * @return
	 */
	public ToolBar getToolbar()
	{
		return toolBar;
	}
	
	
	/**
	 * Initializes the frame and shows it.
	 */
	@Override
	protected void showFrame()
	{
		super.showFrame();
		// --- init windowlistener ---
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		// --- add/set components ---
		toolBar = new ToolBar();
		this.add(toolBar.getToolbar(), BorderLayout.NORTH);
	}
	
	
	/**
	 * Creates the frame menu bar.
	 * 
	 * @return the menu bar
	 */
	@Override
	protected JMenuBar fillMenuBar(final JMenuBar menuBar)
	{
		// File Menu
		JMenu sumatraMenu = new JMenu("Sumatra");
		
		JMenuItem aboutMenuItem;
		JMenuItem exitMenuItem;
		
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new Exit());
		exitMenuItem.setToolTipText("Exits the application");
		
		final JMenuItem shortcutMenuItem = new JMenuItem("Shortcuts");
		shortcutMenuItem.addActionListener(new ShortcutActionListener());
		
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new AboutBox());
		aboutMenuItem.setToolTipText("Information about Sumatra");
		
		sumatraMenu.add(shortcutMenuItem);
		sumatraMenu.add(aboutMenuItem);
		sumatraMenu.add(exitMenuItem);
		
		// moduli menu
		moduliMenu = new JMenu("Moduli");
		
		// look and feel menu
		final ButtonGroup group = new ButtonGroup();
		lookAndFeelMenu = new JMenu("Look & Feel");
		final LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		for (final LookAndFeelInfo info : lafs)
		{
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(info.getName());
			item.addActionListener(new SetLookAndFeel(info));
			group.add(item);
			lookAndFeelMenu.add(item);
			if (info.getClassName().equals(UIManager.getSystemLookAndFeelClassName()))
			{
				item.setSelected(true);
			}
		}
		
		menuBar.add(sumatraMenu);
		menuBar.add(moduliMenu);
		super.fillMenuBar(menuBar);
		menuBar.add(lookAndFeelMenu);
		menuBar.add(replayMenu);
		menuBar.add(botManagerConfigMenu.getConfigMenu());
		menuBar.add(geomConfigMenu.getConfigMenu());
		
		return menuBar;
	}
	
	
	// -------------------------
	// --- ActionListener ------
	// -------------------------
	
	
	protected class AboutBox implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				for (final IMainFrameObserver o : observers)
				{
					o.onAbout();
				}
			}
		}
	}
	
	
	protected static class LoadConfig implements ActionListener
	{
		private final String	configName;
		
		
		/**
		 * @param c
		 */
		public LoadConfig(final String c)
		{
			configName = c;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			SumatraModel.getInstance().setCurrentModuliConfig(configName);
		}
	}
	
	
	protected class SetLookAndFeel implements ActionListener
	{
		private final LookAndFeelInfo	info;
		
		
		/**
		 * @param i
		 */
		public SetLookAndFeel(final LookAndFeelInfo i)
		{
			info = i;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				for (final IMainFrameObserver o : observers)
				{
					o.onSelectLookAndFeel(info);
				}
			}
		}
	}
	
	private static class ShortcutActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			new ShortcutsDialog().setVisible(true);
		}
	}
}
