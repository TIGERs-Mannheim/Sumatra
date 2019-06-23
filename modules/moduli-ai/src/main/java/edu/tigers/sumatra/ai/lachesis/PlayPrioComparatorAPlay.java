/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.lachesis;

import java.util.Comparator;

import edu.tigers.sumatra.ai.pandora.plays.APlay;


/**
 * Comparator for RoleAssigner.
 * Sorts the Plays in assigning-order.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PlayPrioComparatorAPlay implements Comparator<APlay>
{
	@Override
	public int compare(final APlay a, final APlay b)
	{
		return Integer.compare(a.getType().getPrio(), b.getType().getPrio());
	}
}
