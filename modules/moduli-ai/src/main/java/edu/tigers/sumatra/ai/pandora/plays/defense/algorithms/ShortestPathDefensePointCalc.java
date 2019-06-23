/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 1, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.data.math.SteinhausJohnsonTrotter;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers.DefMath;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;


/**
 * Shortest path to targets over all defenders
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ShortestPathDefensePointCalc implements IDefensePointCalc
{
	
	private static final Logger	log						= Logger.getLogger(ShortestPathDefensePointCalc.class.getName());
	
	@Configurable
	private static double			stepSizeToIntersect	= 100.;
	
	@Configurable
	private static double			noPointFoundPenalty	= 5.;
	
	
	static
	{
		ConfigRegistration.registerClass("defensive", ShortestPathDefensePointCalc.class);
	}
	
	
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders, final List<FoeBotData> foeBots)
	{
		Map<DefenderRole, FoeBotData> distr = getDefender2FoeDistribution(frame, defenders, foeBots);
		
		Map<DefenderRole, DefensePoint> distribution = new HashMap<>();
		
		// bugged call
		distr.forEach((defender, foeBot) -> distribution.put(defender,
				new DefensePoint(DefMath.calcNearestDefPoint(foeBot, defender.getBot()), foeBot)));
		
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
		
		foeBots.addAll(foeBotsList.subList(0, Math.min(foeBotsList.size(), defenders.size())));
		
		if (1 == foeBots.size())
		{
			distribution.put(defenders.get(0), foeBots.get(0));
			return distribution;
		}
		
		List<FoeBotData> curOptimum = null;
		double curCost;
		double optCost = Double.MAX_VALUE;
		
		SteinhausJohnsonTrotter<FoeBotData> permutations = new SteinhausJohnsonTrotter<FoeBotData>(foeBots);
		
		while (permutations.hasNext())
		{
			List<FoeBotData> curDistribution = permutations.next();
			curCost = 0;
			for (int i = 0; i < foeBots.size(); i++)
			{
				IVector2 p = DefMath.calcNearestDefPoint(foeBots.get(i), defenders.get(i).getBot());
				if (p != null)
				{
					BangBangTrajectory2D pathToDest = new TrajectoryGenerator().generatePositionTrajectory(
							defenders.get(i).getBot(), p);
					
					curCost += pathToDest.getTotalTime();
				} else
				{
					
					curCost += noPointFoundPenalty;
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
