/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinderInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


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
