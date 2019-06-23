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


/**
 * MainFrame observer
 * 
 * @author AndreR
 * 
 */
public interface IMainFrameObserver
{
	void onStartStopModules();
	

	void onSaveLayout();
	

	void onDeleteLayout();
	

	void onAbout();
	

	void onExit();
	

	void onLoadLayout(String filename);
	

	void onLoadConfig(String filename);
	

	void onRefreshLayoutItems();
	

	void onSelectLookAndFeel(LookAndFeelInfo info);
	

	void onSetFullscreen(GraphicsDevice gd);
	

	void onEmergencyStop();
}
