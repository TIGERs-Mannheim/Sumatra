/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;


/**
 * Interface for PlayControlPanel Observer.
 * @author Malte
 * 
 */
public interface IPlayControlPanelObserver
{
	/**
	 * @param play
	 */
	void addPlay(APlay play);
	
	
	/**
	 * @param play
	 */
	void removePlay(APlay play);
	
	
	/**
	 * Add numRoles roles to play
	 * 
	 * @param play
	 * @param numRoles
	 */
	void addRoles2Play(APlay play, int numRoles);
	
	
	/**
	 * Remove numRoles from play
	 * 
	 * @param play
	 * @param numRoles
	 */
	void removeRolesFromPlay(APlay play, int numRoles);
}
