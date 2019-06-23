/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main;

import java.awt.GraphicsDevice;

import javax.swing.UIManager.LookAndFeelInfo;

import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.IToolbarObserver;


/**
 * MainFrame observer
 * 
 * @author AndreR
 * 
 */
public interface IMainFrameObserver extends IToolbarObserver
{
	
	/**
 *
 */
	void onSaveLayout();
	
	
	/**
 *
 */
	void onDeleteLayout();
	
	
	/**
 *
 */
	void onAbout();
	
	
	/**
 *
 */
	void onExit();
	
	
	/**
	 * 
	 * @param filename
	 */
	void onLoadLayout(String filename);
	
	
	/**
	 * 
	 * @param filename
	 */
	void onLoadModuliConfig(String filename);
	
	
	/**
 *
 */
	void onRefreshLayoutItems();
	
	
	/**
	 * 
	 * @param info
	 */
	void onSelectLookAndFeel(LookAndFeelInfo info);
	
	
	/**
	 * 
	 * @param gd
	 */
	void onSetFullscreen(GraphicsDevice gd);
	
}
