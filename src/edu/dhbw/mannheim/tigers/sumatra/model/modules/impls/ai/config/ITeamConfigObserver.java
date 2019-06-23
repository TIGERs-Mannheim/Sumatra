/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.12.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

/**
 * This observer interface gets notified whenever the {@link TeamConfig} changes.
 * 
 * @author Gero
 * 
 */
public interface ITeamConfigObserver
{
	/**
	 * @param teamProps The new, changed {@link TeamProps}
	 */
	void onNewTeamConfig(TeamProps teamProps);
}
