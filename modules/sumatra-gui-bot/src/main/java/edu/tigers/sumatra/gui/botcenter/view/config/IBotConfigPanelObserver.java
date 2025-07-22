/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.botcenter.view.config;

import edu.tigers.sumatra.botmanager.configs.ConfigFile;


/**
 * Observer interface.
 */
public interface IBotConfigPanelObserver
{
	/**
	 * Query file list.
	 */
	void onQueryFileList();


	/**
	 * Query file list.
	 */
	void onClearFileList();


	/**
	 * @param file
	 */
	void onRefresh(ConfigFile file);


	/**
	 * @param file
	 */
	void onSave(ConfigFile file);


	/**
	 * @param file
	 */
	void onSaveToAll(ConfigFile file);
}
