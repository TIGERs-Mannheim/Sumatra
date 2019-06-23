/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;

import java.util.Map;

import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;


/**
 * Observer for {@link AthenaControlPanel}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IAthenaControlPanelObserver
{
	/**
	 * @param infos
	 */
	void onNewRoleFinderInfos(Map<EPlay, RoleFinderInfo> infos);
	
	
	/**
	 * @param overrides
	 */
	void onNewRoleFinderOverrides(Map<EPlay, Boolean> overrides);
}
