/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.athena;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.data.PlayStrategy;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;


/**
 * Base class for athena adapter. Adapters are used to react differently
 * depending on the current mode, e.g. Match, Test, Emergency
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AAthenaAdapter implements IAthenaAdapterObserver
{
	private static final Logger	log			= Logger.getLogger(AAthenaAdapter.class.getName());
	private final AIControl			aiControl	= new AIControl();
	private final Object				sync			= new Object();
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 */
	public void process(final MetisAiFrame metisAiFrame, final PlayStrategy.Builder playStrategyBuilder)
	{
		synchronized (sync)
		{
			// add all previous plays
			playStrategyBuilder.getActivePlays().addAll(metisAiFrame.getPrevFrame().getPlayStrategy().getActivePlays());
			
			// Remove finished plays
			if (!metisAiFrame.getPrevFrame().getPlayStrategy().getFinishedPlays().isEmpty())
			{
				playStrategyBuilder.getActivePlays().removeAll(
						metisAiFrame.getPrevFrame().getPlayStrategy().getFinishedPlays());
			}
			
			doProcess(metisAiFrame, playStrategyBuilder, aiControl);
			aiControl.reset();
		}
	}
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 * @param aiControl
	 */
	public abstract void doProcess(MetisAiFrame metisAiFrame, PlayStrategy.Builder playStrategyBuilder,
			AIControl aiControl);
	
	
	/**
	 * Clear PlayStrategy, i.e. finish plays and remove
	 * 
	 * @param playStrategyBuilder
	 */
	protected void clear(final PlayStrategy.Builder playStrategyBuilder)
	{
		for (APlay play : playStrategyBuilder.getActivePlays())
		{
			play.changeToFinished();
		}
		playStrategyBuilder.getActivePlays().clear();
	}
	
	
	protected void updatePlays(final Map<EPlay, RoleFinderInfo> roleInfos, final List<APlay> activePlays,
			final Set<EPlay> ignoredPlays)
	{
		// get a list of ePlays from aPlays
		Map<EPlay, APlay> activePlaysMap = new EnumMap<>(EPlay.class);
		for (APlay aPlay : activePlays)
		{
			activePlaysMap.put(aPlay.getType(), aPlay);
		}
		// add plays that are not in activePlays
		for (EPlay ePlay : roleInfos.keySet())
		{
			if (!activePlaysMap.keySet().contains(ePlay))
			{
				APlay newPlay;
				try
				{
					newPlay = (APlay) ePlay.getInstanceableClass().newDefaultInstance();
					activePlays.add(newPlay);
				} catch (NotCreateableException err)
				{
					log.warn("Could not instantiate play", err);
				}
			}
		}
		// remove plays that should not be active anymore
		for (APlay aPlay : activePlaysMap.values())
		{
			if (roleInfos.containsKey(aPlay.getType())
					|| ignoredPlays.contains(aPlay.getType()))
			{
				continue;
			}
			aPlay.removeRoles(aPlay.getRoles().size(), null);
			activePlays.remove(aPlay);
		}
	}
	
	
	/**
	 * @return the aiControl
	 */
	public final AIControl getAiControl()
	{
		synchronized (sync)
		{
			return aiControl;
		}
	}
}
