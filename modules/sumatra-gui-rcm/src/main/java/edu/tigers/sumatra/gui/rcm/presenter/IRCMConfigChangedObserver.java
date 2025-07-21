/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.rcm.presenter;

import edu.tigers.sumatra.rcm.RcmActionMap.ERcmControllerConfig;
import edu.tigers.sumatra.rcm.RcmActionMapping;

import java.io.File;


/**
 * Observer for RCM controller presenter
 */
public interface IRCMConfigChangedObserver
{
	/**
	 * @param configType
	 * @param value
	 */
	void onConfigChanged(ERcmControllerConfig configType, double value);


	/**
	 * @param mapping
	 */
	void onActionMappingCreated(RcmActionMapping mapping);


	/**
	 * @param mapping
	 */
	void onActionMappingChanged(RcmActionMapping mapping);


	/**
	 * @param mapping
	 */
	void onActionMappingRemoved(RcmActionMapping mapping);


	/**
	 *
	 */
	void onSaveConfig();


	/**
	 * @param file
	 */
	void onSaveConfigAs(File file);


	/**
	 * @param file
	 */
	void onLoadConfig(File file);


	/**
	 *
	 */
	void onLoadDefaultConfig();


	/**
	 * @param actionMapping
	 */
	void onSelectAssignment(RcmActionMapping actionMapping);


	/**
	 *
	 */
	void onSelectionAssistant();


	/**
	 *
	 */
	void onUnassignBot();

	/**
	 * @param enabled
	 */
	void onEnabled(boolean enabled);
}
