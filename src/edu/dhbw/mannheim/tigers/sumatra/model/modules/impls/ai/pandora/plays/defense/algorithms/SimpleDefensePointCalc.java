/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 24, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;


/**
 * This is a very simple defense point calculator. Every Foe bot gets a defender which blocks the direct shot at our
 * penalty line.
 * The mapping is not coordinated but at random.
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class SimpleDefensePointCalc implements IDefensePointCalc
{
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders, final List<FoeBotData> foeBots)
	{
		Map<DefenderRole, DefensePoint> distribution = new HashMap<>();
		for (int i = 0; i < foeBots.size(); i++)
		{
			List<DefenderRole> defs = new ArrayList<>();
			defs.add(defenders.get(i));
			distribution.put(defenders.get(0), new DefensePoint(foeBots.get(i).getBot2goalNearestToBot(), foeBots.get(i)
					.getFoeBot()));
		}
		
		return distribution;
	}
}
