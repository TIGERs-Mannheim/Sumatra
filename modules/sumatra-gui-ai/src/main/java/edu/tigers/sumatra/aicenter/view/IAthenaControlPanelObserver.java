/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import java.util.Map;

import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
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
	void onNewRoleMapping(Map<EPlay, RoleMapping> infos);
	
	
	/**
	 * @param useAiFlags
	 */
	void onNewUseAiFlags(Map<EPlay, Boolean> useAiFlags);
}
