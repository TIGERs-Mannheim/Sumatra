/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 1, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers.DefMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers.SteinhausJohnsonTrotter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Shortest path to targets over all defenders
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ShortestPathDefensePointCalc implements IDefensePointCalc
{
	
	private static final Logger	log						= Logger.getLogger(ShortestPathDefensePointCalc.class.getName());
	
	
	@Configurable
	private static float				stepSizeToIntersect	= 100;
	
	
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders, final List<FoeBotData> foeBots)
	{
		Map<DefenderRole, FoeBotData> distr = getDefender2FoeDistribution(frame, defenders, foeBots);
		
		Map<DefenderRole, DefensePoint> distribution = new HashMap<>();
		
		distr.forEach((defender, foeBot) -> distribution.put(defender,
				new DefensePoint(DefMath.calcNearestDefPoint(foeBot, defender.getPos()), foeBot)));
		
		return distribution;
	}
	
	
	/**
	 * @param frame
	 * @param defenders
	 * @param foeBotsList
	 * @return
	 */
	public Map<DefenderRole, FoeBotData> getDefender2FoeDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders, final List<FoeBotData> foeBotsList)
	{
		Map<DefenderRole, FoeBotData> distribution = new HashMap<>();
		
		if (defenders.isEmpty() || foeBotsList.isEmpty())
		{
			return new HashMap<DefenderRole, FoeBotData>();
		}
		
		// get as much foeBots as there are defenders
		List<FoeBotData> foeBots = new ArrayList<>(defenders.size());
		// loop only twice, as we do not want more than 2 bots per point, anyway
		for (int i = 0; i < 2; i++)
		{
			foeBots.addAll(foeBotsList.subList(0,
					Math.min(Math.abs(defenders.size() - foeBots.size()), foeBotsList.size())));
		}
		
		if (1 == foeBots.size())
		{
			distribution.put(defenders.get(0), foeBots.get(0));
			return distribution;
		}
		
		List<FoeBotData> curOptimum = null;
		int curCost;
		int optCost = Integer.MAX_VALUE;
		
		SteinhausJohnsonTrotter<FoeBotData> permutations = new SteinhausJohnsonTrotter<FoeBotData>(foeBots);
		
		while (permutations.hasNext())
		{
			List<FoeBotData> curDistribution = permutations.next();
			curCost = 0;
			for (int i = 0; i < foeBots.size(); i++)
			{
				IVector2 p = DefMath.calcNearestDefPoint(curDistribution.get(i), defenders.get(i).getPos());
				if (p != null)
				{
					BangBangTrajectory2D pathToDest = TrajectoryGenerator.generatePositionTrajectory(
							defenders.get(i).getBot(), p);
					
					curCost += pathToDest.getTotalTime();
				} else
				{
					curCost += AIConfig.getGeometry().getFieldLength();
				}
			}
			if (curCost < optCost)
			{
				curOptimum = curDistribution;
				optCost = curCost;
			}
		}
		
		for (int i = 0; i < foeBots.size(); i++)
		{
			// curOptimum cannot be null due to the return point earlier in this routine
			if (curOptimum == null)
			{
				log.error("curOptimum == null! this should not happen. See ShortestPathDefensePointCalc");
			} else
			{
				distribution.put(defenders.get(i), curOptimum.get(i));
			}
		}
		
		return distribution;
	}
}
