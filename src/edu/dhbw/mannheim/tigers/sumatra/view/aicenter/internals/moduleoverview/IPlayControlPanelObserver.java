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

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;

/**
 * Interface for PlayControlPanel Observer.
 * @author Malte
 * 
 */
public interface IPlayControlPanelObserver
{
	public void addPlay(EPlay play);
	

	public void removePlay(List<EPlay> oddPlays);
	
	
	public void forceNewDecision();
}
