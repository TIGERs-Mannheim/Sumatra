/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.view.config;

import edu.tigers.sumatra.botmanager.configs.ConfigFile;


public interface IBotConfigObserver
{
	/**
	 * @param file
	 */
	void onSaveToFile(ConfigFile file);
}
