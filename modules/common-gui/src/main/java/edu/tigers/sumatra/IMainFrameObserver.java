/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra;

import javax.swing.UIManager.LookAndFeelInfo;


/**
 * MainFrame observer
 *
 * @author AndreR
 */
public interface IMainFrameObserver
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
	void onExit();


	/**
	 *
	 */
	default void onAbout()
	{
	}


	/**
	 * @param filename
	 */
	void onLoadLayout(String filename);


	/**
	 * @param info
	 */
	default void onSelectLookAndFeel(final LookAndFeelInfo info)
	{
	}
}
