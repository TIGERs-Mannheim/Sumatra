/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 24, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.interfaces;

import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public interface IDefensePointCalc
{
	/**
	 * @param frame current Frame
	 * @param defenders list of defenders
	 * @param foeBotDataList list of the data calculated by the defense calculator containing information about the foe
	 *           bots
	 * @return
	 */
	Map<DefenderRole, DefensePoint> getDefenderDistribution(MetisAiFrame frame,
			List<DefenderRole> defenders, List<FoeBotData> foeBotDataList);
}
