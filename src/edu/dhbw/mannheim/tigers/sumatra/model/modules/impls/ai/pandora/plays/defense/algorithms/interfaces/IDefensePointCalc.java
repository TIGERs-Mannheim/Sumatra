/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 24, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.interfaces;

import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public interface IDefensePointCalc
{
	/**
	 * @param frame
	 * @param defenders
	 * @param foeBotDataList
	 * @return
	 */
	Map<DefenderRole, DefensePoint> getDefenderDistribution(MetisAiFrame frame,
			List<DefenderRole> defenders, List<FoeBotData> foeBotDataList);
}
