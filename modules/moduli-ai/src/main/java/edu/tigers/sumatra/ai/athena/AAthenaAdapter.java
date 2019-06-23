/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.athena;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;


/**
 * Base class for athena adapter. Adapters are used to react differently
 * depending on the current mode, e.g. Match, Test, Emergency
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AAthenaAdapter
{
	private static final Logger log = Logger.getLogger(AAthenaAdapter.class.getName());
	private final AIControl aiControl = new AIControl();
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 */
	public void process(final MetisAiFrame metisAiFrame, final PlayStrategy.Builder playStrategyBuilder)
	{
		// add all previous plays
		playStrategyBuilder.getActivePlays().addAll(metisAiFrame.getPrevFrame().getPlayStrategy().getActivePlays());
		
		doProcess(metisAiFrame, playStrategyBuilder, aiControl);
	}
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 * @param aiControl
	 */
	protected abstract void doProcess(MetisAiFrame metisAiFrame, PlayStrategy.Builder playStrategyBuilder,
			AIControl aiControl);
	
	
	/**
	 * Clear PlayStrategy, i.e. finish plays and remove
	 * 
	 * @param playStrategyBuilder
	 */
	protected void clear(final PlayStrategy.Builder playStrategyBuilder)
	{
		playStrategyBuilder.getActivePlays().forEach(APlay::changeToFinished);
		playStrategyBuilder.getActivePlays().clear();
	}
	
	
	protected void syncTargetPlaySet(final Set<EPlay> targetPlaySet, final List<APlay> activePlays)
	{
		Map<EPlay, APlay> activePlaysMap = mapToPlayMap(activePlays);
		addNewPlays(targetPlaySet, activePlays, activePlaysMap);
		removeVanishedPlays(targetPlaySet, activePlays, activePlaysMap);
	}
	
	
	private void removeVanishedPlays(final Set<EPlay> targetPlaySet, final List<APlay> activePlays,
			final Map<EPlay, APlay> activePlaysMap)
	{
		for (APlay aPlay : activePlaysMap.values())
		{
			if (!targetPlaySet.contains(aPlay.getType()))
			{
				aPlay.removeRoles(aPlay.getRoles().size(), null);
				activePlays.remove(aPlay);
			}
		}
	}
	
	
	private void addNewPlays(final Set<EPlay> targetPlaySet, final List<APlay> activePlays,
			final Map<EPlay, APlay> activePlaysMap)
	{
		for (EPlay ePlay : targetPlaySet)
		{
			if (!activePlaysMap.keySet().contains(ePlay))
			{
				try
				{
					APlay newPlay = (APlay) ePlay.getInstanceableClass().newDefaultInstance();
					activePlays.add(newPlay);
				} catch (NotCreateableException err)
				{
					log.warn("Could not instantiate play", err);
				}
			}
		}
	}
	
	
	private Map<EPlay, APlay> mapToPlayMap(final List<APlay> activePlays)
	{
		Map<EPlay, APlay> activePlaysMap = new EnumMap<>(EPlay.class);
		for (APlay aPlay : activePlays)
		{
			activePlaysMap.put(aPlay.getType(), aPlay);
		}
		return activePlaysMap;
	}
	
	
	/**
	 * @return the aiControl
	 */
	public final synchronized AIControl getAiControl()
	{
		return aiControl;
	}
}
