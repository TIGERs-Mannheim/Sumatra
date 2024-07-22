/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


public record DefensePenAreaTargetGroup(IVector2 centerDest,
                                        IVector2 velAdaptedCenterDest,
                                        List<IVector2> moveDestinations,
                                        List<IVector2> velAdaptedMoveDestinations,
                                        List<IDefenseThreat> threats,
                                        int priority)
{
	public static DefensePenAreaTargetGroup fromTargetCluster(IVector2 centerDest, IVector2 velAdaptedCenterDest,
			List<IVector2> moveDestinations,
			List<IVector2> velAdaptedMoveDestinations,
			List<IDefenseThreat> threats, int priority)
	{
		return new DefensePenAreaTargetGroup(centerDest, velAdaptedCenterDest, moveDestinations,
				velAdaptedMoveDestinations, threats, priority);
	}


	public static DefensePenAreaTargetGroup fromSpace(IVector2 centerDest)
	{
		return new DefensePenAreaTargetGroup(centerDest, centerDest, List.of(centerDest), List.of(centerDest), List.of(), 99);
	}


	public boolean isProtectedByPos(IVector2 pos, double penAreaInterchangeDist)
	{
		return moveDestinations.stream()
				.map(pos::distanceTo)
				.anyMatch(dist -> dist < penAreaInterchangeDist);
	}
}
