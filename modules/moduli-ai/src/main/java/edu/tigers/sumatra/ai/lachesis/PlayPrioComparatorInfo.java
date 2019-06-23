/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.lachesis;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;


/**
 * Comparator for RoleAssigner.
 * Sorts the Plays in assigning-order.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PlayPrioComparatorInfo implements Comparator<Map.Entry<EPlay, RoleFinderInfo>>, Serializable
{
	/**  */
	private static final long	serialVersionUID	= -5929457403712458061L;
	
	
	/**
	 */
	public PlayPrioComparatorInfo()
	{
	}
	
	
	@Override
	public int compare(final Map.Entry<EPlay, RoleFinderInfo> a, final Map.Entry<EPlay, RoleFinderInfo> b)
	{
		return Integer.compare(a.getKey().getPrio(), b.getKey().getPrio());
	}
}
