/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * Comparator for RoleAssigner.
 * Sorts the Plays in assigning-order.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PlayPrioComparatorInfo implements Comparator<Map.Entry<EPlay, RoleFinderInfo>>, Serializable
{
	/**  */
	private static final long		serialVersionUID	= -5929457403712458061L;
	private final EGameBehavior	gb;
	
	
	/**
	 * @param gb
	 */
	public PlayPrioComparatorInfo(final EGameBehavior gb)
	{
		this.gb = gb;
	}
	
	
	@Override
	public int compare(final Map.Entry<EPlay, RoleFinderInfo> a, final Map.Entry<EPlay, RoleFinderInfo> b)
	{
		return Integer.compare(a.getKey().getPrio(gb), b.getKey().getPrio(gb));
	}
}
