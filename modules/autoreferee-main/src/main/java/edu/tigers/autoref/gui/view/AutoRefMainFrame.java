/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 19, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.gui.view;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.infonode.docking.RootWindow;
import net.infonode.docking.properties.RootWindowProperties;
import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogView;
import edu.tigers.autoref.view.ballspeed.BallSpeedView;
import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.autoref.view.humanref.HumanRefView;
import edu.tigers.autoref.view.main.AutoRefView;
import edu.tigers.autoref.view.visualizer.VisualizerAutoRefView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.config.ConfigEditorView;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMainFrame extends AMainFrame
{
	
	private static final long	serialVersionUID	= 8459059861313702417L;
	
	
	/**
	 * 
	 */
	public AutoRefMainFrame()
	{
		setTitle("Autoreferee");
		
		addView(new LogView(true));
		addView(new VisualizerAutoRefView());
		addView(new ConfigEditorView());
		addView(new AutoRefView());
		addView(new GameLogView());
		addView(new BallSpeedView());
		addView(new HumanRefView());
		
		updateViewMenu();
		fillMenuBar();
	}
	
	
	/**
	 * 
	 */
	private void fillMenuBar()
	{
		JMenu fileMenu = new JMenu("File");
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new Exit());
		fileMenu.add(exitItem);
		
		getJMenuBar().add(fileMenu);
		
		/*
		 * Adds the menu items for layout and views
		 */
		super.addMenuItems();
		
	}
	
	
	@Override
	protected ImageIcon getFrameIcon()
	{
		return loadIconImage("/whistle.png");
	}
	
	
	@Override
	protected RootWindow createRootWindow()
	{
		RootWindow rootWindow = super.createRootWindow();
		
		/*
		 * Specifies that all floating windows should be created as separate JFrame instead of a JDialog.
		 * This ensures that the HumanRefView Panel can be minimized/maximized independently.
		 */
		RootWindowProperties windowProps = rootWindow.getRootWindowProperties();
		windowProps.getFloatingWindowProperties().setUseFrame(true);
		
		return rootWindow;
	}
}
